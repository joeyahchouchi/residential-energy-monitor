package com.univ.energymonitor.ui.navigation

sealed interface Screen {
    object Login : Screen
    object CreateAccount : Screen
    object Dashboard : Screen
    object SavedHomes : Screen
    object HomeAnalysis : Screen
    object Optimization : Screen
    object SurveyStep1 : Screen
    object SurveyStep2 : Screen
    object SurveyStep3 : Screen
    object SurveyStep4 : Screen
    object SurveyStep5 : Screen
    object SurveyStep6 : Screen
    object Results : Screen
}