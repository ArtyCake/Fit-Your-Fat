package com.artycake.fityourfat.fragments;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.activities.WorkoutFormActivity;
import com.artycake.fityourfat.adapters.ExercisesAdapter;
import com.artycake.fityourfat.drag.OnStartDragListener;
import com.artycake.fityourfat.drag.SimpleItemTouchHelperCallback;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.models.Workout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutFormFragment extends Fragment {

    @BindView(R.id.workout_name)
    EditText workoutName;
    @BindView(R.id.workout_laps)
    EditText workoutLaps;
    @BindView(R.id.exercises)
    RecyclerView exercisesList;

    private ExercisesAdapter adapter;
    private Workout workout = new Workout();
    private List<Exercise> exercises = new ArrayList<>();

    public WorkoutFormFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_workout_form, container, false);
        ButterKnife.bind(this, rootView);
        adapter = new ExercisesAdapter(exercises);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        adapter.setOnStartDragListener(new OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                itemTouchHelper.startDrag(viewHolder);
            }
        });
        adapter.setOnItemClick(new ExercisesAdapter.OnItemClick() {
            @Override
            public void onClick(Exercise exercise) {
                ((WorkoutFormActivity) getActivity()).openExercise(exercise);
            }
        });
        exercisesList.setAdapter(adapter);
        itemTouchHelper.attachToRecyclerView(exercisesList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        exercisesList.setLayoutManager(layoutManager);
        updateUI();
        return rootView;
    }

    public void setWorkout(Workout workout) {
        this.workout = workout;
        exercises.clear();
        if (workout.getExercises() != null) {
            exercises.addAll(workout.getExercises());
        }
    }

    private void updateUI() {
        workoutName.setText(workout.getName());
        workoutLaps.setText(String.valueOf(workout.getLaps()));
        adapter.notifyDataSetChanged();
    }

    public void updateExercises(List<Exercise> exercises) {
        this.exercises.clear();
        this.exercises.addAll(exercises);
        adapter.notifyDataSetChanged();
    }

    public List<Exercise> getExercises() {
        return exercises;
    }

    public boolean validate() {
        String name = workoutName.getText().toString();
        String lapsValue = workoutLaps.getText().toString();
        if (name.isEmpty() || lapsValue.isEmpty()) {
            Snackbar.make(getView(), R.string.form_err_required, Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (Integer.valueOf(lapsValue) == 0) {
            Snackbar.make(getView(), R.string.form_err_laps, Snackbar.LENGTH_LONG).show();
            return false;
        }
        if (exercises.size() == 0) {
            Snackbar.make(getView(), R.string.form_err_exercises, Snackbar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public Workout getWorkout() {
        workout.setName(workoutName.getText().toString());
        int laps = Integer.valueOf(workoutLaps.getText().toString());
        workout.setLaps(laps);
        return workout;
    }
}
