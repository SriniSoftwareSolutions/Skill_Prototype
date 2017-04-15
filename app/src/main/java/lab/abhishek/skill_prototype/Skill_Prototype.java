package lab.abhishek.skill_prototype;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Abhishek on 15-Apr-17.
 */

public class Skill_Prototype extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
