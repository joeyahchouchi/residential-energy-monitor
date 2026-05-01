package com.univ.energymonitor.domain.engine

object ApplianceEfficiencyTable {

    data class UpgradeRecommendation(
        val currentLabel: String,
        val targetLabel: String,
        val savedKwh: Double,
        val percentReduction: Double,
        val wasInferredFromYear: Boolean = false
    )

    private val fridgeKwh = mapOf(
        "A" to 108.0,
        "B" to 134.0,
        "C" to 169.0,
        "D" to 204.0,
        "E" to 264.0,
        "F" to 427.0
    )

    private val fridgePct = mapOf(
        ("F" to "C") to 60.42,
        ("E" to "C") to 35.98,
        ("D" to "C") to 17.16,
        ("C" to "B") to 20.71,
        ("C" to "A") to 36.09,
        ("B" to "A") to 19.40
    )

    private val washingKwh = mapOf(
        "A" to 87.36,
        "B" to 114.4,
        "C" to 128.96,
        "D" to 151.84,
        "E" to 172.64,
        "F" to 189.28
    )

    private val washingPct = mapOf(
        ("F" to "C") to 31.87,
        ("E" to "C") to 25.30,
        ("D" to "C") to 15.07,
        ("C" to "B") to 11.29,
        ("C" to "A") to 32.26,
        ("B" to "A") to 23.64
    )

    private val dishwasherKwh = mapOf(
        "A" to 101.92,
        "B" to 112.32,
        "C" to 122.72,
        "D" to 176.8,
        "E" to 197.6
    )

    private val dishwasherPct = mapOf(
        ("E" to "C") to 37.89,
        ("D" to "C") to 30.59,
        ("C" to "B") to 8.47,
        ("C" to "A") to 16.95,
        ("B" to "A") to 9.26
    )

    private val tvKwh = mapOf(
        "B" to 28.0,
        "C" to 33.0,
        "D" to 46.0,
        "E" to 63.0,
        "F" to 76.0,
        "G" to 102.0
    )

    private val tvPct = mapOf(
        ("G" to "C") to 67.65,
        ("F" to "C") to 56.58,
        ("E" to "C") to 47.62,
        ("D" to "C") to 28.26,
        ("C" to "B") to 15.15
    )

    private val ovenKwh = mapOf(
        "A+++" to 171.55,
        "A++" to 189.8,
        "A+" to 237.25,
        "A" to 317.55,
        "B" to 408.8,
        "C" to 514.65,
        "D" to 563.195
    )

    private val ovenPct = mapOf(
        ("D" to "A") to 43.62,
        ("C" to "A") to 38.30,
        ("B" to "A") to 22.32,
        ("A" to "A+") to 25.29,
        ("A+" to "A++") to 20.00,
        ("A++" to "A+++") to 9.62
    )

    private val waterHeaterKwh = mapOf(
        "A" to 600.0,
        "B" to 1312.0,
        "C" to 1343.0,
        "D" to 1420.0,
        "E" to 1470.0,
        "F" to 1500.0
    )

    private val waterHeaterPct = mapOf(
        ("F" to "A") to 60.00,
        ("F" to "B") to 12.53,
        ("F" to "C") to 10.47,
        ("F" to "D") to 5.33,
        ("F" to "E") to 2.00,

        ("E" to "A") to 59.18,
        ("E" to "B") to 10.75,
        ("E" to "C") to 8.64,
        ("E" to "D") to 3.40,

        ("D" to "A") to 57.75,
        ("D" to "B") to 7.61,
        ("D" to "C") to 5.42,

        ("C" to "A") to 55.32,
        ("C" to "B") to 2.31,

        ("B" to "A") to 54.27
    )

    private const val YEAR_THRESHOLD = 2013

    fun hasTierData(applianceName: String): Boolean {
        return labelsFor(applianceName).isNotEmpty()
    }

    fun labelsFor(applianceName: String): List<String> {
        return when (applianceName.trim().lowercase()) {
            "fridge", "refrigerator" -> listOf("A", "B", "C", "D", "E", "F")
            "washing machine" -> listOf("A", "B", "C", "D", "E", "F")
            "dishwasher" -> listOf("A", "B", "C", "D", "E")
            "tv", "television" -> listOf("B", "C", "D", "E", "F", "G")
            "electric oven", "oven" -> listOf("A+++", "A++", "A+", "A", "B", "C", "D")
            "water heater", "waterheater" -> listOf("A", "B", "C", "D", "E", "F")
            else -> emptyList()
        }
    }

