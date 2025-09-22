package com.avatye.haru.memo

import android.app.Application
import com.avatye.adcash.ADCashSDK
import com.avatye.haru.log.LogTrack
import com.avatye.haru.memo.data.database.MemoDatabase
import com.avatye.haru.memo.data.utils.PreferenceUtil
import com.avatye.haru.memo.data.utils.RemoteConfigUtil
import com.avatye.haru.memo.helper.MemoWidgetUpdater
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.jakewharton.threetenabp.AndroidThreeTen

internal class HaruMemoApplication : Application() {

    lateinit var generativeModel: GenerativeModel
        private set

    override fun onCreate() {
        super.onCreate()

        // Preference
        PreferenceUtil.init(this@HaruMemoApplication)
        // Log
        LogTrack.initializeLog(
            moduleName = getString(R.string.haru_log_moudle_name),
            allowLog = true
        )
        // DB
        MemoDatabase.getInstance(this@HaruMemoApplication)
        // Widget
        MemoWidgetUpdater().observeMemoChanges(this@HaruMemoApplication)
        // ThreeTen
        AndroidThreeTen.init(this@HaruMemoApplication)

        // Firebase
        FirebaseApp.initializeApp(this@HaruMemoApplication)

        // Firebase RemoteConfig
        RemoteConfigUtil.initRemoteConfiguration(this@HaruMemoApplication)

        // AdCash
        val builder = ADCashSDK.Builder(
            context = this,
            appId = BuildConfig.ADCASH_APP_ID,
            appSecret = BuildConfig.ADCASH_APP_SECRET,
        )
        builder.build()

        // AI
        generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash",
            tools = listOf(Tool.googleSearch()))
    }
}