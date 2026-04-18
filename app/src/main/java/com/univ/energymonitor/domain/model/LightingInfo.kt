package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class LightingInfo(
    val numberOfDirectLamps: String = "",
    val numberOfDirectTypes: String = "",
    val directLampSamples: List<LampInfo> = emptyList(),
    val hasIndirectLighting: Boolean = false,
    val numberOfIndirectRooms: String = "",
    val indirectRooms: List<IndirectLampInfo> = emptyList(),
    val hasOutdoorLighting: Boolean = false,
    val numberOfOutdoorLamps: String = "",
    val outdoorLamps: List<LampInfo> = emptyList()
)