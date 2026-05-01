package com.univ.energymonitor.domain.engine

/**
 * Solar water heating sizing analysis.
 *
 * Reference values (Lebanon defaults):
 *   - 1.79 m² of solar panel produces 100 L hot water/day = 4.65 kWh/day thermal
 *   - One person needs 35 L hot water/day = 1.63 kWh/day thermal
 *
 * Physics check (heating water from 15°C → 55°C, ΔT = 40°C):
 *   Q = m × c × ΔT = 100 kg × 4.186 kJ/(kg·K) × 40 K = 16,744 kJ ≈ 4.65 kWh ✓
 *
 * Recommendation rules:
 *   1. No backup, solar ≥ demand           → no recommendation
 *   2. No backup, solar < demand           → "Add a backup system"
 *   3. Has electric backup, total ≥ demand → no recommendation
 *   4. Has electric backup, total < demand → "Add an auxiliary system"
 *
 * Gas/Diesel backups are treated as "no backup" for this analysis since
 * the survey does not collect their power/consumption data.
 */
object SolarWaterHeating {

    // ─── Reference constants ────────────────────────────────────────────
    const val PANEL_REFERENCE_AREA_M2 = 1.79
    const val PANEL_REFERENCE_KWH_PER_DAY = 4.65
    const val PANEL_REFERENCE_LITERS_PER_DAY = 100.0

    const val HOT_WATER_PER_PERSON_KWH_PER_DAY = 1.63
    const val HOT_WATER_PER_PERSON_LITERS_PER_DAY = 35.0

    const val ELECTRIC_BACKUP_DEFAULT_KW = 1.5

    // ─── Result types ───────────────────────────────────────────────────
    enum class SolarRecommendation {
        NONE,            // System covers demand → no action needed
        NEED_BACKUP,     // No backup, solar < demand
        NEED_AUXILIARY   // Has electric backup, solar + backup < demand
    }

    data class SolarWaterAnalysis(
        val demandKwhPerDay: Double,
        val demandLitersPerDay: Double,
        val supplyKwhPerDay: Double,
        val supplyLitersPerDay: Double,
        val deficitKwhPerDay: Double,
        val hasElectricBackup: Boolean,
        val backupKwhPerDay: Double,
        val totalSupplyKwhPerDay: Double,
        val recommendation: SolarRecommendation
    )

    // ─── Public analysis function ──────────────────────────────────────
    /**
     * @param panelAreaM2 Total solar panel area (length × width). Must be > 0.
     * @param occupants Number of people in the household. Must be > 0.
     * @param backupType "None" | "Electric" | "Gas" | "Diesel" | "" (blank).
     *                   Only "Electric" contributes to the supply calculation.
     * @param backupHoursPerDay Daily run hours of the electric backup.
     * @return Analysis result, or null if inputs are invalid.
     */
    fun analyze(
        panelAreaM2: Double,
        occupants: Int,
        backupType: String,
        backupHoursPerDay: Double
    ): SolarWaterAnalysis? {
        if (panelAreaM2 <= 0.0 || occupants <= 0) return null

        // Demand: occupants × per-person daily need
        val demandKwhPerDay = occupants * HOT_WATER_PER_PERSON_KWH_PER_DAY
        val demandLitersPerDay = occupants * HOT_WATER_PER_PERSON_LITERS_PER_DAY

        // Supply from solar panel: règle de trois on the reference panel
        val supplyKwhPerDay =
            (panelAreaM2 / PANEL_REFERENCE_AREA_M2) * PANEL_REFERENCE_KWH_PER_DAY
        val supplyLitersPerDay =
            (panelAreaM2 / PANEL_REFERENCE_AREA_M2) * PANEL_REFERENCE_LITERS_PER_DAY

        val deficitKwhPerDay = (demandKwhPerDay - supplyKwhPerDay).coerceAtLeast(0.0)

        // Backup: only Electric is counted (we don't collect data for gas/diesel power)
        val isElectricBackup = backupType.trim().equals("Electric", ignoreCase = true)
        val backupKwhPerDay = if (isElectricBackup) {
            ELECTRIC_BACKUP_DEFAULT_KW * backupHoursPerDay.coerceAtLeast(0.0)
        } else {
            0.0
        }

        val totalSupplyKwhPerDay = supplyKwhPerDay + backupKwhPerDay

        // Apply your three rules
        val recommendation = when {
            // Rule 3: solar alone covers demand → no recommendation
            supplyKwhPerDay >= demandKwhPerDay -> SolarRecommendation.NONE
            // Rule 2: no electric backup, solar < demand → add backup
            !isElectricBackup -> SolarRecommendation.NEED_BACKUP
            // Rule 1: has electric backup, solar + backup < demand → add auxiliary
            totalSupplyKwhPerDay < demandKwhPerDay -> SolarRecommendation.NEED_AUXILIARY
            // Has electric backup AND solar + backup ≥ demand → no recommendation
            else -> SolarRecommendation.NONE
        }

        return SolarWaterAnalysis(
            demandKwhPerDay = demandKwhPerDay,
            demandLitersPerDay = demandLitersPerDay,
            supplyKwhPerDay = supplyKwhPerDay,
            supplyLitersPerDay = supplyLitersPerDay,
            deficitKwhPerDay = deficitKwhPerDay,
            hasElectricBackup = isElectricBackup,
            backupKwhPerDay = backupKwhPerDay,
            totalSupplyKwhPerDay = totalSupplyKwhPerDay,
            recommendation = recommendation
        )
    }
}