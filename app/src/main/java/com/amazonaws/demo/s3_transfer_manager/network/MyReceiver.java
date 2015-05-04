package com.amazonaws.demo.s3_transfer_manager.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.amazonaws.demo.s3_transfer_manager.MainActivity;
import com.amazonaws.demo.s3_transfer_manager.R;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // assumes WordService is a registered service
//        Intent intent = new Intent(context, WordService.class);
//        context.startService(intent);

        //be able to send a notification
        System.out.println("Called in BroadcastReceiver");
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show();
        //showNotification(context);
    }

    private void showNotification(Context context) {
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.down)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }
}
