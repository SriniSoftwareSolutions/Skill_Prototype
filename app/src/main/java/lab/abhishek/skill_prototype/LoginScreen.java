package lab.abhishek.skill_prototype;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class LoginScreen extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private Button btn_login, btn_fb_login, btn_google_login, btn_register;
    private LoginButton fb_login;
    private CallbackManager callbackManager;
    private FirebaseAuth mAuth;
    private GoogleApiClient mClient;
    private EditText et_userName, et_passWord;
    private ProgressDialog pd;
    private String imageUrl, userName, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this);
        setContentView(R.layout.activity_login_screen);

        fullScreen();
        mAuth = FirebaseAuth.getInstance();
        pd = new ProgressDialog(this);
        pd.setTitle("Please Wait!");
        pd.setMessage("Logging in...");

        btn_login = (Button) findViewById(R.id.btn_login);
        fb_login = (LoginButton) findViewById(R.id.fb_login);
        btn_fb_login = (Button) findViewById(R.id.btn_fb_login);
        btn_google_login = (Button) findViewById(R.id.btn_google_login);
        et_userName = (EditText) findViewById(R.id.et_username);
        et_passWord = (EditText) findViewById(R.id.et_password);
        btn_register = (Button) findViewById(R.id.btn_register);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        if (!isNetworkAvailable()){
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setCancelable(false);
            adb.setTitle("No Internet Connection!");
            adb.setMessage("Please Try Again Later...");
            adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            });
            adb.setNegativeButton("CANCEL",null);
            adb.show();
        }

        callbackManager = CallbackManager.Factory.create();
        fb_login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                FbLoginWithFirebase(loginResult.getAccessToken());
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback(){

                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try{
                                    imageUrl = object.getString("id");
                                    userName = object.getString("name");
                                    email = object.getString("email");
                                } catch (Exception e){
                                    //
                                }
                            }
                        }
                );
                Bundle param = new Bundle();
                param.putString("fields","id,name,email");
                request.setParameters(param);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginScreen.this, "Cancelled!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(LoginScreen.this, "Error!", Toast.LENGTH_SHORT).show();

            }
        });

        btn_fb_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fb_login.performClick();
            }
        });

        btn_google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(et_userName.getText().toString()) || TextUtils.isEmpty(et_passWord.getText().toString())){
                    Toast.makeText(LoginScreen.this, "Field cannot be left blank!", Toast.LENGTH_SHORT).show();
                } else if (et_passWord.getText().toString().length() < 9){
                    Toast.makeText(LoginScreen.this, "Password must be atleast 8 characters long!", Toast.LENGTH_SHORT).show();
                } else {
                    emailSignInWithFirebase(et_userName.getText().toString(), et_passWord.getText().toString());
                }
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginScreen.this, Register.class));
            }
        });

    }

    private void emailSignInWithFirebase(String s, String s1) {

        pd.show();
        mAuth.signInWithEmailAndPassword(s, s1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        pd.dismiss();
                        if (task.isSuccessful()){

                            getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","Email").apply();
                            startActivity(new Intent(LoginScreen.this, MainActivity.class));
                            finishAffinity();

                        } else {
                            Toast.makeText(LoginScreen.this, "E-Mail/Password combination not matching!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void googleSignIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mClient);
        startActivityForResult(signInIntent, 101);

    }

    private void FbLoginWithFirebase(AccessToken accessToken) {

        pd.show();
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            addUser();
                            getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","Fb").apply();
                        } else
                            Toast.makeText(LoginScreen.this, "Failure!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()){
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseWithGoogle(account);
                getProfileInfo(account);
            } else
                Toast.makeText(this, "Login Failed!", Toast.LENGTH_SHORT).show();

        } else {
            callbackManager.onActivityResult(requestCode,resultCode, data);
        }
    }

    private void getProfileInfo(GoogleSignInAccount account) {

        imageUrl = account.getPhotoUrl().toString();
        userName = account.getDisplayName().toString();
        email = account.getEmail().toString();

    }

    private void firebaseWithGoogle(GoogleSignInAccount account) {

        pd.show();
        AuthCredential credentials = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credentials)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            addUser();
                            getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","Gmail").apply();

                        } else
                            Toast.makeText(LoginScreen.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void addUser() {

        final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        Query myQuery = mDatabaseReference.orderByKey().equalTo(mAuth.getCurrentUser().getUid().toString());
        myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0){

                    String[] name = userName.split(" ");
                    DatabaseReference newUser = mDatabaseReference.child(mAuth.getCurrentUser().getUid().toString());
                    newUser.child("fname").setValue(name[0]);
                    try{
                        newUser.child("lname").setValue(""+ name[1]);
                    } catch (Exception e){
                        //
                    }
                    newUser.child("image_url").setValue(imageUrl);
                    newUser.child("email").setValue(email);
                    Toast.makeText(LoginScreen.this, "New User Added!", Toast.LENGTH_SHORT).show();

                }
                pd.dismiss();
                startActivity(new Intent(LoginScreen.this, MainActivity.class));
                finishAffinity();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
