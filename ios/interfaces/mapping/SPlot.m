//
//  SPlot.m
//  Supermap
//
//  Created by zhouyuming on 2019/9/9.
//  Copyright © 2019年 Facebook. All rights reserved.
//

#import "SPlot.h"

static SMap *sMap = nil;
static Point2Ds *animationWayPoint2Ds;
static Point2Ds *animationWaySavePoint2Ds;

@implementation SPlot

RCT_EXPORT_MODULE();
#pragma mark 初始化标绘符号库
RCT_REMAP_METHOD(initPlotSymbolLibrary, initPlotSymbolLibrary:(NSArray*)plotSymbolPaths isFirst:(BOOL) isFirst newName:(NSString*)newName isDefaultNew:(BOOL)isDefaultNew resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    
    @try {
        sMap = [SMap singletonInstance];
        Workspace *workspace =sMap.smMapWC.mapControl.map.workspace;
        
        Dataset* dataset=nil;
        Layer* cadLayer=nil;
        NSString* userpath=nil;
        NSString* Name=[@"PlotEdit_" stringByAppendingString:(isDefaultNew?newName:sMap.smMapWC.mapControl.map.name)];
        NSArray *array = [plotSymbolPaths[0] componentsSeparatedByString:@"/"];
        for (int i=0; i<array.count; i++) {
            if([array[i] isEqualToString:@"User"]&&(i+1)<array.count){
                userpath=array[i+1];
                break;
            }
        }
        
        NSString *plotDatasourceName = [NSString  stringWithFormat:@"%@%@%@",@"Plotting_",Name,@"#"];
        plotDatasourceName = [plotDatasourceName stringByReplacingOccurrencesOfString:@"." withString:@""];
        Datasource *opendatasource = [workspace.datasources getAlias:plotDatasourceName];
        Datasource *datasource = nil;
        if(opendatasource == nil){
            DatasourceConnectionInfo *info = [[DatasourceConnectionInfo alloc]init];
            info.alias = plotDatasourceName;
            info.engineType = ET_UDB;
            NSString *path = [NSString stringWithFormat: @"%@%@%@%@%@",NSHomeDirectory(),@"/Documents/iTablet/User/",userpath,@"/Data/Datasource/",plotDatasourceName];
            info.server = path;
            if([[NSFileManager defaultManager] fileExistsAtPath:[path stringByAppendingString:@".udb"] ]){
                datasource = [workspace.datasources open:info];
                if(!datasource){
                    [[NSFileManager defaultManager] removeItemAtPath:[path stringByAppendingString:@".udb"] error:nil];
                    [[NSFileManager defaultManager] removeItemAtPath:[path stringByAppendingString:@".udd"] error:nil];
                    datasource=[workspace.datasources create:info];
                    
                    
                }
            }else{
                datasource=[workspace.datasources create:info];
            }
            
            if(!datasource){
                datasource=[workspace.datasources open:info];
            }
            [info dispose];
        }else{
            datasource=opendatasource;
        }
        if(!datasource){
            resolve(nil);
            return;
        }
        Datasets *datasets = datasource.datasets;
        
        for (int i=0; i<[sMap.smMapWC.mapControl.map.layers getCount]; i++) {
            Layer* tempLayer=[sMap.smMapWC.mapControl.map.layers getLayerAtIndex:i];
            if([tempLayer.name hasPrefix:@"PlotEdit_"]&&tempLayer.dataset.datasetType==CAD){
                dataset=tempLayer.dataset;
                cadLayer=tempLayer;
                //                break;
            }else{
                [tempLayer setEditable:NO];
            }
        }
        
        //        dataset = [datasets getWithName:Name];
        NSString* datasetName;
        if(!dataset){
            datasetName = [datasets availableDatasetName: Name];
            DatasetVectorInfo *datasetVectorInfo = [[DatasetVectorInfo alloc]init];
            [datasetVectorInfo setDatasetType:CAD];
            [datasetVectorInfo setEncodeType:NONE];
            [datasetVectorInfo setName:datasetName];
            DatasetVector *datasetVector = [datasets create:datasetVectorInfo];
//            //创建数据集时创建好字段
//            [SMap addFieldInfo:datasetVector Name:@"name" FieldType:FT_TEXT Required:NO Value:@"" Maxlength:255];
//            [SMap addFieldInfo:datasetVector Name:@"remark" FieldType:FT_TEXT Required:NO Value:@"" Maxlength:255];
//            [SMap addFieldInfo:datasetVector Name:@"address" FieldType:FT_TEXT Required:NO Value:@"" Maxlength:255];
//
            dataset = [datasets getWithName:datasetName];
            Map *map = sMap.smMapWC.mapControl.map;
            Layer *layer = [map.layers addDataset:dataset ToHead:YES];
            [layer setEditable:YES];
            [datasetVectorInfo dispose];
            [datasetVector close];
        }else{
            [cadLayer setEditable:YES];
            //            Layers *layers =sMap.smMapWC.mapControl.map.layers;
            //            Layer *editLayer = [layers getLayerWithName:[NSString stringWithFormat:@"%@@%@",Name,datasource.alias]];
            //            if(editLayer){
            //                [editLayer setEditable:YES];
            //            }else{
            //                Layer* layer=[layers addDataset:dataset ToHead:true];
            //                [layer setEditable:YES];
            //            }
        }
        
        NSMutableDictionary* libInfo = [[NSMutableDictionary alloc] init];
        for (NSString* path in plotSymbolPaths) {
            int libId=[sMap.smMapWC.mapControl addPlotLibrary:path];
            if(-1 != libId){
                NSString* libName=[sMap.smMapWC.mapControl getPlotSymbolLibName: libId];
                [libInfo setObject:@(libId) forKey:libName];
            }
            
            //            if(isFirst&&[libName isEqualToString:@"警用标号"]){
            //                Point2Ds* point2Ds=[[Point2Ds alloc] init];
            //                Point2D* point2D=[[Point2D alloc] initWithX:sMap.smMapWC.mapControl.map.viewBounds.left Y:sMap.smMapWC.mapControl.map.viewBounds.top];
            //                [point2Ds add:point2D];
            //                [sMap.smMapWC.mapControl addPlotObject:libId symbolCode:20100 point:point2Ds];
            //                [sMap.smMapWC.mapControl cancel];
            ////                Recordset *recordset = [(DatasetVector*)dataset recordset:NO cursorType:DYNAMIC];
            ////                [recordset moveLast];
            //////                [recordset delete];
            ////                [recordset update];
            ////                [recordset dispose];
            //                [sMap.smMapWC.mapControl.map refresh];
            //                [sMap.smMapWC.mapControl setAction:PAN];
            //            }
        }
        //        if(isFirst){
        //            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        //                [self delay:dataset];
        //            });
        //        }
        
        [sMap.smMapWC.mapControl.map refresh];
        resolve(libInfo);
    } @catch (NSException *exception) {
        reject(@"initPlotSymbolLibrary", exception.reason, nil);
    }
}

