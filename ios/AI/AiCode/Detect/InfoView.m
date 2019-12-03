//
//  InfoView.m
//  SuperMapAITest
//
//  Created by zhouyuming on 2019/11/20.
//  Copyright © 2019年 supermap. All rights reserved.
//

#import "InfoView.h"
#import "AIRecognition.h"

#define HORIZ_SWIPE_DRAG_MIN  4    //水平滑动最小间距
#define VERT_SWIPE_DRAG_MAX    4    //垂直方向最大偏移量

@interface InfoView()


@end

@implementation InfoView


-(void)refresh{
//    [self setNeedsDisplay];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self setNeedsDisplay];
    });
}
-(NSArray*)colors{
    
    NSMutableArray* array=[[NSMutableArray alloc] init];
//    [array addObject:[UIColor grayColor]];
    [array addObject:[UIColor redColor]];
    [array addObject:[UIColor greenColor]];
    [array addObject:[UIColor blueColor]];
    [array addObject:[UIColor cyanColor]];
    [array addObject:[UIColor yellowColor]];
    [array addObject:[UIColor magentaColor]];
    [array addObject:[UIColor orangeColor]];
    [array addObject:[UIColor purpleColor]];
    [array addObject:[UIColor purpleColor]];
    
    return array;
}

-(void)drawRect:(CGRect)rect//UIView绘制入口，系统回调
{
    NSArray *drawColors=[self colors];
    UIColor* color=nil;
    
    // 获得上下文
    CGContextRef context =UIGraphicsGetCurrentContext();
    // 设置线宽
    if(_aIDetectStyle){
        CGContextSetLineWidth(context, _aIDetectStyle.aiStrokeWidth);
    }else{
        CGContextSetLineWidth(context, 2);
    }
    //清空所有rect对象
    [self.aIRectArr removeAllObjects];
    for(int i=0;i<self.aIRecognitionArray.count;i++){
        AIRecognition* recognition=[self.aIRecognitionArray objectAtIndex:i];
        
        CGRect rx = self.frame;
        CGRect tempCGRect=recognition.rect;
//        tempCGRect.origin.x=recognition.rect.origin.x*rx.size.width;
//        tempCGRect.origin.y=recognition.rect.origin.y*rx.size.height;
//        tempCGRect.size.width=recognition.rect.size.width*rx.size.width;
//        tempCGRect.size.height=recognition.rect.size.height*rx.size.height;
        
//        tempCGRect.origin.x=recognition.rect.origin.y*rx.size.height;
//        tempCGRect.origin.y=recognition.rect.origin.x*rx.size.width;
//        tempCGRect.size.width=recognition.rect.size.height*rx.size.height;
//        tempCGRect.size.height=recognition.rect.size.width*rx.size.width;
        
        
        tempCGRect.size.width=recognition.rect.size.height*rx.size.height;
        tempCGRect.size.height=recognition.rect.size.width*rx.size.width;
        tempCGRect.origin.x=recognition.rect.origin.y*rx.size.height+tempCGRect.size.width/2;
        tempCGRect.origin.y=recognition.rect.origin.x*rx.size.width-tempCGRect.size.height/2;
        if(tempCGRect.origin.x<0){
            tempCGRect.origin.x=2;
            tempCGRect.size.width=tempCGRect.size.width-(2-tempCGRect.origin.x);
        }
        if(tempCGRect.origin.y<0){
            tempCGRect.origin.y=2;
            tempCGRect.size.height=tempCGRect.size.height-(2-tempCGRect.origin.y);
        }

        if(tempCGRect.origin.x+tempCGRect.size.width>rx.size.width){
            tempCGRect.size.width=tempCGRect.size.width-(tempCGRect.origin.x+tempCGRect.size.width-rx.size.width);
        }
        if(tempCGRect.origin.y+tempCGRect.size.height>rx.size.height){
            tempCGRect.size.height=tempCGRect.size.height-(tempCGRect.origin.y+tempCGRect.size.height-rx.size.height);
        }

        [self.aIRectArr addObject:[NSValue valueWithCGRect:tempCGRect]];
//        NSValue* value;
//        value.CGRectValue
        //设置识别框颜色
        if(_aIDetectStyle&&_aIDetectStyle.isSameColor){
            if(_aIDetectStyle.aiColor){
                color=_aIDetectStyle.aiColor;
            }else if(!color){
                int x = arc4random() % drawColors.count;
                color=drawColors[x];
            }
        }else{
            int x = arc4random() % drawColors.count;
            color=drawColors[x];
        }
        
        //获取一个随机整数范围在：颜色数组的长度内
//        if(!color||(_aIDetectStyle&&!_aIDetectStyle.isSameColor)){
//            int x = arc4random() % drawColors.count;
//            color=drawColors[x];
//        }
        
        // 设置绘制颜色
        CGContextSetStrokeColorWithColor(context, color.CGColor);
        //设置线条样式
        CGContextSetLineCap(context, kCGLineCapSquare);
        //设置线条粗细宽度
        CGContextSetLineWidth(context, 2.0);
        //开始一个起始路径
        CGContextBeginPath(context);
        //起始点设置为(0,0):注意这是上下文对应区域中的相对坐标，
        CGContextMoveToPoint(context, tempCGRect.origin.x, tempCGRect.origin.y);
        //设置下一个坐标点
        CGContextAddLineToPoint(context, tempCGRect.origin.x+tempCGRect.size.width, tempCGRect.origin.y);
        //设置下一个坐标点
        CGContextAddLineToPoint(context, tempCGRect.origin.x+tempCGRect.size.width, tempCGRect.origin.y+tempCGRect.size.height);
        //设置下一个坐标点
        CGContextAddLineToPoint(context, tempCGRect.origin.x, tempCGRect.origin.y+tempCGRect.size.height);
        CGContextAddLineToPoint(context, tempCGRect.origin.x, tempCGRect.origin.y);
        //连接上面定义的坐标点
        CGContextStrokePath(context);


        // 绘制文字
        NSString* content=@"";
        if(_aIDetectStyle&&_aIDetectStyle.isDrawTitle){
            content=[content stringByAppendingString:recognition.label];
//            [recognition.label stringByAppendingFormat:@"%.2f",recognition.confidence*100];
        }
        //绘制可信度
        if(_aIDetectStyle&&_aIDetectStyle.isDrawConfidence){
            content=[content stringByAppendingFormat:@"%.2f",recognition.confidence*100];
            content=[content stringByAppendingString:@"%"];
        }
        //文本属性
        NSMutableDictionary *textAttributes = [[NSMutableDictionary alloc] init];
        //字体名称和大小
        [textAttributes setValue:[UIFont systemFontOfSize:20.0] forKey:NSFontAttributeName];
        
        UIColor* backgroundColor=[UIColor colorWithRed:CGColorGetComponents(color.CGColor)[0] green:CGColorGetComponents(color.CGColor)[1] blue:CGColorGetComponents(color.CGColor)[2] alpha:0.5f];
    
        [textAttributes setValue:backgroundColor forKey:NSBackgroundColorAttributeName];
        
        //颜色
        [textAttributes setValue:[UIColor whiteColor] forKey:NSForegroundColorAttributeName];
        [content drawAtPoint:tempCGRect.origin withAttributes:textAttributes];

    }
}

