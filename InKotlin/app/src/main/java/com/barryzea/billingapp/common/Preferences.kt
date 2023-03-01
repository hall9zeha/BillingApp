package com.barryzea.billingapp.common

import android.content.Context
import android.content.SharedPreferences
import com.barryzea.billingapp.R

/****
 * Project BillingApp
 * Created by Barry Zea H. on 1/3/23.
 * Copyright (c)  All rights reserved.
 ***/
class Preferences(context: Context){
    private var ctx = context
    private val NAME_FILE = "myPreferences"
    private val preferences: SharedPreferences = ctx.getSharedPreferences(NAME_FILE, Context.MODE_PRIVATE)

     var buyRemoveAds:Boolean
        get() = preferences.getBoolean(ctx.getString(R.string.removeAds),false)
        set(value) = preferences.edit().putBoolean(ctx.getString(R.string.removeAds),value).apply()


     var buyPremiumUser:Boolean
         get() = preferences.getBoolean(ctx.getString(R.string.premiumUser),false)
         set(value) = preferences.edit().putBoolean(ctx.getString(R.string.premiumUser),value).apply()

}