//-(void)delay:(DatasetVector*)dataset {
//    Recordset *recordset = [(DatasetVector*)dataset recordset:NO cursorType:DYNAMIC];
//    [recordset moveLast];
//    [recordset delete];
//    [recordset update];
//    [recordset dispose];
//}
#pragma mark 移除标绘库
RCT_REMAP_METHOD(removePlotSymbolLibraryArr, removePlotSymbolLibraryArr:(NSArray*)plotSymbolIds resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        for (int i=0; i<[plotSymbolIds count]; i++) {
            [mapControl removePlotLibrary:(int)plotSymbolIds[i]];
        }
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"removePlotSymbolLibraryArr", exception.reason, nil);
    }
}

#pragma mark 设置标绘符号
RCT_REMAP_METHOD(setPlotSymbol, setPlotSymbol:(int)libId symbolCode:(int)symbolCode resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        for (int i=0; i<[sMap.smMapWC.mapControl.map.layers getCount]; i++) {
            Layer* tempLayer=[sMap.smMapWC.mapControl.map.layers getLayerAtIndex:i];
            if([tempLayer.name hasPrefix:@"PlotEdit_"]&&tempLayer.dataset.datasetType==CAD){
                [tempLayer setEditable:YES];
            }else{
                [tempLayer setEditable:NO];
            }
        }
        
        [mapControl setAction:CREATE_PLOT];
        [mapControl setPlotSymbol:libId symbolCode:symbolCode];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setPlotSymbol", exception.reason, nil);
    }
}

#pragma mark 导入标绘模板库
RCT_REMAP_METHOD(importPlotLibData, importPlotLibData:(NSString*)fromPath  resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        NSString* userpath=nil;
        NSArray *array = [fromPath componentsSeparatedByString:@"/"];
        for (int i=0; i<array.count; i++) {
            if([array[i] isEqualToString:@"User"]&&(i+1)<array.count){
                userpath=array[i+1];
                break;
            }
        }
        NSString *toPath = [NSString stringWithFormat: @"%@%@%@%@",NSHomeDirectory(),@"/Documents/iTablet/User/",userpath,@"/Data/Plotting/"];
        BOOL result=[FileUtils copyFiles:fromPath targetDictionary:toPath filterFileSuffix:@"plot" filterFileDicName:@"Symbol" otherFileDicName:@"SymbolIcon" isOnly:NO];
        
        resolve([NSNumber numberWithBool:result]);
    } @catch (NSException *exception) {
        reject(@"importPlotLibData", exception.reason, nil);
    }
}


