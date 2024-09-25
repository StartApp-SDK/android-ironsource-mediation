package com.ironsource.adapters.custom.startapp;

import static com.ironsource.adapters.custom.startapp.BuildConfig.DEBUG;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_INTERNAL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType.ADAPTER_ERROR_TYPE_NO_FILL;
import static com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrors.ADAPTER_ERROR_INTERNAL;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.adunit.adapter.BaseBanner;
import com.ironsource.mediationsdk.adunit.adapter.listener.BannerAdListener;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdData;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdapterErrorType;
import com.ironsource.mediationsdk.model.NetworkSettings;
import com.startapp.sdk.ads.banner.BannerCreator;
import com.startapp.sdk.ads.banner.BannerFormat;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.banner.BannerRequest;

import java.util.Locale;

public class StartAppCustomBanner extends BaseBanner<StartAppCustomAdapter> {
    private static final String LOG_TAG = StartAppCustomBanner.class.getSimpleName();

    public StartAppCustomBanner(NetworkSettings networkSettings) {
        super(networkSettings);
        if (DEBUG) {
            Log.v(LOG_TAG, "StartAppCustomBanner created: ");
        }
    }

    @Override
    public void destroyAd(@NonNull AdData adData) {
        // none
    }

    @Override
    public void loadAd(@NonNull AdData adData,
                       @NonNull Activity activity,
                       @NonNull ISBannerSize bannerSize,
                       @NonNull BannerAdListener bannerAdListener
    ) {
        final Context context = activity;
        final int adWidthDp, adHeightDp;

        if (bannerSize.getWidth() > 0 && bannerSize.getHeight() > 0) {
            adWidthDp = bannerSize.getWidth();
            adHeightDp = bannerSize.getHeight();
        } else {
            adWidthDp = ISBannerSize.BANNER.getWidth();
            adHeightDp = ISBannerSize.BANNER.getHeight();
        }

        if (DEBUG) {
            Log.d(LOG_TAG, "loadBannerAd: " + bannerSize + " => " + adWidthDp + "x" + adHeightDp +
                    " ServerData: " + adData.getServerData() +
                    "UnitData: " + adData.getAdUnitData());
        }

        StartAppMediationExtras extras = new StartAppMediationExtras(adData.getConfiguration(), adData.getServerData());

        new BannerRequest(context)
                .setAdFormat(bannerSize.equals(ISBannerSize.RECTANGLE) ? BannerFormat.MREC : BannerFormat.BANNER)
                .setAdSize(adWidthDp, adHeightDp)
                .setAdPreferences(extras.getAdPreferences())
                .load(new BannerRequest.Callback() {

                    @Override
                    public void onFinished(@Nullable BannerCreator creator, @Nullable String error) {
                        if (creator != null) {
                            if (DEBUG) {
                                Log.v(LOG_TAG, "onFinished: success");
                            }

                            final View view = creator.create(context, new BannerListener() {
                                @Override
                                public void onReceiveAd(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "adReceived width: " + view.getWidth() + " height: " + view.getHeight());
                                    }
                                }

                                @Override
                                public void onFailedToReceiveAd(View view) {
                                    // none
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "onFailedToReceiveAd " + view);
                                    }
                                    AdapterErrorType errorType = "NO FILL".equals(error) ? ADAPTER_ERROR_TYPE_NO_FILL : ADAPTER_ERROR_TYPE_INTERNAL;
                                    bannerAdListener.onAdLoadFailed(
                                            errorType,
                                            ADAPTER_ERROR_INTERNAL,
                                            ""
                                    );
                                }

                                @Override
                                public void onImpression(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onImpression");
                                    }

                                    bannerAdListener.onAdOpened();
                                }

                                @Override
                                public void onClick(View view) {
                                    if (DEBUG) {
                                        Log.v(LOG_TAG, "loadBannerAd: onClick");
                                    }

                                    bannerAdListener.onAdClicked();
                                }
                            });
                            bannerAdListener.onAdLoadSuccess(
                                    view, new FrameLayout.LayoutParams(
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            FrameLayout.LayoutParams.WRAP_CONTENT,
                                            Gravity.CENTER
                                    ));
                        } else {
                            if (DEBUG) {
                                Log.w(LOG_TAG, "loadBannerAd: onFinished: error: " + error);
                            }

                            boolean noFill = error != null
                                    && (error.contains("204") || error.toLowerCase(Locale.ENGLISH).contains("no fill"));

                            bannerAdListener.onAdLoadFailed(
                                    noFill ? ADAPTER_ERROR_TYPE_NO_FILL : ADAPTER_ERROR_TYPE_INTERNAL,
                                    ADAPTER_ERROR_INTERNAL,
                                    error);
                        }
                    }
                });
    }
}
