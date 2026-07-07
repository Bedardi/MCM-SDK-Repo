package com.mistahub.mcm_sdk;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.UUID;

public class MCMClient {
    private static final String PREF_NAME = "MCM_PREFS";
    private static final String KEY_TOKEN = "MCM_DEVICE_TOKEN";

    public static synchronized String getToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_TOKEN, null);

        if (token == null) {
            token = "MCM-" + UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
            prefs.edit().putString(KEY_TOKEN, token).apply();
        }
        return token;
    }

    // YAHAN PAR URL LIYA JAYEGA JAB KOI APP IS SDK KO USE KAREGI
    public static void initialize(Context context, String appId, String adminDashboardUrl) {
        String token = getToken(context);
        MCMWorker.enqueueTokenSync(context, token, appId, adminDashboardUrl);
    }
}
