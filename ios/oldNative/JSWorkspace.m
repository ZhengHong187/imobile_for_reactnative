//
//  JSWorkspace.m
//  iMobileRnIos
//
//  Created by imobile-xzy on 16/5/12.
//  Copyright © 2016年 Facebook. All rights reserved.
//

#import "JSObjManager.h"
#import "JSWorkspace.h"
#import "SuperMap/Workspace.h"
#import "SuperMap/WorkspaceConnectionInfo.h"
#import "SuperMap/Datasources.h"
#import "SuperMap/Datasource.h"
#import "SuperMap/DatasourceConnectionInfo.h"
#import "SuperMap/Rectangle2D.h"
#import "SuperMap/Maps.h"
#import "SuperMap/Scenes.h"
#import "SuperMap/WorkspaceType.h"
#import "JSWorkspaceConnectionInfo.h"
#import "JSDatasourceConnectionInfo.h"
#import "JSDatasources.h"
#import "JSMaps.h"
#import "SMap.h"

@implementation JSWorkspace
@synthesize bridge = _bridge;
//注册为Native模块
RCT_EXPORT_MODULE();



#pragma mark - Public APIs

RCT_REMAP_METHOD(createObj,resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  Workspace* workspace = [[Workspace alloc]init];
  NSLog(@"__________________%@__________________",NSHomeDirectory());
  if(workspace){
     NSInteger key = (NSInteger)workspace;
    [JSObjManager addObj:workspace];
    resolve(@{@"workspaceId":@(key).stringValue});
  }else{
    reject(@"WorkSpaceInfo",@"workspace create workSaceInfo failed!!!",nil);
  }
}
RCT_REMAP_METHOD(destroyObj,destroyJSObjKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  Workspace* workspace = [JSObjManager getObjWithKey:key];
  if(workspace){
    [workspace close];
    [workspace dispose];
    [JSObjManager removeObj:key];
    resolve(@"1");
  }else{
    reject(@"workspace",@"workspace destroy obj failed!!!",nil);
  }
}

RCT_REMAP_METHOD(getDatasources,getDatasourcesKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    if(workspace){
        Datasources* dataSource = workspace.datasources;
        [JSObjManager addObj:dataSource];
        NSInteger nsDataSource = (NSInteger)dataSource;
        resolve(@{@"datasourcesId":@(nsDataSource).stringValue});
    }else{
        reject(@"workspace",@"workspace not exeist!!!",nil);
    }
}

#pragma mark - 原datasources类方法
/*
RCT_REMAP_METHOD(openDatasourceConnectionInfo, openDatasourceConnectionInfoByKey:(NSString*)key datasourceConnectionInfo:(NSString*)info resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  Workspace* workspace = [JSObjManager getObjWithKey:key];
  DatasourceConnectionInfo* infoObj = [JSObjManager getObjWithKey:info];
  if(workspace&&infoObj){
      Datasource* datasource = [workspace.datasources open:infoObj];
      [JSObjManager addObj:datasource];
      NSInteger nsDatasource = (NSInteger)datasource;
      resolve(@{@"datasourceId":@(nsDatasource).stringValue});
  }else{
      reject(@"workspace",@"open DatasourceConnectionInfo failed!!!",nil);
  }
}
*/
RCT_REMAP_METHOD(getDatasource, getDatasourceByKey:(NSString*)key andIndex:(int)index resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    if(workspace){
        Datasource* datasource = [workspace.datasources get:index];
        [JSObjManager addObj:datasource];
        NSInteger nsDatasource = (NSInteger)datasource;
        resolve(@{@"datasourceId":@(nsDatasource).stringValue});
    }else{
        reject(@"workspace",@"workspace get Datasource failed!!!",nil);
    }
}

RCT_REMAP_METHOD(getDatasourceByName, getDatasourceByKey:(NSString*)key andName:(NSString*)name resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    if(workspace){
        Datasource* datasource = [workspace.datasources getAlias:name];
        [JSObjManager addObj:datasource];
        NSInteger nsDatasource = (NSInteger)datasource;
        resolve(@{@"datasourceId":@(nsDatasource).stringValue});
    }else{
        reject(@"workspace",@"workspace get Datasource failed!!!",nil);
    }
}

#pragma mark - workspace类方法