- (void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event{
    // 获得当前点
    UITouch *touch = [touches anyObject];
    // 初始化起始点和结束点
    self.startPoint = [touch locationInView:self];
    self.isTouchEvent=YES;
//    self.endPoint = [touch locationInView:self];
    // 触发绘制
//    [self setNeedsDisplay];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event{

    UITouch *aTouch = [touches anyObject];
    CGPoint currentTouchPosition = [aTouch locationInView:self];
    //  判断水平滑动的距离是否达到了设置的最小距离，并且是否是在接近直线的路线上滑动（y轴偏移量）
    if (fabsf(self.startPoint.x - currentTouchPosition.x) >= HORIZ_SWIPE_DRAG_MIN ||
        fabsf(self.startPoint.y - currentTouchPosition.y) >= VERT_SWIPE_DRAG_MAX)
    {
       self.isTouchEvent=NO;
        //重置开始点坐标值
        self.startPoint = CGPointZero;
    }
    
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event{
    UITouch *aTouch = [touches anyObject];
    CGPoint currentTouchPosition = [aTouch locationInView:self];
    //  判断水平滑动的距离是否达到了设置的最小距离，并且是否是在接近直线的路线上滑动（y轴偏移量）
    if (self.isTouchEvent && fabsf(self.startPoint.x - currentTouchPosition.x) <= HORIZ_SWIPE_DRAG_MIN &&
        fabsf(self.startPoint.y - currentTouchPosition.y) <= VERT_SWIPE_DRAG_MAX )
    {
        [self touchPoint:self.startPoint];
        self.isTouchEvent=NO;
        //重置开始点坐标值
        self.startPoint = CGPointZero;
    }
}

-(AIRecognition*)touchPoint:(CGPoint)touchPoint{
    if(!self.aIRectArr||[self.aIRectArr count]==0){
        return nil;
    }
    CGRect touchCGRect;
    int index=-1;
    for(int i=0;i<[self.aIRectArr count];i++){
        NSValue* tempValue=[self.aIRectArr objectAtIndex:i];
        CGRect tempCGRect=tempValue.CGRectValue;
        if(CGRectContainsPoint(tempCGRect,touchPoint)){
            //筛选面积最小的
            if(index==-1||touchCGRect.size.height*touchCGRect.size.height>tempCGRect.size.height*tempCGRect.size.height){
                touchCGRect=tempCGRect;
                index=i;
            }
        }
    }
    if(index!=-1&&index<[self.aIRectArr count]){
        self.callBackBlock([self.aIRectArr objectAtIndex:index]);
        return [self.aIRectArr objectAtIndex:index];
    }
    
    return nil;
}

@end
