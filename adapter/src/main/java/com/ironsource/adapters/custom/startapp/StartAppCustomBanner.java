package com.ironsource.adapters.custom.startapp;

import static com.ironsource.adapters.custom.startapp.BuildConfig.DEBUG;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.adunit.adapter.BaseBanner;
import com.ironsource.mediationsdk.adunit.adapter.internal.listener.AdapterAdListener;
import com.ironsource.mediationsdk.adunit.adapter.listener.BannerAdListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType;
import com.ironsource.mediationsdk.model.NetworkSettings;
import com.ironsource.mediationsdk.utils.IronSourceConstants;
import com.startapp.sdk.ads.banner.BannerCreator;
import com.startapp.sdk.ads.banner.BannerFormat;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.BannerRequest;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.model.AdPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;


public class StartAppCustomBanner extends BaseBanner {

    public enum Mode {
        OFFERWALL,
        VIDEO,
        OVERLAY
    }

    public enum Size {
        SIZE72X72,
        SIZE100X100,
        SIZE150X150,
        SIZE340X340,
        SIZE1200X628
    }


    public static class Extras {
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

        @NonNull
        AdPreferences getAdPreferences() {
            return adPreferences;
        }

        private boolean is3DBanner;

        boolean is3DBanner() {
            return is3DBanner;
        }

        @Nullable
        private StartAppAd.AdMode adMode;

        @Nullable
        StartAppAd.AdMode getAdMode() {
            return adMode;
        }

        @Nullable
        private String appId;

        @Nullable
        public String getAppId() {
            return appId;
        }

        Extras(
                @NonNull Map<String, Object> customEventExtras,
                @NonNull String serverParameter
        ) {
            adPreferences = makeAdPreferences(customEventExtras, serverParameter, false, null);
        }

