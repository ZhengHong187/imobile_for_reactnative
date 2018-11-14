//
//  JSGeometry.m
//  HelloWorldDemo
//
//  Created by 王子豪 on 2016/11/22.
//  Copyright © 2016年 Facebook. All rights reserved.
//

#import "JSGeometry.h"
#import "SuperMap/Geometry.h"
#import "JSObjManager.h"
@implementation JSGeometry
RCT_EXPORT_MODULE();
RCT_REMAP_METHOD(getInnerPoint,getInnerPointByGeometryId:(NSString*)geometryId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  Geometry* geo = [JSObjManager getObjWithKey:geometryId];
  Point2D* p2D = [geo getInnerPoint];
  if(p2D){
    NSInteger key = (NSInteger)p2D;
    [JSObjManager addObj:p2D];
    resolve(@{@"point2DId":@(key).stringValue});
  }else{
    reject(@"geometry",@"getInnerPoint failed!!!",nil);
  }
}

RCT_REMAP_METHOD(setStyle,setStyleByGeometryId:(NSString*)geometryId geoStyleId:(NSString*)geoStyleId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    
    @try {
        Geometry* geo = [JSObjManager getObjWithKey:geometryId];
        GeoStyle* style = [JSObjManager getObjWithKey:geoStyleId];
        [geo setStyle:style];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"geometry", exception.reason, nil);
    }
   
}

RCT_REMAP_METHOD(getID,getIDId:(NSString*)geometryId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Geometry* geo = [JSObjManager getObjWithKey:geometryId];
        int p2D = [geo getID];
        resolve(@(p2D));
    } @catch (NSException *exception) {
         reject(@"geometry",@"geometry getID failed!!!",nil);
    }
}

RCT_REMAP_METHOD(getType,getTypeId:(NSString*)geometryId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Geometry* geo = [JSObjManager getObjWithKey:geometryId];
        int p2D = [geo getType];
        resolve(@(p2D));
    } @catch (NSException *exception) {
        reject(@"geometry",@"geometry getType failed!!!",nil);
    }
}

RCT_REMAP_METHOD(dispose, disposeId:(NSString*)geometryId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Geometry* geo = [JSObjManager getObjWithKey:geometryId];
        [geo dispose];
        [JSObjManager removeObj:geometryId];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"geometry",@"geometry getType failed!!!",nil);
    }
}
@end