#pragma mark 添加cad图层
RCT_REMAP_METHOD(addCadLayer, addCadLayer:(NSString*)layerName resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        Layer* layer=[mapControl.map.layers getLayerWithName:layerName];
        if(!layer){
            DatasetVectorInfo* datasetVectorInfo=[[DatasetVectorInfo alloc] init];
            [datasetVectorInfo setDatasetType:CAD];
            [datasetVectorInfo setName:layerName];
            Dataset* datasetVector=[[sMap.smMapWC.workspace.datasources get:0].datasets getWithName:layerName];
            if(!datasetVector){
                datasetVector=[[sMap.smMapWC.workspace.datasources get:0].datasets create:datasetVectorInfo];
            }
            layer=[mapControl.map.layers addDataset:datasetVector ToHead:true];
            [mapControl.map.layers addLayer:layer];
        }
        [layer setEditable:YES];
        
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"addCadLayer", exception.reason, nil);
    }
}

#pragma mark 态势推演定时器
RCT_REMAP_METHOD(initAnimation,initAnimation:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        //获取全局队列
        dispatch_queue_t global = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        
        //创建一个定时器，并将定时器的任务交给全局队列执行(并行，不会造成主线程阻塞)
        dispatch_source_t timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, global);
        
        self.timer = timer;
        
        //设置触发的间隔时间
        dispatch_source_set_timer(timer, DISPATCH_TIME_NOW, 0.1 * NSEC_PER_SEC, 0 * NSEC_PER_SEC);
        
        //设置定时器的触发事件
        dispatch_source_set_event_handler(timer, ^{
            [[AnimationManager getInstance] excute];
            
        });
        
        dispatch_resume(timer);
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"addCadLayer", exception.reason, nil);
    }
}

#pragma mark 读取态势推演xml文件
RCT_REMAP_METHOD(readAnimationXmlFile,readAnimationXmlFile:(NSString*) filePath resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        //获取全局队列
        dispatch_queue_t global = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
        
        //创建一个定时器，并将定时器的任务交给全局队列执行(并行，不会造成主线程阻塞)
        dispatch_source_t timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, global);
        
        self.timer = timer;
        
        //设置触发的间隔时间
        dispatch_source_set_timer(timer, DISPATCH_TIME_NOW, 0.1 * NSEC_PER_SEC, 0 * NSEC_PER_SEC);
        
        //设置定时器的触发事件
        dispatch_source_set_event_handler(timer, ^{
            [[AnimationManager getInstance] excute];
            
        });
        
        dispatch_resume(timer);
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        [mapControl setAnimation];
        [[AnimationManager getInstance] deleteAll];
        [[AnimationManager getInstance] getAnimationFromXML:filePath];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 播放态势推演动画
