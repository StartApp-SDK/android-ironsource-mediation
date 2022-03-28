/**
 * Copyright 2022 Start.io Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#import <StartApp/StartApp.h>
#import <IronSource/ISAdapterErrors.h>
#import "ISStartAppCustomRewardedVideo.h"
#import "ISStartAppExtras.h"

@interface ISStartAppCustomRewardedVideo() <STADelegateProtocol>
@property (nonatomic, strong) STAStartAppAd *rewardedAd;
@property (nonatomic, weak) id<ISRewardedVideoAdDelegate> delegate;
@end

@implementation ISStartAppCustomRewardedVideo

- (void)loadAdWithAdData:(ISAdData *)adData
                delegate:(id<ISRewardedVideoAdDelegate>)delegate {
    self.delegate = delegate;
    if ([NSThread isMainThread]) {
        [self loadAdWithAdData:adData];
    }
    else {
        __weak typeof(self)weakSelf = self;
        dispatch_sync(dispatch_get_main_queue(), ^{
            [weakSelf loadAdWithAdData:adData];
        });
    }
}

- (void)loadAdWithAdData:(ISAdData *)adData {
    ISStartAppExtras *extras = [[ISStartAppExtras alloc] initWithParamsDictionary:adData.configuration];
    self.rewardedAd = [[STAStartAppAd alloc] init];
    [self.rewardedAd loadRewardedVideoAdWithDelegate:self withAdPreferences:extras.prefs];
}

- (void)showAdWithViewController:(UIViewController *)viewController
                          adData:(ISAdData *)adData
                        delegate:(id<ISRewardedVideoAdDelegate>)delegate {
    self.delegate = delegate;
    [self.rewardedAd showAd];
}

- (BOOL)isAdAvailableWithAdData:(ISAdData*)adData {
    return self.rewardedAd.isReady;
}

#pragma mark STADelegateProtocol methods
- (void)failedLoadAd:(STAAbstractAd *)ad withError:(NSError *)error {
    if ([self.delegate respondsToSelector:@selector(adDidFailToLoadWithErrorType:errorCode:errorMessage:)]) {
        [self.delegate adDidFailToLoadWithErrorType:(error.code == STAErrorNoContent) ? ISAdapterErrorTypeNoFill : ISAdapterErrorTypeInternal
                                          errorCode:ISAdapterErrorInternal
                                       errorMessage:error.localizedDescription];
    }
}

-(void)failedShowAd:(STAAbstractAd *)ad withError:(NSError *)error {
    if ([self.delegate respondsToSelector:@selector(adDidFailToShowWithErrorCode:errorMessage:)]) {
        [self.delegate adDidFailToShowWithErrorCode:(error.code == STAErrorAdExpired) ? ISAdapterErrorAdExpired : ISAdapterErrorInternal
                                       errorMessage:error.localizedDescription];
    }
}

- (void)didLoadAd:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidLoad)]) {
        [self.delegate adDidLoad];
    }
}

- (void)didShowAd:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidStart)]) {
        [self.delegate adDidStart];
    }
}

- (void)didSendImpression:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidOpen)]) {
        [self.delegate adDidOpen];
    }
    if ([self.delegate respondsToSelector:@selector(adDidBecomeVisible)]) {
        [self.delegate adDidBecomeVisible];
    }
    if ([self.delegate respondsToSelector:@selector(adDidShowSucceed)]) {
        [self.delegate adDidShowSucceed];
    }
}

- (void)didCompleteVideo:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidEnd)]) {
        [self.delegate adDidEnd];
    }
    if ([self.delegate respondsToSelector:@selector(adRewarded)]) {
        [self.delegate adRewarded];
    }
}

- (void)didClickAd:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidClick)]) {
        [self.delegate adDidClick];
    }
}

- (void)didCloseAd:(STAAbstractAd *)ad {
    if ([self.delegate respondsToSelector:@selector(adDidClose)]) {
        [self.delegate adDidClose];
    }
}

@end
