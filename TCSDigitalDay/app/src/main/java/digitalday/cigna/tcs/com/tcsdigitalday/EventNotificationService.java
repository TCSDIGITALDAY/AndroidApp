package digitalday.cigna.tcs.com.tcsdigitalday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.support.v4.app.NotificationCompat.DEFAULT_ALL;
import static android.support.v4.app.NotificationCompat.DEFAULT_SOUND;

/**
 * Created by USER on 10/9/2017.
 */

public class EventNotificationService extends FirebaseMessagingService  {
    private final String TAG=this.getClass().getSimpleName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            long[] vibrate = { 0, 100, 200, 300 };
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String  messageBody=remoteMessage.getNotification().getBody()+" SOUND URI"+defaultSoundUri.toString();
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = "fcm_default_channel";

            Log.d("SERVICE","SOUND URI"+defaultSoundUri.toString());
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this.getApplicationContext(),channelId)
                            .setSmallIcon(R.drawable.ic_drawing)
                            .setContentTitle("FCM Message")
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent)
                            .setVibrate(vibrate)
//                            .setDefaults(DEFAULT_SOUND);
                    ;
            Notification notification = notificationBuilder.build();

            notification.defaults |= Notification.DEFAULT_SOUND;
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(29 /* ID of notification */, notification);
        }
    }


}
