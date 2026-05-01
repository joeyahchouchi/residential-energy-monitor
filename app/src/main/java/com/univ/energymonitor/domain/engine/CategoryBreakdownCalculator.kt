package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.SurveyData

data class BreakdownItem(
    val label: String,
    val detail: String,
    val yearlyKwh: Double
)

object CategoryBreakdownCalculator {

    fun coolingItems(survey: SurveyData): List<BreakdownItem> {
        val hvac = survey.hvacInfo ?: return emptyList()
        val buildingAge = survey.houseInfo?.buildingAge ?: ""

        return hvac.acUnits.mapIndexedNotNull { index, unit ->
            val kwh = calculateAcUnitYearlyKwh(unit, buildingAge)
            if (kwh <= 0.0) return@mapIndexedNotNull null

            BreakdownItem(
                label = unit.roomName.ifBlank { "AC Unit ${index + 1}" },
                detail = "${unit.dailyUsageHours.ifBlank { "0" }} h/day x ${unit.daysPerYear.ifBlank { "0" }} days/year",
                yearlyKwh = round2(kwh)
            )
        }
    }

    fun heatingItems(survey: SurveyData): List<BreakdownItem> {
        val hvac = survey.hvacInfo ?: return emptyList()

        return when (hvac.heatingSystemType) {
            "AC" -> {
                val buildingAge = survey.houseInfo?.buildingAge ?: ""

                hvac.heatingAcUnits.mapIndexedNotNull { index, unit ->
                    val kwh = calculateAcUnitYearlyKwh(unit, buildingAge)
                    if (kwh <= 0.0) return@mapIndexedNotNull null

                    BreakdownItem(
                        label = unit.roomName.ifBlank { "Heating AC ${index + 1}" },
                        detail = "${unit.dailyUsageHours.ifBlank { "0" }} h/day x ${unit.daysPerYear.ifBlank { "0" }} days/year",
                        yearlyKwh = round2(kwh)
                    )
                }
            }

            "Electric Heater" -> {
                val units = hvac.numberOfHeatingUnits.toSafeDouble()
                val powerKw = hvac.heatingPowerKw.toSafeDouble()
                val hours = hvac.heatingDailyUsageHours.toSafeDouble()
                val days = hvac.heatingDaysPerYear.toSafeDouble()
                val kwh = units * powerKw * hours * days

                if (kwh > 0.0) {
                    listOf(
                        BreakdownItem(
                            label = "Electric heater",
                            detail = "${units.toCleanText()} unit(s), ${powerKw.toCleanText()} kW, ${hours.toCleanText()} h/day",
                            yearlyKwh = round2(kwh)
                        )
                    )
                } else {
                    emptyList()
                }
            }

            else -> emptyList()
        }
    }

    fun waterHeatingItems(survey: SurveyData): List<BreakdownItem> {
        val hvac = survey.hvacInfo ?: return emptyList()

        return hvac.waterHeaters.mapIndexedNotNull { index, heater ->
            when (heater.type) {
                "Electrical Resistance" -> {
                    val powerKw = heater.powerKw.toSafeDouble().let {
                        if (it > 0.0) it else LebanonDefaults.WATER_HEATER_DEFAULT_KW
                    }
                    val hours = heater.dailyHours.toSafeDouble()
                    val days = heater.daysPerYear.toSafeDouble().let {
                        if (it > 0.0) it else LebanonDefaults.DAYS_PER_YEAR.toDouble()
                    }
                    val kwh = powerKw * hours * days

                    if (kwh <= 0.0) return@mapIndexedNotNull null

                    BreakdownItem(
                        label = "Water Heater ${index + 1}: Electrical",
                        detail = "${powerKw.toCleanText()} kW x ${hours.toCleanText()} h/day x ${days.toCleanText()} days",
                        yearlyKwh = round2(kwh)
                    )
                }

                "Solar Heater" -> {
                    if (heater.solarBackupType != "Electric") return@mapIndexedNotNull null

                    val hours = heater.solarBackupHoursPerDay.toSafeDouble()
                    val kwh = LebanonDefaults.WATER_HEATER_DEFAULT_KW * hours * LebanonDefaults.DAYS_PER_YEAR

                    if (kwh <= 0.0) return@mapIndexedNotNull null

                    BreakdownItem(
                        label = "Water Heater ${index + 1}: Solar backup",
                        detail = "Electric backup ${hours.toCleanText()} h/day",
                        yearlyKwh = round2(kwh)
                    )
                }

                "Gas Tank" -> {
                    val kwh = EnergyCalculator.calculateGasWaterHeaterUsefulKwhPerYear(
                        heater.gasTankCountPerYear
                    )

                    if (kwh <= 0.0) return@mapIndexedNotNull null

                    BreakdownItem(
                        label = "Water Heater ${index + 1}: Gas tank",
                        detail = "${heater.gasTankCountPerYear.ifBlank { "0" }} tank(s)/year",
                        yearlyKwh = round2(kwh)
                    )
                }

                else -> null
            }
        }
    }

