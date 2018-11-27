//
//  SScene.m
//  Supermap
//
//  Created by Yang Shang Long on 2018/11/9.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "SScene.h"
#import "TouchUtil3D.h"

#import "LableHelper3D.h"
#import "Constants.h"
#import "SMSceneWC.h"
#import "FlyHelper3D.h"


typedef enum{
    /**
     * 空操作
     */
    SS_None_Action = 0x0,
    /**
     * 选择属性
     */
    SS_Feature_Action = 0x1,
    /**
     * 添加兴趣点
     */
    SS_Label_Action = 0x2
    
}SSceneAction;

@interface SScene()<FlyHelper3DProgressDelegate,LableHelper3DDelegate>

@end

static SScene* sScene = nil;
@implementation SScene
RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents
{
    return @[
//             ANALYST_MEASURELINE,
//             ANALYST_MEASURESQUARE,
             POINTSEARCH_KEYWORDS,
             SSCENE_FLY,
             SSCENE_ATTRIBUTE,
             SSCENE_SYMBOL,
             ];
}

+ (instancetype)singletonInstance{
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        sScene = [[self alloc] init];
    });
    
    return sScene;
}
+ (void)setInstance:(SceneControl *)sceneControl{
    sScene = [SScene singletonInstance];
    if (sScene.smSceneWC == nil) {
        sScene.smSceneWC = [[SMSceneWC alloc] init];
    }
    sScene.smSceneWC.sceneControl = sceneControl;
    if (sScene.smSceneWC.workspace == nil) {
        sScene.smSceneWC.workspace = [[Workspace alloc] init];
    }
   
    
//    UITapGestureRecognizer *recognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(singleTap:)];
//    //使用一根手指双击时，才触发点按手势识别器
//    recognizer.numberOfTapsRequired = 1;
//    recognizer.numberOfTouchesRequired = 1;
//    dispatch_async(dispatch_get_main_queue(), ^{
//        [sceneControl.superview addGestureRecognizer:recognizer];
//    });
//
//    UILongPressGestureRecognizer *longRecognizer = [[UILongPressGestureRecognizer alloc]initWithTarget:self action:@selector(longTap:)];
//    longRecognizer.minimumPressDuration = 0.5;
//    dispatch_async(dispatch_get_main_queue(), ^{
//        [sceneControl.superview addGestureRecognizer:longRecognizer];
//    });
    
}

int sSceneAction = SS_None_Action;
-(void)singleTap:(CGPoint)tapPoint{
    NSLog(@"wnmng_______________");
    if( sSceneAction & SS_Feature_Action ){
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        NSDictionary* info;
        [TouchUtil3D getAttribute:sceneControl attribute:&info];
        [self sendEventWithName:SSCENE_ATTRIBUTE
                           body:info];
    }
}

-(void)doubleTap:(CGPoint)tapPoint{
    NSLog(@"wnmngddddddddddddddd");
}

-(void)longPress:(CGPoint)longPressPoint{
    NSLog(@"wnmnglllllllllllll");
    if( sSceneAction & SS_Label_Action ){

    }
}


