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
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainingInfo extends AppCompatActivity {

    private double lat, lon;
    private TextView et_location;
    private ImageView iv_image;
    private Uri imageUri;
    private FloatingActionButton fab;
    private Button btn_create_training;
    private EditText ed_t_name, ed_t_price, ed_t_mob, ed_t_dur, ed_t_desc;
    private Spinner sp_avail, sp_cat;
    private FirebaseAuth mAuth;
    private DatabaseReference mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainers);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et_location = (TextView) findViewById(R.id.ed_training_location);
        iv_image = (ImageView) findViewById(R.id.iv_training_image);
        fab = (FloatingActionButton) findViewById(R.id.training_fab);
        btn_create_training = (Button) findViewById(R.id.btn_training_create);
        ed_t_name = (EditText) findViewById(R.id.ed_training_name);
        ed_t_price = (EditText) findViewById(R.id.ed_training_price);
        ed_t_mob = (EditText) findViewById(R.id.ed_training_mobile);
        ed_t_dur = (EditText) findViewById(R.id.ed_training_duration);
        ed_t_desc = (EditText) findViewById(R.id.ed_training_description);
        sp_avail = (Spinner) findViewById(R.id.spinner_training_availability);
        sp_cat = (Spinner) findViewById(R.id.spinner_training_category);

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmss");
        String datetime = ft.format(dNow);

        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference().child("Trainings").child(datetime);
        mData.keepSynced(true);

        String key = getIntent().getStringExtra("key");
        if (key != null){

            setValues(key);
            if (getIntent().getStringExtra("action").equals("view"))
                disableEdits();

        } else {

            new LoadLocation().execute();

        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 201);
            }
        });

        et_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(TrainingInfo.this), 101);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn_create_training.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFormFilled()){

                    mData.child("training_name").setValue(ed_t_name.getText().toString());
                    mData.child("user_id").setValue(mAuth.getCurrentUser().getUid().toString());
                    mData.child("location").setValue(et_location.getText().toString());
                    mData.child("lat").setValue(""+lat);
                    mData.child("lon").setValue(""+lon);
                    mData.child("price").setValue(ed_t_price.getText().toString());
                    mData.child("mobile").setValue(ed_t_mob.getText().toString());
                    mData.child("availabilty").setValue(""+sp_avail.getSelectedItemPosition());
                    mData.child("category").setValue(""+sp_cat.getSelectedItemPosition());
                    mData.child("duration").setValue(ed_t_dur.getText().toString());
                    mData.child("description").setValue(ed_t_desc.getText().toString());

                    if (imageUri!=null){
                        final ProgressDialog pd = new ProgressDialog(TrainingInfo.this);
                        pd.setTitle("Please Wait!");
                        pd.setMessage("Uploading...");
                        pd.show();
                        StorageReference filePath = FirebaseStorage.getInstance().getReference().child("TrainingImages").child(imageUri.getLastPathSegment());
                        filePath.putFile(imageUri).addOnSuccessListener(
                                new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        //noinspection VisibleForTests
                                        mData.child("image_url").setValue(taskSnapshot.getDownloadUrl().toString());
                                        pd.dismiss();
                                        finish();
                                    }
                                }
                        );
                    } else
                        finish();

                } else {
                    Toast.makeText(TrainingInfo.this, "Field cannot be left blank!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void disableEdits() {

        fab.setVisibility(View.INVISIBLE);
        ed_t_name.setEnabled(false);
        et_location.setEnabled(false);
        ed_t_price.setEnabled(false);
        ed_t_mob.setEnabled(false);
        sp_avail.setEnabled(false);
        sp_cat.setEnabled(false);
        ed_t_dur.setEnabled(false);
        ed_t_desc.setEnabled(false);
        btn_create_training.setVisibility(View.GONE);

    }

    private void setValues(String key) {

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("Trainings").child(key);
        data.keepSynced(true);

        data.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    Picasso.with(TrainingInfo.this).load(dataSnapshot.child("image_url").getValue().toString()).placeholder(R.mipmap.userdp).into(iv_image);
                } catch (Exception e){
                    Picasso.with(TrainingInfo.this).load(R.mipmap.userdp).into(iv_image);
                }

                ed_t_name.setText(dataSnapshot.child("training_name").getValue().toString());
                et_location.setText(dataSnapshot.child("location").getValue().toString());
                ed_t_price.setText(dataSnapshot.child("price").getValue().toString());
                ed_t_mob.setText(dataSnapshot.child("mobile").getValue().toString());
                sp_cat.setSelection(Integer.parseInt(dataSnapshot.child("category").getValue().toString()));
                sp_avail.setSelection(Integer.parseInt(dataSnapshot.child("availabilty").getValue().toString()));
                ed_t_dur.setText(dataSnapshot.child("duration").getValue().toString());
                if (dataSnapshot.child("description").getValue()!=null)
                    ed_t_desc.setText(dataSnapshot.child("description").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean isFormFilled() {

        if (TextUtils.isEmpty(ed_t_name.getText().toString()) ||
                TextUtils.isEmpty(ed_t_price.getText().toString()) ||
                TextUtils.isEmpty(ed_t_mob.getText().toString()) ||
                TextUtils.isEmpty(ed_t_dur.getText().toString()))

            return false;

        return true;

    }

    class LoadLocation extends AsyncTask<Void , Void, String> {


        @Override
        protected String doInBackground(Void... params) {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(TrainingInfo.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(TrainingInfo.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }

            Criteria criteria = new Criteria();
            lm.getBestProvider(criteria, true);
            Location location = getLastLocation();

            Geocoder geocoder = new Geocoder(TrainingInfo.this, Locale.getDefault());
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
            iv_image.setImageURI(imageUri);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
