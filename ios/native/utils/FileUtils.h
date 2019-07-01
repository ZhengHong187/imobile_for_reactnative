//
//  FileUtils.h
//  Supermap
//
//  Created by Shanglong Yang on 2019/5/27.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SMap.h"

NS_ASSUME_NONNULL_BEGIN

@interface FileUtils : NSObject

+(BOOL)deleteFile:(NSString *)path;
+(BOOL)createFileDirectories:(NSString*)path;
+(BOOL)copyFile:(NSString *)fromPath targetPath:(NSString *)toPath;
+(NSArray *)copyFiles:(NSArray *)fromPaths targetDictionary:(NSString *)targetDictionary;
+(NSString*)getLastModifiedTime:(NSDate*) nsDate;
+(NSDictionary *)readLocalFileWithPath:(NSString *)path;

@end

NS_ASSUME_NONNULL_END