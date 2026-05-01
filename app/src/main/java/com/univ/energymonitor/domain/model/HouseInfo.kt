package com.univ.energymonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WallLayerInfo(
    val material: String = "",
    val thickness: String = ""
)

@Serializable
data class HouseInfo(
    val houseName: String = "",
    val location: String = "",
    val houseType: String = "",
    val floorNumber: String = "",
    val buildingAge: String = "",
    val totalAreaM2: String = "",
    val numberOfRooms: String = "",
    val numberOfOccupants: String = "",
    val glassSurfaceM2: String = "",
    val exposedWallSurfaceM2: String = "",
    val numberOfWallLayers: String = "",
    val wallLayers: List<WallLayerInfo> = emptyList(),
    val glassType: String = "",
    val roofExposure: String = "",
    val insulationLevel: String = ""
)
