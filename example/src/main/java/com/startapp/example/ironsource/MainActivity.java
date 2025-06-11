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

import com.startapp.mediation.ironsource.example.R;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.mediation.LevelPlay;
import com.unity3d.mediation.LevelPlayAdError;
import com.unity3d.mediation.LevelPlayAdInfo;
import com.unity3d.mediation.LevelPlayAdSize;
import com.unity3d.mediation.LevelPlayConfiguration;
import com.unity3d.mediation.LevelPlayInitError;
import com.unity3d.mediation.LevelPlayInitListener;
import com.unity3d.mediation.LevelPlayInitRequest;
import com.unity3d.mediation.banner.LevelPlayBannerAdView;
import com.unity3d.mediation.banner.LevelPlayBannerAdViewListener;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAd;
import com.unity3d.mediation.interstitial.LevelPlayInterstitialAdListener;
import com.unity3d.mediation.rewarded.LevelPlayReward;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAd;
import com.unity3d.mediation.rewarded.LevelPlayRewardedAdListener;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    enum AdState {
        IDLE,
        LOADING,
        VISIBLE,
    }

    private static final MutableLiveData<Boolean> initialized = new MutableLiveData<>(null);

    private final MutableLiveData<Pair<LevelPlayInterstitialAd, AdState>> interstitialLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<LevelPlayRewardedAd, AdState>> rewardedLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<LevelPlayBannerAdView, AdState>> bannerLiveData = new MutableLiveData<>();
    private final MutableLiveData<Pair<LevelPlayBannerAdView, AdState>> mrecLiveData = new MutableLiveData<>();

    private ViewGroup bannerContainer;
    private ViewGroup mrecContainer;

    @Override
    protected void onCreate(@Nullable Bundle state) {
        super.onCreate(state);

        setTitle("LevelPlay " + LevelPlay.getSdkVersion() + " - Start.io " + StartAppSDK.getVersion());

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

                LevelPlay.init(getApplicationContext(), new LevelPlayInitRequest.Builder(getString(R.string.app_key))
                        .withLegacyAdFormats(Arrays.asList(
                                LevelPlay.AdFormat.INTERSTITIAL,
                                LevelPlay.AdFormat.REWARDED,
                                LevelPlay.AdFormat.BANNER
                        ))
                        .build(), new LevelPlayInitListener() {
                    @Override
                    public void onInitFailed(@NonNull LevelPlayInitError error) {
                        Log.e(LOG_TAG, "onCreate: onInitFailed: " + error);

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
        loadAdView(LevelPlayAdSize.BANNER, getString(R.string.banner_ad_unit_id), layoutParams, bannerLiveData);
    }

    private void loadMrec(@NonNull View view) {
        int widthPx = getResources().getDimensionPixelSize(R.dimen.mrec_width);
        int heightPx = getResources().getDimensionPixelSize(R.dimen.mrec_height);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(widthPx, heightPx, Gravity.CENTER);
        loadAdView(LevelPlayAdSize.MEDIUM_RECTANGLE, getString(R.string.mrec_ad_unit_id), layoutParams, mrecLiveData);
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

    private void loadAdView(@NonNull LevelPlayAdSize size, @NonNull String adUnitId, @NonNull ViewGroup.LayoutParams layoutParams, @NonNull MutableLiveData<Pair<LevelPlayBannerAdView, AdState>> liveData) {
        LevelPlayBannerAdView.Config adConfig = new LevelPlayBannerAdView.Config.Builder()
                .setAdSize(size)
                .build();

        LevelPlayBannerAdView banner = new LevelPlayBannerAdView(this, adUnitId, adConfig);
        banner.setBannerListener(new LevelPlayBannerAdViewListener() {
            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdLoaded: " + adInfo);

                runOnUiThread(() -> {
                    Pair<LevelPlayBannerAdView, AdState> pair = liveData.getValue();

                    if (pair == null || pair.second == AdState.LOADING) {
                        liveData.setValue(new Pair<>(banner, AdState.IDLE));
                    }
                });
            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                liveData.postValue(null);
            }
        });

        liveData.setValue(new Pair<>(null, AdState.LOADING));

        banner.setLayoutParams(layoutParams);
        banner.loadAd();
    }

    private void showAdView(@NonNull MutableLiveData<Pair<LevelPlayBannerAdView, AdState>> liveData, @NonNull ViewGroup container) {
        Pair<LevelPlayBannerAdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            container.removeAllViews();
            container.addView(pair.first);
            liveData.setValue(new Pair<>(pair.first, AdState.VISIBLE));
        } else {
            Toast.makeText(this, "AdView is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideAdView(@NonNull MutableLiveData<Pair<LevelPlayBannerAdView, AdState>> liveData, @NonNull ViewGroup container) {
        container.removeAllViews();

        Pair<LevelPlayBannerAdView, AdState> pair = liveData.getValue();
        if (pair != null && pair.first != null) {
            pair.first.destroy();
        }

        liveData.setValue(new Pair<>(null, AdState.IDLE));
    }

    // endregion

    // region Interstitial

    private void loadInterstitial(@NonNull View view) {
        loadInterstitial(getString(R.string.interstitial_ad_unit_id));
    }

    private void loadInterstitial(@NonNull String adUnitId) {
        interstitialLiveData.setValue(new Pair<>(null, AdState.LOADING));

        LevelPlayInterstitialAd localAd = new LevelPlayInterstitialAd(adUnitId);
        localAd.setListener(new LevelPlayInterstitialAdListener() {
            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdLoaded: " + adInfo);

                interstitialLiveData.setValue(new Pair<>(localAd, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                interstitialLiveData.setValue(null);
            }

            // TODO unfortunately, this method is never called by LevelPlay
            //      thus we'll call interstitialLiveData.setValue(null) right after .showAd()
            @Override
            public void onAdDisplayed(@NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdDisplayed");

                interstitialLiveData.setValue(null);
            }
        });

        localAd.loadAd();
    }

    public void showInterstitial(@NonNull View view) {
        Pair<LevelPlayInterstitialAd, AdState> pair = interstitialLiveData.getValue();
        if (pair != null && pair.first != null) {
            pair.first.showAd(this);

            // TODO see a comment for onAdDisplayed()
            interstitialLiveData.setValue(null);
        } else {
            Toast.makeText(this, "Interstitial is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion

    // region Rewarded

    private void loadRewarded(@NonNull View view) {
        loadRewarded(getString(R.string.rewarded_ad_unit_id));
    }

    private void loadRewarded(@NonNull String adUnitId) {
        rewardedLiveData.setValue(new Pair<>(null, AdState.LOADING));

        LevelPlayRewardedAd localAd = new LevelPlayRewardedAd(adUnitId);
        localAd.setListener(new LevelPlayRewardedAdListener() {
            @Override
            public void onAdLoaded(@NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdLoaded: " + adInfo);

                rewardedLiveData.setValue(new Pair<>(localAd, AdState.IDLE));
            }

            @Override
            public void onAdLoadFailed(@NonNull LevelPlayAdError error) {
                Log.v(LOG_TAG, "onAdLoadFailed: " + error);

                rewardedLiveData.setValue(null);
            }

            // TODO unfortunately, this method is never called by LevelPlay
            //      thus we'll call rewardedLiveData.setValue(null) right after .showAd()
            @Override
            public void onAdDisplayed(@NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdDisplayed");

                rewardedLiveData.setValue(null);
            }

            @Override
            public void onAdRewarded(@NonNull LevelPlayReward reward, @NonNull LevelPlayAdInfo adInfo) {
                Log.v(LOG_TAG, "onAdRewarded");

                runOnUiThread(() -> Toast.makeText(getApplicationContext(), "User gained a reward", Toast.LENGTH_SHORT).show());
            }
        });
        localAd.loadAd();
    }

    public void showRewarded(@NonNull View view) {
        Pair<LevelPlayRewardedAd, AdState> pair = rewardedLiveData.getValue();
        if (pair != null && pair.first != null) {
            pair.first.showAd(this);

            // TODO see a comment for onAdDisplayed()
            rewardedLiveData.setValue(null);
        } else {
            Toast.makeText(this, "Rewarded is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    // endregion
}
