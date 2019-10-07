package com.example.raindown.finalyearproject;
/*Author : Lee Thian Xin
Programme : RSD3
Year : 2018*/

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class UserSharedPreferences {
    private static SharedPreferences mSharedPref;
    public static final String userID = "userID";
    public static final String userName = "userName";
    public static final String userPhoto = "userPhoto";
    public static final String userProgramme = "userprogramme";

    private UserSharedPreferences() {
    }

    public static void init(Context context){
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    public static String read(String key, String defValue){
        return mSharedPref.getString(key, defValue);
    }

    public static void write(String key, String value){
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
    }

    public static void remove(String Key){
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.remove(Key);
    }
}
