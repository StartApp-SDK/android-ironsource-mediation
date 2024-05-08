package com.ironsource.ironsourcesdkdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ironsource.adapters.supersonicads.SupersonicConfig;
import com.ironsource.mediationsdk.ISBannerSize;
import com.ironsource.mediationsdk.IronSource;
import com.ironsource.mediationsdk.IronSourceBannerLayout;
import com.ironsource.mediationsdk.adunit.adapter.utility.AdInfo;
import com.ironsource.mediationsdk.impressionData.ImpressionData;
import com.ironsource.mediationsdk.impressionData.ImpressionDataListener;
import com.ironsource.mediationsdk.integration.IntegrationHelper;
import com.ironsource.mediationsdk.logger.IronSourceError;
import com.ironsource.mediationsdk.model.Placement;
import com.ironsource.mediationsdk.sdk.LevelPlayBannerListener;
import com.ironsource.mediationsdk.sdk.LevelPlayInterstitialListener;
import com.ironsource.mediationsdk.sdk.LevelPlayRewardedVideoListener;
import com.ironsource.mediationsdk.utils.IronSourceUtils;

public class DemoActivity extends Activity implements LevelPlayRewardedVideoListener, LevelPlayInterstitialListener, ImpressionDataListener {

    private final String TAG = "DemoActivity";
    // TODO find your app key in IronSource portal
    private final String APP_KEY = "1204ec035";
    private final String FALLBACK_USER_ID = "userId";
    private Button mVideoButton;
    private Button mOfferwallButton;
    private Button mInterstitialLoadButton;
    private Button mInterstitialShowButton;

    private Placement mPlacement;

