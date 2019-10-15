import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_volume/flutter_volume.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
            child: Column(
          children: <Widget>[
            FlatButton(
                onPressed: () {
                  FlutterVolume.up();
                },
                child: Text("up")),
            FlatButton(
                onPressed: () {
                  FlutterVolume.down();
                },
                child: Text("down")),
            FlatButton(
                onPressed: () {
                  FlutterVolume.mute();
                },
                child: Text("mute")),
            Slider(
              value: FlutterVolume.value.vol,
              onChanged: (v) {
                FlutterVolume.set(v);
              },
            ),
            FlatButton(
                onPressed: () {
                  FlutterVolume.enableWatcher();
                },
                child: Text("enable watch")),
            FlatButton(
                onPressed: () {
                  FlutterVolume.disableWatcher();
                },
                child: Text("disable watch")),
          ],
        )),
      ),
    );
  }
}
