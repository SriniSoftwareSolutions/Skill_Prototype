package lab.abhishek.skill_prototype;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrainingSearch extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView recyclerView;
    private DatabaseReference mDatabaseReference;
    private List<Trainings> trainingsList;
    private List<Trainings> tempList;
    private boolean sort_price, sort_location, sort_name;
    private double myLat, myLon;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Trainings");
        mDatabaseReference.keepSynced(true);
        trainingsList = new ArrayList<>();

        String latlon[] = getIntent().getStringExtra("myLatLon").split(" ");
        myLat = Double.parseDouble(latlon[0]);
        myLon = Double.parseDouble(latlon[1]);
        prepareOriginalList();

        //query = mDatabaseReference;
        sort_location = sort_name = sort_price = false;

        recyclerView = (RecyclerView) findViewById(R.id.search_training_rv);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager lm = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(lm);


        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(TrainingSearch.this,
                recyclerView,
                new RecyclerTouchListener.OnItemClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        Intent intent = new Intent(TrainingSearch.this, TrainingInfo.class);
                        intent.putExtra("key",tempList.get(position).getId());
                        intent.putExtra("action","view");
                        startActivity(intent);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

    }

    @Override
    protected void onResume() {
        super.onResume();



    }

    private void prepareOriginalList() {

        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot mData : dataSnapshot.getChildren()){

                    String image_url = "";
                    if (mData.child("image_url").getValue() != null)
                        image_url = mData.child("image_url").getValue().toString();

                    Location curr = new Location("curr");
                    curr.setLongitude(Double.parseDouble(mData.child("lon").getValue().toString()));
                    curr.setLatitude(Double.parseDouble(mData.child("lat").getValue().toString()));

                    Location myLoc = new Location("myLoc");
                    myLoc.setLatitude(myLat);
                    myLoc.setLongitude(myLon);

                    Trainings training = new Trainings(
                            mData.getKey(),
                            mData.child("training_name").getValue().toString(),
                            mData.child("location").getValue().toString(),
                            mData.child("price").getValue().toString(),
                            mData.child("mobile").getValue().toString(),
                            image_url,
                            mData.child("user_id").getValue().toString(),
                            (int) curr.distanceTo(myLoc)
                    );
                    trainingsList.add(training);

                }

                tempList = new ArrayList<Trainings>(trainingsList);
                updateList();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateList() {

        TrainingAdapter adapter = new TrainingAdapter(tempList , TrainingSearch.this);
        recyclerView.setAdapter(adapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){

            startActivity(new Intent(this, MainActivity.class));
            finishAffinity();
            return true;

        } else if (item.getItemId() == R.id.action_filter){

            PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_filter));
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.sort_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    switch (item.getItemId()){

                        case R.id.sort_location :

                            if (sort_location){

                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        return o2.getDist() - o1.getDist();
                                    }
                                });
                                sort_location = false;
                                Toast.makeText(TrainingSearch.this, "Farthest location first", Toast.LENGTH_SHORT).show();

                            } else {

                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        return o1.getDist() - o2.getDist();
                                    }
                                });
                                sort_location = true;
                                Toast.makeText(TrainingSearch.this, "Nearest location first", Toast.LENGTH_SHORT).show();

                            }
                            updateList();
                            break;

                        case R.id.sort_name :

                            if (sort_name){
                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        return o1.getTraining_name().compareToIgnoreCase(o2.getTraining_name());
                                    }
                                });
                                sort_name = false;
                                Toast.makeText(TrainingSearch.this, "Name Ascending", Toast.LENGTH_SHORT).show();
                            } else {
                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        return o2.getTraining_name().compareToIgnoreCase(o1.getTraining_name());
                                    }
                                });
                                sort_name = true;
                                Toast.makeText(TrainingSearch.this, "Name Descending", Toast.LENGTH_SHORT).show();
                            }
                            updateList();
                            break;

                        case R.id.sort_price :

                            if (sort_price){
                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        double price1 = Double.parseDouble(o1.getPrice());
                                        double price2 = Double.parseDouble(o2.getPrice());
                                        return Double.compare(price1, price2);
                                    }
                                });
                                sort_price = false;
                                Toast.makeText(TrainingSearch.this, "Lowest price first", Toast.LENGTH_SHORT).show();
                            } else {
                                Collections.sort(tempList, new Comparator<Trainings>() {
                                    @Override
                                    public int compare(Trainings o1, Trainings o2) {
                                        double price1 = Double.parseDouble(o1.getPrice());
                                        double price2 = Double.parseDouble(o2.getPrice());
                                        return Double.compare(price2, price1);
                                    }
                                });
                                sort_price = true;
                                Toast.makeText(TrainingSearch.this, "Highest price first", Toast.LENGTH_SHORT).show();
                            }
                            updateList();
                            break;
                    }

                    return true;

                }
            });
            popup.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.search_training_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(this);

        if (getIntent().getBooleanExtra("search",false))
            MenuItemCompat.expandActionView(menuItem);


        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() == 0){
            tempList = new ArrayList<Trainings>(trainingsList);
            updateList();
        } else {
            tempList = new ArrayList<Trainings>();
            for (Trainings train : trainingsList){

                if (train.getTraining_name().toLowerCase().contains(newText.toLowerCase()) ||
                        train.getLocation().toLowerCase().contains(newText.toLowerCase()))
                    tempList.add(train);

            }
            updateList();
        }

        return true;
    }
}