RCT_REMAP_METHOD(animationPlay,animationPlay:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        if([AnimationManager.getInstance getGroupCount]>0)
        {
            Rectangle2D* rectangle2D=[[Rectangle2D alloc] init];
            AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
            int animationCount=[animationGroup getAnimationCount];
            if(animationCount>0){
                for(int i=0;i<animationCount;i++){
                    AnimationGO* animationGo=[animationGroup getAnimationByIndex:i];
                    NSString* layerName=[animationGo getLayerName];
                    DatasetVector* dataset=(DatasetVector*)[mapControl.map.layers getLayerWithName:layerName].dataset;
                    QueryParameter* queryParameter=[[QueryParameter alloc] init];
                    [queryParameter setQueryIDs:[[NSArray alloc]initWithObjects:@([animationGo getGeometry]), nil]];
                    [queryParameter setQueryType:IDS];
                    Recordset* recordset=[dataset query:queryParameter];
                    Geometry* geometry=[recordset geometry];
                    if(geometry){
                        if(i==0){
                            rectangle2D=[[geometry getBounds] clone];
                        }else{
                            Rectangle2D* bounds=[geometry getBounds];
                            if(bounds.left<rectangle2D.left){
                                [rectangle2D setLeft:bounds.left];
                            }
                            if(bounds.right>rectangle2D.right){
                                [rectangle2D setRight:bounds.right];
                            }
                            if(bounds.bottom<rectangle2D.bottom){
                                [rectangle2D setBottom:bounds.bottom];
                            }
                            if(bounds.top>rectangle2D.top){
                                [rectangle2D setTop:bounds.top];
                            }
                        }
                        //                        [rectangle2D unions:[geometry getBounds]];    组件接口方式有问题left和bottom一直为-1.7976931348623157E+308值不变
                    }
                }
                double offsetX=(rectangle2D.right-rectangle2D.left)/6;
                double offsetY=(rectangle2D.top-rectangle2D.bottom)/6;
                [rectangle2D setLeft:rectangle2D.left-offsetX];
                [rectangle2D setRight:rectangle2D.right+offsetX];
                [rectangle2D setBottom:rectangle2D.bottom-offsetY*1.5];
                [rectangle2D setTop:rectangle2D.top+offsetY*0.5];
                
                [mapControl.map setViewBounds:rectangle2D];
            }
        }
        //        double scale = mapControl.map.scale ;
        //        mapControl.map.scale += 0.1;
        //        [mapControl.map refresh];
        //        mapControl.map.scale = scale;
        [mapControl.map refresh];
        //        [mapControl zoomTo:mapControl.map.scale*0.95 time:100];
        //        [mapControl.map refresh];
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            [mapControl.map refresh];
            [[AnimationManager getInstance] play];
        });
        
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 暂停态势推演动画
RCT_REMAP_METHOD(animationPause,animationPause:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[AnimationManager getInstance] pause];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 复位态势推演动画
RCT_REMAP_METHOD(animationReset,animationReset:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[AnimationManager getInstance] reset];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 停止态势推演动画
RCT_REMAP_METHOD(animationStop,animationStop:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[AnimationManager getInstance] stop];
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 关闭态势推演动画
RCT_REMAP_METHOD(animationClose,animationClose:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        [[AnimationManager getInstance] stop];
        [[AnimationManager getInstance] reset];
        [[AnimationManager getInstance] deleteAll];
        //        [[AnimationManager getInstance] deleteAnimationManager];
        if(_timer){
            dispatch_source_cancel(_timer);
            _timer = nil;
        }
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 创建推演动画对象
RCT_REMAP_METHOD(createAnimationGo,createAnimationGo:(NSDictionary *)createInfo newPlotMapName:(NSString*)newPlotMapName  resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        if (![createInfo objectForKey:@"animationMode"]) {
            resolve(@(NO));
            return;
        }
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        if(count==0){
            animationGroup=[AnimationManager.getInstance addAnimationGroup:animationGroupName];
        }else{
            animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        }
        
        NSNumber* animationMode=[createInfo objectForKey:@"animationMode"];
        AnimationType type;
        switch (animationMode.integerValue) {
            case 0:
                type=WayAnimation;
                break;
            case 1:
                type=BlinkAnimation;
                break;
            case 2:
                type=AttribAnimation;
                break;
            case 3:
                type=ShowAnimation;
                break;
            case 4:
                type=RotateAnimation;
                break;
            case 5:
                type=ScaleAnimation;
                break;
            case 6:
                type=GrowAnimation;
                break;
        }
        AnimationGO* animationGo=[AnimationManager.getInstance createAnimation:type];
        
        if(type==WayAnimation){
            AnimationWay* animationWay=(AnimationWay*)animationGo;
            Point3Ds* point3Ds=[[Point3Ds alloc] init];
            if ([createInfo objectForKey:@"wayPoints"]) {
                NSMutableArray* array=[createInfo objectForKey:@"wayPoints"];
                for(int i=0;i<array.count;i++){
                    NSDictionary* map=[array objectAtIndex:i];
                    double x=[[map objectForKey:@"x"] doubleValue];
                    double y=[[map objectForKey:@"y"] doubleValue];
                    if ([mapControl.map.prjCoordSys type] != PCST_EARTH_LONGITUDE_LATITUDE) {//若投影坐标不是经纬度坐标则进行转换
                        Point2Ds *points = [[Point2Ds alloc]init];
                        [points add:[[Point2D alloc]initWithX:x Y:y]];
                        PrjCoordSys *srcPrjCoorSys = [[PrjCoordSys alloc]init];
                        [srcPrjCoorSys setType:PCST_EARTH_LONGITUDE_LATITUDE];
                        CoordSysTransParameter *param = [[CoordSysTransParameter alloc]init];
                        
                        //根据源投影坐标系与目标投影坐标系对坐标点串进行投影转换，结果将直接改变源坐标点串
                        [CoordSysTranslator convert:points PrjCoordSys:[mapControl.map prjCoordSys]  PrjCoordSys:srcPrjCoorSys CoordSysTransParameter:param CoordSysTransMethod:(CoordSysTransMethod)9603];
                        x = [points getItem:0].x;
                        y = [points getItem:0].y;
                    }
                    Point3D point3D = {x,y,0};
                    [point3Ds addPoint3D:point3D];
                    [animationWay addPathPt:point3D];
                    //                    [animationWay insertPathPt:0 pt:point3D];
                }
            }
            [animationWay setTrackLineWidth:0.5];
            [animationWay setPathType:0];
            [animationWay setTrackLineColor:[[Color alloc] initWithR:255 G:0 B:0]];
            [animationWay setPathTrackDir:YES];
            animationWay.showPathTrack=YES;
            animationGo=animationWay;
        }else if(type==BlinkAnimation){
            AnimationBlink* animationBlink=(AnimationBlink*)animationGo;
            [animationBlink setBlinkNumberofTimes:20];
            [animationBlink setBlinkStyle:1];
            [animationBlink setBlinkAnimationReplaceStyle:1];
            [animationBlink setBlinkAnimationReplaceColor:[[Color alloc] initWithR:0 G:0 B:255]];
            animationGo=(AnimationGO*)animationBlink;
        }else if(type==AttribAnimation){
            AnimationAttribute* animationAttribute=(AnimationAttribute*)animationGo;
            [animationAttribute setStartLineColor:[[Color alloc] initWithR:255 G:0 B:0]];
            [animationAttribute setEndLineColor:[[Color alloc] initWithR:0 G:0 B:255]];
            [animationAttribute setLineColorAttr:YES];
            [animationAttribute setStartLineWidth:0];
            [animationAttribute setEndLineWidth:1];
            [animationAttribute setLineWidthAttr:YES];
            animationGo=(AnimationGO*)animationAttribute;
        }else if(type==ShowAnimation){
            AnimationShow* animationShow=(AnimationShow*)animationGo;
            [animationShow setShowEffect:NO];
            [animationShow setShowState:YES];
            animationGo=(AnimationGO*)animationShow;
        }else if(type==RotateAnimation){
            Point3D startPnt = {0,0,0};
            Point3D endPnt = {720,720,0};
            AnimationRotate* animationRotate=(AnimationRotate*)animationGo;
            [animationRotate setStartangle:startPnt];
            [animationRotate setEndAngle:endPnt];
            animationGo=(AnimationGO*)animationRotate;
            
        }else if(type==ScaleAnimation){
            AnimationScale* animationScale=(AnimationScale*)animationGo;
            [animationScale setStartScaleFactor:0];
            [animationScale setEndScaleFactor:1];
            animationGo=(AnimationGO*)animationScale;
        }else if(type==GrowAnimation){
            //默认是从0生成到1
        }
        //清空创建路径动画时的数据
        [mapControl.map.trackingLayer clear];
        animationWayPoint2Ds=nil;
        animationWaySavePoint2Ds=nil;
        
        if([createInfo objectForKey:@"startTime"]&&[animationGroup getAnimationCount]>0){
            NSNumber* startTimeNumber=[createInfo objectForKey:@"startTime"];
            double startTime=[startTimeNumber doubleValue];
            if([createInfo objectForKey:@"startMode"]){
                NSNumber* startMode=[createInfo objectForKey:@"startMode"];
                AnimationGO* lastAnimationGO=[animationGroup getAnimationByIndex:([animationGroup getAnimationCount]-1)];
                switch ([startMode intValue]) {
                    case 1:
                        startTime+=lastAnimationGO.startTime+lastAnimationGO.duration;
                        break;
                    case 2:
                        break;
                    case 3:
                        startTime+=lastAnimationGO.startTime;
                        break;
                    default:
                        break;
                }
            }
            [animationGo setStartTime:startTime];
        }else if([createInfo objectForKey:@"startTime"]&&[animationGroup getAnimationCount]==0){
            NSNumber* startTimeNumber=[createInfo objectForKey:@"startTime"];
            double startTime=[startTimeNumber doubleValue];
            [animationGo setStartTime:startTime];
        }
        if([createInfo objectForKey:@"durationTime"]){
            NSNumber* durationTimeNumber=[createInfo objectForKey:@"durationTime"];
            double durationTime=[durationTimeNumber doubleValue];
            [animationGo setDuration:durationTime];
        }
        
        NSString* mapName=mapControl.map.name;
        if(!mapName||[mapName isEqualToString:@""]){
            if(newPlotMapName&&![newPlotMapName isEqualToString:@""]){
                mapName=newPlotMapName;
            }else{
                int layerCount=[mapControl.map.layers getCount];
                if(layerCount>0){
                    mapName=[mapControl.map.layers getLayerAtIndex:layerCount-1].name;
                }
            }
            [mapControl.map save:mapName];
        }
        
        NSString* animationGoName=[NSString stringWithFormat:@"动画_%d",[[AnimationManager.getInstance getGroupByName:animationGroupName] getAnimationCount]];
        if([createInfo objectForKey:@"layerName"]&&[createInfo objectForKey:@"geoId"]){
            NSString* layerName=[createInfo objectForKey:@"layerName"];
            int geoId=[[createInfo objectForKey:@"geoId"] intValue];
            Layer* layer=[mapControl.map.layers getLayerWithName:layerName];
            if(layer){
                DatasetVector* dataset=(DatasetVector*)[mapControl.map.layers getLayerWithName:layerName].dataset;
                QueryParameter* queryParameter=[[QueryParameter alloc] init];
                [queryParameter setQueryIDs:[[NSArray alloc]initWithObjects:@(geoId), nil]];
                [queryParameter setQueryType:IDS];
                Recordset* recordset=[dataset query:queryParameter];
                Geometry* geometry=[recordset geometry];
                if(geometry){
                    [animationGo setName:animationGoName];
                    [animationGo setGeomtry:geometry mapControl:mapControl layer:layer.name];
                    [animationGroup addAnimation:animationGo];
                }
            }
        }
        resolve(@(YES));
    } @catch (NSException *exception) {
        reject(@"setDynamicProjection", exception.reason, nil);
    }
}

