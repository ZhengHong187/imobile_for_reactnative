//
//  SMCollector.m
//  Supermap
//
//  Created by Yang Shang Long on 2018/10/30.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "SMCollector.h"
#import "SCollectorType.h"
#import "SuperMap/CollectorElement.h"
#import "SuperMap/Action.h"
#import "SuperMap/MapControl.h"

@implementation SMCollector

+ (BOOL)setCollector:(Collector *)collector mapControl:(MapControl *)mapControl type:(int)type {
    BOOL result = NO;
    switch (type) {
        case POINT_GPS: // POINT_GPS
            result = [collector createElement:COL_POINT];
            [collector setIsSingleTapEnable:NO];
            break;
        case POINT_HAND: // POINT_HAND
            result = [collector createElement:COL_POINT];
            [collector setIsSingleTapEnable:YES];
            break;
        case LINE_GPS_POINT: // LINE_GPS_POINT
            result = [collector createElement:COL_LINE];
            [collector setIsSingleTapEnable:NO];
            break;
        case LINE_GPS_PATH: // LINE_GPS_PATH
            result = [collector createElement:COL_LINE];
            [collector setIsSingleTapEnable:NO];
            break;
        case LINE_HAND_POINT: // LINE_HAND_POINT
            result = [collector createElement:COL_LINE];
            [collector setIsSingleTapEnable:YES];
            break;
        case LINE_HAND_PATH: // LINE_HAND_PATH
            [mapControl setAction:CREATE_FREE_DRAW];
            result = YES;
            break;
        case REGION_GPS_POINT: // REGION_GPS_POINT
            result = [collector createElement:COL_POLYGON];
            [collector setIsSingleTapEnable:NO];
            break;
        case REGION_GPS_PATH: // REGION_GPS_PATH
            result = [collector createElement:COL_POLYGON];
            [collector setIsSingleTapEnable:NO];
            break;
        case REGION_HAND_POINT: // REGION_HAND_POINT
            result = [collector createElement:COL_POLYGON];
            [collector setIsSingleTapEnable:YES];
            break;
        case REGION_HAND_PATH: // REGION_HAND_PATH
            [mapControl setAction:CREATE_FREE_DRAWPOLYGON];
            result = YES;
            break;
        default:
            result = NO;
            break;
    }
    return result;
}

+ (void)openGPS:(Collector *)collector {
    [collector openGPS];
}

+ (void)closeGPS:(Collector *)collector {
    [collector closeGPS];
}


@end