        @NonNull
        private AdPreferences makeAdPreferences(
                @NonNull Map<String, Object> customEventExtras,
                @NonNull String serverParameter,
                boolean isNative,
                @Nullable AdData nativeAdOptions
        ) {
            String adTag;
            boolean isVideoMuted;
            Double minCPM = null;
            Size nativeImageSize = null;
            Size nativeSecondaryImageSize = null;

            adTag = (String) customEventExtras.get(AD_TAG);
            isVideoMuted = Boolean.parseBoolean((String) customEventExtras.get(MUTE_VIDEO));
            is3DBanner = Boolean.parseBoolean((String) customEventExtras.get(IS_3D_BANNER));

            if (customEventExtras.containsKey(MIN_CPM)) {
                minCPM = (Double) customEventExtras.get(MIN_CPM);
            }

            if (customEventExtras.containsKey(INTERSTITIAL_MODE)) {
                Mode srcAdMode= null;
                try {
                    srcAdMode = (Mode) customEventExtras.get(INTERSTITIAL_MODE);
                } catch (ClassCastException e) {
                    Log.e(LOG_TAG, "can't get srcAdMode" + e);
                }

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
                try {
                    nativeImageSize = (Size) customEventExtras.get(NATIVE_IMAGE_SIZE);
                } catch (ClassCastException e) {
                    Log.e(LOG_TAG, "can't get nativeImageSize" + e);
                }
            }

            if (customEventExtras.containsKey(NATIVE_SECONDARY_IMAGE_SIZE)) {
                try {
                    nativeSecondaryImageSize = (Size) customEventExtras.get(NATIVE_SECONDARY_IMAGE_SIZE);
                } catch (ClassCastException e) {
                    Log.e(LOG_TAG, "can't get nativeSecondaryImageSize" + e);
                }
            }

            String jsonParameter = null;
            try {
                JSONObject serverParameterJSON = new JSONObject(serverParameter);
                jsonParameter = serverParameterJSON.getString(IronSourceConstants.EVENTS_CUSTOM_NETWORK_FIELD);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "serverParameter invalid" + e);
            }
            if (jsonParameter != null) {
                try {
                    JSONObject json = new JSONObject(jsonParameter);
                    Log.v(LOG_TAG, "Start.io server parameter:" + json);

                    if (json.has(AD_TAG)) {
                        adTag = json.getString(AD_TAG);
                    }

                    if (json.has(MUTE_VIDEO)) {
                        isVideoMuted = json.getBoolean(MUTE_VIDEO);
                    }

                    if (json.has(IS_3D_BANNER)) {
                        is3DBanner = json.getBoolean(IS_3D_BANNER);
                    }

                    if (json.has(MIN_CPM)) {
                        minCPM = json.getDouble(MIN_CPM);
                    }

                    if (json.has(NATIVE_IMAGE_SIZE)) {
                        String name = json.getString(NATIVE_IMAGE_SIZE);
                        try {
                            nativeImageSize = Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse imageSize parameter: " + name);
                        }
                    }

                    if (json.has(NATIVE_SECONDARY_IMAGE_SIZE)) {
                        String name = json.getString(NATIVE_SECONDARY_IMAGE_SIZE);
                        try {
                            nativeSecondaryImageSize = Size.valueOf(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(LOG_TAG, "Could not parse secondaryImageSize parameter: " + name);
                        }
                    }

                    if (json.has(INTERSTITIAL_MODE)) {
                        String mode = json.getString(INTERSTITIAL_MODE);
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
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Could not parse malformed JSON: " + jsonParameter);
                }
            }

            NativeAdPreferences nativeAdPrefs = null;
            AdPreferences prefs;
            if (isNative) {
                nativeAdPrefs = new NativeAdPreferences();
                prefs = nativeAdPrefs;
            } else {
                prefs = new AdPreferences();
            }

            prefs.setAdTag(adTag);
            prefs.setMinCpm(minCPM);

            if (isVideoMuted) {
                prefs.muteVideo();
            }

            if (isNative) {
                if (nativeImageSize != null) {
                    nativeAdPrefs.setPrimaryImageSize(nativeImageSize.ordinal());
                }

                if (nativeSecondaryImageSize != null) {
                    nativeAdPrefs.setSecondaryImageSize(nativeSecondaryImageSize.ordinal());
                }

                nativeAdPrefs.setAutoBitmapDownload(true);
            }

            return prefs;
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
    }


    static final String LOG_TAG = StartAppCustomBanner.class.getSimpleName();

    public StartAppCustomBanner(NetworkSettings networkSettings) {
        super(networkSettings);
    }

    public void destroyAd(AdData adData) {
    }

    @Override
    public void loadAd(AdData adData, Activity activity, ISBannerSize isBannerSize, AdapterAdListener listener) {

        final Context context = activity;
        final int adWidthDp, adHeightDp;

        adWidthDp = isBannerSize.getWidth();
        adHeightDp = isBannerSize.getHeight();

        if (DEBUG) {
            Log.v(LOG_TAG, "loadBannerAd: " + isBannerSize + " => " + adWidthDp + "x" + adHeightDp);
        }

        Extras extras = new Extras(adData.getAdUnitData(), adData.getServerData());

        new BannerRequest(context)
                .setAdFormat(isBannerSize.equals(ISBannerSize.RECTANGLE) ? BannerFormat.MREC : BannerFormat.BANNER)
                .setAdSize(adWidthDp, adHeightDp)
                .setAdPreferences(extras.getAdPreferences())
                .load(new BannerRequest.Callback() {
                    @Nullable
                    BannerAdListener bannerAdListener;

                    @Override
                    public void onFinished(@Nullable BannerCreator creator, @Nullable String error) {
                        if (creator != null) {
                            if (DEBUG) {
                                Log.v(LOG_TAG, "loadBannerAd: onFinished: success");
                            }

                            final View view = creator.create(context, new BannerListener() {
                                @Override
                                public void onReceiveAd(View view) {
                                    bannerAdListener.onAdLoadSuccess(
                                            view, new FrameLayout.LayoutParams(
                                                    view.getWidth(),
                                                    view.getHeight(),
                                                    Gravity.CENTER
                                            )
                                    );
                                }

                                @Override
                                public void onFailedToReceiveAd(View view) {
                                    // none
                                }

                                @Override
                                public void onImpression(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onImpression");
                                    }

                                    if (bannerAdListener != null) {
                                        bannerAdListener.onAdLoadSuccess(); // TODO is it impression?
                                    }
                                }

                                @Override
                                public void onClick(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onClick");
                                    }

                                    if (bannerAdListener != null) {
                                        bannerAdListener.onAdClicked();
                                    }
                                }
                            });

                        } else {
                            if (DEBUG) {
                                Log.w(LOG_TAG, "loadBannerAd: onFinished: error: " + error);
                            }

                            listener.onAdLoadFailed(messageToError(error), 0, error);
                        }
                    }
                });
    }

    @NonNull
    private static AdapterErrorType messageToError(@Nullable String message) {
        message = message != null ? message : "Internal error";
        boolean isNoFill = message.contains("204") || message.contains("Empty Response");
        boolean isExpired = message.contains("504"); // TODO check code
        AdapterErrorType result;
        if (isNoFill) {
            result = AdapterErrorType.ADAPTER_ERROR_TYPE_NO_FILL;
        } else if (isExpired) {
            result = AdapterErrorType.ADAPTER_ERROR_TYPE_AD_EXPIRED;
        } else {
            result = AdapterErrorType.ADAPTER_ERROR_TYPE_INTERNAL;
        }
        return result;
    }
}
