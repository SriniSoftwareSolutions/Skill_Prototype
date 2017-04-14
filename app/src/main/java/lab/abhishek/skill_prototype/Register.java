package lab.abhishek.skill_prototype;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Register extends AppCompatActivity {

    private EditText et_fname, et_lname, et_location, et_email, et_password;
    private Button btn_register;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullScreen();
        mAuth = FirebaseAuth.getInstance();

        btn_register = (Button) findViewById(R.id.btn_register_form);
        et_fname = (EditText) findViewById(R.id.ed_fname);
        et_lname = (EditText) findViewById(R.id.ed_lname);
        et_location = (EditText) findViewById(R.id.ed_location);
        et_email = (EditText) findViewById(R.id.ed_email);
        et_password = (EditText) findViewById(R.id.ed_password);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testField()){
                    if (et_password.getText().toString().length() < 9){
                        Toast.makeText(Register.this, "Password must be atleast 8 characters long!", Toast.LENGTH_SHORT).show();
                    } else {
                        createUserWithFirebase(et_email.getText().toString(), et_password.getText().toString());
                    }
                } else {
                    Toast.makeText(Register.this, "Required Fields Cannot Be Left Blank!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void createUserWithFirebase(String s, String s1) {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Please Wait!");
        pd.setMessage("Logging In...");
        pd.show();
        mAuth.createUserWithEmailAndPassword(s, s1)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        pd.dismiss();
                        if (task.isSuccessful()){

                            getSharedPreferences("srini_prefs",MODE_PRIVATE).edit().putString("LoggedIn","Email").apply();
                            startActivity(new Intent(Register.this, MainActivity.class));
                            finishAffinity();

                        } else {
                            Toast.makeText(Register.this, "E-mail already used!", Toast.LENGTH_SHORT).show();
                        }

                    }
                });

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
