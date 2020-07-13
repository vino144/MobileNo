package com.eriumyedu.myapplication.ui.login;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Looper;
import android.provider.Settings;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.eriumyedu.myapplication.R;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

   // private LoginViewModel loginViewModel;
    String TAG = "PhoneActivityTAG";
    //Activity activity = MainActivity.this;
    String wantPermission = Manifest.permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    ArrayList<String> _mst = new ArrayList<>();
    AppCompatTextView txt_simno,txt_address;

    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        txt_simno=findViewById(R.id.Id_txt_no);
        txt_address=findViewById(R.id.Id_txt_Address);
        /*loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();*/

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        mGoogleApiClient.connect();

        HintRequest hintRequest = new HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build();

        PendingIntent intent = Auth.CredentialsApi.getHintPickerIntent(mGoogleApiClient, hintRequest);
        try {
            startIntentSenderForResult(intent.getIntentSender(), 1008, null, 0, 0, 0, null);
        } catch (IntentSender.SendIntentException e) {
            Log.e("", "Could not start hint picker Intent", e);
        }

        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            if (getPhone()!=null) {
                txt_simno.setText("MOBILE NUMBER :"+getPhone().toString());
                Toast.makeText(LoginActivity.this, "phone number." + getPhone().get(2), Toast.LENGTH_LONG).show();
                Log.d(TAG, "Phone number: " + getPhone());
                _mst = getPhone();

                for (String op : _mst) {
                    Log.i("Device Information", String.valueOf(op));
                }
            }
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }


    @SuppressLint("MissingPermission")
    private void getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    System.out.println("CHECK_LATLNG: 1"+ "LATT:"+location.getLatitude()+": LONGI: "+location.getLongitude());
                                    getAddressfromlatlng(location.getLatitude(),location.getLongitude());
                                }
                            }
                        }
                );
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }

    private void getAddressfromlatlng(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        //double latt= Double.parseDouble("11.005210");
        //double longi= Double.parseDouble("77.012670");

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            System.out.println("CHECK_ADDRESS: 1"+address);
            String city = addresses.get(0).getLocality();
            System.out.println("CHECK_ADDRESS: 2"+city);
            String state = addresses.get(0).getAdminArea();
            System.out.println("CHECK_ADDRESS: 3"+state);
            String country = addresses.get(0).getCountryName();
            System.out.println("CHECK_ADDRESS: 4"+country);
            String postalCode = addresses.get(0).getPostalCode();
            System.out.println("CHECK_ADDRESS: 5"+postalCode);
            String knownName = addresses.get(0).getFeatureName();
            System.out.println("CHECK_ADDRESS: 6"+knownName);
            txt_address.setText("FULL ADDRESS: "+address+"\n"+"CITY: "+city+"\n"+"state: "+state+"\n"+"country: "+country+"\n"+"postalCode: "+postalCode+"\n"+"knownName: "+knownName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(0);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            System.out.println("CHECK_LATLNG: 2"+ "LATT:"+mLastLocation.getLatitude()+": LONGI: "+mLastLocation.getLongitude());
            getAddressfromlatlng(mLastLocation.getLatitude(),mLastLocation.getLongitude());

        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID
        );
    }



    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("HardwareIds")
    @TargetApi(Build.VERSION_CODES.O)
    private ArrayList<String> getPhone() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(LoginActivity.this);
            @SuppressLint("MissingPermission")
            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();

            Log.d("Test", "Current list = " + subsInfoList);

            for (SubscriptionInfo subscriptionInfo : subsInfoList) {

                String number = subscriptionInfo.getNumber();

                Log.d("Test", " Number is  " + number);
            }
        }

        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(LoginActivity.this, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        ArrayList<String> _lst =new ArrayList<>();
        if (phoneMgr!=null) {
            _lst.add(String.valueOf(phoneMgr.getCallState()));
            _lst.add("IMEI NUMBER :-" + phoneMgr.getImei());
            _lst.add("MOBILE NUMBER :-" + phoneMgr.getLine1Number());
            _lst.add("SERIAL NUMBER :-" + phoneMgr.getSimSerialNumber());
            _lst.add("SIM OPERATOR NAME :-" + phoneMgr.getSimOperatorName());
            _lst.add("MEI NUMBER :-" + phoneMgr.getMeid());
            _lst.add("SIM STATE :-" + phoneMgr.getSimState());
            _lst.add("COUNTRY ISO :-" + phoneMgr.getSimCountryIso());
        }
        return _lst;
    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this, permission)){
            Toast.makeText(LoginActivity.this, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

        /*TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }*/
/*
    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Phone number: " + getPhone());
                if (getPhone()!=null) {
                    Toast.makeText(LoginActivity.this, "phone number." + getPhone().get(2), Toast.LENGTH_LONG).show();
                    txt_simno.setText("MOBILE NUMBER :"+getPhone().toString());
                }

            } else {
                Toast.makeText(LoginActivity.this, "Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
            }
        }else if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                getLastLocation();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            return result == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1008) {
            if (resultCode == RESULT_OK) {
                if (data!=null) {
                    Credential cred = data.getParcelableExtra(Credential.EXTRA_KEY);
//                    cred.getId====: ====+919*******
                    if (cred!=null) {
                        Log.e("cred.getId", cred.getId());
                        //userMob = cred.getId();
                        txt_simno.setText("MOBILE NUMBER: "+cred.getId());
                    }
                }
            } else {
                // Sim Card not found!
                Log.e("cred.getId", "1008 else");

            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}