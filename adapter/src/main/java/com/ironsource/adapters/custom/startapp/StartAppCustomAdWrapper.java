package com.ironsource.adapters.custom.startapp;

import static com.ironsource.adapters.custom.startapp.StartAppMediationExtras.isValidAdTag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.startapp.sdk.adsbase.Ad;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

class StartAppCustomAdWrapper {
    @NonNull
    private final StartAppAd ad;

    @NonNull
    private final StartAppMediationExtras extras;

    private volatile boolean isLoaded = false;

    StartAppCustomAdWrapper(
            @NonNull StartAppAd ad,
            @NonNull StartAppMediationExtras extras,
            @Nullable String adTag
    ) {
        this.ad = ad;
        this.extras = extras;

        if (isValidAdTag(adTag)) {
            extras.getAdPreferences().setAdTag(adTag);
        }
    }

    synchronized boolean isReady() {
        return isLoaded;
    }

    void loadAd(@NonNull AdEventListener listener) {
        if (extras.getAdMode() != null) {
            ad.loadAd(extras.getAdMode(), extras.getAdPreferences(), stateLoadListener(listener));
        } else {
            ad.loadAd(extras.getAdPreferences(), stateLoadListener(listener));
        }
    }

    /**
     * @noinspection SameParameterValue
     */
    void loadAd(@NonNull AdEventListener listener, StartAppAd.AdMode adMode) {
        ad.loadAd(adMode, extras.getAdPreferences(), stateLoadListener(listener));
    }

    synchronized boolean showAd(@NonNull AdDisplayListener listener) {
        isLoaded = false;
        return ad.showAd(listener);
    }

    synchronized boolean showAd(@NonNull AdDisplayListener listener, VideoListener videoListener) {
        isLoaded = false;
        ad.setVideoListener(videoListener);
        return ad.showAd(listener);
    }

    @NonNull
    private AdEventListener stateLoadListener(@NonNull AdEventListener listener) {
        return new AdEventListener() {
            @Override
            public void onReceiveAd(@NonNull Ad ad) {
                synchronized (StartAppCustomAdWrapper.this) {
                    isLoaded = true;
                    listener.onReceiveAd(ad);
                }
            }

            @Override
            public void onFailedToReceiveAd(@Nullable Ad ad) {
                synchronized (StartAppCustomAdWrapper.this) {
                    isLoaded = false;
                    listener.onFailedToReceiveAd(ad);
                }
            }
        };
    }
}
