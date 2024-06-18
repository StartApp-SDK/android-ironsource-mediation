package com.ironsource.adapters.custom.startapp;

import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.model.AdPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

// IMPORTANT: Never change this class! it was copied from
// https://github.com/StartApp-SDK/android-mediation-common
// If you need to make changes, please, do it in the original repo at first!
@SuppressWarnings("unused")
public class StartAppMediationExtras {
    private static final String AD_TAG = "adTag";
    private static final String INTERSTITIAL_MODE = "interstitialMode";
    private static final String MIN_CPM = "minCPM";
    private static final String MUTE_VIDEO = "muteVideo";
    private static final String IS_3D_BANNER = "is3DBanner";
    private static final String NATIVE_IMAGE_SIZE = "nativeImageSize";
    private static final String NATIVE_SECONDARY_IMAGE_SIZE = "nativeSecondaryImageSize";
    private static final String APP_ID = "startappAppId";

    @NonNull
    private final AdPreferences adPreferences;

    private boolean banner3d;

    @Nullable
    private StartAppAd.AdMode adMode;

    @Nullable
    private String appId;

    @NonNull
    public AdPreferences getAdPreferences() {
        return adPreferences;
    }

    public boolean isBanner3d() {
        return banner3d;
    }

    @Nullable
    public StartAppAd.AdMode getAdMode() {
        return adMode;
    }

    @Nullable
    public String getAppId() {
        return appId;
    }

    public StartAppMediationExtras(@Nullable Bundle bundle, @Nullable String json, boolean nativeAd, boolean nativeAutoDownload) {
        adPreferences = parseAdPreferences(bundle, json, nativeAd, nativeAutoDownload);
    }

    public StartAppMediationExtras(@Nullable Map<String, Object> customEventExtras, @Nullable String json) {
        Bundle bundle = new Bundle();
        String adTag = (String) customEventExtras.get(AD_TAG);
        Boolean isVideoMuted = Boolean.parseBoolean((String) customEventExtras.get(MUTE_VIDEO));
        Boolean is3DBanner = Boolean.parseBoolean((String) customEventExtras.get(IS_3D_BANNER));
        if (customEventExtras.containsKey(MIN_CPM) && customEventExtras.get(MIN_CPM) != null) {
            try {
                Double minCPM = Double.parseDouble((String) Objects.requireNonNull(customEventExtras.get(MIN_CPM)));
                bundle.putDouble(MIN_CPM, minCPM);
            } catch (Exception e) {
                //ignore
            }
        }
        bundle.putString(AD_TAG, adTag);
        bundle.putBoolean(MUTE_VIDEO, isVideoMuted);
        bundle.putBoolean(IS_3D_BANNER, is3DBanner);
        adPreferences = parseAdPreferences(bundle, json, false, false);
    }


