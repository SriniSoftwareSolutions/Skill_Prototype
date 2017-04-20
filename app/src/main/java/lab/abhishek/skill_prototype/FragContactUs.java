package lab.abhishek.skill_prototype;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.Context.INPUT_METHOD_SERVICE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragContactUs extends Fragment {

    private EditText ed_name, ed_email, ed_msg;
    private Button btn_send;
    private String number;

    public FragContactUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        getActivity().setTitle("Contact Us");
        View view = inflater.inflate(R.layout.fragment_frag_contact_us, container, false);

        ed_name = (EditText) view.findViewById(R.id.ed_contact_name);
        ed_email = (EditText) view.findViewById(R.id.ed_contact_email);
        btn_send = (Button) view.findViewById(R.id.btn_contact_send);
        ed_msg = (EditText) view.findViewById(R.id.ed_contact_msg);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseReference mData = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid().toString());
        mData.keepSynced(true);

        mData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ed_name.setText(dataSnapshot.child("fname").getValue().toString() + " " + dataSnapshot.child("lname").getValue().toString());
                try{
                    ed_email.setText(dataSnapshot.child("email").getValue().toString());
                } catch (Exception e){
                    //
                    ed_email.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
                try{
                    number = dataSnapshot.child("mobile").getValue().toString();
                } catch (Exception e){

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("Messages").push();
                data.child("email").setValue(ed_email.getText().toString());
                data.child("name").setValue(ed_name.getText().toString());
                data.child("msg").setValue(ed_msg.getText().toString());
                data.child("userId").setValue(mAuth.getCurrentUser().getUid().toString());
                if (number != null)
                    data.child("mobile").setValue(number);
                Toast.makeText(getContext(), "Message Sent Successfully!", Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_main, new MapFragment()).commit();


            }
        });

        ed_msg.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = (MenuItem) menu.findItem(R.id.main_search);
        item.setVisible(false);
    }
}
