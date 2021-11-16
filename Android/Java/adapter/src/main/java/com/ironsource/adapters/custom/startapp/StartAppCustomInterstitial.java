package com.ironsource.adapters.custom.startapp;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.adunit.adapter.BaseInterstitial;
import com.ironsource.mediationsdk.adunit.adapter.listener.InterstitialAdListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType;
import com.ironsource.mediationsdk.model.NetworkSettings;
import com.startapp.mediation.common.StartAppMediationExtras;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;

import java.util.HashMap;
import java.util.Map;

import static com.ironsource.adapters.custom.startapp.BuildConfig.DEBUG;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_AD_TAG;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_EXTRA;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_INTERNAL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_NO_FILL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrors.ADAPTER_ERROR_INTERNAL;

@Keep
@SuppressWarnings("unused")
public class StartAppCustomInterstitial extends BaseInterstitial<StartAppCustomAdapter> {
    private static final String LOG_TAG = StartAppCustomInterstitial.class.getSimpleName();

    @NonNull
    private final Map<String, StartAppCustomAdWrapper> wrappers = new HashMap<>();

    public StartAppCustomInterstitial(NetworkSettings networkSettings) {
        super(networkSettings);

        if (DEBUG) {
            Log.v(LOG_TAG, "ctor");
        }
    }

    @Nullable
    private StartAppCustomAdWrapper getWrapper(@NonNull AdData adData) {
        synchronized (wrappers) {
            return wrappers.get(StartAppCustomHelper.adKey(adData));
        }
    }

    @NonNull
    private StartAppCustomAdWrapper createWrapperIfAbsent(@NonNull AdData adData, @NonNull Context context) {
        synchronized (wrappers) {
            String key = StartAppCustomHelper.adKey(adData);

            StartAppCustomAdWrapper result = wrappers.get(key);
            if (result == null) {
                String extra = adData.getString(KEY_EXTRA);
                if (extra != null) {
                    try {
                        // noinspection CharsetObjectCanBeUsed,RedundantSuppression
                        extra = new String(Base64.decode(extra, Base64.DEFAULT), "UTF-8");
                    } catch (Throwable ex) {
                        if (DEBUG) {
                            Log.w(LOG_TAG, ex);
                        }
                    }
                }

                result = new StartAppCustomAdWrapper(
                        new StartAppAd(context),
                        new StartAppMediationExtras(null, extra, false, false),
                        adData.getString(KEY_AD_TAG)
                );

                wrappers.put(key, result);
            }

            return result;
        }
    }

    @Override
    public void loadAd(@NonNull AdData adData, @NonNull Activity activity, @NonNull InterstitialAdListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadAd: " + adData.getConfiguration());
        }

        StartAppCustomAdWrapper wrapper = createWrapperIfAbsent(adData, activity.getApplicationContext());

        wrapper.loadAd(new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                listener.onAdLoadSuccess();
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                AdapterErrorType errorType = ADAPTER_ERROR_TYPE_INTERNAL;
                String errorMessage = ad != null ? ad.getErrorMessage() : null;

                if ("NO FILL".equals(errorMessage)) {
                    errorType = ADAPTER_ERROR_TYPE_NO_FILL;
                }

                listener.onAdLoadFailed(errorType, ADAPTER_ERROR_INTERNAL, errorMessage);
            }
        });
    }

    @Override
    public boolean isAdAvailable(@NonNull AdData adData) {
        if (DEBUG) {
            Log.v(LOG_TAG, "isAdAvailable: " + adData.getConfiguration());
        }

        StartAppCustomAdWrapper wrapper = getWrapper(adData);
        return wrapper != null && wrapper.isReady();
    }

    @Override
    public void showAd(@NonNull AdData adData, @NonNull InterstitialAdListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showAd: " + adData.getConfiguration());
        }

        StartAppCustomAdWrapper wrapper = getWrapper(adData);

        if (wrapper != null) {
            wrapper.showAd(new AdDisplayListener() {
                @Override
                public void adHidden(Ad ad) {
                    listener.onAdClosed();
                }

                @Override
                public void adDisplayed(Ad ad) {
                    listener.onAdShowSuccess();
                }

                @Override
                public void adClicked(Ad ad) {
                    listener.onAdClicked();
                }

                @Override
                public void adNotDisplayed(Ad ad) {
                    listener.onAdShowFailed(ADAPTER_ERROR_INTERNAL, ad != null ? ad.getErrorMessage() : null);
                }
            });
        } else {
            listener.onAdShowFailed(ADAPTER_ERROR_INTERNAL, null);
        }
    }
}