    @NonNull
    private AdPreferences parseAdPreferences(
            @Nullable Bundle customEventExtras,
            @Nullable String serverParameter,
            boolean nativeAd,
            boolean nativeAutoDownload
    ) {
        String adTag = null;
        boolean isVideoMuted = false;
        Double minCPM = null;
        Size nativeImageSize = null;
        Size nativeSecondaryImageSize = null;

        if (customEventExtras != null) {
            adTag = customEventExtras.getString(AD_TAG);
            isVideoMuted = customEventExtras.getBoolean(MUTE_VIDEO);
            banner3d = customEventExtras.getBoolean(IS_3D_BANNER);

            if (customEventExtras.containsKey(MIN_CPM)) {
                minCPM = customEventExtras.getDouble(MIN_CPM);
            }

            if (customEventExtras.containsKey(INTERSTITIAL_MODE)) {
                final Mode srcAdMode = (Mode) customEventExtras.getSerializable(INTERSTITIAL_MODE);
                if (srcAdMode != null) {
                    switch (srcAdMode) {
                        case OVERLAY:
                            adMode = StartAppAd.AdMode.OVERLAY;
                            break;
                        case VIDEO:
                            adMode = StartAppAd.AdMode.VIDEO;
                            break;
                        case OFFERWALL:
                            adMode = StartAppAd.AdMode.OFFERWALL;
                            break;
                    }
                }
            }

            if (customEventExtras.containsKey(NATIVE_IMAGE_SIZE)) {
                nativeImageSize = (Size) customEventExtras.getSerializable(NATIVE_IMAGE_SIZE);
            }

            if (customEventExtras.containsKey(NATIVE_SECONDARY_IMAGE_SIZE)) {
                nativeSecondaryImageSize = (Size) customEventExtras.getSerializable(NATIVE_SECONDARY_IMAGE_SIZE);
            }
        }

        if (serverParameter != null) {
            try {
                final JSONObject json = new JSONObject(serverParameter);

                if (json.has(AD_TAG)) {
                    adTag = json.getString(AD_TAG);
                }

                if (json.has(MUTE_VIDEO)) {
                    isVideoMuted = json.getBoolean(MUTE_VIDEO);
                }

                if (json.has(IS_3D_BANNER)) {
                    banner3d = json.getBoolean(IS_3D_BANNER);
                }

                if (json.has(MIN_CPM)) {
                    minCPM = json.getDouble(MIN_CPM);
                }

                if (json.has(NATIVE_IMAGE_SIZE)) {
                    String name = json.getString(NATIVE_IMAGE_SIZE);

                    try {
                        nativeImageSize = Size.valueOf(name);
                    } catch (RuntimeException ex) {
                        // ignore
                    }
                }

                if (json.has(NATIVE_SECONDARY_IMAGE_SIZE)) {
                    String name = json.getString(NATIVE_SECONDARY_IMAGE_SIZE);

                    try {
                        nativeSecondaryImageSize = Size.valueOf(name);
                    } catch (RuntimeException ex) {
                        // ignore
                    }
                }

                if (json.has(INTERSTITIAL_MODE)) {
                    final String mode = json.getString(INTERSTITIAL_MODE);
                    switch (mode) {
                        case "OVERLAY":
                            adMode = StartAppAd.AdMode.OVERLAY;
                            break;
                        case "VIDEO":
                            adMode = StartAppAd.AdMode.VIDEO;
                            break;
                        case "OFFERWALL":
                            adMode = StartAppAd.AdMode.OFFERWALL;
                            break;
                    }
                }

                if (json.has(APP_ID)) {
                    appId = json.getString(APP_ID);
                }
            } catch (JSONException ex) {
                // ignore
            }
        }

        NativeAdPreferences nativeAdPrefs = null;
        AdPreferences prefs;
        if (nativeAd) {
            nativeAdPrefs = new NativeAdPreferences();
            prefs = nativeAdPrefs;
        } else {
            prefs = new AdPreferences();
        }

        if (isValidAdTag(adTag)) {
            prefs.setAdTag(adTag);
        }

        prefs.setMinCpm(minCPM);

        if (isVideoMuted) {
            prefs.muteVideo();
        }

        if (nativeAd) {
            if (nativeImageSize != null) {
                nativeAdPrefs.setPrimaryImageSize(nativeImageSize.ordinal());
            }

            if (nativeSecondaryImageSize != null) {
                nativeAdPrefs.setSecondaryImageSize(nativeSecondaryImageSize.ordinal());
            }

            nativeAdPrefs.setAutoBitmapDownload(nativeAutoDownload);
        }

        return prefs;
    }

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isValidAdTag(@Nullable String adTag) {
        if (adTag == null) {
            return false;
        }

        if (adTag.trim().isEmpty()) {
            return false;
        }

        if (adTag.equals("default")) {
            return false;
        }

        return true;
    }

    public static class Builder {
        @NonNull
        final Bundle extras = new Bundle();

        @NonNull
        public Builder setAdTag(@NonNull String adTag) {
            extras.putString(AD_TAG, adTag);
            return this;
        }

        @NonNull
        public Builder setInterstitialMode(@NonNull Mode interstitialMode) {
            extras.putSerializable(INTERSTITIAL_MODE, interstitialMode);
            return this;
        }

        @NonNull
        public Builder setMinCPM(double cpm) {
            extras.putDouble(MIN_CPM, cpm);
            return this;
        }

        @NonNull
        public Builder setNativeImageSize(@NonNull Size size) {
            extras.putSerializable(NATIVE_IMAGE_SIZE, size);
            return this;
        }

        @NonNull
        public Builder setNativeSecondaryImageSize(@NonNull Size size) {
            extras.putSerializable(NATIVE_SECONDARY_IMAGE_SIZE, size);
            return this;
        }

        @NonNull
        public Builder muteVideo() {
            extras.putBoolean(MUTE_VIDEO, true);
            return this;
        }

        @NonNull
        public Builder enable3DBanner() {
            extras.putBoolean(IS_3D_BANNER, true);
            return this;
        }

        @NonNull
        public Bundle toBundle() {
            return extras;
        }
    }

    @Keep
    public enum Mode {
        OFFERWALL,
        VIDEO,
        OVERLAY,
    }

    @Keep
    public enum Size {
        SIZE72X72,
        SIZE100X100,
        SIZE150X150,
        SIZE340X340,
        SIZE1200X628,
    }
}
