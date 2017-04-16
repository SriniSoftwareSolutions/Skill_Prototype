package lab.abhishek.skill_prototype;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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
public class FragThree extends Fragment {

    private DatabaseReference mData;
    private FirebaseAuth mAuth;
    private List<Trainings> trainingsList;
    private RecyclerView recyclerView;
    private List<String> keyList;
    private TrainingAdapter adapter;

    public FragThree() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_frag_three, container, false);

        mAuth = FirebaseAuth.getInstance();
        mData = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getCurrentUser().getUid().toString())
                .child("registered_trainings");
        mData.keepSynced(true);

        keyList = new ArrayList<>();
        trainingsList = new ArrayList<>();
        adapter = new TrainingAdapter(trainingsList, getContext());
        recyclerView = (RecyclerView) view.findViewById(R.id.frag_three_rv);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager lm = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(lm);
        recyclerView.setAdapter(adapter);

        mData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot myData : dataSnapshot.getChildren()){

                    keyList.add(myData.getKey());

                }

                new AsyncTask<Void, Void, Void>(){

                    @Override
                    protected Void doInBackground(Void... params) {
                        getTrainingData();
                        return null;
                    }
                }.execute();
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

    private void getTrainingData() {

        final DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("Trainings");
        data.keepSynced(true);

        for (final String key : keyList){

            Query query = data.orderByKey().equalTo(key);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    dataSnapshot = dataSnapshot.child(key);
                    String image_url = "";
                    if (dataSnapshot.child("image_url").getValue() != null)
                        image_url = dataSnapshot.child("image_url").getValue().toString();

                    Trainings training = new Trainings(
                            dataSnapshot.getKey(),
                            dataSnapshot.child("training_name").getValue().toString(),
                            dataSnapshot.child("location").getValue().toString(),
                            dataSnapshot.child("price").getValue().toString(),
                            dataSnapshot.child("mobile").getValue().toString(),
                            image_url,
                            dataSnapshot.child("user_id").getValue().toString()
                    );
                    trainingsList.add(training);
                    adapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

    }

}
