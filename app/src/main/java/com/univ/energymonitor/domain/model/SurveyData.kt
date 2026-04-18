package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class SurveyData(
    val houseInfo: HouseInfo? = null,
    val hvacInfo: HvacInfo? = null,
    val lightingInfo: LightingInfo? = null,
    val applianceInfo: ApplianceInfo? = null,
    val consumptionInfo: ConsumptionInfo? = null,
    val reviewInfo: ReviewInfo? = null
)