package com.artycake.fityourfat.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.models.Workout;
import com.artycake.fityourfat.utils.RealmController;
import com.artycake.fityourfat.utils.UserPrefs;

import io.realm.Realm;
import io.realm.RealmList;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (UserPrefs.getInstance(this).getBoolPref(UserPrefs.FIRST_LAUNCH, true)) {
            createDemoWorkout();
            UserPrefs.getInstance(this).putPreferences(UserPrefs.FIRST_LAUNCH, false);
        }
        startActivity(new Intent(this, MainActivity.class));
    }

    private void createDemoWorkout() {
        final RealmController controller = RealmController.getInstance(this);
        Realm realm = controller.getRealm();
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Workout workout = realm.createObject(Workout.class, controller.getNewWorkoutId());
                workout.setName("Tabata");
                workout.setLaps(8);
                workout.setWarmUpTime(0);
                Exercise exercise = realm.createObject(Exercise.class, controller.getNewExerciseId());
                exercise.setName("Work");
                exercise.setDuration(20);
                workout.getExercises().add(exercise);
                Exercise secondExercise = realm.createObject(Exercise.class, controller.getNewExerciseId());
                secondExercise.setName("Rest");
                secondExercise.setDuration(10);
                workout.getExercises().add(secondExercise);
                UserPrefs.getInstance(SplashActivity.this).putPreferences(UserPrefs.CURRENT_WORKOUT, workout.getId());
            }
        });
    }
}