    private FrameLayout mBannerParentLayout;
    private IronSourceBannerLayout mIronSourceBannerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        //The integrationHelper is used to validate the integration. Remove the integrationHelper before going live!
        IntegrationHelper.validateIntegration(this);
        initUIElements();
        startIronSourceInitTask();
        IronSource.getAdvertiserId(this);
        //Network Connectivity Status
        IronSource.shouldTrackNetworkState(this, true);

    }
    private void startIronSourceInitTask(){
        String advertisingId = IronSource.getAdvertiserId(DemoActivity.this);
        // we're using an advertisingId as the 'userId'
        initIronSource(APP_KEY, advertisingId);

    }

    private void initIronSource(String appKey, String userId) {
        // Be sure to set a listener to each product that is being initiated
        // set the IronSource rewarded video listener
        IronSource.setLevelPlayRewardedVideoListener(this);
        // set client side callbacks for the offerwall
        SupersonicConfig.getConfigObj().setClientSideCallbacks(true);
        // set the interstitial listener
        IronSource.setLevelPlayInterstitialListener(this);
        // add the Impression Data listener
        IronSource.addImpressionDataListener(this);

        // set the IronSource user id
        IronSource.setUserId(userId);
        // init the IronSource SDK
        IronSource.init(this, appKey, IronSource.AD_UNIT.INTERSTITIAL, IronSource.AD_UNIT.REWARDED_VIDEO, IronSource.AD_UNIT.BANNER);

        updateButtonsState();

        // In order to work with IronSourceBanners you need to add Providers who support banner ad unit and uncomment next line
         createAndloadBanner();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // call the IronSource onResume method
        IronSource.onResume(this);
        updateButtonsState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // call the IronSource onPause method
        IronSource.onPause(this);
        updateButtonsState();
    }

    /**
     * Handle the button state according to the status of the IronSource producs
     */
    private void updateButtonsState() {
            handleVideoButtonState(IronSource.isRewardedVideoAvailable());
            handleOfferwallButtonState(false);
            handleLoadInterstitialButtonState(true);
            handleInterstitialShowButtonState(false);

    }



    /**
     * initialize the UI elements of the activity
     */
    @SuppressLint("SetTextI18n")
    private void initUIElements() {
        mVideoButton = findViewById(R.id.rv_button);
        mVideoButton.setOnClickListener(view -> {
                // check if video is available
                if (IronSource.isRewardedVideoAvailable())
                    //show rewarded video
                    IronSource.showRewardedVideo();
        });

        mOfferwallButton = findViewById(R.id.ow_button);
        mOfferwallButton.setOnClickListener(view -> {
        });

        mInterstitialLoadButton = findViewById(R.id.is_button_1);
        mInterstitialLoadButton.setOnClickListener(view -> IronSource.loadInterstitial());


        mInterstitialShowButton = findViewById(R.id.is_button_2);
        mInterstitialShowButton.setOnClickListener(view -> {
                // check if interstitial is available
                if (IronSource.isInterstitialReady()) {
                    //show the interstitial
                    IronSource.showInterstitial();
            }
        });

        TextView versionTV = findViewById(R.id.version_txt);
        versionTV.setText(getResources().getString(R.string.version) + " " + IronSourceUtils.getSDKVersion());

        mBannerParentLayout = findViewById(R.id.banner_footer);
    }


    /**
     * Creates and loads IronSource Banner
     *
     */
    private void createAndloadBanner() {
        // choose banner size
        ISBannerSize size = ISBannerSize.BANNER;

        // instantiate IronSourceBanner object, using the IronSource.createBanner API
        mIronSourceBannerLayout = IronSource.createBanner(this, size);

        // add IronSourceBanner to your container
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mBannerParentLayout.addView(mIronSourceBannerLayout, 0, layoutParams);

        if (mIronSourceBannerLayout != null) {
            // set the banner listener
            mIronSourceBannerLayout.setLevelPlayBannerListener(new LevelPlayBannerListener() {
                @Override
                public void onAdLoaded(AdInfo adInfo) {
                    Log.d(TAG, "onAdLoaded");
                    // since banner container was "gone" by default, we need to make it visible as soon as the banner is ready
                    mBannerParentLayout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdLoadFailed(IronSourceError ironSourceError) {
                    Log.d(TAG, "onAdLoadFailed" + " " + ironSourceError.getErrorMessage());
                }

                @Override
                public void onAdClicked(AdInfo adInfo) {
                    Log.d(TAG, "onAdLoaded");
                }

                @Override
                public void onAdLeftApplication(AdInfo adInfo) {
                    Log.d(TAG, "onAdLeftApplication");
                }

                @Override
                public void onAdScreenPresented(AdInfo adInfo) {
                    Log.d(TAG, "onAdScreenPresented");
                }

                @Override
                public void onAdScreenDismissed(AdInfo adInfo) {
                    Log.d(TAG, "onAdScreenDismissed");
                }
            });

            // load ad into the created banner
            IronSource.loadBanner(mIronSourceBannerLayout);
        } else {
            Toast.makeText(DemoActivity.this, "IronSource.createBanner returned null", Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Destroys IronSource Banner and removes it from the container
     *
     */
    private void destroyAndDetachBanner() {
        IronSource.destroyBanner(mIronSourceBannerLayout);
        if (mBannerParentLayout != null) {
            mBannerParentLayout.removeView(mIronSourceBannerLayout);
        }
    }

    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the video is available
     */
    public void handleVideoButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show) + " " + getResources().getString(R.string.rv);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.rv);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mVideoButton.setTextColor(color);
                mVideoButton.setText(text);
                mVideoButton.setEnabled(available);

            }
        });
    }

    /**
     * Set the Rewareded Video button state according to the product's state
     *
     * @param available if the offerwall is available
     */
    public void handleOfferwallButtonState(final boolean available) {
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.show) + " " + getResources().getString(R.string.ow);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.ow);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mOfferwallButton.setTextColor(color);
                mOfferwallButton.setText(text);
                mOfferwallButton.setEnabled(available);

            }
        });

    }

    /**
     * Set the Interstitial button state according to the product's state
     *
     * @param available if the interstitial is available
     */
    public void handleLoadInterstitialButtonState(final boolean available) {
        Log.d(TAG, "handleInterstitialButtonState | available: " + available);
        final String text;
        final int color;
        if (available) {
            color = Color.BLUE;
            text = getResources().getString(R.string.load) + " " + getResources().getString(R.string.is);
        } else {
            color = Color.BLACK;
            text = getResources().getString(R.string.initializing) + " " + getResources().getString(R.string.is);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInterstitialLoadButton.setTextColor(color);
                mInterstitialLoadButton.setText(text);
                mInterstitialLoadButton.setEnabled(available);
            }
        });

    }

    /**
     * Set the Show Interstitial button state according to the product's state
     *
     * @param available if the interstitial is available
     */
    public void handleInterstitialShowButtonState(final boolean available) {
        final int color;
        if (available) {
            color = Color.BLUE;
        } else {
            color = Color.BLACK;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mInterstitialShowButton.setTextColor(color);
                mInterstitialShowButton.setEnabled(available);
            }
        });
    }

    // --------- IronSource Interstitial Listener ---------

    @Override
    public void onAdReady(AdInfo adInfo) {
        // called when the interstitial is ready
        Log.d(TAG, "onAdReady");
        handleInterstitialShowButtonState(true);
    }

    @Override
    public void onAdLoadFailed(IronSourceError ironSourceError) {
        // called when the interstitial has failed to show
        // you can get the error data by accessing the IronSourceError object
        // IronSourceError.getErrorCode();
        // IronSourceError.getErrorMessage();
        Log.d(TAG, "onInterstitialAdShowFailed" + " " + ironSourceError);
        handleInterstitialShowButtonState(false);
    }

    @Override
    public void onAdOpened(AdInfo adInfo) {
        Log.d(TAG, "onAdOpened");
    }

    @Override
    public void onAdClicked(AdInfo adInfo) {
        Log.d(TAG, "onAdClicked");
    }

    @Override
    public void onAdShowSucceeded(AdInfo adInfo) {
        Log.d(TAG, "onAdShowSucceeded");
    }

    // --------- Impression Data Listener ---------

    @Override
    public void onImpressionSuccess(ImpressionData impressionData) {
        // The onImpressionSuccess will be reported when the rewarded video and interstitial ad is opened.
        // For banners, the impression is reported on load success.
        if (impressionData != null) {
            Log.d(TAG, "onImpressionSuccess " + impressionData);
        }
        }

    public void showRewardDialog(Placement placement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(DemoActivity.this);
        builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setTitle(getResources().getString(R.string.rewarded_dialog_header));
        builder.setMessage(getResources().getString(R.string.rewarded_dialog_message) + " " + placement.getRewardAmount() + " " + placement.getRewardName());
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // --------- IronSource Rewarded Video Listener ---------

    @Override
    public void onAdAvailable(AdInfo adInfo) {
        // called when the video availability has changed
        Log.d(TAG, "onAdAvailable");
        handleVideoButtonState(true);
    }

    @Override
    public void onAdUnavailable() {
        // called when the video availability has changed
        Log.d(TAG, "onAdUnavailable");
        handleVideoButtonState(false);
    }

    @Override
    public void onAdShowFailed(IronSourceError ironSourceError, AdInfo adInfo) {
        // called when the video has failed to show
        // you can get the error data by accessing the IronSourceError object
        // IronSourceError.getErrorCode();
        // IronSourceError.getErrorMessage();
        Log.d(TAG, "onAdShowFailed" + " " + ironSourceError);
    }

    @Override
    public void onAdClicked(Placement placement, AdInfo adInfo) {
        Log.d(TAG, "onAdClicked");
    }

    @Override
    public void onAdRewarded(Placement placement, AdInfo adInfo) {
        // called when the video has been rewarded and a reward can be given to the user
        Log.d(TAG, "onAdRewarded" + " " + placement);
        mPlacement = placement;
    }

    @Override
    public void onAdClosed(AdInfo adInfo) {
        // called when the video is closed
        Log.d(TAG, "onAdClosed");
        // here we show a dialog to the user if he was rewarded
        if (mPlacement != null) {
            // if the user was rewarded
            showRewardDialog(mPlacement);
            mPlacement = null;
        }
    }
}