#pragma mark 保存推演动画
RCT_REMAP_METHOD(animationSave,animationSave:(NSString*) savePath fileName:(NSString*)fileName resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        if(![[NSFileManager defaultManager] fileExistsAtPath:savePath]){
            [[NSFileManager defaultManager] createDirectoryAtPath:savePath withIntermediateDirectories:YES attributes:nil error:nil];
        }
        //        NSString* mapName=mapControl.map.name;
        NSString* tempPath=[NSString stringWithFormat:@"%@/%@.xml",savePath,fileName];
        NSString* path=[FileUtils formateNoneExistFileName:tempPath isDir:false];
        BOOL result=[AnimationManager.getInstance saveAnimationToXML:path];
        [AnimationManager.getInstance reset];
        [AnimationManager.getInstance deleteAll];
        
        resolve(@(result));
    } @catch (NSException *exception) {
        reject(@"animationSave", exception.reason, nil);
    }
}

#pragma mark 获取标号对象type
RCT_REMAP_METHOD(getGeometryTypeById,getGeometryTypeById:(NSString*) layerName geoId:(int)geoId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        int type=-1;
        Layer* layer=[mapControl.map.layers getLayerWithName:layerName];
        if(layer){
            DatasetVector* dataset=(DatasetVector*)[mapControl.map.layers getLayerWithName:layerName].dataset;
            QueryParameter* queryParameter=[[QueryParameter alloc] init];
            [queryParameter setQueryIDs:[[NSArray alloc]initWithObjects:@(geoId), nil]];
            [queryParameter setQueryType:IDS];
            Recordset* recordset=[dataset query:queryParameter];
            Geometry* geometry=[recordset geometry];
            if(geometry){
                GeoGraphicObject* geoGraphicObject=(GeoGraphicObject*)geometry;
                type=[geoGraphicObject getSymbolType];
            }
        }
        resolve(@(type));
    } @catch (NSException *exception) {
        resolve(@(-1));
        //        reject(@"getGeometryTypeById", exception.reason, nil);
    }
}