BOOL bTouchBegin = NO;
float dTap_x = 0;
float dTap_y = 0;
#define SSceneTapTolerance 20*20
//float tapTolerance = 30;
NSTimeInterval tapDelaytime = 0.4;
NSTimeInterval longPressDelaytime = 0.8;
//BOOL bSinglePoint = true;
-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event{
    
    if (event.allTouches.count > 1 ) {
        bTouchBegin = NO;
    }else{
        UITouch *touch = [touches anyObject];
        if (touch.tapCount == 1) {
            CGPoint touchPoint = [touch locationInView: self.smSceneWC.sceneControl];
            bTouchBegin = YES;
            dTap_x = touchPoint.x;
            dTap_y = touchPoint.y;
            // 长按开始计时
            [self performSelector:@selector(longTouch:) withObject:nil afterDelay:longPressDelaytime];
        }
    }

    return;
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event{
    
    UITouch *touch = [touches anyObject];
    CGPoint touchPoint = [touch locationInView: self.smSceneWC.sceneControl];
    float dx = dTap_x-touchPoint.x;
    float dy = dTap_y-touchPoint.y;
    if (bTouchBegin && dx*dx+dy*dy>SSceneTapTolerance) {
        // 移动了就拜拜
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(longPress:) object:nil];
        bTouchBegin = NO;
    }
    
    return;
}
- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event{

    // 如果已经响应过了就跳过
    if (bTouchBegin) {
        UITouch *touch = [touches anyObject];
        // 长按计时取消
        [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(longPress:) object:nil];
        if (touch.tapCount == 1) {
            // 单击准备
            [self performSelector:@selector(singleTouch:) withObject:nil afterDelay:tapDelaytime];
        }else{
            UITouch *touch = [touches anyObject];
            CGPoint touchPoint = [touch locationInView: self.smSceneWC.sceneControl];
            float dx = dTap_x-touchPoint.x;
            float dy = dTap_y-touchPoint.y;
            if(touch.tapCount == 2 && dx*dx+dy*dy<=SSceneTapTolerance){
                // 单击取消
                [NSObject cancelPreviousPerformRequestsWithTarget:self selector:@selector(singleTouch:) object:nil];
                [self doubleTouch:nil];
            }
        }
    }
    
    
    //    if(touches.count > 1)
    //        bSinglePoint = NO;
    //    if(!bSinglePoint){
    //        bSinglePoint = YES;
    //        return;
    //    }
    //    sScene = [SScene singletonInstance];
    //    SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
    //    NSDictionary* info;
    //    [TouchUtil3D getAttribute:sceneControl attribute:&info];
    //    [self sendEventWithName:SSCENE_ATTRIBUTE
    //                       body:info];
    return;
}
- (void)touchesCancelled:(NSSet *)touches withEvent:(UIEvent *)event{
    return;
}

-(void)longTouch:(id)sender{
    if (bTouchBegin) {
        bTouchBegin = NO;
        [self longPress:CGPointMake(dTap_x, dTap_y)];
    }
}
-(void)singleTouch:(id)sender{
    if (bTouchBegin) {
        bTouchBegin = NO;
        [self singleTap:CGPointMake(dTap_x, dTap_y)];
    }
}
-(void)doubleTouch:(id)sender{
    if (bTouchBegin) {
        bTouchBegin = NO;
        [self doubleTap:CGPointMake(dTap_x, dTap_y)];
    }
}

