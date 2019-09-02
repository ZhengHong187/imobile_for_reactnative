//
//  SIPortalService.m
//  Supermap
//
//  Created by apple on 2019/8/6.
//  Copyright © 2019 Facebook. All rights reserved.
//
#import "SIPortalService.h"

static IPortalService* m_iportalService;

@interface SIPortalService(){
   
}
@end

@implementation SIPortalService
static NSString* TAG =  @"SIPortalService";
static RCTPromiseResolveBlock _resolve;
static NSString* _uploadFilePath;

#pragma mark -- 定义宏，让该类暴露给RN层
RCT_EXPORT_MODULE();
- (NSArray<NSString *> *)supportedEvents{
    return @[IPORTAL_SERVICE_UPLOADING,
             IPORTAL_SERVICE_UPLOADED,
             ];
}

#pragma mark -- 定义宏的方法，让该类的方法暴露给RN层
#pragma mark ---------------------------- init
RCT_REMAP_METHOD(init, initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    m_iportalService = [IPortalService sharedInstance];
    m_iportalService.responseDelegate = self;
    resolve([NSNumber numberWithBool:YES]);
}

#pragma mark ---------------------------- 网络请求失败回调
-(void) onFailedException:(NSException*)exception{
    _resolve([NSNumber numberWithBool:NO]);
}

#pragma mark ---------------------------- login
RCT_REMAP_METHOD(login, loginWithUrl:(NSString *) url name:(NSString*)name password:(NSString*) password rememberme:(BOOL)rememberme initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    [m_iportalService loginPortalUrl:url user:name password:password remembered:rememberme];
}

#pragma mark ---------------------------- login回调
-(void) onLoginFinished:(BOOL)bSucc message:(NSString*)strInfo {
    if(bSucc){
         _resolve([NSNumber numberWithBool:bSucc]);
    } else {
        _resolve(@"登陆失败:请检查用户名和密码");
    }
}

#pragma mark ---------------------------- logout
RCT_REMAP_METHOD(logout, logoutWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    [m_iportalService logout];
    resolve([NSNumber numberWithBool:YES]);
}

#pragma mark ---------------------------- getMyAccount
RCT_REMAP_METHOD(getMyAccount, getMyAccountWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    [m_iportalService getMyAccount];
}

#pragma mark ---------------------------- getMyAccount回调
-(void)myAccountResult:(NSDictionary*)result{
    _resolve([SIPortalService convertDicToString:result]);
}

#pragma mark ---------------------------- getMyDatas
RCT_REMAP_METHOD(getMyDatas, getMyDatasAt:(int)currentPage pageSize:(int)pageSize initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    NSMutableDictionary* searchParameter = [[NSMutableDictionary alloc]init];
    [searchParameter setObject:[NSString stringWithFormat:@"%d",currentPage] forKey:@"currentPage"];
    [searchParameter setObject:[NSString stringWithFormat:@"%d",pageSize] forKey:@"pageSize"];
    [m_iportalService getMyDatas:searchParameter];
}

#pragma mark ---------------------------- getMyDatas回调
-(void)myDatasResult:(NSDictionary *)result{
    _resolve([SIPortalService convertDicToString:result]);
}

#pragma mark ---------------------------- getMyServices
RCT_REMAP_METHOD(getMyServices, getMyServicesAt:(int)currentPage pageSize:(int)pageSize initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    NSMutableDictionary* searchParameter = [[NSMutableDictionary alloc]init];
    [searchParameter setObject:[NSString stringWithFormat:@"%d",currentPage] forKey:@"currentPage"];
    [searchParameter setObject:[NSString stringWithFormat:@"%d",pageSize] forKey:@"pageSize"];
    [m_iportalService getMyServices:searchParameter];
}

#pragma mark ---------------------------- getMyServices回调
-(void)myServicesResult:(NSDictionary *)result{
    _resolve([SIPortalService convertDicToString:result]);
}

#pragma mark ---------------------------- deleteMyData
RCT_REMAP_METHOD(deleteMyData, deleteMyDataID:(NSString*)id initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    [m_iportalService deleteMyContentItem:MY_DATA id:[id intValue]];
}

#pragma mark ---------------------------- deleteMyService
RCT_REMAP_METHOD(deleteMyService, deleteMyServiceID:(NSString*)id initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    [m_iportalService deleteMyContentItem:MY_SERVICE id:[id intValue]];
}

