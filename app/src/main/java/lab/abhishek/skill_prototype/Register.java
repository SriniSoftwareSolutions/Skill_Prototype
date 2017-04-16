package lab.abhishek.skill_prototype;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    private EditText et_fname, et_lname, et_email, et_password, et_confirm_pass, et_mobile;
    private TextView et_location;
    private Button btn_register;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private double lat, lon;
    private CircleImageView userImage;
    private Uri imageUri;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullScreen();
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseReference.keepSynced(true);
        pd = new ProgressDialog(this);

        btn_register = (Button) findViewById(R.id.btn_register_form);
        et_fname = (EditText) findViewById(R.id.ed_fname);
        et_lname = (EditText) findViewById(R.id.ed_lname);
        et_location = (TextView) findViewById(R.id.ed_location);
        et_email = (EditText) findViewById(R.id.ed_email);
        et_password = (EditText) findViewById(R.id.ed_password);
        et_confirm_pass = (EditText) findViewById(R.id.ed_confirm_password);
        et_mobile = (EditText) findViewById(R.id.ed_mobile);
        userImage = (CircleImageView) findViewById(R.id.iv_userImage);


        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testField()) {
                    if (et_password.getText().toString().length() < 9) {
                        Toast.makeText(Register.this, "Password must be atleast 8 characters long!", Toast.LENGTH_LONG).show();
                    } else if (!et_password.getText().toString().equals(et_confirm_pass.getText().toString())){
                        Toast.makeText(Register.this, "Passwords does not match!", Toast.LENGTH_LONG).show();
                    } else if (et_mobile.getText().toString().length() < 10){
                        Toast.makeText(Register.this, "Mobile Number is less than 10-digits!", Toast.LENGTH_LONG).show();
                    } else {
                        createUserWithFirebase(et_email.getText().toString(), et_password.getText().toString());
                    }
                } else {
                    Toast.makeText(Register.this, "Required Fields Cannot Be Left Blank!", Toast.LENGTH_LONG).show();
                }
            }
        });

        et_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(Register.this), 101);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 201);
            }
        });

        new LoadLocation().execute();

    }

    class LoadLocation extends AsyncTask<Void , Void, String>{


        @Override
        protected String doInBackground(Void... params) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(Register.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(Register.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            Criteria criteria = new Criteria();
            lm.getBestProvider(criteria, true);
            Location location = getLastLocation();

            Geocoder geocoder = new Geocoder(Register.this, Locale.getDefault());
            try {

                lat = location.getLatitude();
                lon = location.getLongitude();
                List<Address> address = geocoder.getFromLocation(lat, lon, 1);
                return address.get(0).getLocality().toString();


            } catch (Exception e) {
                //
                return "";
            }
        }

        @Override
        protected void onPostExecute(String s) {
            et_location.setText(s);
        }
    }

    private Location getLastLocation() {
        LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(data,this);
            et_location.setText(place.getName());
            lat = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
        } else if (requestCode == 201 && resultCode == RESULT_OK){
            imageUri = data.getData();
            userImage.setImageURI(imageUri);
        }

    }

    private void createUserWithFirebase(String s, String s1) {


        pd.setTitle("Please Wait!");
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.createUserWithEmailAndPassword(s, s1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {


                        if (task.isSuccessful()){

                            addUser();

                        } else {
                            Toast.makeText(Register.this, "E-mail already used!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

    }

    private void addUser() {

        getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","Email").apply();

        final DatabaseReference newUser = mDatabaseReference.child(mAuth.getCurrentUser().getUid().toString());
        newUser.keepSynced(true);

        newUser.child("fname").setValue(et_fname.getText().toString().trim());
        newUser.child("lname").setValue(et_lname.getText().toString().trim());
        newUser.child("location").setValue(et_location.getText().toString().trim());
        newUser.child("latitude").setValue(""+lat);
        newUser.child("longitude").setValue(""+lon);
        newUser.child("mobile").setValue(et_mobile.getText().toString().trim());
        newUser.child("email").setValue(et_email.getText().toString().trim());
        newUser.child("password").setValue(et_password.getText().toString().trim());

        if (imageUri!=null){

            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("UserImage").child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //noinspection VisibleForTests
                            newUser.child("image_url").setValue(taskSnapshot.getDownloadUrl().toString());
                            pd.dismiss();
                            startActivity(new Intent(Register.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
            );

        } else {
            startActivity(new Intent(Register.this, MainActivity.class));
            finishAffinity();
        }

    }

    private boolean testField() {

        if (TextUtils.isEmpty(et_email.getText().toString()) ||
                TextUtils.isEmpty(et_fname.getText().toString()) ||
                TextUtils.isEmpty(et_lname.getText().toString()) ||
                TextUtils.isEmpty(et_location.getText().toString()) ||
                TextUtils.isEmpty(et_password.getText().toString()))

            return false;

        return true;

    }

    private void fullScreen() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }
}
