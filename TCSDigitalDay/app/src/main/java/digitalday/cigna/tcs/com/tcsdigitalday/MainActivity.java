package digitalday.cigna.tcs.com.tcsdigitalday;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public ProgressDialog progressDialog;
    private final String TAG = "MAIN_ACT";
    DatabaseReference mDatabase;
    DatabaseReference mUserData,mMessages,mBeacons;
    String mUserName,strMsg;
    Users muser;
    Messages msg;
    public TrackingInfo track,exitTracker;
    public String uid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    NestedScrollView mBottomSheet;
    BottomSheetBehavior mBottomSheetBehavior;
    ImageView mfrescoTalk;
    ImageView mfrescoPlay;
    /*public  Map<String, List<String>> PLACES_BY_BEACONS;*/
    public  Map<String, String> PLACES_BY_BEACONS=new HashMap<>();;
    private final int REQUEST_CODE=20,RESULT_CLOSE_APPLICATION=30;
    /*public   Map<String, List<String>> placesByBeacons = new HashMap<>();*/
    public   Map<String,String> placesByBeacons = new HashMap<>();

    public TextView txtBeaconmsg;
    public ArrayList<BeaconData> beaconData=null;
    // TODO: replace "<major>:<minor>" strings to match your own beacons.
   /* static {
        Map<String, List<String>> placesByBeacons = new HashMap<>();
        placesByBeacons.put("7329:22744", new ArrayList<String>() {{
            add("3 Waterside Crossing, CT, 06095, US");

        }});
        placesByBeacons.put("648:12", new ArrayList<String>() {{
            add("900 Cottage Grove Rd, Bloomfield, CT, US");

        }});
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }*/

    /*private List<String> placesNearBeacon(Beacon beacon) {*/
    private String placesNearBeacon(Beacon beacon) {
        String beaconKey = String.format("%d:%d", beacon.getMajor(), beacon.getMinor());
        Log.d(TAG,String.format("%d:%d", beacon.getMajor(), beacon.getMinor()));
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return "";
    }

    private BeaconManager beaconManager;
    private BeaconRegion region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtBeaconmsg = (TextView) findViewById(R.id.tcs_greeting);
        mBottomSheet = (NestedScrollView)findViewById(R.id.bottom_sheet);
        mfrescoTalk = (ImageView) findViewById(R.id.frescotalk_img);
        mfrescoPlay = (ImageView) findViewById(R.id.frescoplay_img);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setPeekHeight(200);
        mDatabase = FirebaseDatabase.getInstance().getReference();

