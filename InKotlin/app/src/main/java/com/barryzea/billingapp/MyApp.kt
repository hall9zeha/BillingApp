package com.barryzea.billingapp

import android.annotation.SuppressLint
import android.app.Application
import com.barryzea.billingapp.common.Preferences

/****
 * Project BillingApp
 * Created by Barry Zea H. on 1/3/23.
 * Copyright (c)  All rights reserved.
 ***/
class MyApp: Application() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var prefs: Preferences
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Preferences(applicationContext)
    }
}