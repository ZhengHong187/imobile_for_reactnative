//
//  OnlineService.h
//  LibUGC
//
//  Created by wnmng on 2017/9/13.
//  Copyright © 2017年 beijingchaotu. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

typedef void(^OnlineServiceCompletionCallback)(NSError *error);
typedef void(^OnlineServiceInfoCompletionCallback)(NSDictionary *info, NSError *error);

@protocol OnlineServiceUploadDelegate <NSObject>
@optional
/**
 上传结果
 @param error   若为nil，则成功
 */
-(void)uploadResult:(NSString*)error;
@end

@protocol OnlineServiceDownloadDelegate <NSObject>
@optional

/**
 下载进度显示
 @param bytesWritten    当前需要下载的大小
 @param totalBytesWritten   当前已经下载的总大小
 @param totalBytesExpectedToWrite  当前下载文件的总大小
 */
-(void)bytesWritten:(int64_t) bytesWritten totalBytesWritten:(int64_t) totalBytesWritten
totalBytesExpectedToWrite:(int64_t) totalBytesExpectedToWrite;
/**
 下载结果
 @param error    若为nil，则成功
 */
-(void)downloadResult:(NSString*)error;

@end

@interface OnlineService : NSObject

+ (instancetype)sharedService;

// 登录
- (void)loginWithUsername:(NSString *)username password:(NSString *)password completionCallback:(OnlineServiceCompletionCallback)completionCallback;

// 登出
- (void)logoutWithCompletionCallback:(OnlineServiceCompletionCallback)completionCallback;

// 获取账号信息
- (void)infoWithCompletionCallback:(OnlineServiceInfoCompletionCallback)completionCallback;
/**
 上传online在线数据,需要在登录过后才能调用
 @param filePath 完整的数据路径
 @param fileName 服务器上数据的名称
 注：目前仅支持上传.zip压缩包
 */
-(void)uploadFilePath:(NSString*)filePath onlineFileName:(NSString*)fileName;
/**
 上传协议
 */
@property(nonatomic)id<OnlineServiceUploadDelegate> uploadDelegate;
/**
 下载online在线数据,需要在登录过后才能调用
 @param onlineFileName 服务器上的数据名称
 @param filePath 保存完整的数据路径 ，默认保存在/Library/Caches/SupermapOnlineData/服务器上的数据名称.zip
 */
-(void)downloadFileName:(NSString*)onlineFileName filePath:(NSString*)filePath;
/**
 下载协议
 */
@property(nonatomic)id<OnlineServiceDownloadDelegate> downloadDelegate;
/**
 发布rest服务,若成功，则result为true
 @param dataName 数据名称
 */
-(void) publishService:(NSString*) dataName completionHandler:(void(^)(BOOL result,NSString*_Nullable  error))completionHandler;
/**
 返回用户已上传的数据,dataJson为当前页的json数据
 @param currentPage 当前页
 */
-(void)getDataList:(NSInteger)currentPage pageSize:(NSInteger)pageSize completionHandler:(void(^)(NSString* dataJson,NSString* error))completionHandler;
/**
 用户第currentPage页的json信息
 @param currentPage 当前页
 */
-(void) getServiceList:(NSInteger)currentPage pageSize:(NSInteger)pageSize completionHandler:(void(^)(NSString *serviceJson,NSString* error))completionHandler;
/**
 返回用户已发布的地图和对应的地图图片
 */
-(void) getServiceListWithCompletionHandler:(void(^)(NSArray* _Nullable serviceUrls,NSArray* _Nullable servicePictures))completionHandler;
/**
 用邮箱注册supermap online账号，如果成功，则result为true
 @param email 邮箱
 @param nickname 昵称
 @param password 密码
 */
-(void)registerWithEmail:(NSString*)email  nickname:(NSString*)nickname password:(NSString*)password completionHandler:(void(^)(BOOL result,NSString*_Nullable  info))completionHandler;
/**
 用手机号注册supermap online账号，如果成功，则result为true
 @param phoneNumber 手机号
 @param smsVerifyCode 手机号验证码
 @param nickname 昵称
 @param password 密码
 */
-(void)registerWithPhone:(NSString*)phoneNumber smsVerifyCode:(NSString*)smsVerifyCode nickname:(NSString*)nickname password:(NSString*)password completionHandler:(void(^)(BOOL result,NSString* _Nullable info))completionHandler;
/**
 发送手机号验证码，如果成功，则result为true，info为发送成功
 @param phoneNumber 手机号
 */
-(void)sendSMSVerifyCodeWithPhoneNumber:(NSString*)phoneNumber completionHandler:(void(^)(BOOL result,NSString*_Nullable  info))completionHandler;
/**
 改变服务可见类型，分为公开、私有，默认服务可见类型为私有
 @param serviceName 服务名称
 @param isPublic 是否公开  YES代表公开，NO代表私有
 */
-(void) changeServiceVisibility:(NSString*)serviceName isPublic:(BOOL)isPublic completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 找回密码的第一步 ，首先调用该接口进行对账号的核实，然后进行retrievePasswordSecond，retrievePasswordThrid，retrievePasswordFourth步骤
 @param account 账号
 @param verifyCode 图片中的验证码
 @param isPhoneAccount 是否为手机账号 若是，则YES；若为邮箱，则为NO
 */
-(void)retrievePassword:(NSString*)account verifyCodeImage:(NSString*)verifyCode isPhoneAccount:(BOOL)isPhoneAccount completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 找回密码的第二步
 @param firstResult 第一步找回中的结果
 */
-(void)retrievePasswordSecond:(BOOL)firstResult completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 找回密码的第三步
 @param secondResult 第二步找回中的结果
 */
-(void)retrievePasswordThrid:(BOOL)secondResult safeCode:(NSString*)safeCode completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 找回密码的第四步
 @param thridResult 第三步找回中的结果
 */
-(void)retrievePasswordFourth:(BOOL)thridResult newPassword:(NSString*)newPassword completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/*
 获取找回密码中的验证码图片
 */
-(void) verifyCodeImage:(void(^)(UIImage* _Nullable verifyCodeImage,NSString* error ))completionHandler;
/**
 删除数据,若result为true，则代表删除成功
 @param dataName online上的数据名称
 */
-(void) deleteData:(NSString*)dataName completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 删除服务,若result为true，则代表删除成功
 @param dataName online上的数据名称
 */
-(void) deleteService:(NSString*)dataName completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 数据公开私有
 @param dataName online上的数据名称
 @param isPublic 是否公开 若为YES，则代表为公开，反之为私有
 */
-(void) changeDataVisibility:(NSString*)dataName isPublic:(BOOL)isPubilc completionHandler:(void(^)(BOOL result,NSString* error))completionHandler;
/**
 获取所有用户公开的第currentPage页数据信息,dataJson为第currentPage页的json数据
  @param currentPage 第几页
 */
-(void)getAllUserDataList:(NSInteger)currentPage completionHandler:(void(^)(NSString* dataJson,NSString* error))completionHandler;
/**
 获取所有用户公开的第currentPage页符号库信息,dataJson为第currentPage页的json数据
 @param currentPage 第几页
 */
-(void)getAllUserSymbolLibList:(NSInteger)currentPage completionHandler:(void(^)(NSString* dataJson,NSString* error))completionHandler;
@end
