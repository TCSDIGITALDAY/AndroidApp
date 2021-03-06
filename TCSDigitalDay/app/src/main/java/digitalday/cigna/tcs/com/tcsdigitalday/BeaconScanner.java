package digitalday.cigna.tcs.com.tcsdigitalday;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.estimote.coresdk.common.config.EstimoteSDK;
import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;

import java.util.List;
import java.util.UUID;

public class BeaconScanner extends Application {

    private BeaconManager beaconManager;
    ///private BeaconRegion region;
    public TextView notifyBeacon;

    @Override
    public void onCreate() {
        super.onCreate();
        //super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_beacon_scanner);
       /* beaconscanner-50t
                e0d99c81c723331c51fab7762086c00a*/
        //EstimoteSDK.initialize(getApplicationContext(), "<Here goes your application ID>", "<>Here goes your application token");
        EstimoteSDK.initialize(getApplicationContext(), "beaconscanner-50t", "e0d99c81c723331c51fab7762086c00a");

        beaconManager = new BeaconManager(getApplicationContext());
        //region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
        beaconManager.setMonitoringListener(new BeaconManager.BeaconMonitoringListener() {
        
            @Override
            public void onEnteredRegion(BeaconRegion beaconRegion, List<Beacon> beacons) {
                showNotification(
                        "TCS event is about to begin in few minutes.",
                        "Excitement has it's new Address!!. "
                                + "Walk to the conference room to catch up the most exciting event of the year!!");
            }

            @Override
            public void onExitedRegion(BeaconRegion beaconRegion) {
                cancelNotification();
            }
        });


        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(new BeaconRegion("monitored region",
                        //UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), 22504, 48827));
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null));
            }
        });
    }


    public void cancelNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
    }

    public void showNotification(String title, String message) {
        //Intent notifyIntent = new Intent(this, MainActivity.class);
        //notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
          //      new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                //.setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
        //notifyBeacon=(TextView)findViewById(R.id.txtbecaonmsg);
        //notifyBeacon.setText(message);

    }

  /*  @Override
    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
