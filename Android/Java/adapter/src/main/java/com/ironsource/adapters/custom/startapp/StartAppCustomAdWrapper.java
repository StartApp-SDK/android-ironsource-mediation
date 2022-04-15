package com.ironsource.adapters.custom.startapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdDisplayListener;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.startapp.sdk.adsbase.adlisteners.VideoListener;

class StartAppCustomAdWrapper {
    @NonNull
    private final StartAppAd ad;

    @NonNull
    private final StartAppMediationExtras extras;

    StartAppCustomAdWrapper(
            @NonNull StartAppAd ad,
            @NonNull StartAppMediationExtras extras,
            @Nullable String adTag
    ) {
        this.ad = ad;
        this.extras = extras;

        if (adTag != null) {
            extras.getAdPreferences().setAdTag(adTag);
        }
    }

    boolean isReady() {
        return ad.isReady();
    }

    void loadAd(@NonNull AdEventListener listener) {
        if (extras.getAdMode() != null) {
            ad.loadAd(extras.getAdMode(), extras.getAdPreferences(), listener);
        } else {
            ad.loadAd(extras.getAdPreferences(), listener);
        }
    }

    void showAd(@NonNull AdDisplayListener listener) {
        ad.showAd(extras.getAdPreferences().getAdTag(), listener);
    }

    void showAd(@NonNull AdDisplayListener listener, VideoListener videoListener) {
        ad.setVideoListener(videoListener);
        ad.showAd(extras.getAdPreferences().getAdTag(), listener);
    }
}
