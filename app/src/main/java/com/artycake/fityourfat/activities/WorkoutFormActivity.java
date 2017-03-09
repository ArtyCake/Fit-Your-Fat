package com.artycake.fityourfat.activities;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

    @BindView(R.id.workout_name)
    EditText workoutName;
    @BindView(R.id.workout_laps)
    EditText workoutLaps;
    @BindView(R.id.exercises)
    RecyclerView exercisesList;
    @BindView(android.R.id.content)
    View rootView;

    private ExercisesAdapter adapter;
    private List<Exercise> exercises = new ArrayList<>();
    private Workout workout;

    public static final String ID = "id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_form);
        ButterKnife.bind(this);
        int workoutId = getIntent().getIntExtra(ID, -1);
        if (workoutId != -1) {
            workout = RealmController.getInstance(this).getWorkout(workoutId);
            exercises = workout.getExercises();
            Log.d("TAG", String.valueOf(exercises.size()));
            workoutName.setText(workout.getName());
            workoutLaps.setText(String.valueOf(workout.getLaps()));
        } else {
            workout = RealmController.getInstance(this).getRealm()
                    .createObject(Workout.class, RealmController.getInstance(this).getNewWorkoutId());
        }

        adapter = new ExercisesAdapter(exercises);
        exercisesList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        exercisesList.setLayoutManager(layoutManager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.workout_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            updateWorkout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWorkout() {
        final String name = workoutName.getText().toString();
        String lapsValue = workoutLaps.getText().toString();
        if (name.isEmpty() || lapsValue.isEmpty()) {
            Snackbar.make(rootView, R.string.form_err_required, Snackbar.LENGTH_LONG).show();
            return;
        }
        if (exercises.size() == 0) {
            Snackbar.make(rootView, R.string.form_err_exercises, Snackbar.LENGTH_LONG).show();
            return;
        }
        final int laps = Integer.valueOf(lapsValue);
        RealmController.getInstance(this).getRealm()
                .executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        workout.setName(name);
                        workout.setLaps(laps);
                        workout.getExercises().clear();
                        workout.getExercises().addAll(exercises);
                    }
                });
    }

    @OnClick(R.id.add_exercise)
    public void addExercise() {
        // start activity;
    }
}