RCT_REMAP_METHOD(open,openBykey:(NSString*)key andWorkspaceConnectionInfoId:(NSString*)infoId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    WorkspaceConnectionInfo* info = [JSObjManager getObjWithKey:infoId];
    if(workspace&&info){
        BOOL openBit = [workspace open:info];
        if(openBit){
            
            resolve(@{@"isOpen":@(YES)});
        }else{
            reject(@"workspace",@"workspace open failed!!!",nil);
        }
    }else{
        reject(@"workspace",@"workspace open failed!!!",nil);
    }
}

RCT_REMAP_METHOD(getMaps,geMapsByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    if(workspace){
        Maps* maps = workspace.maps;
        [JSObjManager addObj:maps];
        NSInteger nsMaps = (NSInteger)maps;
        resolve(@{@"mapsId":@(nsMaps).stringValue});
    }else{
        reject(@"workspace",@"workspace get maps failed!",nil);
    }
}

#pragma mark - maps类方法
RCT_REMAP_METHOD(getMapName,getMapNameByKey:(NSString*)key andMapIndex:(int)index resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        if(workspace){
            Maps* maps = workspace.maps;
            NSString* mapName = [maps get:index];
            resolve(@{@"mapName":mapName});
        }
    } @catch (NSException *exception) {
        reject(@"workspace",@"workspace get mapName failed!",nil);
    }
}

RCT_REMAP_METHOD(renameDatasource,renameDatasourceKey:(NSString*)key oldName:(NSString*)oldName newName:(NSString*)newName resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        if(workspace){
            Datasources* ds = workspace.datasources;
            [ds RenameDatasource:oldName with:newName];
            resolve(@(1));
        }
    } @catch (NSException *exception) {
        reject(@"workspace",@"workspace renameDatasourcefailed!",nil);
    }
}

RCT_REMAP_METHOD(getDatasourcesCount, getDatasourcesCountKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        NSInteger count = workspace.datasources.count;
        NSNumber* countNum = [NSNumber numberWithInteger:count];
        resolve(countNum);
    } @catch (NSException *exception) {
        reject(@"workspace", exception.reason, nil);
    }
}

RCT_REMAP_METHOD(getDatasourceAlias, getDatasourceAliasKey:(NSString*)key index:(int)index resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        Datasource* datasource = [workspace.datasources get:index];
        NSString* alias = datasource.alias;
        resolve(alias);
    } @catch (NSException *exception) {
        reject(@"workspace", exception.reason, nil);
    }
}

/*
RCT_REMAP_METHOD(openLocalDatasource,openLocalDatasourceByKey:(NSString*)key andPath:(NSString*)path andEngineType:(int)type resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Datasources* dataSources = workspace.datasources;
    DatasourceConnectionInfo* info = [[DatasourceConnectionInfo alloc]init];
    NSString* firstStr = [path substringToIndex:1];
    if([firstStr isEqualToString:@"/"]){
    path = [NSHomeDirectory() stringByAppendingString:path];
    }
    if(workspace&&info){
        info.server = path;
        info.engineType = type;
        Datasource* dataSource = [dataSources open:info];
        NSInteger nsDSource = (NSInteger)dataSource;
        [JSObjManager addObj:dataSource];
        resolve(@{@"datasourceId":@(nsDSource).stringValue});
    }else{
        reject(@"workspace",@"open LocalDatasource failed!",nil);
    }
}

RCT_REMAP_METHOD(openDatasource,openDatasourceByKey:(NSString*)key andPath:(NSString*)path andEngineType:(int)type andDriverStr:(NSString*)driver resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Datasources* dataSources = workspace.datasources;
    DatasourceConnectionInfo* info = [[DatasourceConnectionInfo alloc]init];
    if(workspace&&info){
        info.server = path;
        info.engineType = type;
        info.driver = driver;
        Datasource* dataSource = [dataSources open:info];
        NSInteger nsDSource = (NSInteger)dataSource;
        [JSObjManager addObj:dataSource];
        resolve(@{@"datasourceId":@(nsDSource).stringValue});
    }else{
        reject(@"workspace",@"open LocalDatasource failed!",nil);
    }
}
*/