#pragma mark 添加路径动画点获取回退路径动画点
RCT_REMAP_METHOD(addAnimationWayPoint,addAnimationWayPoint:(NSDictionary*)point isAdd:(BOOL)isAdd resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        if(!isAdd){
            if(!animationWayPoint2Ds||[animationWayPoint2Ds getCount]==0){
                resolve([NSNumber numberWithBool:NO]);
                return;
            }else{
                [animationWayPoint2Ds remove:[animationWayPoint2Ds getCount]-1];
            }
        }else{
            int x=[[point objectForKey:@"x"] intValue];
            int y=[[point objectForKey:@"y"] intValue];
            CGPoint point1=CGPointMake(x, y);
            Point2D* point2D=[mapControl.map pixelTomap:point1];
            if(!animationWayPoint2Ds){
                animationWayPoint2Ds=[[Point2Ds alloc] init];
            }
            [animationWayPoint2Ds add:point2D];
        }
        GeoStyle* style=[[GeoStyle alloc] init];
        [style setMarkerSize:[[Size2D alloc] initWithWidth:10 Height:10]];
        [style setLineColor:[[Color alloc] initWithR:225 G:105 B:0]];
        //        [style setMarkerID:@"3614"];
        {
            [mapControl.map.trackingLayer clear];
            if([animationWayPoint2Ds getCount]==1){
                GeoPoint* geoPoint=[[GeoPoint alloc] initWithPoint2D:[animationWayPoint2Ds getItem:0]];
                [geoPoint setStyle:style];
                [mapControl.map.trackingLayer addGeometry:geoPoint WithTag:@"point"];
            }else if([animationWayPoint2Ds getCount]>1){
                GeoLine* geoline=[[GeoLine alloc] initWithPoint2Ds:animationWayPoint2Ds];
                [geoline setStyle:style];
                [mapControl.map.trackingLayer addGeometry:geoline WithTag:@"line"];
            }
            [mapControl.map refresh];
        }
        
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"addAnimationWayPoint", exception.reason, nil);
    }
}

