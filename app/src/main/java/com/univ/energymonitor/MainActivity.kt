package com.univ.energymonitor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import com.univ.energymonitor.screens.*
import com.univ.energymonitor.ui.theme.EnergyMonitorTheme

// ─────────────────────────────────────────────────────────────────────────────
// Screen — sealed interface for type-safe navigation
// ─────────────────────────────────────────────────────────────────────────────
sealed interface Screen {
    object Login       : Screen
    object Dashboard   : Screen
    object SurveyStep1 : Screen   // House Information
    object SurveyStep2 : Screen   // HVAC & Water Heating
    object SurveyStep3 : Screen   // Lighting Systems
    object SurveyStep4 : Screen   // Appliances & Electrical Loads
    object SurveyStep5 : Screen   // Energy Bills & Habits
    object SurveyStep6 : Screen   // Review & Submit
}

// ─────────────────────────────────────────────────────────────────────────────
// Step data models
// ─────────────────────────────────────────────────────────────────────────────

data class HouseInfo(
    val houseName: String,
    val location: String,
    val houseType: String,
    val floorNumber: String,
    val totalAreaM2: String,
    val numberOfRooms: String,
    val numberOfOccupants: String,
    val wallMaterial: String,
    val wallThickness: String,
    val glassType: String,
    val roofExposure: String,
    val insulationLevel: String
)

data class HvacInfo(
    val numberOfAcUnits: String,
    val acType: String,
    val acCapacityKw: String,
    val acDailyUsageHours: String,
    val acThermostatSetpoint: String,
    val isInverterAc: Boolean,
    val heatingSystemType: String,
    val numberOfHeatingUnits: String,
    val heatingDailyUsageHours: String,
    val waterHeaterType: String,
    val waterHeaterPowerKw: String,
    val waterHeaterDailyHours: String,
    val waterTankSizeLiters: String
)

data class LightingInfo(
    val totalFixtures: String,
    val mainBulbType: String,
    val avgDailyUsageHours: String,
    val mostlyEnergyEfficient: Boolean,
    val bulbsLivingRoom: String,
    val bulbsBedrooms: String,
    val bulbsKitchen: String,
    val bulbsBathroom: String,
    val bulbsHallwayOther: String,
    val avgBulbWattage: String,
    val hasOutdoorLighting: Boolean,
    val outdoorLightingHours: String
)

data class ApplianceInfo(
    val refrigeratorsCount        : String,
    val refrigeratorHoursPerDay   : String,
    val washingMachinesCount      : String,
    val washingMachineUsesPerWeek : String,
    val tvCount                   : String,
    val tvHoursPerDay             : String,
    val microwaveExists           : Boolean,
    val ovenExists                : Boolean,
    val dishwasherExists          : Boolean,
    val waterPumpExists           : Boolean,
    val computersCount            : String,
    val otherAppliancesNotes      : String
)

data class ConsumptionInfo(
    // Electricity bills
    val averageMonthlyBill   : String,
    val highestMonthlyBill   : String,
    val billCurrency         : String,
    val hasPreviousBills     : Boolean,
    // Generator
    val usesGenerator        : Boolean,
    val generatorAmperage    : String,
    val generatorMonthlyBill : String,
    val generatorBillCurrency: String,
    val generatorHoursPerDay : String,
    // Usage patterns
    val peakUsagePeriod      : String,
    val daytimeOccupancy     : String,
    val heavyWeekendUsage    : Boolean,
    val frequentAcUse        : Boolean,
    // Energy behavior & solar
    val triesToSaveEnergy         : Boolean,
    val turnsOffUnusedAppliances  : Boolean,
    val interestedInRecommendations: Boolean,
    val hasSolarSystem            : Boolean,
    val solarInverterSizeKw       : String
)

data class ReviewInfo(
    val confirmAccuracy : Boolean,
    val finalNotes      : String
)

// ─────────────────────────────────────────────────────────────────────────────
// SurveyDataContainer — accumulates all step data
// null = step not yet completed
// ─────────────────────────────────────────────────────────────────────────────
data class SurveyDataContainer(
    val houseInfo       : HouseInfo?       = null,   // Step 1
    val hvacInfo        : HvacInfo?        = null,   // Step 2
    val lightingInfo    : LightingInfo?    = null,   // Step 3
    val applianceInfo   : ApplianceInfo?   = null,   // Step 4
    val consumptionInfo : ConsumptionInfo? = null,   // Step 5
    val reviewInfo      : ReviewInfo?      = null    // Step 6
)