#pragma mark ---------------------------- deleteMyContentItem回调
-(void)deleteMyContentItemResult:(BOOL)bSucceed{
    _resolve([NSNumber numberWithBool:bSucceed]);
}

#pragma mark ---------------------------- publishService
RCT_REMAP_METHOD(publishService, publishServiceID:(NSString*)id initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    NSMutableDictionary* parameter = [[NSMutableDictionary alloc]init];
    [parameter setObject:@"RESTMAP,RESTDATA" forKey:@"serviceType"];
    [m_iportalService publishServices:[id intValue] parameter:parameter];
}

#pragma mark ---------------------------- publishService回调
-(void)publishServiceResult:(BOOL)bSucceed{
    _resolve([NSNumber numberWithBool:bSucceed]);
}

#pragma mark ---------------------------- setServicesShareConfig
RCT_REMAP_METHOD(setServicesShareConfig, setServiceID:(NSString*)id public:(BOOL) public initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    NSMutableArray *ids = [NSMutableArray array];
    [ids addObject:[NSNumber numberWithInt:[id intValue]]];
    NSMutableDictionary* parameter = [[NSMutableDictionary alloc]init];
    NSString* paramString;
    if(public){
        [parameter setObject:@"USER" forKey:@"entityType"];
        [parameter setObject:@"GUEST" forKey:@"entityName"];
        [parameter setObject:@"READ" forKey:@"permissionType"];
        paramString = [SIPortalService convertDicToString:parameter];
    } else {
        paramString = @"[]";
    }
    [m_iportalService setServicesShareConfig:ids parameter:paramString];
}

#pragma mark ---------------------------- setServicesShareConfig回调
-(void)servicesShareConfigFinished:(BOOL)bSucceed{
    _resolve([NSNumber numberWithBool:bSucceed]);
}

#pragma mark ---------------------------- uploadData
RCT_REMAP_METHOD(uploadData, uploadFrom:(NSString*)filePath As:(NSString*)fileName initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    _uploadFilePath = filePath;
    NSString* tags = @"用户数据";
    [m_iportalService getMyDataIDFor:fileName tag:tags type:DIT_WORKSPACE];
}

#pragma mark ---------------------------- uploadDataByType
RCT_REMAP_METHOD(uploadDataByType, uploadFrom:(NSString*)filePath As:(NSString*)fileName dataType:(NSString*)type initWithResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    _resolve = resolve;
    _uploadFilePath = filePath;
    NSString* tags = @"用户数据";
    DataItemType dataType=DIT_WORKSPACE;
    if([type isEqualToString:@"UDB"]){
        dataType=DIT_UDB;
    }
    [m_iportalService getMyDataIDFor:fileName tag:tags type:dataType];
}

#pragma mark ----------------------------获取上传id回调
-(void)myDataIDResult:(NSDictionary *)result{
    NSNumber* id = [result objectForKey:@"childID"];
    if(id != nil){
        [m_iportalService uploadData:_uploadFilePath dataID:[id intValue] progressListener:self];
    } else {
        _resolve([NSNumber numberWithBool:NO]);
    }
}

#pragma mark ---------------------------- 上传进度
-(void)upLoadProgress:(float)newProgress{
    float progress = newProgress * 100;
    [self sendEventWithName:IPORTAL_SERVICE_UPLOADING body:[NSNumber numberWithFloat:progress]];
}

#pragma mark ---------------------------- uploadData完成回调
- (void)upLoadDataComplite:(NSDictionary *)result{
    _resolve([NSNumber numberWithBool:YES]);
    [self sendEventWithName:IPORTAL_SERVICE_UPLOADED body:[NSNumber numberWithBool:YES]];
}

+(NSString*) convertDicToString:(NSDictionary*)dict{
    NSError *error;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    NSString *jsonString;
    
    if (!jsonData) {
        NSLog(@"%@",error);
    }else{
        jsonString = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    
    NSMutableString *mutStr = [NSMutableString stringWithString:jsonString];
    NSRange range2 = {0,mutStr.length};
    //去掉字符串中的换行符
    [mutStr replaceOccurrencesOfString:@"\n" withString:@"" options:NSLiteralSearch range:range2];
    
    return mutStr;
}
@end