- (Datasource *)openDatasource:(NSDictionary*)params{
    @try{
        Workspace* workspace = [SMap singletonInstance].smMapWC.workspace;
        Datasources* dataSources = workspace.datasources;
        DatasourceConnectionInfo* info = [[DatasourceConnectionInfo alloc]init];
        if(params&&info){
            NSArray* keyArr = [params allKeys];
            BOOL bDefault = YES;
            if ([keyArr containsObject:@"alias"]){
                info.alias = [params objectForKey:@"alias"];
                bDefault = NO;
            }
            if ([keyArr containsObject:@"engineType"]){
                NSNumber* num = [params objectForKey:@"engineType"];
                long type = num.floatValue;
                info.engineType = (EngineType)type;
            }
            if ([keyArr containsObject:@"server"]){
                NSString* path = [params objectForKey:@"server"];
                info.server = path;
                if(bDefault){
                    info.alias = [[path lastPathComponent] stringByDeletingPathExtension];
                }
            }
            if([workspace.datasources indexOf:info.alias]!=-1){
                [workspace.datasources closeAlias:info.alias];
            }
            if ([keyArr containsObject:@"driver"]) info.driver = [params objectForKey:@"driver"];
            if ([keyArr containsObject:@"user"]) info.user = [params objectForKey:@"user"];
            if ([keyArr containsObject:@"readOnly"]) info.readOnly = ((NSNumber*)[params objectForKey:@"readOnly"]).boolValue;
            if ([keyArr containsObject:@"password"]) info.password = [params objectForKey:@"password"];
            if ([keyArr containsObject:@"webCoordinate"]) info.webCoordinate = [params objectForKey:@"webCoordinate"];
            if ([keyArr containsObject:@"webVersion"]) info.webVersion = [params objectForKey:@"webVersion"];
            if ([keyArr containsObject:@"webFormat"]) info.webFormat = [params objectForKey:@"webFormat"];
            if ([keyArr containsObject:@"webVisibleLayers"]) info.webVisibleLayers = [params objectForKey:@"webVisibleLayers"];
            if ([keyArr containsObject:@"webExtendParam"]) info.webExtendParam = [params objectForKey:@"webExtendParam"];
            if ([keyArr containsObject:@"webBBox"]){
                Rectangle2D* rect2d = [JSObjManager getObjWithKey:[params objectForKey:@"webBBox"]];
                info.webBBox = rect2d;
            }
            Datasource* dataSource = [dataSources open:info];
            return dataSource;
        }
    }@catch (NSException *exception) {
        @throw exception;
//        reject(@"workspace",@"open LocalDatasource failed!",nil);
    }
}
/*
RCT_REMAP_METHOD(openWMSDatasource,openDatasourceByKey:(NSString*)key andServer:(NSString*)server andEngineType:(int)type andDriverStr:(NSString*)driver andVersionStr:(NSString*)version andVisableLayers:(NSString*)vLayers andWebBox:(NSDictionary*)webBox andWebCoordinate:(NSString*)webCoordinate resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Datasources* dataSources = workspace.datasources;
    DatasourceConnectionInfo* info = [[DatasourceConnectionInfo alloc]init];
    if(workspace&&info){
        info.server = server;
        info.engineType = type;
        info.driver = driver;
        info.webVersion = version;
        info.webVisibleLayers = vLayers;
        
        NSNumber* nsBottom = [webBox objectForKey:@"bottom"];
        double bottom = nsBottom.doubleValue;
        NSNumber* nsLeft = [webBox objectForKey:@"left"];
        double left = nsLeft.doubleValue;
        NSNumber* nsRight = [webBox objectForKey:@"right"];
        double right = nsRight.doubleValue;
        NSNumber* nsTop = [webBox objectForKey:@"top"];
        double top = nsTop.doubleValue;
        
        Rectangle2D* rect2D = [[Rectangle2D alloc]initWith:left bottom:bottom right:right top:top];
        info.webBBox = rect2D;
        info.webCoordinate = webCoordinate;
        
        Datasource* dataSource = [dataSources open:info];
        NSInteger dsKey = (NSInteger)dataSource;
        [JSObjManager addObj:dataSource];
        resolve(@{@"datasourceId":@(dsKey).stringValue});
    }else{
        reject(@"workspace",@"open LocalDatasource failed!",nil);
    }
}
*/
RCT_REMAP_METHOD(saveWorkspaceWithServer,saveWorkspaceByKey:(NSString*)key server:(NSString*)server resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    if(workspace){
        WorkspaceConnectionInfo*info = workspace.connectionInfo;
        info.server = server;
        BOOL saved = [workspace save];
        NSNumber* nsSaved = [NSNumber numberWithBool:saved];
        resolve(@{@"saved":nsSaved});
    }else
        reject(@"workspace",@"save failed!!!",nil);
}