    fun lightingItems(survey: SurveyData): List<BreakdownItem> {
        val light = survey.lightingInfo ?: return emptyList()
        val items = mutableListOf<BreakdownItem>()

        val directCount = light.numberOfDirectLamps.toIntOrNull() ?: 0
        if (directCount > 0 && light.directLampSamples.isNotEmpty()) {
            val lampsPerType = directCount.toDouble() / light.directLampSamples.size

            light.directLampSamples.forEachIndexed { index, lamp ->
                val powerW = lamp.powerWatts.toSafeDouble()
                val hours = lamp.dailyUsageHours.toSafeDouble()
                val kwh = powerW * hours * lampsPerType * LebanonDefaults.DAYS_PER_YEAR / 1000.0

                if (kwh > 0.0) {
                    items.add(
                        BreakdownItem(
                            label = lamp.roomName.ifBlank { "Direct Type ${index + 1}" },
                            detail = "${lampsPerType.toCleanText()} lamp(s), ${powerW.toCleanText()} W, ${hours.toCleanText()} h/day",
                            yearlyKwh = round2(kwh)
                        )
                    )
                }
            }
        }

        if (light.hasIndirectLighting) {
            light.indirectRooms.forEachIndexed { index, room ->
                val length = room.lengthMeters.toSafeDouble()
                val powerWPerMeter = room.powerWatts.toSafeDouble()
                val hours = room.dailyUsageHours.toSafeDouble()
                val kwh = powerWPerMeter * length * hours * LebanonDefaults.DAYS_PER_YEAR / 1000.0

                if (kwh > 0.0) {
                    items.add(
                        BreakdownItem(
                            label = room.roomName.ifBlank { "Indirect Room ${index + 1}" },
                            detail = "${length.toCleanText()} m, ${powerWPerMeter.toCleanText()} W/m, ${hours.toCleanText()} h/day",
                            yearlyKwh = round2(kwh)
                        )
                    )
                }
            }
        }

        if (light.hasOutdoorLighting) {
            light.outdoorLamps.forEachIndexed { index, lamp ->
                val powerW = lamp.powerWatts.toSafeDouble()
                val hours = lamp.dailyUsageHours.toSafeDouble()
                val kwh = powerW * hours * LebanonDefaults.DAYS_PER_YEAR / 1000.0

                if (kwh > 0.0) {
                    items.add(
                        BreakdownItem(
                            label = lamp.roomName.ifBlank { "Outdoor Lamp ${index + 1}" },
                            detail = "${powerW.toCleanText()} W, ${hours.toCleanText()} h/day",
                            yearlyKwh = round2(kwh)
                        )
                    )
                }
            }
        }

        return items
    }

