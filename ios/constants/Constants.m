//
//  Constants.m
//  Supermap
//
//  Created by Yang Shang Long on 2018/10/30.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "Constants.h"

/** 采集 **/
NSString * const COLLECTION_SENSOR_CHANGE = @"com.supermap.RN.Mapcontrol.collection_sensor_change";

/** 在线服务 **/

//NSString * const ONLINE_SERVICE_REVERSEGEOCODING = @"com.supermap.RN.Mapcontrol.online_service_reversegeocoding";
NSString * const ONLINE_SERVICE_REVERSEGEOCODING = @"com.supermap.RN.Mapcontrol.online_service_reversegeocoding";
NSString * const ONLINE_SERVICE_LOGIN = @"com.supermap.RN.Mapcontrol.online_service_login";
NSString * const ONLINE_SERVICE_LOGOUT = @"com.supermap.RN.Mapcontrol.online_service_logout";
NSString * const ONLINE_SERVICE_UPLOADING = @"com.supermap.RN.Mapcontrol.online_service_uploading";
NSString * const ONLINE_SERVICE_UPLOADED = @"com.supermap.RN.Mapcontrol.online_service_uploaded";
NSString * const ONLINE_SERVICE_UPLOADFAILURE = @"com.supermap.RN.Mapcontrol.online_service_uploadfailure";


//下载回调
NSString * const ONLINE_SERVICE_DOWNLOADING = @"com.supermap.RN.Mapcontrol.online_service_downloading";
NSString * const ONLINE_SERVICE_DOWNLOADED = @"com.supermap.RN.Mapcontrol.online_service_downloaded";
NSString * const ONLINE_SERVICE_DOWNLOADFAILURE = @"com.supermap.RN.Mapcontrol.online_service_downloadfailure";


/** 消息服务 **/
NSString * const MESSAGE_SERVICE_RECEIVE = @"com.supermap.RN.Mapcontrol.message_service_receive";
NSString * const MESSAGE_SERVICE_SEND_FILE = @"com.supermap.RN.MessageService.send_file_progress";
NSString * const MESSAGE_SERVICE_RECEIVE_FILE = @"com.supermap.RN.MessageService.receive_file_progress";

/** 量算 **/
NSString * const MEASURE_LENGTH = @"com.supermap.RN.Mapcontrol.length_measured";
NSString * const MEASURE_AREA = @"com.supermap.RN.Mapcontrol.area_measured";
NSString * const MEASURE_ANGLE = @"com.supermap.RN.Mapcontrol.angle_measured";

/** 3D **/
NSString * const ANALYST_MEASURELINE = @"com.supermap.RN.SMSceneControl.Analyst_measureLine";
NSString * const ANALYST_MEASURESQUARE = @"com.supermap.RN.SMSceneControl.Analyst_measureSquare";
NSString * const POINTSEARCH_KEYWORDS = @"com.supermap.RN.SMSceneControl.PointSearch_keyWords";
NSString * const SSCENE_FLY = @"com.supermap.RN.SMSceneControl.Scene_fly";
NSString * const SSCENE_ATTRIBUTE = @"com.supermap.RN.SMSceneControl.Scene_attribute";
NSString * const SSCENE_SYMBOL = @"com.supermap.RN.SMSceneControl.Scene_symbol";
NSString * const SSCENE_CIRCLEFLY = @"com.supermap.RN.SMSceneControl.Scene_circleFly";
NSString * const SSCENE_FAVORITE = @"com.supermap.RN.SMSceneControl.Scene_favorite";

/** 地图 **/
NSString * const MAP_LONG_PRESS = @"com.supermap.RN.Mapcontrol.long_press_event";
NSString * const MAP_SINGLE_TAP = @"com.supermap.RN.Mapcontrol.single_tap_event";
NSString * const MAP_DOUBLE_TAP = @"com.supermap.RN.Mapcontrol.double_tap_event";
NSString * const MAP_TOUCH_BEGAN = @"com.supermap.RN.Mapcontrol.touch_began_event";
NSString * const MAP_TOUCH_END = @"com.supermap.RN.Mapcontrol.touch_end_event";
NSString * const MAP_SCROLL = @"com.supermap.RN.Mapcontrol.scroll_event";

NSString * const MAP_GEOMETRY_MULTI_SELECTED = @"com.supermap.RN.Mapcontrol.geometry_multi_selected";
NSString * const MAP_GEOMETRY_SELECTED = @"com.supermap.RN.Mapcontrol.geometry_selected";
NSString * const MAP_SCALE_CHANGED = @"Supermap.MapControl.MapParamChanged.ScaleChanged";
NSString * const MAP_BOUNDS_CHANGED = @"Supermap.MapControl.MapParamChanged.BoundsChanged";

/** 比例尺改变 RN显示比例尺专用 **/
NSString * const MAP_SCALEVIEW_CHANGED = @"com.supermap.RN.Map.ScaleView.scaleView_change";
/** 多媒体采集 **/
NSString * const MEDIA_CAPTURE = @"com.supermap.RN.MediaCapture";

/** 多媒体采集，Callout点击回调 **/
NSString * const MEDIA_CAPTURE_TAP_ACTION = @"com.supermap.RN.MediaCaptureTapAction";

/** 分析 **/
NSString * const ONLINE_ANALYST_RESULT = @"com.supermap.RN.online_analyst_result";

@implementation Constants : NSObject

@end
