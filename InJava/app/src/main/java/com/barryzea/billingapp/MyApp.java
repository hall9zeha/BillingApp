package com.barryzea.billingapp;

import android.annotation.SuppressLint;
import android.app.Application;

import com.barryzea.billingapp.common.Preferences;

/****
 * Project BillingApp
 * Created by Barry Zea H. on 01/03/23.
 * Copyright (c)  All rights reserved.
 ***/
public class MyApp extends Application {

    @SuppressLint("StaticFieldLeak")
    public  static Preferences prefs;
    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new Preferences(this);

    }


}