    fun applianceItems(survey: SurveyData): List<BreakdownItem> {
        val app = survey.applianceInfo ?: return emptyList()
        val items = mutableListOf<BreakdownItem>()

        app.appliances.filter { it.exists }.forEach { appliance ->
            val powerW = appliance.powerWatts.toSafeDouble()
            val hours = appliance.dailyUsageHours.toSafeDouble()
            val dutyFactor = when (appliance.name.trim().lowercase()) {
                "fridge", "refrigerator" -> 0.35
                else -> 1.0
            }

            val kwh = powerW * hours * dutyFactor * LebanonDefaults.DAYS_PER_YEAR / 1000.0

            if (kwh > 0.0) {
                items.add(
                    BreakdownItem(
                        label = appliance.name,
                        detail = if (dutyFactor < 1.0) {
                            "${powerW.toCleanText()} W, ${hours.toCleanText()} h/day, 35% duty factor"
                        } else {
                            "${powerW.toCleanText()} W, ${hours.toCleanText()} h/day"
                        },
                        yearlyKwh = round2(kwh)
                    )
                )
            }
        }

        app.customAppliances.forEach { appliance ->
            val powerW = appliance.powerWatts.toSafeDouble()
            val hours = appliance.dailyUsageHours.toSafeDouble()
            val kwh = powerW * hours * LebanonDefaults.DAYS_PER_YEAR / 1000.0

            if (kwh > 0.0) {
                items.add(
                    BreakdownItem(
                        label = appliance.name.ifBlank { "Custom appliance" },
                        detail = "${powerW.toCleanText()} W, ${hours.toCleanText()} h/day",
                        yearlyKwh = round2(kwh)
                    )
                )
            }
        }

        return items.sortedByDescending { it.yearlyKwh }
    }

    private fun calculateAcUnitYearlyKwh(unit: AcUnitInfo, buildingAge: String = ""): Double {
        val rawCapacity = unit.capacityValue.toSafeDouble()

        val normalizedUnit = unit.capacityUnit
            .trim()
            .lowercase()
            .replace(" ", "")

        val capacityKw = when (normalizedUnit) {
            "btu/h", "btu/hr", "btuh", "btu" -> rawCapacity / LebanonDefaults.BTU_PER_KW
            "tons", "ton" -> rawCapacity * 3.517
            "kw", "kilowatt", "kilowatts" -> rawCapacity
            else -> rawCapacity
        }

        val cop = when (unit.copMethod) {
            "I know the COP" -> unit.cop.toSafeDouble().let { if (it > 0) it else 3.0 }
            "I know the AC year" -> copFromAcAge(unit.acYear)
            else -> copFromBuildingAge(buildingAge)
        }

        val effectiveCapacityKw = if (capacityKw > 0) {
            capacityKw
        } else {
            estimateCapacityFromRoomSize(unit.roomSizeM2.toSafeDouble())
        }

        val electricalKw = if (cop > 0) effectiveCapacityKw / cop else 0.0
        val dailyHours = unit.dailyUsageHours.toSafeDouble()
        val daysPerYear = unit.daysPerYear.toSafeDouble()

        return electricalKw * dailyHours * daysPerYear
    }


    private fun estimateCapacityFromRoomSize(roomM2: Double): Double {
        val btuPerHour = when {
            roomM2 <= 22.0 -> 9000.0
            roomM2 <= 30.0 -> 12000.0
            roomM2 <= 45.0 -> 18000.0
            else -> 24000.0
        }

        return btuPerHour / LebanonDefaults.BTU_PER_KW
    }

    private fun copFromAcAge(acAge: String): Double {
        return when (acAge) {
            "After 2020" -> 4.0
            "2015–2020" -> 3.5
            "2012–2015" -> 3.2
            "2000–2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    private fun copFromBuildingAge(buildingAge: String): Double {
        return when (buildingAge) {
            "After 2020" -> 4.0
            "2015–2020" -> 3.5
            "2012–2015" -> 3.2
            "2000–2012" -> 2.8
            "Before 2000" -> 2.5
            else -> 3.0
        }
    }

    private fun String.toSafeDouble(): Double {
        return this.trim().toDoubleOrNull() ?: 0.0
    }

    private fun Double.toCleanText(): String {
        return if (this == this.toLong().toDouble()) {
            this.toLong().toString()
        } else {
            "%.1f".format(this)
        }
    }

    private fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }
}
