package lab.abhishek.skill_prototype;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TrainingSearch extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Trainings");
        mDatabaseReference.keepSynced(true);

        recyclerView = (RecyclerView) findViewById(R.id.search_training_rv);
        recyclerView.setHasFixedSize(true);
        GridLayoutManager lm = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(lm);

    }

    @Override
    protected void onStart() {
        super.onStart();

        final FirebaseRecyclerAdapter<Trainings, TrainingHolder> mFirebaseAdapter =
                new FirebaseRecyclerAdapter<Trainings, TrainingHolder>(
                        Trainings.class,
                        R.layout.training_card,
                        TrainingHolder.class,
                        mDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(TrainingHolder viewHolder, Trainings model, int position) {
                        final String key = getRef(position).getKey();
                        viewHolder.setvalues(model.getTraining_name(), model.getLocation(), model.getMobile(), model.getPrice(),model.getImage_url(), getApplicationContext());
                        //progressDialog.dismiss();

                        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(TrainingSearch.this, TrainingInfo.class);
                                intent.putExtra("key",key);
                                intent.putExtra("action","view");
                                startActivity(intent);
                            }
                        });

                        /*viewHolder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {

                                PopupMenu popup = new PopupMenu(v.getContext(),v);
                                MenuInflater inflater = popup.getMenuInflater();
                                inflater.inflate(R.menu.popup_options, popup.getMenu());
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {

                                        Intent intent = new Intent(TrainingSearch.this, TrainingInfo.class);

                                        switch (item.getItemId()){

                                            case R.id.action_view :
                                                intent.putExtra("key",key);
                                                intent.putExtra("action","view");
                                                startActivity(intent);
                                                return true;

                                            case R.id.action_edit :
                                                intent.putExtra("key",key);
                                                intent.putExtra("action","edit");
                                                startActivity(intent);
                                                return true;

                                            case R.id.action_delete :
                                                mDatabaseReference.child(key).removeValue();
                                                return true;
                                        }

                                        return false;
                                    }
                                });
                                popup.show();
                                return true;

                            }
                        });*/

                    }
                };

        recyclerView.setAdapter(mFirebaseAdapter);

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