#pragma mark 刷新路径动画点
RCT_REMAP_METHOD(refreshAnimationWayPoint,refreshAnimationWayPoint:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        if(!animationWaySavePoint2Ds||([animationWaySavePoint2Ds getCount]==0)){
            animationWayPoint2Ds=nil;
            [mapControl.map.trackingLayer clear];
            resolve([NSNumber numberWithBool:YES]);
            return;
        }
        animationWayPoint2Ds=[[Point2Ds alloc] initWithPoint2Ds:animationWaySavePoint2Ds];
        
        GeoStyle* style=[[GeoStyle alloc] init];
        [style setMarkerSize:[[Size2D alloc] initWithWidth:10 Height:10]];
        [style setLineColor:[[Color alloc] initWithR:225 G:105 B:0]];
        //        [style setMarkerID:@"3614"];
        {
            if([animationWayPoint2Ds getCount]==0){
                [mapControl.map.trackingLayer clear];
            }
            else if([animationWayPoint2Ds getCount]==1){
                [mapControl.map.trackingLayer clear];
                GeoPoint* geoPoint=[[GeoPoint alloc] initWithPoint2D:[animationWayPoint2Ds getItem:0]];
                [geoPoint setStyle:style];
                [mapControl.map.trackingLayer addGeometry:geoPoint WithTag:@"point"];
            }else if([animationWayPoint2Ds getCount]>1){
                [mapControl.map.trackingLayer clear];
                GeoLine* geoline=[[GeoLine alloc] initWithPoint2Ds:animationWayPoint2Ds];
                [geoline setStyle:style];
                [mapControl.map.trackingLayer addGeometry:geoline WithTag:@"line"];
            }
            [mapControl.map refresh];
        }
        
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"refreshAnimationWayPoint", exception.reason, nil);
    }
}

#pragma mark 取消路径动画，清除点
RCT_REMAP_METHOD(cancelAnimationWayPoint,cancelAnimationWayPoint:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        [mapControl.map.trackingLayer clear];
        animationWayPoint2Ds=nil;
        animationWaySavePoint2Ds=nil;
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"cancelAnimationWayPoint", exception.reason, nil);
    }
}

#pragma mark 结束添加路径动画
RCT_REMAP_METHOD(endAnimationWayPoint,endAnimationWayPoint:(BOOL)isSave resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        if(!isSave){
            [AnimationManager.getInstance deleteAll];
            [mapControl.map.trackingLayer clear];
            animationWayPoint2Ds=nil;
            animationWaySavePoint2Ds=nil;
            resolve([NSNumber numberWithBool:YES]);
            return;
        }
        NSMutableArray* arr=[[NSMutableArray alloc] init];
        if([animationWayPoint2Ds getCount]>0){
            for(int i=0;i<[animationWayPoint2Ds getCount];i++){
                NSDictionary* map=@{
                                    @"x":[NSNumber numberWithDouble:[animationWayPoint2Ds getItem:i].x],
                                    @"y":[NSNumber numberWithDouble:[animationWayPoint2Ds getItem:i].y],
                                    };
                [arr addObject:map];
            }
            animationWaySavePoint2Ds=[[Point2Ds alloc] initWithPoint2Ds:animationWayPoint2Ds];
        }
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"endAnimationWayPoint", exception.reason, nil);
    }
}

#pragma mark 根据geoId获取已经创建的动画类型和数量
RCT_REMAP_METHOD(getGeoAnimationTypes,getGeoAnimationTypes:(int)geoId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        NSMutableArray* arr=[[NSMutableArray alloc] init];
        for (int x=0; x<7; x++) {
            [arr addObject:@(0)];
        }
        
        //        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        if(count==0){
            resolve(arr);
            return;
        }else{
            animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        }
        int size=[animationGroup getAnimationCount];
        for (int i=0; i<size; i++) {
            AnimationGO* animationGo=[animationGroup getAnimationByIndex:i];
            int id=[animationGo getGeometry];
            if(id==geoId){
                int type=[animationGo getAnimationType];
                int typeCount=[arr[type] intValue]+1;
                [arr replaceObjectAtIndex:type withObject:@(typeCount)];
            }
        }
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"getGeoAnimationTypes", exception.reason, nil);
    }
}

#pragma mark 获取所有动画节点数据
RCT_REMAP_METHOD(getAnimationNodeList,getAnimationNodeList:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        NSMutableArray* arr=[[NSMutableArray alloc] init];
        
        //        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        if(count==0){
            resolve(arr);
            return;
        }else{
            animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        }
        int size=[animationGroup getAnimationCount];
        for (int i=0; i<size; i++) {
            AnimationGO* animationGo=[animationGroup getAnimationByIndex:i];
            NSDictionary* map=@{
                                @"index":[NSNumber numberWithInt:i],
                                @"name":animationGo.name
                                };
            [arr addObject:map];
        }
        resolve(arr);
    } @catch (NSException *exception) {
        reject(@"getAnimationNodeList", exception.reason, nil);
    }
}

#pragma mark 删除动画节点
RCT_REMAP_METHOD(deleteAnimationNode,deleteAnimationNode:(NSString*)nodeName resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        
        //        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        if(count==0){
            resolve([NSNumber numberWithBool:NO]);
            return;
        }else{
            animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        }
        BOOL result=[animationGroup deleteAnimationByName:nodeName];
        resolve([NSNumber numberWithBool:result]);
    } @catch (NSException *exception) {
        reject(@"deleteAnimationNode", exception.reason, nil);
    }
}

