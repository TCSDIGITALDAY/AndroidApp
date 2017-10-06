package digitalday.cigna.tcs.com.tcsdigitalday;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.telephony.TelephonyManager;
import android.content.Context;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by venkatesh on 10/4/2017.
 */

public class RegisterActivity  extends AppCompatActivity
        implements View.OnClickListener{
    private final String TAG = "FB_SIGNIN";
    String   imeistring = null;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    //private WeakReference<Activity> activity;
    private EditText etPass;
    private EditText etEmail,etfirstname,etlastname;
    public TextView hiddenText;
    public String advid=null,taskResult=null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        //findViewById(R.id.btnSignIn).setOnClickListener(this);
        //findViewById(R.id.btnSignOut).setOnClickListener(this);

        etEmail = (EditText)findViewById(R.id.etEmailAddr);
        etPass = (EditText)findViewById(R.id.etPassword);
        etfirstname=(EditText)findViewById(R.id.firstname);
        etlastname=(EditText)findViewById(R.id.etlastname);
        hiddenText=(TextView)findViewById(R.id.hiddenView);
        hiddenText.setVisibility(View.INVISIBLE);
        // TODO: Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // TODO: Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "Signed in: " + user.getUid());

                    updateStatus("User is signed in");

                    Toast.makeText(RegisterActivity.this, "Signed in "+user.getUid(), Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                    Toast.makeText(RegisterActivity.this, "Signed Out", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        };


    }

    private void updateStatus(String stat) {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        tvStat.setText(stat);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCreate:
                createUserAccount();
                break;
        }
    }

    private boolean checkFormFields() {
        String email, password,firstname,lastname;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();
        firstname=etfirstname.getText().toString();
        lastname=etlastname.getText().toString();
        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()){
            etPass.setError("Password Required");
            return false;
        }

        if (firstname.isEmpty()){
            etfirstname.setError("Password Required");
            return false;
        }

        if (lastname.isEmpty()){
            etlastname.setError("Password Required");
            return false;
        }
        return true;
    }

    private void createUserAccount() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        final String password = etPass.getText().toString();

        // TODO: Create the user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String mail = etEmail.getText().toString();
                                    String firstname=etfirstname.getText().toString();
                                    String lastname=etlastname.getText().toString();
                                    writeNewUser(firstname,lastname,password,mail);
                                    Toast.makeText(RegisterActivity.this, "User created", Toast.LENGTH_SHORT)
                                            .show();
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                Toast.makeText(RegisterActivity.this, "Inside addOnCompleteListener", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            updateStatus("This email address is already in use.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                        Toast.makeText(RegisterActivity.this, "Inside addOnFailureListener", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }


    private void writeNewUser( String fname,String lname, String password, String email) {
        Log.e(TAG, "Add New User to Firebase !!!");
        Users user = new Users();
        //user.setDeviceid(advid);
        reqPermissionCheck();
        user.setDeviceid(advid);
        //user.setDeviceid("1234547");

        user.setEmail(email);
        user.setFirstname(fname);
        user.setLastname(lname);
        user.setPassword(password);
        String userId = mDatabase.child("users").push().getKey();
        mDatabase.child("users").child(userId).setValue(user);
        Log.e(TAG, "Add New User to Firebase Completed!!!");
    }

    private String getDeviceId(){
        TelephonyManager    telephonyManager;

        telephonyManager  =
                ( TelephonyManager )getSystemService( Context.TELEPHONY_SERVICE );

    /*
     * getDeviceId() function Returns the unique device ID.
     * for example,the IMEI for GSM and the MEID or ESN for CDMA phones.
     */
        imeistring = telephonyManager.getDeviceId();
        Log.d(TAG,"imeistring");
        return imeistring;

   /*
    * getSubscriberId() function Returns the unique subscriber ID,
  * for example, the IMSI for a GSM phone.
  */
        /*imsistring = telephonyManager.getSubscriberId();*/
    }

    public String getAdvtersieId() {
        Toast.makeText(RegisterActivity.this, "Getting Advertiseid...", Toast.LENGTH_SHORT)
                .show();
        //Activity context;
        //this.activity=new WeakReference<Activity>(context);
        ///UniqueAdvertisingId uniqueId= new UniqueAdvertisingId(RegisterActivity.this);
        UniqueAdvertisingId uniqueId=new UniqueAdvertisingId(this,hiddenText);
        uniqueId.execute();
        //String advertiseid=hiddenText.getText().toString();
        String advertiseid=taskResult;
        Toast.makeText(RegisterActivity.this, "Advertiseid:"+advertiseid, Toast.LENGTH_SHORT)
                .show();
        return advertiseid;
    }

    public void reqPermissionCheck() {

        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
            advid=getAdvtersieId();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    advid=getAdvtersieId();
                }
                break;

            default:
                advid="Not Shared";
                break;
        }
    }
}


class UniqueAdvertisingId extends AsyncTask<Void, Void, String> {

    //private WeakReference<Activity> activity;
    private TextView hiddenView;
    RegisterActivity mActivity;

    Context context;
    //public UniqueAdvertisingId(Activity context){
    public UniqueAdvertisingId(Context ctxt,TextView uiView){
        //this.activity = new WeakReference<Activity>(context);
        context=ctxt;
        Activity act=(Activity)ctxt;
        mActivity=(RegisterActivity)ctxt;
        this.hiddenView=uiView;
    }

@Override
protected String doInBackground(Void... params) {
        AdvertisingIdClient.Info idInfo = null;
        try {
        idInfo = AdvertisingIdClient.getAdvertisingIdInfo(context.getApplicationContext());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
        e.printStackTrace();
        } catch (IOException e) {
        e.printStackTrace();
        }
        String advertId = null;
        try {
        advertId = idInfo.getId();
        } catch (NullPointerException e) {
        e.printStackTrace();
        }

        return advertId;
        }

@Override
protected void onPostExecute(String advertId) {
        Toast.makeText(context.getApplicationContext(), "Inside On PostExecute:"+advertId, Toast.LENGTH_SHORT).show();
        //TextView txtAdvertId = (TextView)
            mActivity.taskResult=advertId;
        //hiddenView.setText(advertId);

        //sendAdvertId(advertId);
        }
/*public String sendAdvertId(String advertId)
{
    return advertId;
}*/
        //task.execute();
        //}

}