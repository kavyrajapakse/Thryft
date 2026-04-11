package lk.fujilanka.thryft.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import lk.fujilanka.thryft.activity.MainActivity;
import lk.fujilanka.thryft.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if(remoteMessage.getNotification() != null){

            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            showNotification(title, body);
        }
    }

    private void showNotification(String title, String message){

        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String channelId = "order_channel";

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel =
                    new NotificationChannel(channelId,
                            "Order Notifications",
                            NotificationManager.IMPORTANCE_HIGH);

            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,0,intent,PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_notification_order)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        manager.notify(1, builder.build());
    }
}