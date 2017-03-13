package com.artycake.fityourfat.activities;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.adapters.ExercisesAdapter;
import com.artycake.fityourfat.fragments.ExerciseFormFragment;
import com.artycake.fityourfat.fragments.WorkoutFormFragment;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.models.Workout;
import com.artycake.fityourfat.utils.RealmController;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

public class WorkoutFormActivity extends AppCompatActivity {

    @BindView(R.id.add_exercise)
    FloatingActionButton addExercise;

    private List<Exercise> exercises = new ArrayList<>();
    private FragmentManager fragmentManager;
    private Workout workout;
    private WorkoutFormFragment workoutFormFragment;
    private ExerciseFormFragment exerciseFormFragment;
    private boolean inExercise = false;
    public static final String ID = "id";
    private int newExerciseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_form);
        ButterKnife.bind(this);
        fragmentManager = getSupportFragmentManager();
        int workoutId = getIntent().getIntExtra(ID, -1);
        if (workoutId != -1) {
            workout = RealmController.getInstance(this).getWorkout(workoutId);
            exercises = workout.getExercises();
            getSupportActionBar().setTitle(workout.getName());
        } else {
            workout = new Workout();
            workout.setId(RealmController.getInstance(this).getNewWorkoutId());
            getSupportActionBar().setTitle(R.string.new_workout_title);
        }
        workoutFormFragment = (WorkoutFormFragment) fragmentManager.findFragmentById(R.id.container);
        if (workoutFormFragment == null) {
            workoutFormFragment = new WorkoutFormFragment();
            workoutFormFragment.setWorkout(workout);
            fragmentManager.beginTransaction().add(R.id.container, workoutFormFragment).commit();
        } else {
            workoutFormFragment = new WorkoutFormFragment();
            workoutFormFragment.setWorkout(workout);
            fragmentManager.beginTransaction().replace(R.id.container, workoutFormFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            if (inExercise) {
                storeExercise();
            } else {
                updateWorkout();
            }
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
                inExercise = false;
                addExercise.show();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeExercise() {
        if (exerciseFormFragment.validate()) {
            Exercise exercise = exerciseFormFragment.getExercise();
            boolean found = false;
            int index = 0;
            for (int i = 0; i < exercises.size(); i++) {
                if (exercises.get(i).getId() == exercise.getId()) {
                    index = i;
                    found = true;
                }
            }
            if (!found) {
                exercises.add(exercise);
            } else {
                exercises.set(index, exercise);
            }
            fragmentManager.popBackStack();
            workoutFormFragment.updateExercises(exercises);
            inExercise = false;
            addExercise.show();
        }
    }

    public void openExercise(Exercise exercise) {
        exerciseFormFragment = new ExerciseFormFragment();
        exerciseFormFragment.setExercise(exercise);
        fragmentManager.beginTransaction()
                .replace(R.id.container, exerciseFormFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack(exercise.getName())
                .commit();
        inExercise = true;
        addExercise.hide();
    }

    private void updateWorkout() {
        if (workoutFormFragment.validate()) {
            exercises = workoutFormFragment.getExercises();
            final Workout workout = workoutFormFragment.getWorkout();
            RealmController.getInstance(this).getRealm()
                    .executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            Workout storedWorkout = realm.copyToRealmOrUpdate(workout);
                            storedWorkout.getExercises().clear();
                            storedWorkout.getExercises().addAll(exercises);
                        }
                    });
            finish();
        }
    }

    @OnClick(R.id.add_exercise)
    public void addExercise() {
        exerciseFormFragment = new ExerciseFormFragment();
        if (newExerciseId == -1) {
            newExerciseId = RealmController.getInstance(this).getNewExerciseId();
        } else {
            newExerciseId++;
        }
        Exercise exercise = new Exercise();
        exercise.setId(newExerciseId);
        exerciseFormFragment.setExercise(exercise);
        fragmentManager.beginTransaction()
                .replace(R.id.container, exerciseFormFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .addToBackStack("New Exercise")
                .commit();
        inExercise = true;
        addExercise.hide();
    }
}
