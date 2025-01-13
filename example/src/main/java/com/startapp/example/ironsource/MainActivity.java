package com.startapp.example.ironsource;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoManualListener;
import com.ironsource.mediationsdk.utils.IronSourceUtils;
import com.startapp.mediation.ironsource.example.R;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    enum AdState {
        IDLE,
        LOADING,
        VISIBLE,
    }

    private static final MutableLiveData<Boolean> initialized = new MutableLiveData<>(null);

    private final MutableLiveData<Pair<AdInfo, AdState>> interstitialLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<AdInfo, AdState>> rewardedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<IronSourceBannerLayout, AdState>> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<IronSourceBannerLayout, AdState>> mrecLiveData = new MutableLiveData<>();

    private ViewGroup bannerContainer;
    private ViewGroup mrecContainer;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setTitle("IronSource " + IronSourceUtils.getSDKVersion() + " - Start.io " + StartAppSDK.getVersion());

        setContentView(R.layout.activity_main);

        // region UI initialization

        View loadInterstitial = findViewById(R.id.load_interstitial);
        View showInterstitial = findViewById(R.id.show_interstitial);
        View loadRewarded = findViewById(R.id.load_rewarded);
        View showRewarded = findViewById(R.id.show_rewarded);
        View loadBanner = findViewById(R.id.load_banner);
        View showBanner = findViewById(R.id.show_banner);
        View hideBanner = findViewById(R.id.hide_banner);
        View loadMrec = findViewById(R.id.load_mrec);
        View showMrec = findViewById(R.id.show_mrec);
        View hideMrec = findViewById(R.id.hide_mrec);
        bannerContainer = findViewById(R.id.banner_container);
        mrecContainer = findViewById(R.id.mrec_container);

        loadInterstitial.setOnClickListener(this::loadInterstitial);
        showInterstitial.setOnClickListener(this::showInterstitial);
        loadRewarded.setOnClickListener(this::loadRewarded);
        showRewarded.setOnClickListener(this::showRewarded);
        loadBanner.setOnClickListener(this::loadBanner);
        showBanner.setOnClickListener(this::showBanner);
        hideBanner.setOnClickListener(this::hideBanner);
        loadMrec.setOnClickListener(this::loadMrec);
        showMrec.setOnClickListener(this::showMrec);
        hideMrec.setOnClickListener(this::hideMrec);

        interstitialLiveData.observe(this, pair -> {
            loadInterstitial.setEnabled(isLoadButtonEnabled(pair));
            showInterstitial.setEnabled(isShowButtonEnabled(pair));
        });

        rewardedLiveData.observe(this, pair -> {
            loadRewarded.setEnabled(isLoadButtonEnabled(pair));
            showRewarded.setEnabled(isShowButtonEnabled(pair));
        });

        bannerLiveData.observe(this, pair -> {
            loadBanner.setEnabled(isLoadButtonEnabled(pair));
            showBanner.setEnabled(isShowButtonEnabled(pair));
            hideBanner.setEnabled(isHideButtonVisible(pair));
            bannerContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        mrecLiveData.observe(this, pair -> {
            loadMrec.setEnabled(isLoadButtonEnabled(pair));
            showMrec.setEnabled(isShowButtonEnabled(pair));
            hideMrec.setEnabled(isHideButtonVisible(pair));
            mrecContainer.setVisibility(isHideButtonVisible(pair) ? View.VISIBLE : View.GONE);
        });

        // endregion

        initialized.observe(this, value -> {
            if (value == null) {
                initialized.setValue(false);

                initListeners();

                LevelPlay.init(getApplicationContext(), new LevelPlayInitRequest.Builder(getString(R.string.app_key))
                        .withLegacyAdFormats(Arrays.asList(
                                LevelPlay.AdFormat.INTERSTITIAL,
                                LevelPlay.AdFormat.REWARDED,
                                LevelPlay.AdFormat.BANNER
                        ))
                        .build(), new LevelPlayInitListener() {
                    @Override
                    public void onInitFailed(@NonNull LevelPlayInitError error) {
                        Toast.makeText(getApplicationContext(), String.valueOf(error), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onInitSuccess(@NonNull LevelPlayConfiguration configuration) {
                        initialized.postValue(true);

                        // TODO remove this line in production
                        StartAppSDK.setTestAdsEnabled(true);
                    }
                });
            } else if (value) {
                interstitialLiveData.setValue(null);
                rewardedLiveData.setValue(null);
                bannerLiveData.setValue(null);
                mrecLiveData.setValue(null);
            }
        });
    }

    private void initListeners() {
        initInterstitialListener();
        initRewardedListener();
    }

    private static boolean isInitialized() {
        return Boolean.TRUE.equals(initialized.getValue());
    }

    private static <T> boolean isLoadButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return (pair == null || pair.first == null && pair.second != AdState.LOADING) && isInitialized();
    }

    private static <T> boolean isShowButtonEnabled(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.first != null && pair.second != AdState.VISIBLE;
    }

    private static <T> boolean isHideButtonVisible(@Nullable Pair<T, AdState> pair) {
        return pair != null && pair.second == AdState.VISIBLE;
    }

    // region Banner & Mrec

    private void loadBanner(@NonNull View view) {
        int heightPx = getResources().getDimensionPixelSize(R.dimen.banner_height);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, heightPx, Gravity.CENTER);
        loadAdView(ISBannerSize.BANNER, layoutParams, bannerLiveData);
    }

    private void loadMrec(@NonNull View view) {
        int widthPx = getResources().getDimensionPixelSize(R.dimen.mrec_width);
        int heightPx = getResources().getDimensionPixelSize(R.dimen.mrec_height);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(widthPx, heightPx, Gravity.CENTER);
        loadAdView(ISBannerSize.RECTANGLE, layoutParams, mrecLiveData);
    }

    private void showBanner(@NonNull View view) {
        showAdView(bannerLiveData, bannerContainer);
    }

    private void showMrec(@NonNull View view) {
        showAdView(mrecLiveData, mrecContainer);
    }

    private void hideBanner(@NonNull View view) {
        hideAdView(bannerLiveData, bannerContainer);
    }

    private void hideMrec(@NonNull View view) {
        hideAdView(mrecLiveData, mrecContainer);
    }

    private void loadAdView(@NonNull ISBannerSize size, @NonNull ViewGroup.LayoutParams layoutParams, @NonNull MutableLiveData<Pair<IronSourceBannerLayout, AdState>> liveData) {
        IronSourceBannerLayout banner = IronSource.createBanner(this, size);
        banner.setLevelPlayBannerListener(new LevelPlayBannerListener() {
            @Override
            public void onAdLoaded(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdLoaded: " + adInfo);

                runOnUiThread(() -> {
                    Pair<IronSourceBannerLayout, AdState> pair = liveData.getValue();

                    if (pair == null || pair.second == AdState.LOADING) {
                        liveData.setValue(new Pair<>(banner, AdState.IDLE));
                    }
                });
            }

            @Override
            public void onAdLoadFailed(IronSourceError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                liveData.postValue(null);
            }

            @Override
            public void onAdClicked(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdClicked: " + adInfo);
            }

            @Override
            public void onAdScreenPresented(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdScreenPresented: " + adInfo);
            }

            @Override
            public void onAdScreenDismissed(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdScreenDismissed: " + adInfo);
            }

            @Override
            public void onAdLeftApplication(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdLeftApplication: " + adInfo);
            }
        });

        liveData.setValue(new Pair<>(null, AdState.LOADING));

        banner.setLayoutParams(layoutParams);
        IronSource.loadBanner(banner);
    }

    private void showAdView(@NonNull MutableLiveData<Pair<IronSourceBannerLayout, AdState>> liveData, @NonNull ViewGroup container) {
        Pair<IronSourceBannerLayout, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            container.removeAllViews();
            container.addView(pair.first);
            liveData.setValue(new Pair<>(pair.first, AdState.VISIBLE));
        } else {
            Toast.makeText(this, "AdView is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAdView(@NonNull MutableLiveData<Pair<IronSourceBannerLayout, AdState>> liveData, @NonNull ViewGroup container) {
        container.removeAllViews();

        Pair<IronSourceBannerLayout, AdState> pair = liveData.getValue();
        if (pair != null) {
            IronSource.destroyBanner(pair.first);
        }

        liveData.setValue(new Pair<>(null, AdState.IDLE));
    }

    // endregion

    // region Interstitial

    private void initInterstitialListener() {
        IronSource.setLevelPlayInterstitialListener(new LevelPlayInterstitialListener() {
            @Override
            public void onAdReady(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdReady: " + adInfo);

                interstitialLiveData.setValue(new Pair<>(adInfo, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(IronSourceError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdOpened(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdOpened: " + adInfo);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdClosed(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdClosed: " + adInfo);
            }

            @Override
            public void onAdShowFailed(IronSourceError error, AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdShowFailed: " + adInfo + ", " + error);

                interstitialLiveData.setValue(null);
            }

            @Override
            public void onAdClicked(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdClicked: " + adInfo);
            }

            @Override
            public void onAdShowSucceeded(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdShowSucceeded: " + adInfo);
            }
        });
    }

    private void loadInterstitial(@NonNull View view) {
        interstitialLiveData.setValue(new Pair<>(null, AdState.LOADING));
        IronSource.loadInterstitial();
    }

    public void showInterstitial(@NonNull View view) {
        Pair<AdInfo, AdState> pair = interstitialLiveData.getValue();
        if (pair != null && pair.first != null) {
            IronSource.showInterstitial(this);
        } else {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion

    // region Rewarded

    private void initRewardedListener() {
        IronSource.setLevelPlayRewardedVideoManualListener(new LevelPlayRewardedVideoManualListener() {
            @Override
            public void onAdReady(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdReady: " + adInfo);

                rewardedLiveData.setValue(new Pair<>(adInfo, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(IronSourceError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdOpened(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdOpened: " + adInfo);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdShowFailed(IronSourceError error, AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdShowFailed: " + error + ", " + adInfo);

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdClicked(Placement placement, AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdClicked: " + placement + ", " + adInfo);
            }

            @Override
            public void onAdRewarded(Placement placement, AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdRewarded: " + placement + ", " + adInfo);

                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "User gained a reward", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onAdClosed(AdInfo adInfo) {
                Log.v(LOG_TAG, "onAdClosed: " + adInfo);
            }
        });
    }

    private void loadRewarded(@NonNull View view) {
        rewardedLiveData.setValue(new Pair<>(null, AdState.LOADING));
        IronSource.loadRewardedVideo();
    }

    public void showRewarded(@NonNull View view) {
        Pair<AdInfo, AdState> pair = rewardedLiveData.getValue();
        if (pair != null && pair.first != null) {
            IronSource.showRewardedVideo(this);
        } else {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion
}
