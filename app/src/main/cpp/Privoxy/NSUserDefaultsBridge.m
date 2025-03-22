#import <Foundation/Foundation.h>
#include "NSUserDefaultsBridge.h"

// Save string to NSUserDefaults
// Singleton NSUserDefaults instance
NSUserDefaults *GetSharedDefaultsInstance() {
    static NSUserDefaults *sharedDefaults = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        NSString *appGroup = @"group.com.boostgo.hoangsinh2";
        sharedDefaults = [[NSUserDefaults alloc] initWithSuiteName:appGroup];
    });
    return sharedDefaults;
}

void SaveToUserDefaults(const char* key, long value) {
    NSUserDefaults *sharedDefaults = GetSharedDefaultsInstance();
    NSString *nsKey = [NSString stringWithUTF8String:key];
    
    long current =  0;
    NSString *storedValue = [sharedDefaults stringForKey:nsKey];
    if (storedValue) {
        current = [storedValue longLongValue];
    }
    current  = current + value;
    
    NSString *nsValue = [NSString stringWithFormat:@"%ld", current];
    [sharedDefaults setObject:nsValue forKey:nsKey];
    NSLog(@"save %s %ld %d",key,value,[sharedDefaults synchronize]);
}
