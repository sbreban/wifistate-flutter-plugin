package com.example.wifistate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import java.net.NetworkInterface;
import java.util.List;
import java.util.Collections;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** WifistatePlugin */
public class WifistatePlugin implements MethodCallHandler, EventChannel.StreamHandler {
  private final Registrar registrar;
  private final WifiManager manager;
  private BroadcastReceiver receiver;

  /** Plugin registration. */
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel =
            new MethodChannel(registrar.messenger(), "plugins.flutter.io/connectivity");
    final EventChannel eventChannel =
            new EventChannel(registrar.messenger(), "plugins.flutter.io/connectivity_status");
    WifistatePlugin instance = new WifistatePlugin(registrar);
    channel.setMethodCallHandler(instance);
    eventChannel.setStreamHandler(instance);
  }

  private WifistatePlugin(Registrar registrar) {
    this.registrar = registrar;
    this.manager =
            (WifiManager) registrar.context().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
  }

  @Override
  public void onListen(Object arguments, EventChannel.EventSink events) {
    receiver = createReceiver(events);
    registrar
            .context()
            .registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }

  @Override
  public void onCancel(Object arguments) {
    registrar.context().unregisterReceiver(receiver);
    receiver = null;
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("check")) {
      WifiInfo info = manager.getConnectionInfo();
      if (info != null) {
        result.success(getMacAddr());
      } else {
        result.success("none");
      }
    } else {
      result.notImplemented();
    }
  }

  private BroadcastReceiver createReceiver(final EventChannel.EventSink events) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        boolean isLost = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if (isLost) {
          events.success("none");
          return;
        }

        events.success(getMacAddr());
      }
    };
  }

  public static String getMacAddr() {
    try {
      List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
      for (NetworkInterface nif : all) {
        if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

        byte[] macBytes = nif.getHardwareAddress();
        if (macBytes == null) {
          return "";
        }

        StringBuilder res1 = new StringBuilder();
        for (byte b : macBytes) {
          res1.append(String.format("%02X:",b));
        }

        if (res1.length() > 0) {
          res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
      }
    } catch (Exception ex) {
    }
    return "02:00:00:00:00:00";
  }
}
