package com.ironsource.adapters.custom.startapp;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.adunit.adapter.BaseAdapter;
import com.ironsource.mediationsdk.adunit.adapter.listener.NetworkInitializationListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import static com.ironsource.adapters.custom.startapp.BuildConfig.DEBUG;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_ACCOUNT_ID;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_APP_ID;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrors.ADAPTER_ERROR_MISSING_PARAMS;

@Keep
@SuppressWarnings("unused")
public class StartAppCustomAdapter extends BaseAdapter {
    private static final String LOG_TAG = StartAppCustomAdapter.class.getSimpleName();

    public StartAppCustomAdapter() {
        if (DEBUG) {
            Log.v(LOG_TAG, "ctor");
        }
    }

    @Override
    public void init(@NonNull AdData adData, @NonNull Context context, @Nullable NetworkInitializationListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "init: " + adData.getConfiguration());
        }

        String appId = adData.getString(KEY_APP_ID);
        appId = appId != null ? appId.trim() : null;

        if (appId != null && appId.length() > 0) {
            String accountId = adData.getString(KEY_ACCOUNT_ID);

            StartAppAd.disableSplash();
            StartAppAd.enableConsent(context, false);
            StartAppSDK.addWrapper(context, "IronSource", getAdapterVersion());
            StartAppSDK.init(context.getApplicationContext(), accountId, appId, null, false);

            if (listener != null) {
                listener.onInitSuccess();
            }
        } else {
            if (listener != null) {
                listener.onInitFailed(ADAPTER_ERROR_MISSING_PARAMS, KEY_APP_ID);
            }
        }
    }

    @Override
    public String getNetworkSDKVersion() {
        return StartAppSDK.getVersion();
    }

    @NonNull
    @Override
    public String getAdapterVersion() {
        return BuildConfig.VERSION_NAME;
    }
}
