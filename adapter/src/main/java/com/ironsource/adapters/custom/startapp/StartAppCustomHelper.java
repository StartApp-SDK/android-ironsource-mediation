package com.ironsource.adapters.custom.startapp;

import androidx.annotation.NonNull;

import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;

import java.util.Map;

public class StartAppCustomHelper {
    public static final String KEY_INSTANCE_TYPE = "instanceType";
    public static final String KEY_INSTANCE_NAME = "instanceName";
    public static final String KEY_ACCOUNT_ID = "accountId";
    public static final String KEY_APP_ID = "appId";
    public static final String KEY_EXTRA = "extra";
    public static final String KEY_AD_TAG = "adTag";

    @NonNull
    public static String adKey(@NonNull AdData adData) {
        Map<String, Object> cfg = adData.getConfiguration();
        if (cfg == null) {
            return "";
        }

        return cfg.get(KEY_INSTANCE_TYPE) + "-" +
                cfg.get(KEY_INSTANCE_NAME) + "-" +
                cfg.get(KEY_ACCOUNT_ID) + "-" +
                cfg.get(KEY_APP_ID) + "-" +
                cfg.get(KEY_EXTRA) + "-" +
                cfg.get(KEY_AD_TAG);
    }
}