#pragma mark 修改动画节点名称
RCT_REMAP_METHOD(modifyAnimationNodeName,modifyAnimationNodeName:(int)index withNewName:(NSString*)newNodeName resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        //        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        if(count==0){
            resolve([NSNumber numberWithBool:NO]);
            return;
        }else{
            animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        }
        AnimationGO* animationGo=[animationGroup getAnimationByIndex:index];
        [animationGo setName:newNodeName];
        
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"getAnimationNodeList", exception.reason, nil);
    }
}

#pragma mark 移动节点位置
RCT_REMAP_METHOD(moveAnimationNode,moveAnimationNode:(int)index isUp:(BOOL)isUp resolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        
        sMap = [SMap singletonInstance];
        //        MapControl* mapControl=sMap.smMapWC.mapControl;
        
        NSString* animationGroupName=@"Create_Animation_Instance_#";   //动画动画组名，名称特殊，保证唯一
        int count=[AnimationManager.getInstance getGroupCount];
        //组件缺陷，group的count等于0调用getGroupByName还是能返回一个对象
        //        AnimationGroup* animationGroup=[AnimationManager.getInstance getGroupByName:animationGroupName];
        AnimationGroup* animationGroup;
        AnimationGroup* tempGroup;
        if(count==0){
            resolve([NSNumber numberWithBool:NO]);
            return;
        }
        animationGroup=[AnimationManager.getInstance getGroupByIndex:0];
        int size=[animationGroup getAnimationCount];
        if((isUp&&index==0)||(!isUp&&index==size-1)){
            resolve([NSNumber numberWithBool:NO]);
            return;
        }
        AnimationGO* tempAnimationGo;
        int tempIndex=isUp?index-1:index;
        
        NSString* tempGroupName=@"temp";
        tempGroup=[AnimationManager.getInstance addAnimationGroup:tempGroupName];
        for (int i=0; i<size; i++) {
            AnimationGO* animationGo=[animationGroup getAnimationByIndex:i];
            if(tempIndex==i){
                NSString* xmlStr=[animationGo toXML];
                tempAnimationGo=[AnimationManager.getInstance createAnimation:animationGo.getAnimationType];
                [tempAnimationGo fromXML:xmlStr];
                
                NSString* xmlStr2=[[animationGroup getAnimationByIndex:i+1] toXML];
                AnimationGO* animationGo2=[AnimationManager.getInstance createAnimation:[animationGroup getAnimationByIndex:i+1].getAnimationType];
                [animationGo2 fromXML:xmlStr2];
                
                [tempGroup addAnimation:animationGo2];
                [tempGroup addAnimation:tempAnimationGo];
                i++;
            }
            else{
                AnimationGO* animationGo2=[AnimationManager.getInstance createAnimation:animationGo.getAnimationType];
                NSString* xmlStr=[animationGo toXML];
                [animationGo2 fromXML:xmlStr];
                [tempGroup addAnimation:animationGo2];
            }
        }
        
        //        NSString *tempAnimationDic = [NSString stringWithFormat: @"%@%@",NSHomeDirectory(),@"/Documents/iTablet/Cache"];
        //        NSString *tempAnimationXmlPath = [NSString stringWithFormat: @"%@%@",tempAnimationDic,@"/tempAnimation.xml"];
        //        NSFileManager *fileManager = [NSFileManager defaultManager];
        //        BOOL isDir = FALSE;
        //        BOOL isDirExist = [fileManager fileExistsAtPath:tempAnimationDic isDirectory:&isDir];
        //        if(!(isDirExist && isDir))
        //        {
        //            [fileManager createDirectoryAtPath:tempAnimationDic withIntermediateDirectories:YES attributes:nil error:nil];
        //        }
        //        [AnimationManager.getInstance saveAnimationToXML:tempAnimationXmlPath];
        //        [AnimationManager.getInstance deleteAll];   //这个地方报错，不知道什么原因
        //        isDirExist = [fileManager fileExistsAtPath:tempAnimationXmlPath isDirectory:&isDir];
        //        if(isDirExist&&!isDir){
        //            [AnimationManager.getInstance getAnimationFromXML:tempAnimationXmlPath];
        //            int groupCount=[AnimationManager.getInstance getGroupCount];
        //            if(groupCount==2){
        //                [AnimationManager.getInstance deleteGroupByName:animationGroupName];
        //                [[AnimationManager.getInstance getGroupByIndex:0] setName:animationGroupName];
        //            }
        //            [fileManager removeItemAtPath:tempAnimationXmlPath error:nil];
        //        }
        
        [[AnimationManager.getInstance getGroupByIndex:0] setName:animationGroupName];
        [AnimationManager.getInstance deleteGroupByName:animationGroupName];
        [[AnimationManager.getInstance getGroupByIndex:0] setName:animationGroupName];
        
        
        resolve([NSNumber numberWithBool:YES]);
    } @catch (NSException *exception) {
        reject(@"getAnimationNodeList", exception.reason, nil);
    }
}


@end