RCT_REMAP_METHOD(saveWorkspace,saveWorkspaceByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  Workspace* workspace = [JSObjManager getObjWithKey:key];
  if(workspace){
    BOOL saved = [workspace save];
      NSNumber* nsSaved = [NSNumber numberWithBool:saved];
      resolve(@{@"saved":nsSaved});
  }else
    reject(@"workspace",@"save failed!!!",nil);
}

RCT_REMAP_METHOD(saveWorkspaceWithInfo, saveWorkspaceWithInfoByKey:(NSString*)key path:(NSString *)path caption: (NSString *)caption type:(NSInteger)type resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        WorkspaceConnectionInfo* info = workspace.connectionInfo;
        
//        [info setServer:path];
        [workspace setCaption:caption];
        switch (type) {
            case 4:
                [info setType:SM_SXW];
                [info setServer:[NSString stringWithFormat:@"%@/%@%@", path, caption, @".sxw"]];
                break;
                
                // SMW 工作空间信息设置
            case 5:
                [info setType:SM_SMW];
                [info setServer:[NSString stringWithFormat:@"%@/%@%@", path, caption, @".smw"]];
                break;
                
                // SXWU 文件工作空间信息设置
            case 8:
                [info setType:SM_SXWU];
                [info setServer:[NSString stringWithFormat:@"%@/%@%@", path, caption, @".sxwu"]];
                break;
                
                // SMWU 工作空间信息设置
            case 9:
                [info setType:SM_SMWU];
                [info setServer:[NSString stringWithFormat:@"%@/%@%@", path, caption, @".smwu"]];
                break;
                
                // 其他情况
            default:
                [info setType:SM_SMWU];
                [info setServer:[NSString stringWithFormat:@"%@/%@%@", path, caption, @".smwu"]];
                break;
        }
        
        BOOL saved = [workspace save];
        NSNumber* nsSaved = [NSNumber numberWithBool:saved];
        resolve(@{@"saved":nsSaved});
    } @catch (NSException* exception) {
        reject(@"workspace", exception.reason, nil);
    }
}

RCT_REMAP_METHOD(closeWorkspace,closeWorkspaceByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        if(workspace){
            [workspace close];
            [workspace dispose];
            [JSObjManager removeObj:key];
            NSNumber* nsClosed = [NSNumber numberWithBool:TRUE];
            resolve(@{@"closed":nsClosed});
        }
    } @catch (NSException *exception) {
        reject(@"workspace",@"close failed!!!",nil);
    } 
}

RCT_REMAP_METHOD(dispose, disposeByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        if(workspace){
            [workspace dispose];
            [JSObjManager removeObj:key];
            NSNumber* nsClosed = [NSNumber numberWithBool:TRUE];
            resolve(nsClosed);
        }
    } @catch (NSException *exception) {
        reject(@"workspace",@"close failed!!!",nil);
    }
}

#pragma mark - 原datasources类放法
+(BOOL)createFileDirectories:(NSString*)path
{
    
    // 判断存放音频、视频的文件夹是否存在，不存在则创建对应文件夹
    NSString* DOCUMENTS_FOLDER_AUDIO = path;
    NSFileManager *fileManager = [NSFileManager defaultManager];
    
    BOOL isDir = FALSE;
    BOOL isDirExist = [fileManager fileExistsAtPath:DOCUMENTS_FOLDER_AUDIO isDirectory:&isDir];
    
    
    if(!(isDirExist && isDir)){
        BOOL bCreateDir = [fileManager createDirectoryAtPath:DOCUMENTS_FOLDER_AUDIO withIntermediateDirectories:YES attributes:nil error:nil];
        
        if(!bCreateDir){
            
            NSLog(@"Create Directory Failed.");
            return NO;
        }else
        {
            //  NSLog(@"%@",DOCUMENTS_FOLDER_AUDIO);
            return YES;
        }
    }
    
    return YES;
}
RCT_REMAP_METHOD(createDatasource,createDatasourceByKey:(NSString*)key  andFilePath:(NSString*)path andEngineType:(int)type  resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        Datasources* dataSources = workspace.datasources;
        if(dataSources){
            DatasourceConnectionInfo* info = [[DatasourceConnectionInfo alloc]init];
            
            [JSWorkspace createFileDirectories:[path stringByDeletingLastPathComponent]];
            info.server = path;
            info.engineType = type;
            info.alias = [[path lastPathComponent] stringByDeletingPathExtension];
            Datasource* dataSource = [dataSources create:info];
            [JSObjManager addObj:dataSource];
            NSInteger jsKey = (NSInteger)dataSource;
            resolve(@(jsKey).stringValue);
        }
    }@catch (NSException *exception) {
        
        reject(@"workspace",@"create Datasource failed!!!",nil);
    }
}