    fun inferLabelFromYear(
        applianceName: String,
        purchaseYear: String
    ): String? {
        val year = purchaseYear.trim().toIntOrNull() ?: return null
        if (year < 1980 || year > 2100) return null

        return when (applianceName.trim().lowercase()) {
            "fridge", "refrigerator",
            "washing machine",
            "dishwasher",
            "tv", "television",
            "water heater", "waterheater" -> if (year < YEAR_THRESHOLD) "F" else "C"

            "electric oven", "oven" -> if (year < YEAR_THRESHOLD) "D" else "A"

            else -> null
        }
    }

    fun resolveLabel(
        applianceName: String,
        chosenLabel: String,
        purchaseYear: String
    ): Pair<String, Boolean> {
        if (chosenLabel.isNotBlank()) return chosenLabel to false

        val inferred = inferLabelFromYear(
            applianceName = applianceName,
            purchaseYear = purchaseYear
        )

        return if (inferred != null) {
            inferred to true
        } else {
            "" to false
        }
    }

    fun recommendUpgrade(
        applianceName: String,
        currentLabel: String,
        wasInferredFromYear: Boolean = false
    ): UpgradeRecommendation? {
        val key = applianceName.trim().lowercase()
        val label = currentLabel.trim()

        val kwhTable = kwhTableForAppliance(key) ?: return null
        val pctTable = pctTableForAppliance(key) ?: return null

        val targetLabel = targetLabelForAppliance(
            applianceKey = key,
            currentLabel = label
        ) ?: return null

        val currentKwh = kwhTable[label] ?: return null
        val targetKwh = kwhTable[targetLabel] ?: return null
        val percentReduction = pctTable[label to targetLabel] ?: return null

        val savedKwh = currentKwh - targetKwh
        if (savedKwh <= 0.0) return null

        return UpgradeRecommendation(
            currentLabel = label,
            targetLabel = targetLabel,
            savedKwh = round2(savedKwh),
            percentReduction = percentReduction,
            wasInferredFromYear = wasInferredFromYear
        )
    }

    private fun kwhTableForAppliance(applianceKey: String): Map<String, Double>? {
        return when (applianceKey) {
            "fridge", "refrigerator" -> fridgeKwh
            "washing machine" -> washingKwh
            "dishwasher" -> dishwasherKwh
            "tv", "television" -> tvKwh
            "electric oven", "oven" -> ovenKwh
            "water heater", "waterheater" -> waterHeaterKwh
            else -> null
        }
    }

    private fun pctTableForAppliance(
        applianceKey: String
    ): Map<Pair<String, String>, Double>? {
        return when (applianceKey) {
            "fridge", "refrigerator" -> fridgePct
            "washing machine" -> washingPct
            "dishwasher" -> dishwasherPct
            "tv", "television" -> tvPct
            "electric oven", "oven" -> ovenPct
            "water heater", "waterheater" -> waterHeaterPct
            else -> null
        }
    }

    private fun targetLabelForAppliance(
        applianceKey: String,
        currentLabel: String
    ): String? {
        val isTv = applianceKey == "tv" || applianceKey == "television"
        val isElectricOven = applianceKey == "electric oven" || applianceKey == "oven"

        return when {
            isElectricOven -> {
                when (currentLabel) {
                    "D", "C", "B" -> "A"
                    "A" -> "A+"
                    "A+" -> "A++"
                    "A++" -> "A+++"
                    else -> null
                }
            }

            isTv -> {
                when (currentLabel) {
                    "D", "E", "F", "G" -> "C"
                    "C" -> "B"
                    else -> null
                }
            }

            else -> {
                when (currentLabel) {
                    "D", "E", "F", "G" -> "C"
                    "C", "B" -> "A"
                    else -> null
                }
            }
        }
    }

    private fun round2(value: Double): Double {
        return Math.round(value * 100.0) / 100.0
    }
}
