//
//  ISStartAppCustomAdapter.m
//  IronSourceAdapter
//
//  Created by Start.io on 22.11.2021.
//

#import <StartApp/StartApp.h>
#import "ISStartAppCustomAdapter.h"
#import "ISStartAppHelper.h"

@implementation ISStartAppCustomAdapter

#pragma mark ISAdapterBaseProtocol methods
- (void)init:(ISAdData *)adData delegate:(id<ISNetworkInitializationDelegate>)delegate {
    NSString *appId = adData.configuration[ISStartAppKeyAppID];
    if (appId.length == 0) {
        [delegate onInitDidFailWithErrorCode:ISAdapterErrorMissingParams errorMessage:@"Missing StartAppSDK AppID parameter"];
    }
    else {
        dispatch_async(dispatch_get_main_queue(), ^{
            STAStartAppSDK *sdk = [STAStartAppSDK sharedInstance];
            sdk.appID = appId;
            sdk.returnAdEnabled = NO;
            sdk.testAdsEnabled = NO;
            sdk.consentDialogEnabled = NO;
            [sdk addWrapperWithName:@"IronSource" version:self.adapterVersion];
            [delegate onInitDidSucceed];
        });
    }
}

- (NSString *)networkSDKVersion {
    return [[STAStartAppSDK sharedInstance] version];
}

- (NSString *)adapterVersion {
    return ISStartAppAdapterVersion;
}

#pragma mark ISAdapterConsentProtocol methods
- (void)setConsent:(BOOL)consent {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[STAStartAppSDK sharedInstance] setUserConsent:consent forConsentType:@"pas" withTimestamp:[NSDate timeIntervalSinceReferenceDate]];
    });
}

@end