RCT_REMAP_METHOD(closeDatasource,closeDatasourceByKey:(NSString*)key andDatasourceName:(NSString*)name resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Datasources* dataSources = workspace.datasources;
    if(dataSources){
        BOOL closed = [dataSources closeAlias:name];
        NSNumber* nsClosed = [NSNumber numberWithBool:closed];
        resolve(@{@"closed":nsClosed});
    }else{
        reject(@"workspace",@"close Datasource failed!!!",nil);
    }
}

RCT_REMAP_METHOD(closeAllDatasource,closeAllDatasourceByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        Datasources* dataSources = workspace.datasources;
        [dataSources closeAll];
        resolve(@"1");
    } @catch (NSException *exception) {
        reject(@"workspace", exception.reason, nil);
    }
}
RCT_REMAP_METHOD(getConnectionInfo,getConnectionInfoKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        WorkspaceConnectionInfo* info = workspace.connectionInfo;
        NSString* key1 = [JSObjManager addObj:info];
        resolve(key1);
    } @catch (NSException *exception) {
        reject(@"workspace",@"workspace getConnectionInfo all Datasource failed!!!",nil);
    }

}
#pragma mark - 原maps类方法

RCT_REMAP_METHOD(removeMap,removeMapByKey:(NSString*)key andMapName:(NSString*)name resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Maps* maps = workspace.maps;
    if(maps){
        BOOL removed = [maps removeMapName:name];
        NSNumber* nsRemoved = [NSNumber numberWithBool:removed];
        resolve(@{@"removed":nsRemoved});
    }else{
        reject(@"workspace",@"remove map by name failed!!!",nil);
    }
}

RCT_REMAP_METHOD(clearMap,clearMapByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Maps* maps = workspace.maps;
    if(maps){
        [maps clear];
        resolve(@"cleared");
    }else{
        reject(@"workspace",@"clear map failed!!!",nil);
    }
}

RCT_REMAP_METHOD(getSceneName,getSceneNameByKey:(NSString*)key index:(int)index resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    Workspace* workspace = [JSObjManager getObjWithKey:key];
    Scenes* scenes = workspace.scenes;
    if(scenes){
        NSString* name = [scenes get:index];
        resolve(@{@"name":name});
    }else{
        reject(@"workspace",@"get SceneName failed!!!",nil);
    }
}

RCT_REMAP_METHOD(getSceneCount,getSceneCountByKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        Scenes* scenes = workspace.scenes;
        NSInteger count = scenes.count;
        resolve([NSNumber numberWithInteger:count]);
    } @catch (NSException *exception) {
        reject(@"workspace",@"get SceneName failed!!!",nil);
    }
}

RCT_REMAP_METHOD(addMap, addMapByKey:(NSString*)key name:(NSString *)name mapXML:(NSString *)mapXML resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
    @try {
        Workspace* workspace = [JSObjManager getObjWithKey:key];
        NSUInteger index = [workspace.maps add:name withXML:mapXML];
        
        resolve([NSNumber numberWithUnsignedInteger:index]);
    } @catch (NSException *exception) {
        reject(@"JSWorkspace getCount", exception.reason, nil);
    }
}

#pragma mark - ios
/*
RCT_REMAP_METHOD(dispose,disposeKey:(NSString*)key resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  
  Workspace* workspace = [JSObjManager getObjWithKey:key];
  if(workspace){
    [workspace dispose];
    resolve(@"1");
  }else{
    reject(@"workspace",@"save failed!!!",nil);
  }
}
 */
@end
