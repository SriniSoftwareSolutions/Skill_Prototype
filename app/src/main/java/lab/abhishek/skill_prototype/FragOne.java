package lab.abhishek.skill_prototype;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragOne extends Fragment {

    private CircleImageView image;
    private ImageButton ib_edit;
    private EditText fname, lname, mobile, email;
    private TextView location;
    private Button btn;
    private FirebaseAuth mAuth;
    private double lat, lon;
    private Uri imageUri;

    public FragOne() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frag_one, container, false);

        image = (CircleImageView) view.findViewById(R.id.frag1_iv_userImage);
        ib_edit = (ImageButton) view.findViewById(R.id.frag1_ib_edit);
        fname = (EditText) view.findViewById(R.id.frag1_ed_fname);
        lname = (EditText) view.findViewById(R.id.frag1_ed_lname);
        mobile = (EditText) view.findViewById(R.id.frag1_ed_mobile);
        email = (EditText) view.findViewById(R.id.frag1_ed_email);
        location = (TextView) view.findViewById(R.id.frag1_ed_location);
        btn = (Button) view.findViewById(R.id.frag1_btn_register_form);
        mAuth = FirebaseAuth.getInstance();

        disableEdits();
        setValues();

        ib_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableEdit();
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 201);
            }
        });

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(getActivity()), 101);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (testField()){
                    updateData();
                    disableEdits();
                } else {
                    Toast.makeText(getContext(), "Required Fields Cannot Be Left Blank!", Toast.LENGTH_LONG).show();
                }

            }
        });

        return view;
    }

    private void updateData() {

        final DatabaseReference newUser = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(mAuth.getCurrentUser().getUid().toString());
        newUser.keepSynced(true);

        newUser.child("fname").setValue(fname.getText().toString().trim());
        newUser.child("lname").setValue(lname.getText().toString().trim());
        newUser.child("location").setValue(location.getText().toString().trim());
        newUser.child("latitude").setValue(""+lat);
        newUser.child("longitude").setValue(""+lon);
        newUser.child("mobile").setValue(mobile.getText().toString().trim());
        newUser.child("email").setValue(email.getText().toString().trim());

        if (imageUri!=null){
            final ProgressDialog pd = new ProgressDialog(getContext());
            pd.setTitle("Please Wait");
            pd.setMessage("Updating...");
            pd.show();
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("UserImage").child(imageUri.getLastPathSegment());
            filePath.putFile(imageUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //noinspection VisibleForTests
                            newUser.child("image_url").setValue(taskSnapshot.getDownloadUrl().toString());
                            pd.dismiss();
                            Toast.makeText(getContext(), "Details Updated!", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

        } else {
            Toast.makeText(getContext(), "Details Updated!", Toast.LENGTH_SHORT).show();
        }

    }

    private boolean testField() {

        if (TextUtils.isEmpty(fname.getText().toString()) ||
                TextUtils.isEmpty(lname.getText().toString()) ||
                TextUtils.isEmpty(location.getText().toString()) ||
                TextUtils.isEmpty(mobile.getText().toString()) ||
                TextUtils.isEmpty(email.getText().toString()))

            return false;

        return true;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK){
            Place place = PlacePicker.getPlace(data,getContext());
            location.setText(place.getName());
            lat = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
        } else if (requestCode == 201 && resultCode == RESULT_OK){
            imageUri = data.getData();
            image.setImageURI(imageUri);
        }

    }

    private void enableEdit() {

        image.setEnabled(true);
        fname.setEnabled(true);
        lname.setEnabled(true);
        location.setEnabled(true);
        mobile.setEnabled(true);
        ib_edit.setVisibility(View.INVISIBLE);
        btn.setVisibility(View.VISIBLE);

    }

    private void disableEdits() {

        image.setEnabled(false);
        fname.setEnabled(false);
        lname.setEnabled(false);
        mobile.setEnabled(false);
        email.setEnabled(false);
        location.setEnabled(false);
        btn.setVisibility(View.GONE);
        ib_edit.setVisibility(View.VISIBLE);

    }

    private void setValues(){

        DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid().toString());
        mData.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot data) {

                try {
                    Picasso.with(getContext()).load(data.child("image_url").getValue().toString()).
                            placeholder(R.mipmap.userdp).networkPolicy(NetworkPolicy.OFFLINE).into(image, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(getContext()).load(data.child("image_url").getValue().toString()).placeholder(R.mipmap.userdp).into(image);
                        }
                    });
                } catch (Exception e){
                    Picasso.with(getContext()).load(R.mipmap.userdp).into(image);
                }

                fname.setText(data.child("fname").getValue().toString());
                lname.setText(data.child("lname").getValue().toString());
                try {
                    location.setText(data.child("location").getValue().toString());
                } catch (Exception e){
                    //
                }
                try {
                    mobile.setText(data.child("mobile").getValue().toString());
                } catch (Exception e){

                }

                try {
                    lat = Double.parseDouble(data.child("latitude").getValue().toString());
                    lon = Double.parseDouble(data.child("longitude").getValue().toString());
                } catch (Exception e){

                }

                email.setText(data.child("email").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

}
