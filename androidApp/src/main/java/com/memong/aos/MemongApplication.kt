package com.memong.aos

import android.app.Application
import android.content.Context
import com.avatye.adcash.ADCashSDK
import com.avatye.haru.log.LogTrack
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Tool
import com.jakewharton.threetenabp.AndroidThreeTen
import com.memong.aos.data.database.MemoDatabase
import com.memong.aos.data.utils.PreferenceUtil
import com.memong.aos.data.utils.RemoteConfigUtil
import com.memong.aos.helper.MemoWidgetUpdater

internal class MemongApplication : Application() {

    lateinit var generativeModel: GenerativeModel
        private set

    companion object {
        private lateinit var instance: MemongApplication
        fun context(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()

        instance = this

        // Preference
        PreferenceUtil.init(this@MemongApplication)
        // Log
        LogTrack.initializeLog(
            moduleName = getString(R.string.haru_log_moudle_name),
            allowLog = true
        )
        // DB
        MemoDatabase.getInstance(this@MemongApplication)
        // Widget
        MemoWidgetUpdater().observeMemoChanges(this@MemongApplication)
        // ThreeTen
        AndroidThreeTen.init(this@MemongApplication)

        // Firebase
        FirebaseApp.initializeApp(this@MemongApplication)

        // Firebase RemoteConfig
        RemoteConfigUtil.initRemoteConfiguration(this@MemongApplication)

        // AdCash
        val builder = ADCashSDK.Builder(
            context = this,
            appId = BuildConfig.ADCASH_APP_ID,
            appSecret = BuildConfig.ADCASH_APP_SECRET,
        )
        builder.build()

        // Mediation-Admob
        MobileAds.initialize(this) {}


        // AI
        generativeModel = Firebase.ai(
            backend = GenerativeBackend.googleAI()
        ).generativeModel("gemini-2.5-flash",
            tools = listOf(Tool.googleSearch()))
    }
}