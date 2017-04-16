package lab.abhishek.skill_prototype;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleApiClient mClient;
    private CircleImageView header_image;
    private TextView header_name, header_email, header_mob;
    private DatabaseReference mDatabaseReference;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerList.setAdapter(new DrawerAdapter(this));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_main);
        header_image = (CircleImageView) headerView.findViewById(R.id.header_image);
        header_name = (TextView) headerView.findViewById(R.id.header_name);
        header_email = (TextView) headerView.findViewById(R.id.header_email);
        header_mob = (TextView) headerView.findViewById(R.id.header_mob);

        setNavHeader();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_main, new MapFragment()).commit();

        takePermissions();
        if (locationEnabled()){
            //startActivity(new Intent(this, MapsActivity.class));
        } else {
            buildAlertMessageNoGps();
        }
    }

    private void setNavHeader() {

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid().toString());
        mDatabaseReference.keepSynced(true);
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try{
                    Picasso.with(MainActivity.this).load(dataSnapshot.child("image_url").getValue().toString()).placeholder(R.mipmap.userdp).into(header_image);
                } catch (Exception e){
                    Picasso.with(MainActivity.this).load(R.mipmap.userdp).into(header_image);
                }
                header_name.setText(dataSnapshot.child("fname").getValue()+" "+dataSnapshot.child("lname").getValue());
                header_email.setText(dataSnapshot.child("email").getValue().toString());
                try{
                    header_mob.setText(dataSnapshot.child("mobile").getValue().toString());
                } catch (Exception e){
                    header_mob.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private boolean locationEnabled() {

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    private void takePermissions() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);

        }

    }

    private void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_signOut){

            appSignOut();

            return true;
        } else if (id == R.id.action_exit){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void appSignOut() {

        String tag  = getSharedPreferences("srini_prefs",MODE_PRIVATE).getString("LoggedIn","");
        getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","").apply();
        FirebaseAuth.getInstance().signOut();

        switch (tag){
            case "Fb" :
                fbLogOut();
                break;

            case "Gmail" :
                googleLogOut();
                break;

            case "Email" :
                break;
        }

        startActivity(new Intent(MainActivity.this, LoginScreen.class));
        finish();

    }

    private void fbLogOut() {

        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                LoginManager.getInstance().logOut();
            }
        }).executeAsync();
        AccessToken.setCurrentAccessToken(null);

    }

    private void googleLogOut() {

        Auth.GoogleSignInApi.signOut(mClient);

    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        try {
            mClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */, null /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        } catch (Exception e){
            //
        }

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            drawer.closeDrawer(GravityCompat.START);
            switch (position){
                case 0 :
                    startActivity(new Intent(MainActivity.this, TrainingInfo.class));
                    finish();
                    break;
                case 1 :
                    Toast.makeText(MainActivity.this, "About Us", Toast.LENGTH_SHORT).show();
                    break;
                case 2 :
                    Toast.makeText(MainActivity.this, "Contact Us", Toast.LENGTH_SHORT).show();
                    break;
                case 3 :
                    appSignOut();
                    break;
                case 4 :
                    finishAffinity();
            }

        }
    }
}
