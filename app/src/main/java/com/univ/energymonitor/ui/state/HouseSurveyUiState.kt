package com.univ.energymonitor.ui.state

data class HouseSurveyUiState(
    val houseName: String = "",
    val location: String = "",
    val houseType: String = "",
    val floorNumber: String = "",
    val buildingAge: String = "",
    val totalAreaM2: String = "",
    val numberOfRooms: String = "",
    val numberOfOccupants: String = "",
    val interiorWallMaterial: String = "",
    val wallMaterial: String = "",
    val wallThickness: String = "",
    val glassType: String = "",
    val roofExposure: String = "",
    val insulationLevel: String = "",
    val showErrors: Boolean = false
)

fun HouseSurveyUiState.isValid(): Boolean =
    houseName.isNotBlank() &&
            location.isNotBlank() &&
            houseType.isNotBlank() &&
            floorNumber.isNotBlank() &&
            buildingAge.isNotBlank() &&
            totalAreaM2.isNotBlank() &&
            numberOfRooms.isNotBlank() &&
            numberOfOccupants.isNotBlank() &&
            interiorWallMaterial.isNotBlank() &&
            wallMaterial.isNotBlank() &&
            wallThickness.isNotBlank() &&
            glassType.isNotBlank() &&
            roofExposure.isNotBlank() &&
            insulationLevel.isNotBlank()