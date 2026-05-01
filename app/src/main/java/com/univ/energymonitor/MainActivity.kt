package com.univ.energymonitor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.univ.energymonitor.domain.engine.EnergyCalculator
import com.univ.energymonitor.domain.model.*
import com.univ.energymonitor.ui.navigation.AnalysisType
import com.univ.energymonitor.ui.navigation.Screen
import com.univ.energymonitor.ui.screens.*
import com.univ.energymonitor.ui.state.*
import com.univ.energymonitor.ui.theme.EnergyMonitorTheme
import com.univ.energymonitor.ui.viewmodel.AuthViewModel
import com.univ.energymonitor.ui.viewmodel.SurveyViewModel
import kotlinx.coroutines.launch

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
    val authViewModel: AuthViewModel = viewModel()
    val surveyViewModel: SurveyViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    var screen by remember { mutableStateOf<Screen>(Screen.Login) }

    val loggedUser by authViewModel.loggedUser.collectAsState()
    val loginError by authViewModel.loginError.collectAsState()
    val createAccountResult by authViewModel.createAccountResult.collectAsState()

    LaunchedEffect(loggedUser) {
        if (loggedUser.isNotBlank() && screen == Screen.Login) {
            screen = Screen.Dashboard
        }
    }

    LaunchedEffect(loginError) {
        if (loginError.isNotBlank()) {
            Toast.makeText(context, loginError, Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(createAccountResult) {
        when (createAccountResult) {
            true -> {
                Toast.makeText(context, "Account created! You can now sign in.", Toast.LENGTH_SHORT).show()
                screen = Screen.Login
                authViewModel.resetCreateAccountResult()
            }

            false -> {
                Toast.makeText(context, "Username already taken.", Toast.LENGTH_SHORT).show()
                authViewModel.resetCreateAccountResult()
            }

            null -> Unit
        }
    }

    var surveyData by remember { mutableStateOf(SurveyData()) }
    var energyReport by remember { mutableStateOf<EnergyReport?>(null) }

    var step1State by remember { mutableStateOf(HouseSurveyUiState()) }
    var step2State by remember { mutableStateOf(HvacSurveyUiState()) }
    var step3State by remember { mutableStateOf(LightingSurveyUiState()) }
    var step4State by remember { mutableStateOf(ApplianceSurveyUiState()) }
    var step5State by remember { mutableStateOf(ConsumptionSurveyUiState()) }

    var editingFromReview by remember { mutableStateOf(false) }
    var editingSurveyId by remember { mutableStateOf<Long?>(null) }
    var optimizingSurveyId by remember { mutableStateOf<Long?>(null) }
    var selectedAnalysisType by remember { mutableStateOf(AnalysisType.KPI) }

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
        editingSurveyId = null
        optimizingSurveyId = null
        selectedAnalysisType = AnalysisType.KPI
    }

    fun loadExistingSurveyForEdit(id: Long) {
        coroutineScope.launch {
            val result = surveyViewModel.loadSurvey(id)
            if (result != null) {
                val (data, report) = result

                surveyData = data
                energyReport = report ?: EnergyCalculator.calculate(data)
                editingSurveyId = id
                editingFromReview = false

                step1State = data.houseInfo?.let {
                    HouseSurveyUiState(
                        houseName = it.houseName,
                        location = it.location,
                        houseType = it.houseType,
                        floorNumber = it.floorNumber,
                        buildingAge = it.buildingAge,
                        totalAreaM2 = it.totalAreaM2,
                        numberOfRooms = it.numberOfRooms,
                        numberOfOccupants = it.numberOfOccupants,
                        glassSurfaceM2 = it.glassSurfaceM2,
                        exposedWallSurfaceM2 = it.exposedWallSurfaceM2,
                        numberOfWallLayers = it.numberOfWallLayers,
                        wallLayers = it.wallLayers,
                        glassType = it.glassType,
                        roofExposure = it.roofExposure,
                        insulationLevel = it.insulationLevel
                    )
                } ?: HouseSurveyUiState()

                step2State = data.hvacInfo?.let {
                    HvacSurveyUiState(
                        numberOfAcUnits = it.numberOfAcUnits,
                        acUnits = it.acUnits,

                        heatingSystemType = it.heatingSystemType,
                        numberOfHeatingAcUnits = it.numberOfHeatingAcUnits,
                        heatingAcUnits = it.heatingAcUnits,
                        numberOfHeatingUnits = it.numberOfHeatingUnits,
                        heatingPowerKw = it.heatingPowerKw,
                        heatingDailyUsageHours = it.heatingDailyUsageHours,
                        heatingDaysPerYear = it.heatingDaysPerYear,
                        heatingGasKgPerYear = it.heatingGasKgPerYear,
                        heatingFuelLitersPerYear = it.heatingFuelLitersPerYear,
                        heatedAreaM2 = it.heatedAreaM2,

                        heatingEfficiencyMethod = it.heatingEfficiencyMethod,
                        heatingEfficiencyPercent = it.heatingEfficiencyPercent,
                        heatingInstallationYear = it.heatingInstallationYear,

                        numberOfWaterHeaters = it.numberOfWaterHeaters,
                        waterHeaters = it.waterHeaters
                    )
                } ?: HvacSurveyUiState()

                step3State = data.lightingInfo?.let {
                    LightingSurveyUiState(
                        numberOfDirectLamps = it.numberOfDirectLamps,
                        numberOfDirectTypes = it.numberOfDirectTypes,
                        directLampSamples = it.directLampSamples,
                        hasIndirectLighting = it.hasIndirectLighting,
                        numberOfIndirectRooms = it.numberOfIndirectRooms,
                        indirectRooms = it.indirectRooms,
                        hasOutdoorLighting = it.hasOutdoorLighting,
                        numberOfOutdoorLamps = it.numberOfOutdoorLamps,
                        outdoorLamps = it.outdoorLamps
                    )
                } ?: LightingSurveyUiState()

                step4State = data.applianceInfo?.let {
                    ApplianceSurveyUiState(
                        appliances = it.appliances,
                        customAppliances = it.customAppliances
                    )
                } ?: ApplianceSurveyUiState()

                step5State = data.consumptionInfo?.let {
                    ConsumptionSurveyUiState(
                        edlHoursPerDay = it.edlHoursPerDay,
                        usesEdl = it.usesEdl,
                        usesGenerator = it.usesGenerator,
                        usesSolar = it.usesSolar,
                        usesUps = it.usesUps,
                        usesNone = it.usesNone,
                        generatorSubscriptionType = it.generatorSubscriptionType,
                        solarCapacity = it.solarCapacity,
                        solarHasBattery = it.solarHasBattery,
                        yearlyEdlBillUsd = it.yearlyEdlBillUsd,
                        edlPricePerKwhUsd = it.edlPricePerKwhUsd,
                        yearlyGeneratorBillUsd = it.yearlyGeneratorBillUsd,
                        generatorPricePerKwhUsd = it.generatorPricePerKwhUsd,
                        solarYearlyKwh = it.solarYearlyKwh
                    )
                } ?: ConsumptionSurveyUiState()

                screen = Screen.SurveyStep1
            }
        }
    }

    fun loadExistingSurveyForView(id: Long) {
        coroutineScope.launch {
            val result = surveyViewModel.loadSurvey(id)
            if (result != null) {
                val (data, savedReport) = result
                val report = savedReport ?: EnergyCalculator.calculate(data)

                surveyData = data
                energyReport = report
                editingSurveyId = null
                editingFromReview = false

                step1State = HouseSurveyUiState(
                    houseName = data.houseInfo?.houseName ?: "Household"
                )

                screen = Screen.Results
            }
        }
    }

    fun loadExistingSurveyForAnalysis(id: Long, type: AnalysisType) {
        coroutineScope.launch {
            val result = surveyViewModel.loadSurvey(id)
            if (result != null) {
                val (data, savedReport) = result
                val report = savedReport ?: EnergyCalculator.calculate(data)

                surveyData = data
                energyReport = report
                selectedAnalysisType = type
                editingSurveyId = null
                editingFromReview = false

                step1State = HouseSurveyUiState(
                    houseName = data.houseInfo?.houseName ?: "Household"
                )

                screen = Screen.HomeAnalysis
            }
        }
    }

    fun loadExistingSurveyForOptimization(id: Long) {
        coroutineScope.launch {
            val result = surveyViewModel.loadSurvey(id)
            if (result != null) {
                val (data, savedReport) = result
                surveyData = data
                energyReport = savedReport ?: EnergyCalculator.calculate(data)
                optimizingSurveyId = id
                screen = Screen.Optimization
            }
        }
    }

    when (screen) {
        Screen.Login -> LoginScreen(
            onLoginSuccess = { username, password ->
                authViewModel.login(username, password)
            },
            onCreateAccount = {
                screen = Screen.CreateAccount
            }
        )

        Screen.CreateAccount -> CreateAccountScreen(
            onAccountCreated = { newUser ->
                authViewModel.createAccount(
                    username = newUser.username,
                    password = newUser.password,
                    fullName = newUser.fullName,
                    email = newUser.email
                )
            },
            onBackToLogin = {
                screen = Screen.Login
            }
        )

        Screen.Dashboard -> {
            val surveyCount by surveyViewModel
                .observeSurveyCount(loggedUser)
                .collectAsState(initial = 0)

            val avgMonthlyKwh by surveyViewModel
                .observeAvgMonthlyKwh(loggedUser)
                .collectAsState(initial = null)

            val totalCo2 by surveyViewModel
                .observeTotalCo2(loggedUser)
                .collectAsState(initial = 0.0)

            DashboardScreen(
                username = loggedUser,
                surveyCount = surveyCount,
                avgMonthlyKwh = avgMonthlyKwh,
                totalCo2Kg = totalCo2,
                onStartSurvey = {
                    resetSurvey()
                    screen = Screen.SurveyStep1
                },
                onViewSavedHomes = {
                    screen = Screen.SavedHomes
                },
                onLogout = {
                    authViewModel.logout()
                    resetSurvey()
                    screen = Screen.Login
                }
            )
        }

        Screen.SavedHomes -> {
            val surveys by surveyViewModel
                .observeSurveys(loggedUser)
                .collectAsState(initial = emptyList())

            SavedHomesScreen(
                surveys = surveys,
                onBack = { screen = Screen.Dashboard },
                onViewResults = { id -> loadExistingSurveyForView(id) },
                onOpenAnalysis = { id, type -> loadExistingSurveyForAnalysis(id, type) },
                onOpenOptimization = { id -> loadExistingSurveyForOptimization(id) },
                onEditSurvey = { id -> loadExistingSurveyForEdit(id) },
                onDeleteSurvey = { id -> surveyViewModel.deleteSurvey(id) }
            )
        }

        Screen.HomeAnalysis -> {
            val report = energyReport

            if (report != null) {
                HomeAnalysisScreen(
                    analysisType = selectedAnalysisType,
                    report = report,
                    surveyData = surveyData,
                    houseName = surveyData.houseInfo?.houseName
                        ?: step1State.houseName.ifBlank { "Household" },
                    onBack = {
                        screen = Screen.SavedHomes
                    },
                    onViewFullResults = {
                        screen = Screen.Results
                    }
                )
            } else {
                screen = Screen.SavedHomes
            }
        }

        Screen.Optimization -> {
            val report = energyReport
            val surveyId = optimizingSurveyId

            if (report != null && surveyId != null) {
                OptimizationScreen(
                    surveyData = surveyData,
                    report = report,
                    houseName = surveyData.houseInfo?.houseName ?: "Household",
                    onBack = {
                        screen = Screen.SavedHomes
                    },
                    onPreviewOnly = { optimizedReport ->
                        energyReport = optimizedReport
                        screen = Screen.Results
                    },
                    onApplyToSavedHome = { optimizedSurveyData, optimizedReport ->
                        surveyData = optimizedSurveyData
                        energyReport = optimizedReport

                        surveyViewModel.updateSurvey(
                            surveyId,
                            loggedUser,
                            optimizedSurveyData,
                            optimizedReport
                        )

                        Toast.makeText(context, "Optimization applied to saved home!", Toast.LENGTH_SHORT).show()
                        screen = Screen.Results
                    }
                )
            } else {
                screen = Screen.SavedHomes
            }
        }

        Screen.SurveyStep1 -> HouseSurveyScreen(
            initialState = step1State,
            onBackClick = {
                if (editingSurveyId != null) {
                    resetSurvey()
                    screen = Screen.SavedHomes
                } else {
                    screen = Screen.Dashboard
                }
            },
            onNextClick = { state ->
                step1State = state
                surveyData = surveyData.copy(
                    houseInfo = HouseInfo(
                        houseName = state.houseName,
                        location = state.location,
                        houseType = state.houseType,
                        floorNumber = state.floorNumber,
                        buildingAge = state.buildingAge,
                        totalAreaM2 = state.totalAreaM2,
                        numberOfRooms = state.numberOfRooms,
                        numberOfOccupants = state.numberOfOccupants,
                        glassSurfaceM2 = state.glassSurfaceM2,
                        exposedWallSurfaceM2 = state.exposedWallSurfaceM2,
                        numberOfWallLayers = state.numberOfWallLayers,
                        wallLayers = state.wallLayers,
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
            buildingAge = step1State.buildingAge,
            onBackClick = { screen = Screen.SurveyStep1 },
            onNextClick = { state ->
                step2State = state
                surveyData = surveyData.copy(
                    hvacInfo = HvacInfo(
                        numberOfAcUnits = state.numberOfAcUnits,
                        acUnits = state.acUnits,

                        heatingSystemType = state.heatingSystemType,
                        numberOfHeatingAcUnits = state.numberOfHeatingAcUnits,
                        heatingAcUnits = state.heatingAcUnits,
                        numberOfHeatingUnits = state.numberOfHeatingUnits,
                        heatingPowerKw = state.heatingPowerKw,
                        heatingDailyUsageHours = state.heatingDailyUsageHours,
                        heatingDaysPerYear = state.heatingDaysPerYear,
                        heatingGasKgPerYear = state.heatingGasKgPerYear,
                        heatingFuelLitersPerYear = state.heatingFuelLitersPerYear,
                        heatedAreaM2 = state.heatedAreaM2,

                        heatingEfficiencyMethod = state.heatingEfficiencyMethod,
                        heatingEfficiencyPercent = state.heatingEfficiencyPercent,
                        heatingInstallationYear = state.heatingInstallationYear,

                        numberOfWaterHeaters = state.numberOfWaterHeaters,
                        waterHeaters = state.waterHeaters
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
                        numberOfDirectLamps = state.numberOfDirectLamps,
                        numberOfDirectTypes = state.numberOfDirectTypes,
                        directLampSamples = state.directLampSamples,
                        hasIndirectLighting = state.hasIndirectLighting,
                        numberOfIndirectRooms = state.numberOfIndirectRooms,
                        indirectRooms = state.indirectRooms,
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
                        solarHasBattery = state.solarHasBattery,
                        yearlyEdlBillUsd = state.yearlyEdlBillUsd,
                        edlPricePerKwhUsd = state.edlPricePerKwhUsd,
                        yearlyGeneratorBillUsd = state.yearlyGeneratorBillUsd,
                        generatorPricePerKwhUsd = state.generatorPricePerKwhUsd,
                        solarYearlyKwh = state.solarYearlyKwh
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

                val report = EnergyCalculator.calculate(updatedSurveyData)
                energyReport = report

                val currentEditingId = editingSurveyId

                if (currentEditingId != null) {
                    surveyViewModel.updateSurvey(currentEditingId, loggedUser, updatedSurveyData, report)
                    Toast.makeText(context, "Survey updated!", Toast.LENGTH_SHORT).show()
                } else {
                    surveyViewModel.saveSurvey(loggedUser, updatedSurveyData, report)
                    Toast.makeText(context, "Survey submitted & saved!", Toast.LENGTH_SHORT).show()
                }

                screen = Screen.Results
            },
            onSaveDraft = {
                screen = Screen.Dashboard
            }
        )

        Screen.Results -> {
            val report = energyReport
            if (report != null) {
                ResultsScreen(
                    report = report,
                    surveyData = surveyData,
                    houseName = surveyData.houseInfo?.houseName
                        ?: step1State.houseName.ifBlank { "Household" },
                    onBackToDashboard = {
                        resetSurvey()
                        screen = Screen.Dashboard
                    }
                )
            } else {
                screen = Screen.Dashboard
            }
        }
    }
}