RCT_REMAP_METHOD(openWorkspace, openWorkspaceByInfo:(NSDictionary*)infoDic resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        BOOL result = [sScene.smSceneWC openWorkspace:infoDic];
       // [self openGPS];
        resolve([NSNumber numberWithBool:result]);
    } @catch (NSException *exception) {
        reject(@"Resources", exception.reason, nil);
    }
}
RCT_REMAP_METHOD(openMap, openMapByName:(NSString*)name  resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
     //   Maps* maps = sMap.smMapWC.workspace.maps;
        BOOL bOPen = false;
        sScene.smSceneWC.sceneControl.isRender = NO;
        if (sScene.smSceneWC.workspace.scenes.count  > 0) {
            NSString* mapName = name;
            bOPen = [scene open:mapName];
            if(bOPen){
                sScene.smSceneWC.sceneControl.isRender = YES;
                [scene refresh];
            }
        }
        
        resolve([NSNumber numberWithBool:@(bOPen)]);
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 获取场景列表
 *
 * @param promise
 */
RCT_REMAP_METHOD(getMapList, getMapListResolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scenes* scenes = sScene.smSceneWC.workspace.scenes;
        NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = [scenes count];
        
        for (int i = 0; i < count; i++) {
            NSString* name = [scenes get:i];// .get(i).getName();
            NSDictionary* map = @{@"name":name};
            [arr addObject:map];
        }
        
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 获取当前场景图层列表
 *
 * @param promise
 */
RCT_REMAP_METHOD(getLayerList, resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = scene.layers.count;
       
        for (int i = 0; i < count; i++) {
            NSString* name = [scene.layers getLayerWithIndex:i].name;// .get(i).getName();
            BOOL visible = [scene.layers getLayerWithIndex:i].visible;
            BOOL selectable = [scene.layers getLayerWithIndex:i].selectable;// .isSelectable();
            NSDictionary* map;
            if (i == count - 1) {
                map = @{@"name":name,@"visible": @(visible),@"selectable": @(selectable),@"basemap":@(1)};
               // map.putBoolean("basemap", true);
            }
            map = @{@"name":name,@"visible": @(visible),@"selectable": @(selectable)};
            
            
            [arr addObject:map];
        }
        
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

RCT_REMAP_METHOD(changeBaseMap, oldLayer:(NSString*)oldLayer Url:(NSString*) Url Layer3DType:(NSString*) layer3DType layerName:(NSString*) layerName imageFormatType:(NSString*) imageFormatType dpi:(double) dpi addToHead:(BOOL)addToHead resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    sScene = [SScene singletonInstance];
    Scene* scene = sScene.smSceneWC.sceneControl.scene;
    @try {
        if (oldLayer != nil) {
            [scene.layers removeLayerWithName:oldLayer];
            //            scene.getLayers().get(oldLayer).setVisible(false);
        }
        Layer3DType nlayer3DType = 0;
        if([layer3DType isEqualToString:@"IMAGEFILE"]){
            nlayer3DType = IMAGEFILE;
        }else if ([layer3DType isEqualToString:@"KML"]){
            nlayer3DType = KML;
        }else if ([layer3DType isEqualToString:@"l3dBingMaps"]){
            nlayer3DType = BINGMAPS;
        }else if ([layer3DType isEqualToString:@"OSGBFILE"]){
            nlayer3DType = OSGBFILE;
        }else if ([layer3DType isEqualToString:@"VECTORFILE"]){
            nlayer3DType = VECTORFILE;
        }else if ([layer3DType isEqualToString:@"WMTS"]){
            nlayer3DType = WMTS;
        }
        
       
        ImageFormatType imageFormatType1 ;
        if([imageFormatType isEqualToString:@"BMP"]){
            imageFormatType1 = ImageFormatTypeBMP;
        }else if ([imageFormatType isEqualToString:@"DXTZ"]){
            imageFormatType1 = ImageFormatTypeDXTZ;
        }else if ([imageFormatType isEqualToString:@"GIF"]){
            imageFormatType1 = ImageFormatTypeGIF;
        }else if ([imageFormatType isEqualToString:@"JPG"]){
            imageFormatType1 = ImageFormatTypeJPG;
        }else if ([imageFormatType isEqualToString:@"JPG_PNG"]){
            imageFormatType1 = ImageFormatTypeJPG_PNG;
        }else if ([imageFormatType isEqualToString:@"NONE"]){
            imageFormatType1 = ImageFormatTypeNONE;
        }else if ([imageFormatType isEqualToString:@"PNG"]){
            imageFormatType1 = ImageFormatTypeNONE;
        }
        
        if (dpi == 0 && imageFormatType == nil) {
            [scene.layers addLayerWithURL:Url type:layer3DType dataLayerName:layerName toHead:addToHead];
           // scene.getLayers().add(Url, layer3DType, layerName, addToHead);
        } else {
            [scene.layers  addLayerWithTiandituURL:Url type:layer3DType dataLayerName:layerName imageFormatType:imageFormatType1 dpi:dpi toHead:dpi];
           // scene.getLayers().add(Url, layer3DType, layerName, imageFormatType1, dpi, addToHead);
        }
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 获取当前场景地形图层列表
 *
 * @param promise
 */
RCT_REMAP_METHOD(getTerrainLayerList, terrainLayerLisTresolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        //   Maps* maps = sMap.smMapWC.workspace.maps;
        
        NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = scene.terrainLayers.count;
        
        for (int i = 0; i < count; i++) {
            NSString* name = [scene.terrainLayers getLayerAtIndex:i].name;// .get(i).getName();
            BOOL visible = [scene.terrainLayers getLayerAtIndex:i].visible;
           // BOOL selectable = [scene.terrainLayers getLayerWithIndex:i].selectable;// .isSelectable();
            NSDictionary* map;
            map = @{@"name":name,@"visible": @(visible)};
            [arr addObject:map];
        }
        
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 设置场景地形图层是否可见
 *
 * @param promise
 */
RCT_REMAP_METHOD(setTerrainLayerListVisible, name:(NSString*)name  bVisual:(BOOL)bVisual setTerrainLayerListVisible:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        //   Maps* maps = sMap.smMapWC.workspace.maps;
        
      //  NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = scene.terrainLayers.count;
        if(count>0){
            [scene.terrainLayers getLayerWithName:name].visible = bVisual;
        }
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 设置场景图层是否可见
 *
 * @param promise
 */
RCT_REMAP_METHOD(setVisible, name:(NSString*)name  bVisual:(BOOL)bVisual setVisible:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        //   Maps* maps = sMap.smMapWC.workspace.maps;
        
        //NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = scene.layers.count;
        if(count>0){
            [scene.layers getLayerWithName:name].visible = bVisual;
        }
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 设置场景图层是否可选择
 *
 * @param promise
 */
RCT_REMAP_METHOD(setSelectable, name:(NSString*)name  bVisual:(BOOL)bVisual setSelectable:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        //   Maps* maps = sMap.smMapWC.workspace.maps;
        
        //NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = scene.layers.count;
        if(count>0){
            [scene.layers getLayerWithName:name].selectable = bVisual;
        }
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 搜索关键字显示位置相关信息列表
 *
 * @param promise
 */
RCT_REMAP_METHOD(pointSearch, name:(NSString*)name  pointSearch:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
      
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 初始化位置搜索
 *
 * @param promise
 */
RCT_REMAP_METHOD(pointSearch, pointSearch:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 获取飞行列表
 *
 * @param promise
 */
RCT_REMAP_METHOD(getFlyRouteNames, getFlyRouteNames:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = [[sScene smSceneWC]sceneControl];
        NSString* path = [sScene.smSceneWC.workspace.connectionInfo server];
        NSArray* strArr = [path componentsSeparatedByString:@"/"];
        NSString * strServerName = [strArr lastObject];
        NSString* strDir = [path substringToIndex:path.length-strServerName.length];
        [[FlyHelper3D sharedInstance] resetSceneControl:sceneControl SceneDir:strDir];
        NSArray *resultArray = [[FlyHelper3D sharedInstance] getFlyRouteNames];
        
        NSMutableArray* arr = [[NSMutableArray alloc]initWithCapacity:1];
        int count = resultArray.count;
        
        for (int i = 0; i < count; i++) {
            NSString* name =[resultArray objectAtIndex:i];// .get(i).getName();
            NSDictionary* map;
            map = @{@"title":name,@"index": @(i)};
            [arr addObject:map];
        }
        
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 设置飞行
 *
 * @param promise
 */
RCT_REMAP_METHOD(setPosition, index:(int)index setPosition:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[FlyHelper3D sharedInstance] chooseFlyRoute:index];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 开始飞行
 *
 * @param promise
 */
RCT_REMAP_METHOD(flyStart,  flyStart:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[FlyHelper3D sharedInstance] flyStart];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 暂停飞行
 *
 * @param promise
 */
RCT_REMAP_METHOD(flyPause,  flyPause:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[FlyHelper3D sharedInstance] flyPause];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 暂停或开始飞行
 *
 * @param promise
 */
RCT_REMAP_METHOD(flyPauseOrStart,  flyPauseOrStart:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[FlyHelper3D sharedInstance]flyPauseOrStart];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 结束飞行
 *
 * @param promise
 */
RCT_REMAP_METHOD(flyStop,  flyStop:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[FlyHelper3D sharedInstance]flyStop];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 获取飞行进度
 *
 * @param promise
 */
RCT_REMAP_METHOD(getFlyProgress,  getFlyProgress:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [FlyHelper3D sharedInstance].flyProgressDelegate = self;
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

-(void)flyProgressPercent:(int)percent{
    [self sendEventWithName:SSCENE_FLY body:@(percent)];
}

/**
 * 场景放大缩小
 *
 * @param promise
 */
RCT_REMAP_METHOD(zoom,  scale:(double)scale zoom:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        [scene zoom:scale];
        [scene refresh];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 指北针
 *
 * @param promise
 */
RCT_REMAP_METHOD(setHeading,  setHeading:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        Camera camera = [scene camera];
        camera.heading = 0;
        scene.camera = camera;
        [scene refresh];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 设置触控器获取对象属性
 *
 * @param promise
 */
RCT_REMAP_METHOD(getAttribute,  getAttribute:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
         sceneControl.sceneControlDelegate = self;
        sSceneAction |= SS_Feature_Action;
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}



/**
 * 清除对象列表属性
 *
 * @param promise
 */
RCT_REMAP_METHOD(clearSelection,  clearSelection:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        Scene* scene = sScene.smSceneWC.sceneControl.scene;
        dispatch_async(dispatch_get_main_queue(), ^{
            int count = scene.layers.count;
            for (int i = 0; i < count; i++) {
                [[scene.layers getLayerWithIndex:i].selection3D clear];//  get(i).getSelection().clear();
            }
            
            [scene refresh];
        });
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

RCT_REMAP_METHOD(removeOnTouchListener,  removeOnTouchListener:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        sceneControl.sceneControlDelegate = nil;
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注初始化
 */
RCT_REMAP_METHOD(initsymbol,  initsymbol:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        Workspace* workspace = sScene.smSceneWC.workspace;
        NSString* path = sScene.smSceneWC.workspace.connectionInfo.server;
        NSString* result = [path.stringByDeletingLastPathComponent stringByAppendingString:@"/files/"];
        NSString* kmlname = @"newKML.kml";
        
        [[LableHelper3D sharedInstance] initSceneControl:sceneControl path:result kml:kmlname];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注打点
 */
RCT_REMAP_METHOD(startDrawPoint,  startDrawPoint:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
       
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] startDrawPoint];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注点绘线
 */
RCT_REMAP_METHOD(startDrawLine,  startDrawLine:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] startDrawLine];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注点绘面
 */
RCT_REMAP_METHOD(startDrawArea,  startDrawArea:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] startDrawArea];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注撤销
 */
RCT_REMAP_METHOD(symbolback,  symbolback:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] back];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 清除所有标注
 */
RCT_REMAP_METHOD(clearAllLabel,  clearAllLabel:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] clearAllLabel];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 保存所有标注
 */
RCT_REMAP_METHOD(save,  save:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sScene = [SScene singletonInstance];
        SceneControl* sceneControl = sScene.smSceneWC.sceneControl;
        [[LableHelper3D sharedInstance] save];
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 标注绘制文本
 */
RCT_REMAP_METHOD(startDrawText,  startDrawText:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        [[LableHelper3D sharedInstance] startDrawText];
        [LableHelper3D sharedInstance].delegate = self;

        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

-(void)drawTextAtPoint:(CGPoint)pnt{
    NSLog(@"wnmng:_%d__%d_",pnt.x,pnt.y);
            [self sendEventWithName:SSCENE_SYMBOL
                               body:@{@"pointX":@(pnt.x),@"pointY":@(pnt.y)}];
}

/**
 * 标注添加文本
 */
RCT_REMAP_METHOD(addGeoText,  addGeoTextX:(int)x Y:(int)y Text:(NSString*)text resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        dispatch_sync(dispatch_get_main_queue(), ^{
            NSLog(@"wnmng:xxxx_%d_xxxx_%d_%@",x,y,text);
            [[LableHelper3D sharedInstance] addGeoText:CGPointMake(x, y) test:text];
        });
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

RCT_REMAP_METHOD(back,  back:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}

/**
 * 关闭工作空间及地图控件
 */
RCT_REMAP_METHOD(closeWorkspace,  closeWorkspace:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        
        resolve(@(1));
    } @catch (NSException *exception) {
        reject(@"SScene", exception.reason, nil);
    }
}
@end
