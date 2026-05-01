package com.univ.energymonitor.data

import android.content.Context
import com.univ.energymonitor.data.local.AppDatabase
import com.univ.energymonitor.data.local.SurveyDao
import com.univ.energymonitor.data.local.UserDao
import com.univ.energymonitor.data.repository.SurveyRepository
import com.univ.energymonitor.data.repository.UserRepository

/**
 * Lightweight manual dependency injection container.
 * A single instance lives in EnergyMonitorApp and is accessed from ViewModels.
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.Companion.get(context)

    private val userDao: UserDao = database.userDao()
    private val surveyDao: SurveyDao = database.surveyDao()

    val userRepository: UserRepository = UserRepository(userDao)
    val surveyRepository: SurveyRepository = SurveyRepository(surveyDao)
}