#import "FlutterVolumePlugin.h"
#import <flutter_volume/flutter_volume-Swift.h>

@implementation FlutterVolumePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterVolumePlugin registerWithRegistrar:registrar];
}
@end
