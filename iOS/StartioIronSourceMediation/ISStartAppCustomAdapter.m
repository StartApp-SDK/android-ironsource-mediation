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
#import "ISStartAppCustomAdapter.h"
#import "ISStartAppConstants.h"

@implementation ISStartAppCustomAdapter

#pragma mark ISAdapterBaseProtocol methods
- (void)init:(ISAdData *)adData delegate:(id<ISNetworkInitializationDelegate>)delegate {
    NSString *appID = adData.configuration[ISStartAppKeyAppID];
    if (appID.length == 0) {
        if ([delegate respondsToSelector:@selector(onInitDidFailWithErrorCode:errorMessage:)]) {
            [delegate onInitDidFailWithErrorCode:ISAdapterErrorMissingParams errorMessage:@"Missing StartAppSDK AppID parameter"];
        }
    }
    else {
        if ([NSThread isMainThread]) {
            [self setupStartioSDKWithAppID:appID adData:adData];
        }
        else {
            __weak typeof(self)weakSelf = self;
            dispatch_sync(dispatch_get_main_queue(), ^{
                [weakSelf setupStartioSDKWithAppID:appID adData:adData];
            });
        }
        if ([delegate respondsToSelector:@selector(onInitDidSucceed)]) {
            [delegate onInitDidSucceed];
        }
    }
}

- (void)setupStartioSDKWithAppID:(NSString *)appID adData:(ISAdData *)adData {
    STAStartAppSDK *sdk = [STAStartAppSDK sharedInstance];
    sdk.appID = appID;
    sdk.returnAdEnabled = NO;
    sdk.testAdsEnabled = NO;
    sdk.consentDialogEnabled = NO;
    [sdk addWrapperWithName:@"IronSource" version:ISStartAppAdapterVersion];
}

- (NSString *)networkSDKVersion {
    if ([NSThread isMainThread] == NO) {
        __block NSString *version = nil;
        dispatch_sync(dispatch_get_main_queue(), ^{
            version = [[STAStartAppSDK sharedInstance] version];
        });
        return version;
    }
    return [[STAStartAppSDK sharedInstance] version];
}

- (NSString *)adapterVersion {
    return ISStartAppAdapterVersion;
}

#pragma mark ISAdapterConsentProtocol methods
- (void)setConsent:(BOOL)consent {
    [[STAStartAppSDK sharedInstance] setUserConsent:consent forConsentType:@"pas" withTimestamp:[NSDate timeIntervalSinceReferenceDate]];
}

@end
