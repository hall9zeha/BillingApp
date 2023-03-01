package com.barryzea.billingapp.common;

import android.content.Context;
import android.content.SharedPreferences;
import com.barryzea.billingapp.R;


/****
 * SimpleAdMob
 * Created by Barry Zea H. on 01/03/23.
 * Copyright (c)  All rights reserved.
 ***/
public class Preferences {
    private Context ctx;
    private String NAME_FILE="myPreferences";
    public SharedPreferences preferences;
    public  SharedPreferences.Editor editor;
    private Boolean buyRemoveAds=false;
    private Boolean buyPremiumUser=false;
    public Preferences(Context context) {
        ctx=context;
        initPreferences();
    }
    private void initPreferences(){
        preferences=ctx.getSharedPreferences(NAME_FILE,Context.MODE_PRIVATE);
        editor=preferences.edit();
    }
    public Boolean getBuyRemoveAds() {
        return preferences.getBoolean(ctx.getString(R.string.removeAds),false);
    }

    public void setBuyRemoveAds(Boolean buyRemoveAds) {
        this.buyRemoveAds = buyRemoveAds;
        editor.putBoolean(ctx.getString(R.string.removeAds),buyRemoveAds);
        editor.apply();
    }

    public Boolean getBuyPremiumUser() {
        return preferences.getBoolean(ctx.getString(R.string.premiumUser),false);
    }

    public void setBuyPremiumUser(Boolean buyPremiumUser) {
        this.buyPremiumUser = buyPremiumUser;
        editor.putBoolean(ctx.getString(R.string.premiumUser),buyPremiumUser);
        editor.apply();
    }
}
