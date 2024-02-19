package com.gae.scaffolder.plugin;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.messages.push.PushMessageManager;
import com.salesforce.marketingcloud.sfmcsdk.SFMCSdk;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "FCMPlugin";

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);
    SFMCSdk.requestSdk(sdk -> {
      sdk.mp(it -> it.pushMessageManager.setPushToken(token));
    });
  }

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase
   *                      Cloud Messaging.
   */
  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");

    if (PushMessageManager.isMarketingCloudPush(remoteMessage)) {
      SFMCSdk.requestSdk(sdk -> {
        sdk.mp(it -> it.pushMessageManager.handleMessage(remoteMessage));
      });
    } else {
      if (remoteMessage.getNotification() != null) {
        Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());
      }

      Map<String, Object> data = new HashMap<String, Object>();
      data.put("wasTapped", false);
      for (String key : remoteMessage.getData().keySet()) {
        Object value = remoteMessage.getData().get(key);
        Log.d(TAG, "\tKey: " + key + " Value: " + value);
        data.put(key, value);
      }

      Log.d(TAG, "\tNotification Data: " + data.toString());
      FCMPlugin.sendPushPayload(data);
    }
  }

  private Map<String, String> buildNotificationData(RemoteMessage remoteMessage) {
    Map<String, String> data = new HashMap<>();
    for (String key : remoteMessage.getData().keySet()) {
      String value = remoteMessage.getData().get(key);
      data.put(key, value);
    }
    Log.d(TAG, "\tNotification Data: " + data);
    return data;
  }
}