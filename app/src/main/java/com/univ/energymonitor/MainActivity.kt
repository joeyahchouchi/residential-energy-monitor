package com.univ.energymonitor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.univ.energymonitor.domain.engine.EnergyCalculator
import com.univ.energymonitor.domain.model.*
import com.univ.energymonitor.ui.navigation.Screen
import com.univ.energymonitor.ui.screens.ApplianceSurveyScreen
import com.univ.energymonitor.ui.screens.ConsumptionSurveyScreen
import com.univ.energymonitor.ui.screens.CreateAccountScreen
import com.univ.energymonitor.ui.screens.DashboardScreen
import com.univ.energymonitor.ui.screens.HouseSurveyScreen
import com.univ.energymonitor.ui.screens.HvacSurveyScreen
import com.univ.energymonitor.ui.screens.LightingSurveyScreen
import com.univ.energymonitor.ui.screens.LoginScreen
import com.univ.energymonitor.ui.screens.ResultsScreen
import com.univ.energymonitor.ui.screens.ReviewSurveyScreen
import com.univ.energymonitor.ui.state.*
import com.univ.energymonitor.ui.theme.EnergyMonitorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EnergyMonitorTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {

    val context = LocalContext.current

    var screen by remember { mutableStateOf<Screen>(Screen.Login) }
    var loggedUser by rememberSaveable { mutableStateOf("") }
    var surveyData by remember { mutableStateOf(SurveyData()) }
    var energyReport by remember { mutableStateOf<EnergyReport?>(null) }

    var users by remember {
        mutableStateOf(
            mutableMapOf(
                "admin" to "admin123",
                "engineer" to "leb2024",
                "surveyor" to "survey1"
            )
        )
    }

    var step1State by remember { mutableStateOf(HouseSurveyUiState()) }
    var step2State by remember { mutableStateOf(HvacSurveyUiState()) }
    var step3State by remember { mutableStateOf(LightingSurveyUiState()) }
    var step4State by remember { mutableStateOf(ApplianceSurveyUiState()) }
    var step5State by remember { mutableStateOf(ConsumptionSurveyUiState()) }

    var editingFromReview by remember { mutableStateOf(false) }

    fun navigateAfterSave(nextStep: Screen) {
        screen = if (editingFromReview) {
            editingFromReview = false
            Screen.SurveyStep6
        } else {
            nextStep
        }
    }

    fun resetSurvey() {
        surveyData = SurveyData()
        energyReport = null
        step1State = HouseSurveyUiState()
        step2State = HvacSurveyUiState()
        step3State = LightingSurveyUiState()
        step4State = ApplianceSurveyUiState()
        step5State = ConsumptionSurveyUiState()
        editingFromReview = false
    }

    when (screen) {

        Screen.Login -> LoginScreen(
            users = users,
            onLoginSuccess = { user ->
                loggedUser = user
                screen = Screen.Dashboard
            },
            onCreateAccount = {
                screen = Screen.CreateAccount
            }
        )

        Screen.CreateAccount -> CreateAccountScreen(
            existingUsernames = users.keys,
            onAccountCreated = { newUser ->
                users = users.toMutableMap().apply {
                    put(newUser.username, newUser.password)
                }
                screen = Screen.Login
            },
            onBackToLogin = {
                screen = Screen.Login
            }
        )

        Screen.Dashboard -> DashboardScreen(
            username = loggedUser,
            onStartSurvey = {
                resetSurvey()
                screen = Screen.SurveyStep1
            },
            onLogout = {
                loggedUser = ""
                resetSurvey()
                screen = Screen.Login
            }
        )

        Screen.SurveyStep1 -> HouseSurveyScreen(
            initialState = step1State,
            onBackClick = { screen = Screen.Dashboard },
            onNextClick = { state ->
                step1State = state
                surveyData = surveyData.copy(
                    houseInfo = HouseInfo(
                        houseName = state.houseName,
                        location = state.location,
                        houseType = state.houseType,
                        floorNumber = state.floorNumber,
                        totalAreaM2 = state.totalAreaM2,
                        numberOfRooms = state.numberOfRooms,
                        numberOfOccupants = state.numberOfOccupants,
                        wallMaterial = state.wallMaterial,
                        wallThickness = state.wallThickness,
                        glassType = state.glassType,
                        roofExposure = state.roofExposure,
                        insulationLevel = state.insulationLevel
                    )
                )
                navigateAfterSave(Screen.SurveyStep2)
            },
            onSaveDraft = { state ->
                step1State = state
                screen = Screen.Dashboard
            }
        )

        Screen.SurveyStep2 -> HvacSurveyScreen(
            initialState = step2State,
            onBackClick = { screen = Screen.SurveyStep1 },
            onNextClick = { state ->
                step2State = state
                surveyData = surveyData.copy(
                    hvacInfo = HvacInfo(
                        numberOfAcUnits = state.numberOfAcUnits,
                        acUnits = state.acUnits,
                        heatingSystemType = state.heatingSystemType,
                        numberOfHeatingUnits = state.numberOfHeatingUnits,
                        heatingDailyUsageHours = state.heatingDailyUsageHours,
                        numberOfHeatingAcUnits = state.numberOfHeatingAcUnits,
                        heatingAcUnits = state.heatingAcUnits,
                        waterHeaterType = state.waterHeaterType,
                        waterHeaterPowerKw = state.waterHeaterPowerKw,
                        waterHeaterDailyHours = state.waterHeaterDailyHours,
                        waterTankSizeLiters = state.waterTankSizeLiters
                    )
                )
                navigateAfterSave(Screen.SurveyStep3)
            },
            onSaveDraft = { state ->
                step2State = state
                screen = Screen.Dashboard
            }
        )

        Screen.SurveyStep3 -> LightingSurveyScreen(
            initialState = step3State,
            onBackClick = { screen = Screen.SurveyStep2 },
            onNextClick = { state ->
                step3State = state
                surveyData = surveyData.copy(
                    lightingInfo = LightingInfo(
                        numberOfIndoorLamps = state.numberOfIndoorLamps,
                        indoorLamps = state.indoorLamps,
                        hasOutdoorLighting = state.hasOutdoorLighting,
                        numberOfOutdoorLamps = state.numberOfOutdoorLamps,
                        outdoorLamps = state.outdoorLamps
                    )
                )
                navigateAfterSave(Screen.SurveyStep4)
            },
            onSaveDraft = { state ->
                step3State = state
                screen = Screen.Dashboard
            }
        )
        Screen.SurveyStep4 -> ApplianceSurveyScreen(
            initialState = step4State,
            onBackClick = { screen = Screen.SurveyStep3 },
            onNextClick = { state ->
                step4State = state
                surveyData = surveyData.copy(
                    applianceInfo = ApplianceInfo(
                        appliances = state.appliances,
                        customAppliances = state.customAppliances
                    )
                )
                navigateAfterSave(Screen.SurveyStep5)
            },
            onSaveDraft = { state ->
                step4State = state
                screen = Screen.Dashboard
            }
        )
        Screen.SurveyStep5 -> ConsumptionSurveyScreen(
            initialState = step5State,
            onBackClick = { screen = Screen.SurveyStep4 },
            onNextClick = { state ->
                step5State = state
                surveyData = surveyData.copy(
                    consumptionInfo = ConsumptionInfo(
                        edlHoursPerDay = state.edlHoursPerDay,
                        usesEdl = state.usesEdl,
                        usesGenerator = state.usesGenerator,
                        usesSolar = state.usesSolar,
                        usesUps = state.usesUps,
                        usesNone = state.usesNone,
                        generatorSubscriptionType = state.generatorSubscriptionType,
                        solarCapacity = state.solarCapacity,
                        solarHasBattery = state.solarHasBattery
                    )
                )
                navigateAfterSave(Screen.SurveyStep6)
            },
            onSaveDraft = { state ->
                step5State = state
                screen = Screen.Dashboard
            }
        )

        Screen.SurveyStep6 -> ReviewSurveyScreen(
            surveyData = surveyData,
            onBackClick = { screen = Screen.SurveyStep5 },
            onEditStep = { stepNumber ->
                editingFromReview = true
                screen = when (stepNumber) {
                    1 -> Screen.SurveyStep1
                    2 -> Screen.SurveyStep2
                    3 -> Screen.SurveyStep3
                    4 -> Screen.SurveyStep4
                    5 -> Screen.SurveyStep5
                    else -> Screen.SurveyStep6
                }
            },
            onSubmit = { state ->
                val updatedSurveyData = surveyData.copy(
                    reviewInfo = ReviewInfo(
                        confirmAccuracy = state.confirmAccuracy,
                        finalNotes = state.finalNotes
                    )
                )
                surveyData = updatedSurveyData
                energyReport = EnergyCalculator.calculate(updatedSurveyData)

                Toast.makeText(
                    context,
                    "Survey submitted! Generating report… ✅",
                    Toast.LENGTH_SHORT
                ).show()

                screen = Screen.Results
            },
            onSaveDraft = { _ ->
                screen = Screen.Dashboard
            }
        )

        Screen.Results -> {
            energyReport?.let { report ->
                ResultsScreen(
                    report = report,
                    houseName = surveyData.houseInfo?.houseName ?: "Household",
                    onBackToDashboard = {
                        screen = Screen.Dashboard
                    }
                )
            }
        }
    }
}