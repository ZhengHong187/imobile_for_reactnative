//
//  SummaryRegionOnline.h
//  HotMap
//
//  Created by lucd on 2017/9/30.
//  Copyright © 2017年 imobile-lucd. All rights reserved.
//

#import "DistributeAnalyst.h"
@class Rectangle2D;
/**
*  在线区域汇总分析类
*/
@interface SummaryRegionOnline : DistributeAnalyst
/**
 * 汇总类型
 * <p>SUMMARYMESH网格面汇总、SUMMARYREGION多边形汇总</p>
 */
@property(nonatomic,strong)NSString* summaryType;
/**
 * 网络面汇总类型
 * <p>0：四边形网格 , 1：六边形网格</p>
 */
@property(nonatomic)int meshType;
/**
 * 网格单位
 * <p>网格大小单位：Meter(默认),Kilometer,Yard,Foot,Mile</p>
 */
@property(nonatomic,strong)NSString* meshSizeUnit;
/**
 * 分辨率（网格大小）
 */
@property(nonatomic)int resolution;
/**
 * 是否统计长度或者面积
 * <p>true（默认）代表 ：是 ，false代表：否
 */
@property(nonatomic)BOOL lengthOrArea;

/**
 * 设置标准属性字段统计
 * <p>统计字段个数应该和统计模式的个数保持一致，可选统计模式有：max,min,average,sum,variance,stdDeviation</p>
 * @param fieldsName 字段名称
 * @param statisticModes 统计模式
 */
-(void) setStandardFields:(NSString*) fieldsName andStatisticModes:(NSString*) statisticModes;

/**
 * 设置权重字段统计
 * <p>统计字段个数应该和统计模式的个数保持一致，可选统计模式有：max,min,average,sum,variance,stdDeviation</p>
 * @param fieldsName 字段名称
 * @param statisticModes 统计模式
 */
-(void) setWeightedFields:(NSString*) fieldsName andStaticModes:(NSString*) statisticModes;

/**
 * 数据路径
 */
@property(nonatomic,strong)NSString* dataPath;
/**
 * 分析范围
 */
@property(nonatomic,strong)Rectangle2D* bounds;
/**
 * 数字精度
 */
@property (nonatomic)NSInteger numericPrecision;

/**
 * 专题图分段个数
 * 默认为:0
 */
@property(nonatomic)NSInteger rangeCount;
/**
 * 专题图分段模式: EQUALINTERVAL(等距离分段)、LOGARITHM(对数分段)、QUANTILE(等计数分段)、SQUAREROOT(平方根分段)、STDDEVIATION (标准差分段)
 * 默认为:EQUALINTERVAL(等距离分段)
 */
@property(nonatomic,strong)NSString* rangeMode;
/**
 * 专题图颜色渐变模式: GREENORANGEVIOLET(绿橙紫渐变色)、GREENORANGERED(绿橙红渐变)、RAINBOW(彩虹色)、SPECTRUM(光谱渐变)、TERRAIN (地形渐变)
 * 默认为:GREENORANGEVIOLET(绿橙紫渐变色)
 */
@property(nonatomic,strong)NSString* colorGradientType;
@end
