package com.univ.energymonitor.domain.engine

import com.univ.energymonitor.domain.model.ConsumptionInfo

object EnergyCostCalculator {

    data class CostBreakdown(
        val totalCostUsd: Double,
        val weightedAvgPricePerKwh: Double,
        val weightedAvgCo2KgPerKwh: Double,
        val pctEdl: Double,
        val pctGenerator: Double,
        val pctSolar: Double,
        val kWhFromEdl: Double,
        val kWhFromGenerator: Double,
        val kWhFromSolar: Double
    )

    fun computeCost(
        totalCalculatedKwh: Double,
        consumption: ConsumptionInfo?
    ): CostBreakdown {
        if (consumption == null) {
            return zeroBreakdown()
        }

        val edlBill = if (consumption.usesEdl) {
            consumption.yearlyEdlBillUsd.toSafeDouble()
        } else {
            0.0
        }

        val edlPrice = if (consumption.usesEdl) {
            consumption.edlPricePerKwhUsd.toSafeDouble()
        } else {
            0.0
        }

        val kWhFromEdl = if (edlBill > 0.0 && edlPrice > 0.0) {
            edlBill / edlPrice
        } else {
            0.0
        }

        val generatorBill = if (consumption.usesGenerator) {
            consumption.yearlyGeneratorBillUsd.toSafeDouble()
        } else {
            0.0
        }

        val generatorPrice = if (consumption.usesGenerator) {
            consumption.generatorPricePerKwhUsd.toSafeDouble()
        } else {
            0.0
        }

        val kWhFromGenerator = if (generatorBill > 0.0 && generatorPrice > 0.0) {
            generatorBill / generatorPrice
        } else {
            0.0
        }

        val kWhFromSolar = if (consumption.usesSolar) {
            consumption.solarYearlyKwh.toSafeDouble()
        } else {
            0.0
        }

        val totalSourceKwh = kWhFromEdl + kWhFromGenerator + kWhFromSolar

        val pctEdl = if (totalSourceKwh > 0.0) {
            kWhFromEdl / totalSourceKwh
        } else {
            0.0
        }

        val pctGenerator = if (totalSourceKwh > 0.0) {
            kWhFromGenerator / totalSourceKwh
        } else {
            0.0
        }

        val pctSolar = if (totalSourceKwh > 0.0) {
            kWhFromSolar / totalSourceKwh
        } else {
            0.0
        }

        val weightedAvgPrice =
            (pctEdl * edlPrice) +
                    (pctGenerator * generatorPrice)

        val weightedAvgCo2 =
            (pctEdl * LebanonDefaults.CO2_KG_PER_KWH) +
                    (pctGenerator * LebanonDefaults.CO2_KG_PER_KWH)

        val totalCost = totalCalculatedKwh * weightedAvgPrice

        return CostBreakdown(
            totalCostUsd = round2(totalCost),
            weightedAvgPricePerKwh = round4(weightedAvgPrice),
            weightedAvgCo2KgPerKwh = round4(weightedAvgCo2),
            pctEdl = round4(pctEdl),
            pctGenerator = round4(pctGenerator),
            pctSolar = round4(pctSolar),
            kWhFromEdl = round2(kWhFromEdl),
            kWhFromGenerator = round2(kWhFromGenerator),
            kWhFromSolar = round2(kWhFromSolar)
        )
    }

    fun pricePerKwh(consumption: ConsumptionInfo?): Double {
        return computeCost(
            totalCalculatedKwh = 1.0,
            consumption = consumption
        ).weightedAvgPricePerKwh
    }

    private fun zeroBreakdown() = CostBreakdown(
        totalCostUsd = 0.0,
        weightedAvgPricePerKwh = 0.0,
        weightedAvgCo2KgPerKwh = 0.0,
        pctEdl = 0.0,
        pctGenerator = 0.0,
        pctSolar = 0.0,
        kWhFromEdl = 0.0,
        kWhFromGenerator = 0.0,
        kWhFromSolar = 0.0
    )

    private fun String.toSafeDouble(): Double {
        return this.trim().toDoubleOrNull() ?: 0.0
    }

    private fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }

    private fun round4(value: Double): Double {
        return Math.round(value * 10000.0) / 10000.0
    }
}
