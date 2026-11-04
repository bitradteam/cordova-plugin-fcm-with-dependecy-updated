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
    } 
    else {
      // Not a Marketing Cloud push: offer it to the app's own plugins, when they
      // are installed. By reflection on purpose, so this fork keeps compiling and
      // behaving exactly as before in an app without them.
      Map<String, String> data = remoteMessage.getData();
      offerToPlugin("pt.sonae.cc.liveactivity.CCLiveActivityRouter", "handle", data, true);
      offerToPlugin("pt.sonae.cc.pushlistener.CCPushListenerPlugin", "dispatch", data, false);
    }
  }

  /**
   * Calls a static method of another plugin, if that plugin is in the app.
   * A missing plugin is the normal case and does nothing; any other error is
   * logged and swallowed, so a bug there can never break this service.
   */
  private void offerToPlugin(String className, String method, Map<String, String> data, boolean withContext) {
    try {
      Class<?> target = Class.forName(className);
      if (withContext) {
        target.getMethod(method, Context.class, Map.class).invoke(null, getApplicationContext(), data);
      } else {
        target.getMethod(method, Map.class).invoke(null, data);
      }
    } catch (ClassNotFoundException notInstalled) {
      // plugin not installed in this app
    } catch (Throwable t) {
      Log.w(TAG, "plugin " + className + " failed to handle a message", t);
    }
  }

  private Map<String, Object> buildNotificationData(RemoteMessage remoteMessage) {
    Map<String, Object> data = new HashMap<>();

    for (String key : remoteMessage.getData().keySet()) {
      Object value = remoteMessage.getData().get(key);
      data.put(key, value);
    }
    Log.d(TAG, "\tNotification Data: " + data);
    return data;
  }
/*
  private void buildNotification(String channel, String title, String message) {

    Intent intent = new Intent(this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

    createNotificationChannel(this, channel);

    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSmallIcon(R.drawable.apdu_service_banner)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true);

    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(69, builder.build());
  }

  private void createNotificationChannel(Context context, String channelId) {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_HIGH);
      channel.setDescription(channelId);
      // Register the channel with the system; it's not possible to change the importance
      // or other notification behaviors after this
      NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
      notificationManager.createNotificationChannel(channel);
    }
  }
  */
}
