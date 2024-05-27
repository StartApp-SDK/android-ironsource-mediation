package com.ironsource.adapters.custom.startapp;

import static com.ironsource.adapters.custom.startapp.BuildConfig.DEBUG;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_AD_TAG;
import static com.ironsource.adapters.custom.startapp.StartAppCustomHelper.KEY_EXTRA;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_INTERNAL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_NO_FILL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrors.ADAPTER_ERROR_INTERNAL;

import android.app.Activity;
import android.content.Context;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.adunit.adapter.BaseRewardedVideo;
import com.ironsource.mediationsdk.adunit.adapter.listener.RewardedVideoAdListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType;
import com.ironsource.mediationsdk.model.NetworkSettings;
import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

import java.util.HashMap;
import java.util.Map;

@Keep
@SuppressWarnings("unused")
public class StartAppCustomRewardedVideo extends BaseRewardedVideo<StartAppCustomAdapter> {

    private static final String LOG_TAG = StartAppCustomRewardedVideo.class.getSimpleName();

    @NonNull
    private final Map<String, StartAppCustomAdWrapper> wrappers = new HashMap<>();

    public StartAppCustomRewardedVideo(NetworkSettings networkSettings) {
        super(networkSettings);
        if (DEBUG) {
            Log.v(LOG_TAG, "StartAppCustomRewardedVideo created: ");
        }
    }

    @Override
    public void showAd(@NonNull AdData adData, @NonNull RewardedVideoAdListener listener) {
        if (DEBUG) {
            Log.v(LOG_TAG, "showAd: " + adData.getConfiguration());
        }
        StartAppCustomAdWrapper wrapper = getWrapper(adData);

        VideoListener videoListener = listener::onAdRewarded;

        if (wrapper != null) {
            boolean shown = wrapper.showAd(new AdDisplayListener() {
                @Override
                public void adHidden(Ad ad) {
                    listener.onAdClosed();
                }

                @Override
                public void adDisplayed(Ad ad) {
                    // none
                }

                @Override
                public void adClicked(Ad ad) {
                    listener.onAdClicked();
                }

                @Override
                public void adNotDisplayed(Ad ad) {
                    listener.onAdShowFailed(ADAPTER_ERROR_INTERNAL, ad != null ? ad.getErrorMessage() : null);
                }
            }, videoListener);

            if (shown) {
                listener.onAdOpened();
            } else {
                listener.onAdShowFailed(ADAPTER_ERROR_INTERNAL, null);
            }
        } else {
            listener.onAdShowFailed(ADAPTER_ERROR_INTERNAL, null);
        }
    }

    @Override
    public boolean isAdAvailable(@NonNull AdData adData) {
        StartAppCustomAdWrapper wrapper = getWrapper(adData);
        return wrapper != null && wrapper.isReady();
    }

    @Override
    public void loadAd(
            @NonNull AdData adData,
            @NonNull Activity activity,
            @NonNull RewardedVideoAdListener rewardedVideoAdListener
    ) {
        if (DEBUG) {
            Log.v(LOG_TAG, "loadAd: " + adData.getConfiguration());
        }

        StartAppCustomAdWrapper wrapper = createWrapperIfAbsent(adData, activity.getApplicationContext());

        wrapper.loadAd(new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                rewardedVideoAdListener.onAdLoadSuccess();
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                AdapterErrorType errorType = ADAPTER_ERROR_TYPE_INTERNAL;
                String errorMessage = ad != null ? ad.getErrorMessage() : null;

                if ("NO FILL".equals(errorMessage)) {
                    errorType = ADAPTER_ERROR_TYPE_NO_FILL;
                }

                rewardedVideoAdListener.onAdLoadFailed(errorType, ADAPTER_ERROR_INTERNAL, errorMessage);
            }
        }, StartAppAd.AdMode.REWARDED_VIDEO);
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
}