// ─────────────────────────────────────────────────────────────────────────────
// MainActivity
// ─────────────────────────────────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// AppRoot — owns all navigation and survey state
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppRoot() {

    val context = LocalContext.current

    // ⚠️ screen uses remember NOT rememberSaveable
    // sealed interface is not serializable — rememberSaveable would crash the app
    var screen by remember { mutableStateOf<Screen>(Screen.Login) }

    // String — safe to use rememberSaveable, survives screen rotation
    var loggedUser by rememberSaveable { mutableStateOf("") }

    // Survey data — plain remember, resets on process death intentionally
    var surveyData by remember { mutableStateOf(SurveyDataContainer()) }

    when (screen) {

        // ── Login ─────────────────────────────────────────────────────────────
        Screen.Login -> LoginScreen(
            onLoginSuccess = { user ->
                loggedUser = user
                screen = Screen.Dashboard
            }
        )

        // ── Dashboard ─────────────────────────────────────────────────────────
        Screen.Dashboard -> DashboardScreen(
            username = loggedUser,
            onStartSurvey = {
                surveyData = SurveyDataContainer() // reset for a fresh survey
                screen = Screen.SurveyStep1
            },
            onLogout = {
                loggedUser = ""
                surveyData = SurveyDataContainer()
                screen = Screen.Login
            }
        )

        // ── Step 1: House Information ─────────────────────────────────────────
        Screen.SurveyStep1 -> HouseSurveyScreen(
            onNextClick = { state ->
                surveyData = surveyData.copy(
                    houseInfo = HouseInfo(
                        houseName         = state.houseName,
                        location          = state.location,
                        houseType         = state.houseType,
                        floorNumber       = state.floorNumber,
                        totalAreaM2       = state.totalAreaM2,
                        numberOfRooms     = state.numberOfRooms,
                        numberOfOccupants = state.numberOfOccupants,
                        wallMaterial      = state.wallMaterial,
                        wallThickness     = state.wallThickness,
                        glassType         = state.glassType,
                        roofExposure      = state.roofExposure,
                        insulationLevel   = state.insulationLevel
                    )
                )
                screen = Screen.SurveyStep2
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )

        // ── Step 2: HVAC & Water Heating ──────────────────────────────────────
        Screen.SurveyStep2 -> HvacSurveyScreen(
            onBackClick = { screen = Screen.SurveyStep1 },
            onNextClick = { state ->
                surveyData = surveyData.copy(
                    hvacInfo = HvacInfo(
                        numberOfAcUnits        = state.numberOfAcUnits,
                        acType                 = state.acType,
                        acCapacityKw           = state.acCapacityKw,
                        acDailyUsageHours      = state.acDailyUsageHours,
                        acThermostatSetpoint   = state.acThermostatSetpoint,
                        isInverterAc           = state.isInverterAc,
                        heatingSystemType      = state.heatingSystemType,
                        numberOfHeatingUnits   = state.numberOfHeatingUnits,
                        heatingDailyUsageHours = state.heatingDailyUsageHours,
                        waterHeaterType        = state.waterHeaterType,
                        waterHeaterPowerKw     = state.waterHeaterPowerKw,
                        waterHeaterDailyHours  = state.waterHeaterDailyHours,
                        waterTankSizeLiters    = state.waterTankSizeLiters
                    )
                )
                screen = Screen.SurveyStep3
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )

        // ── Step 3: Lighting Systems ──────────────────────────────────────────
        Screen.SurveyStep3 -> LightingSurveyScreen(
            onBackClick = { screen = Screen.SurveyStep2 },
            onNextClick = { state ->
                surveyData = surveyData.copy(
                    lightingInfo = LightingInfo(
                        totalFixtures         = state.totalFixtures,
                        mainBulbType          = state.mainBulbType,
                        avgDailyUsageHours    = state.avgDailyUsageHours,
                        mostlyEnergyEfficient = state.mostlyEnergyEfficient,
                        bulbsLivingRoom       = state.bulbsLivingRoom,
                        bulbsBedrooms         = state.bulbsBedrooms,
                        bulbsKitchen          = state.bulbsKitchen,
                        bulbsBathroom         = state.bulbsBathroom,
                        bulbsHallwayOther     = state.bulbsHallwayOther,
                        avgBulbWattage        = state.avgBulbWattage,
                        hasOutdoorLighting    = state.hasOutdoorLighting,
                        outdoorLightingHours  = state.outdoorLightingHours
                    )
                )
                screen = Screen.SurveyStep4
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )

        // ── Step 4: Appliances & Electrical Loads ─────────────────────────────
        Screen.SurveyStep4 -> ApplianceSurveyScreen(
            onBackClick = { screen = Screen.SurveyStep3 },
            onNextClick = { state ->
                surveyData = surveyData.copy(
                    applianceInfo = ApplianceInfo(
                        refrigeratorsCount        = state.refrigeratorsCount,
                        refrigeratorHoursPerDay   = state.refrigeratorHoursPerDay,
                        washingMachinesCount      = state.washingMachinesCount,
                        washingMachineUsesPerWeek = state.washingMachineUsesPerWeek,
                        tvCount                   = state.tvCount,
                        tvHoursPerDay             = state.tvHoursPerDay,
                        microwaveExists           = state.microwaveExists,
                        ovenExists                = state.ovenExists,
                        dishwasherExists          = state.dishwasherExists,
                        waterPumpExists           = state.waterPumpExists,
                        computersCount            = state.computersCount,
                        otherAppliancesNotes      = state.otherAppliancesNotes
                    )
                )
                screen = Screen.SurveyStep5
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )

        // ── Step 5: Energy Bills & Habits ─────────────────────────────────────
        Screen.SurveyStep5 -> ConsumptionSurveyScreen(
            onBackClick = { screen = Screen.SurveyStep4 },
            onNextClick = { state ->
                surveyData = surveyData.copy(
                    consumptionInfo = ConsumptionInfo(
                        averageMonthlyBill        = state.averageMonthlyBill,
                        highestMonthlyBill        = state.highestMonthlyBill,
                        billCurrency              = state.billCurrency,
                        hasPreviousBills          = state.hasPreviousBills,
                        usesGenerator             = state.usesGenerator,
                        generatorAmperage         = state.generatorAmperage,
                        generatorMonthlyBill      = state.generatorMonthlyBill,
                        generatorBillCurrency     = state.generatorBillCurrency,
                        generatorHoursPerDay      = state.generatorHoursPerDay,
                        peakUsagePeriod           = state.peakUsagePeriod,
                        daytimeOccupancy          = state.daytimeOccupancy,
                        heavyWeekendUsage         = state.heavyWeekendUsage,
                        frequentAcUse             = state.frequentAcUse,
                        triesToSaveEnergy         = state.triesToSaveEnergy,
                        turnsOffUnusedAppliances  = state.turnsOffUnusedAppliances,
                        interestedInRecommendations = state.interestedInRecommendations,
                        hasSolarSystem            = state.hasSolarSystem,
                        solarInverterSizeKw       = state.solarInverterSizeKw
                    )
                )
                screen = Screen.SurveyStep6
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )

        // ── Step 6: Review & Submit ───────────────────────────────────────────
        Screen.SurveyStep6 -> ReviewSurveyScreen(
            surveyData = surveyData,
            onBackClick = { screen = Screen.SurveyStep5 },
            onEditStep = { stepNumber ->
                // Navigate to the step the user wants to edit
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
                surveyData = surveyData.copy(
                    reviewInfo = ReviewInfo(
                        confirmAccuracy = state.confirmAccuracy,
                        finalNotes      = state.finalNotes
                    )
                )
                // TODO: save complete surveyData to Firebase here
                Toast.makeText(
                    context,
                    "Survey submitted successfully! ✅",
                    Toast.LENGTH_LONG
                ).show()
                screen = Screen.Dashboard
            },
            onSaveDraft = { _ ->
                // TODO: save to Firebase
                screen = Screen.Dashboard
            }
        )
    }
}