package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class Recommendation(
    val id: String,
    val title: String,
    val description: String,
    val category: RecommendationCategory,
    val priority: RecommendationPriority,
    val icon: String,
    val estimatedYearlyKwhSaved: Double = 0.0,
    val estimatedYearlyUsdSaved: Double = 0.0,
    val estimatedYearlyCo2Saved: Double = 0.0,
    val actionText: String = "",
    val standardReference: String = ""
)
@Serializable
enum class RecommendationCategory {
    COOLING,
    HEATING,
    WATER_HEATING,
    LIGHTING,
    APPLIANCES,
    ENVELOPE,
    RENEWABLE,
    BEHAVIOR
}
@Serializable
enum class RecommendationPriority {
    HIGH,
    MEDIUM,
    LOW
}