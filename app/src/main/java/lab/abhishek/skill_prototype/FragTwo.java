package lab.abhishek.skill_prototype;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragTwo extends Fragment {

    private DatabaseReference mData;
    private FirebaseAuth mAuth;
    private List<Trainings> trainingsList;
    private RecyclerView recyclerView;
    private TrainingAdapter adapter;

    public FragTwo() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frag_two, container, false);

        trainingsList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference().child("Trainings");
        Query query = mData.orderByChild("user_id").equalTo(mAuth.getCurrentUser()
                .getUid().toString());
        query.keepSynced(true);
        mData.keepSynced(true);
        recyclerView = (RecyclerView) view.findViewById(R.id.frag_two_rv);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager lm = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(lm);
        adapter = new TrainingAdapter(trainingsList , getContext());
        recyclerView.setAdapter(adapter);

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot mData : dataSnapshot.getChildren()){

                    String image_url = "";
                    if (mData.child("image_url").getValue() != null)
                        image_url = mData.child("image_url").getValue().toString();

                    Trainings training = new Trainings(
                            mData.getKey(),
                            mData.child("training_name").getValue().toString(),
                            mData.child("location").getValue().toString(),
                            mData.child("price").getValue().toString(),
                            mData.child("mobile").getValue().toString(),
                            image_url,
                            mData.child("user_id").getValue().toString()
                    );

                    trainingsList.add(training);
                    adapter.notifyDataSetChanged();

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(),
                recyclerView,
                new RecyclerTouchListener.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent(getContext(), TrainingInfo.class);
                        intent.putExtra("key", trainingsList.get(position).getId() );
                        intent.putExtra("action","view");
                        startActivity(intent);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

        return view;
    }

}
