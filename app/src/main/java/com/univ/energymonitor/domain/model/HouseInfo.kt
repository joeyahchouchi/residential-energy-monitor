package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable

@Serializable
data class HouseInfo(
    val houseName: String,
    val location: String,
    val houseType: String,
    val floorNumber: String,
    val buildingAge: String,
    val totalAreaM2: String,
    val numberOfRooms: String,
    val numberOfOccupants: String,
    val interiorWallMaterial: String,
    val wallMaterial: String,
    val wallThickness: String,
    val glassType: String,
    val roofExposure: String,
    val insulationLevel: String
)
