package com.univ.energymonitor.data

import android.content.Context
import com.univ.energymonitor.data.local.AppDatabase
import com.univ.energymonitor.data.local.SurveyDao
import com.univ.energymonitor.data.local.UserDao

/**
 * Lightweight manual dependency injection container.
 *
 * A single instance lives in the Application class and is accessed from
 * MainActivity / ViewModels. No external DI framework (Hilt, Koin) required.
 *
 * Later phases will add repositories here:
 *   val userRepository by lazy { UserRepository(userDao) }
 *   val surveyRepository by lazy { SurveyRepository(surveyDao) }
 */
class AppContainer(context: Context) {

    private val database: AppDatabase = AppDatabase.get(context)

    val userDao: UserDao = database.userDao()
    val surveyDao: SurveyDao = database.surveyDao()
}