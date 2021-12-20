//
//  ISStartAppCustomInterstitial.m
//  IronSourceAdapter
//
//  Created by Start.io on 22.11.2021.
//

#import "ISStartAppCustomInterstitial.h"
#import "IronSource/ISAdapterErrors.h"
#import <StartApp/StartApp.h>

@interface ISStartAppCustomInterstitial() <STADelegateProtocol>
@property (nonatomic, strong) STAStartAppAd *interstitialAd;
@property (nonatomic, weak) id<ISAdapterAdDelegate> delegate;
@end

@implementation ISStartAppCustomInterstitial

- (void)loadAdWithAdData:(ISAdData *)adData delegate:(id<ISAdapterAdDelegate>)delegate {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.delegate = delegate;
        self.interstitialAd = [[STAStartAppAd alloc] init];
        [self.interstitialAd loadAdWithDelegate:self withAdPreferences:nil];
    });
}

- (BOOL)isAdAvailableWithAdData:(ISAdData *)adData {
    return self.interstitialAd.isReady;
}

- (void)showAdWithViewController:(UIViewController *)viewController adData:(ISAdData *)adData delegate:(id<ISAdapterAdDelegate>)delegate {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.interstitialAd showAd];
    });
}

#pragma mark STADelegateProtocol methods
- (void)failedLoadAd:(STAAbstractAd *)ad withError:(NSError *)error {
    [self.delegate adDidFailToLoadWithErrorType:(error.code == STAErrorNoContent) ? ISAdapterErrorTypeNoFill : ISAdapterErrorTypeInternal
                                      errorCode:ISAdapterErrorInternal
                                   errorMessage:error.localizedDescription];
}

-(void)failedShowAd:(STAAbstractAd *)ad withError:(NSError *)error {
    [self.delegate adDidFailToShowWithErrorCode:(error.code == STAErrorAdExpired) ? ISAdapterErrorAdExpired : ISAdapterErrorInternal
                                   errorMessage:error.localizedDescription];
}

- (void)didLoadAd:(STAAbstractAd *)ad {
    [self.delegate adDidLoad];
}

- (void)didShowAd:(STAAbstractAd *)ad {
    [self.delegate adDidShowSucceed];
}

- (void)didSendImpression:(STAAbstractAd *)ad {
    [self.delegate adDidOpen];
}

- (void)didClickAd:(STAAbstractAd *)ad {
    [self.delegate adDidClick];
}

- (void)didCloseAd:(STAAbstractAd *)ad {
    [self.delegate adDidClose];
}


@end
