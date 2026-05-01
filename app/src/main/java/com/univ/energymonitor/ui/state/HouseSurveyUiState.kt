package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.WallLayerInfo

data class HouseSurveyUiState(
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
    val insulationLevel: String = "",
    val showErrors: Boolean = false
)

fun HouseSurveyUiState.withUpdatedWallLayerCount(): HouseSurveyUiState {
    val count = numberOfWallLayers.toIntOrNull()?.coerceIn(0, 10) ?: 0
    return copy(
        wallLayers = List(count) { index ->
            wallLayers.getOrNull(index) ?: WallLayerInfo()
        }
    )
}

fun HouseSurveyUiState.isValid(): Boolean {
    if (
        houseName.isBlank() ||
        location.isBlank() ||
        houseType.isBlank() ||
        floorNumber.isBlank() ||
        buildingAge.isBlank() ||
        totalAreaM2.isBlank() ||
        numberOfRooms.isBlank() ||
        numberOfOccupants.isBlank() ||
        glassSurfaceM2.isBlank() ||
        exposedWallSurfaceM2.isBlank() ||
        numberOfWallLayers.isBlank() ||
        glassType.isBlank() ||
        roofExposure.isBlank() ||
        insulationLevel.isBlank()
    ) {
        return false
    }

    val glassSurface = glassSurfaceM2.toDoubleOrNull() ?: -1.0
    if (glassSurface < 0) return false

    val exposedWallSurface = exposedWallSurfaceM2.toDoubleOrNull() ?: -1.0
    if (exposedWallSurface < 0) return false

    val layerCount = numberOfWallLayers.toIntOrNull() ?: return false
    if (layerCount !in 0..10) return false
    if (layerCount != wallLayers.size) return false

    wallLayers.forEach { layer ->
        if (layer.material.isBlank()) return false
        if (layer.thickness.isBlank()) return false
    }

    return true
}
