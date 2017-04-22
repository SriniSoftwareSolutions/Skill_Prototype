package lab.abhishek.skill_prototype;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class TrainingInfo extends AppCompatActivity {

    private double lat, lon;
    private TextView et_location, tv_change_location;
    private ImageView iv_image;
    private Uri imageUri;
    private FloatingActionButton fab;
    private Button btn_create_training;
    private EditText ed_t_name, ed_t_price, ed_t_mob, ed_t_dur, ed_t_desc;
    private Spinner sp_avail, sp_cat;
    private FirebaseAuth mAuth;
    private String key;
    private DatabaseReference mData;
    private boolean editAllowed = false, deleteAllowed = false;
    private LinearLayout ll_editBox;
    private String trainer_id;

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
        ll_editBox = (LinearLayout) findViewById(R.id.ll_editBox);
        tv_change_location = (TextView) findViewById(R.id.tv_change_location);

        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference().child("Trainings");
        mData.keepSynced(true);

        key = getIntent().getStringExtra("key");
        if (key != null){

            mData = mData.child(key);
            if (getIntent().getStringExtra("action").equals("view"))
                disableEdits();
            else {
                btn_create_training.setText("Update");
                btn_create_training.setVisibility(View.VISIBLE);
            }

            setValues(key);

        } else {

            mData = mData.push();
            new LoadLocation().execute();

        }

        ll_editBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ed_t_desc.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.showSoftInput(ed_t_desc, InputMethodManager.SHOW_IMPLICIT);
            }
        });

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

        tv_change_location.setOnClickListener(new View.OnClickListener() {
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

                //Toast.makeText(getApplicationContext(), ""+btn_create_training.getText().toString(), Toast.LENGTH_SHORT).show();

                if (btn_create_training.getText().toString().equals("Register")){

                    final ProgressDialog myProg = new ProgressDialog(TrainingInfo.this);
                    myProg.setTitle("Please Wait!");
                    myProg.setMessage("Processing Request...");
                    myProg.show();
                    DatabaseReference register = FirebaseDatabase.getInstance().getReference().child("Trainings").child(key);
                    register.child("registered_users")
                            .child(mAuth.getCurrentUser().getUid().toString())
                            .setValue(getSharedPreferences("srini_prefs",MODE_PRIVATE)
                                .getString("userName",""));

                    register = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid().toString());
                    register.child("registered_trainings")
                            .child(key).setValue(ed_t_name.getText().toString());

                    DatabaseReference trainer_ref = FirebaseDatabase.getInstance().getReference().child("Users").child(trainer_id);
                    trainer_ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            String t_name = dataSnapshot.child("fname").getValue().toString();
                            SmsManager smsManager = SmsManager.getDefault();
                            String msg = "I want training for '"+ed_t_name.getText().toString().trim()+"' ,trainer name is '"+t_name+"' with contact '"+ed_t_mob.getText().toString()+"'";
                            myProg.dismiss();
                            try {
                                smsManager.sendTextMessage("+918880390936",null,msg,null,null);
                                Toast.makeText(TrainingInfo.this, "You are successfully added to this Training...", Toast.LENGTH_SHORT).show();
                                finish();
                            } catch (Exception e){
                                Toast.makeText(TrainingInfo.this, "Cannot process request at the moment. Please try again later!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {

                    if (isFormFilled()){

                        saveDataToFirebase();

                    } else {
                        Toast.makeText(TrainingInfo.this, "Field cannot be left blank!", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

    }

    private void saveDataToFirebase() {

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

        DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid().toString())
                .child("trainings_created");

        mUser.child(mData.getKey()).setValue(ed_t_name.getText().toString());

        if (imageUri!=null){
            final ProgressDialog pd = new ProgressDialog(TrainingInfo.this);
            pd.setTitle("Please Wait!");
            pd.setMessage("Uploading...");
            pd.show();
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("TrainingImages").child(mData.getKey());
            /*filePath.putFile(imageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            //noinspection VisibleForTests
                            mData.child("image_url").setValue(taskSnapshot.getDownloadUrl().toString());
                            pd.dismiss();
                            finish();
                            startActivity(new Intent(TrainingInfo.this, MainActivity.class));
                            if (btn_create_training.getText().equals("Update"))
                                Toast.makeText(TrainingInfo.this, "Changes Updated!", Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(TrainingInfo.this, "New Training Created!", Toast.LENGTH_SHORT).show();

                        }
                    }
            );*/
            iv_image.setDrawingCacheEnabled(true);
            iv_image.buildDrawingCache();
            Bitmap bitmap = iv_image.getDrawingCache();
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, boas);
            byte[] data = boas.toByteArray();
            filePath.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //noinspection VisibleForTests
                    mData.child("image_url").setValue(taskSnapshot.getDownloadUrl().toString());
                    pd.dismiss();
                    finish();
                    startActivity(new Intent(TrainingInfo.this, MainActivity.class));
                    if (btn_create_training.getText().equals("Update"))
                        Toast.makeText(TrainingInfo.this, "Changes Updated!", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(TrainingInfo.this, "New Training Created!", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            finish();
            startActivity(new Intent(TrainingInfo.this, MainActivity.class));
            if (btn_create_training.getText().equals("Update"))
                Toast.makeText(TrainingInfo.this, "Changes Updated!", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(TrainingInfo.this, "New Training Created!", Toast.LENGTH_SHORT).show();
        }


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
        ll_editBox.setEnabled(false);
        tv_change_location.setVisibility(View.INVISIBLE);

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

                trainer_id = dataSnapshot.child("user_id").getValue().toString();
                ed_t_name.setText(dataSnapshot.child("training_name").getValue().toString());
                et_location.setText(dataSnapshot.child("location").getValue().toString());
                ed_t_price.setText(dataSnapshot.child("price").getValue().toString());
                ed_t_mob.setText(dataSnapshot.child("mobile").getValue().toString());
                sp_cat.setSelection(Integer.parseInt(dataSnapshot.child("category").getValue().toString()));
                sp_avail.setSelection(Integer.parseInt(dataSnapshot.child("availabilty").getValue().toString()));
                ed_t_dur.setText(dataSnapshot.child("duration").getValue().toString());
                if (dataSnapshot.child("description").getValue()!=null)
                    ed_t_desc.setText(dataSnapshot.child("description").getValue().toString());

                if (!mAuth.getCurrentUser().getUid().toString().equals(dataSnapshot.child("user_id").getValue().toString())){

                    Query query = data.child("registered_users").orderByKey().equalTo(mAuth.getCurrentUser().getUid().toString());
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getChildrenCount() ==0 ){
                                btn_create_training.setVisibility(View.VISIBLE);
                                btn_create_training.setText("Register");
                            } else
                                btn_create_training.setVisibility(View.GONE);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    btn_create_training.setVisibility(View.GONE);
                    editAllowed = true;
                    invalidateOptionsMenu();
                }
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
            if (s.length() == 0)
                Toast.makeText(TrainingInfo.this, "Unable to fetch current location!", Toast.LENGTH_SHORT).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training_info_menu, menu);
        MenuItem item = menu.findItem(R.id.info_edit);
        if (!editAllowed)
            item.setVisible(false);
        if (!deleteAllowed)
            menu.findItem(R.id.info_delete).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home){
            goBack();
        } else if (item.getItemId() == R.id.info_edit){
            enableEdit();
        } else if (item.getItemId() == R.id.info_delete){
            deleteTraining();
            Toast.makeText(this, "Training Deleted!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(TrainingInfo.this, MyProfile.class));
            finishAffinity();
        }

        return true;
    }

    private void deleteTraining() {

        final DatabaseReference training_ref = FirebaseDatabase.getInstance().getReference().child("Trainings")
                .child(key);

        final String[] user_id = new String[1];
        final List<String> registered_users = new ArrayList<>();

        training_ref.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                user_id[0] = dataSnapshot.child("user_id").getValue().toString();

                for (DataSnapshot mData : dataSnapshot.child("registered_users").getChildren()){
                    registered_users.add(mData.getKey());
                }

                DatabaseReference user_ref = FirebaseDatabase.getInstance().getReference()
                        .child("Users").child(user_id[0]);

                user_ref.child("trainings_created").child(key).removeValue();

                for (String id : registered_users){

                    DatabaseReference reg_ref = FirebaseDatabase.getInstance().getReference()
                            .child("Users").child(id);

                    reg_ref.child("registered_trainings").child(key).removeValue();

                }

                training_ref.removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void enableEdit() {

        editAllowed = false;
        deleteAllowed = true;
        invalidateOptionsMenu();
        fab.setVisibility(View.VISIBLE);
        ed_t_name.setEnabled(true);
        et_location.setEnabled(true);
        ed_t_price.setEnabled(true);
        ed_t_mob.setEnabled(true);
        sp_avail.setEnabled(true);
        sp_cat.setEnabled(true);
        ed_t_dur.setEnabled(true);
        ed_t_desc.setEnabled(true);
        ll_editBox.setEnabled(true);
        tv_change_location.setVisibility(View.VISIBLE);
        btn_create_training.setText("Update");
        btn_create_training.setVisibility(View.VISIBLE);
        ed_t_name.setSelection(ed_t_name.getText().length()-1);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(ed_t_name, InputMethodManager.SHOW_IMPLICIT);

    }

    private void goBack() {

        if (key == null){
            startActivity(new Intent(TrainingInfo.this, MainActivity.class));
            finishAffinity();
        } else {
            finish();
        }

    }

    @Override
    public void onBackPressed() {

        goBack();

    }
}
