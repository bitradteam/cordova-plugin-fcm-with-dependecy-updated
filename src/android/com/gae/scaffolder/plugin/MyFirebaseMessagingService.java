package com.gae.scaffolder.plugin;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.salesforce.marketingcloud.MarketingCloudSdk;
import com.salesforce.marketingcloud.messages.push.PushMessageManager;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

  private static final String TAG = "FCMPlugin";

  @Override
  public void onNewToken(@NonNull String token) {
    super.onNewToken(token);
    Log.d(TAG, "New Firebase token: " + token);
    FCMPlugin.sendTokenRefresh(token);
    MarketingCloudSdk.requestSdk(marketingCloudSdk -> marketingCloudSdk.getPushMessageManager().setPushToken(token));
  }

  /**
   * Called when message is received.
   *
   * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
   */
  @Override
  public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
    Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");

    if (PushMessageManager.isMarketingCloudPush(remoteMessage)) {
      MarketingCloudSdk.requestSdk(marketingCloudSdk -> marketingCloudSdk.getPushMessageManager().handleMessage(remoteMessage));
    } else {
      FCMPlugin.sendPushPayload(buildNotificationData(remoteMessage));
    }
  }

  private Map<String, Object> buildNotificationData(RemoteMessage remoteMessage) {
    Map<String, Object> data = new HashMap<>();

    for (String key : remoteMessage.getData().keySet()) {
      Object value = remoteMessage.getData().get(key);
//      Log.d(TAG, "\tKey: " + key + " Value: " + value);
      data.put(key, value);
    }
    Log.d(TAG, "\tNotification Data: " + data.toString());
    return data;
  }
}