//        String userId = mDatabase.child("users").push().getKey();
        String userId = getIntent().getStringExtra("USER_UID");
        String email = getIntent().getStringExtra("USER_EMAIL");
        Log.d("MAIN_ACT","User UID "+userId);
        Log.d("MAIN_ACT","User Email "+email);
        /*ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);*/
        beaconData=new ArrayList<>();
        mUserData = mDatabase.child("users").child(userId);
        mMessages=  mDatabase.child("messages").child("0");
        mBeacons=  mDatabase.child("BeaconData");
        /*Get List of Beacons from Firebase*/
        mBeacons.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of users in datasnapshot
                        //beaconDataMap((Map<String,Object>) dataSnapshot.getValue());
                        dataSnapshot.getValue();
                        Log.d(TAG,String.valueOf(dataSnapshot.getValue()));
                        String res=String.valueOf(dataSnapshot.getValue());

                        for (DataSnapshot jobSnapshot: dataSnapshot.getChildren()) {
                            BeaconData beaconItem = jobSnapshot.getValue(BeaconData.class);
                            //td.put(jobSnapshot.getKey(), job);
                            Log.d(TAG,"Location from DB:"+beaconItem.getGeoLocation());
                            beaconData.add(beaconItem);

                        }

                        for(BeaconData record:beaconData) {

                            //placesByBeacons.put(record.getMajor()+":"+record.getMinor(),record.getGeolocation());
                            //placesByBeacons.put(String.valueOf(record.getMajor())+":"+String.valueOf(record.getMinor()),record.getGeolocation());
                            //Log.d(TAG,record.getMajor()+":"+record.getMinor()+":"+record.getGeolocation());
                            placesByBeacons.put(record.getMversion(),record.getGeoLocation());
                            Log.d(TAG,record.getMversion()+":"+record.getGeoLocation());
                        }

                        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
                        Log.d(TAG,"Check Beacon Id"+PLACES_BY_BEACONS.get("7329:22744"));

                        mfrescoTalk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openFrescoPackage("com.tcs.fresco.talk");
                            }
                        });
                        mfrescoPlay.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openFrescoPackage("com.tcs.fresco.frescoplay");
                            }
                        });                   }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });

      /*  Load places near Beacon*/


        /*Get Messages for the Main Activity*/


      /*  Query msgQuery = mMessages.orderByChild("active").equalTo("Y");
        msgQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    msg = dataSnapshot.getValue(Messages.class);
                    Log.d(TAG,msg.getMsg());

                //}
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });*/

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                msg = dataSnapshot.getValue(Messages.class);
                Log.d(TAG,msg.getMsg());
                strMsg=msg.getMsg();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(MainActivity.this, "Failed to load.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };
        mMessages.addValueEventListener(postListener);



        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Log.d("MAIN_ACT", "Signed in: " + user.getUid());
                    uid=user.getUid();


                } else {
                    // User is signed out
                    Log.d("MAIN_ACT", "Currently signed out");

                }
            }
        };
        mUserData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                muser = dataSnapshot.getValue(Users.class);
                if (muser == null){
                    txtBeaconmsg.setVisibility(View.VISIBLE);
                    Log.d("MAIN_ACT","On Data Change - no data found");
                } else {
                    Log.d("MAIN_ACT", "On Data Change - " + muser.toString());
//                mUserName = dataSnapshot.getKey();
                    mUserName = muser.getFirstname();
                    track=new TrackingInfo();
                    track.setEmail(muser.getEmail());
                    Log.d(TAG,"Track:"+track.getEmail());

                    DateFormat df=null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                         df = (DateFormat) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }
                    Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
                    String currentDate = df.format(calendar.getTimeInMillis());
                    System.out.println(currentDate);
                    Log.d(TAG,currentDate);
                    track.setTimeStamp(currentDate);

                    Log.d(TAG,"Track:"+track.getTimeStamp());
                    Log.d("MAIN_ACT", "On Data Change - " +mUserName);

                    txtBeaconmsg.setVisibility(View.VISIBLE);
                    //txtBeaconmsg.setText("Hi " + mUserName + " Welcome to TCS Digital Day");
                    txtBeaconmsg.setText("Hi " + mUserName + ","+strMsg);
                    Log.d("MAIN_ACT", "On Data Change");
                    progressDialog = ProgressDialog.show(MainActivity.this, "", "We are just one step away to find where you are!!!", true);
                    final Handler h = new Handler() {
                        @Override
                        public void handleMessage(Message message) {
                            progressDialog.dismiss();
                        }
                    };
                    h.sendMessageDelayed(new Message(), 15000);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("MAIN_ACT","On DataERROR "+databaseError.getMessage());
            }
        });

        //txtBeaconmsg=(TextView)findViewById(R.id.txtbecaonmsg);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {

            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                if (!beacons.isEmpty()) {
                    Beacon nearestBeacon = beacons.get(0);
                    /*List<String> places = placesNearBeacon(nearestBeacon);*/
                    String places = placesNearBeacon(nearestBeacon);
                    // TODO: update the UI here
                    //txtBeaconmsg=(TextView)findViewById(R.id.tcs_greeting);
                    /*track.setGeoLocation(places.get(0));*/
                    if(places!="") {
                        track.setGeoLocation(places);
                        Log.d(TAG, "Track:" + track.getGeoLocation());
                    /*txtBeaconmsg.setText("Welcome to TCSDigitalDay!! Your are now in "+places.get(0).toString());*/
                        txtBeaconmsg.setText("Welcome to TCSDigitalDay!! Your are now in " + places);
                    }else
                    {
                        txtBeaconmsg.setText("Welcome to TCSDigitalDay!! We couldn't find where you are!!!");
                    }
                    txtBeaconmsg.setVisibility(View.VISIBLE);
                    String userId = mDatabase.child("trackinginfo").push().getKey();
                    mDatabase.child("trackinginfo").child(userId).setValue(track);
                    if(progressDialog!=null) {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }
                    //Log.d("Airport", "Nearest places: " + places);
                }

            }
        });

        region = new BeaconRegion("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        Log.d("MAIN_ACT", "Start Ranging");
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
    public void onStart() {
        super.onStart();
        // TODO: add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if((progressDialog != null) && progressDialog.isShowing() ){
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        return;
//        super.onBackPressed();
    }


    @Override
    public void onStop() {
        super.onStop();
        // TODO: Remove the AuthListener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
        exitTracker=new TrackingInfo();
        DateFormat df=null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = (DateFormat) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        String currentDate = df.format(calendar.getTimeInMillis());
        exitTracker.setTimeStamp(currentDate);
        exitTracker.setGeoLocation("Exit");
        exitTracker.setEmail(muser.getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void openFrescoPackage(String packageName) {
        final PackageManager packageManager = getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.fresco.me/"));
            startActivity(intent);
        }
//        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//        return list.size() > 0;
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_logout:
                Log.d("MAIN_ACT", "Logout Selected");
                signUserOut();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Log.d("MAIN_ACT", "Settings Selected");

                break;

            case android.R.id.home:
                MainActivity.this.finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        MainActivity.this.setResult(RESULT_CLOSE_APPLICATION);

        //MainActivity.this.finish();
        //return;
        //updateStatus();
    }

   /* private void beaconDataMap(Map<String,Object> users) {

        //Map<String, List<String>> BeaconsByLocation = new HashMap<>();
        BeaconData beaconlist=new BeaconData();
        List<String> location = new ArrayList<>();

        //iterate through each user, ignoring their UID
        for (Map.Entry<String, Object> entry : users.entrySet()){

            //Get user map
            Map beaconMetaData = (Map) entry.getValue();
            //Get phone field and append to list
            location.add(beaconMetaData.get("Geolocation").toString());
            beaconlist.setMinor(beaconMetaData.get("Minor").toString());
            beaconlist.setMajor(beaconMetaData.get("Major").toString());
            beaconData.add(beaconlist);
            //BeaconsByLocation.put(beaconlist.getMajor()+":"+beaconlist.getMinor(),(List)beaconData);
        }

        System.out.println(location.toString());
        //return BeaconsByLocation;
    }
*/
}
