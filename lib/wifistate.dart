// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';

import 'package:flutter/services.dart';

class ConnectivityResult {
  final String mac;

  ConnectivityResult(this.mac);
}

const MethodChannel _methodChannel =
const MethodChannel('plugins.flutter.io/connectivity');

const EventChannel _eventChannel =
const EventChannel('plugins.flutter.io/connectivity_status');

class Wifistate {
  Stream<ConnectivityResult> _onConnectivityChanged;

  /// Fires whenever the connectivity state changes.
  Stream<ConnectivityResult> get onConnectivityChanged {
    if (_onConnectivityChanged == null) {
      _onConnectivityChanged = _eventChannel
          .receiveBroadcastStream()
          .map((dynamic event) => _parseConnectivityResult(event));
    }
    return _onConnectivityChanged;
  }

  /// Checks the connection status of the device.
  ///
  /// Do not use the result of this function to decide whether you can reliably
  /// make a network request. It only gives you the radio status.
  ///
  /// Instead listen for connectivity changes via [onConnectivityChanged] stream.
  Future<ConnectivityResult> checkConnectivity() async {
    final String result = await _methodChannel.invokeMethod('check');
    return _parseConnectivityResult(result);
  }
}

ConnectivityResult _parseConnectivityResult(String mac) {
  return new ConnectivityResult(mac);
}
