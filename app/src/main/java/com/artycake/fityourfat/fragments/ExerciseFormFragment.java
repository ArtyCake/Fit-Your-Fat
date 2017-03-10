package com.artycake.fityourfat.fragments;

import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExerciseFormFragment extends Fragment {
    @BindView(R.id.exercise_name)
    TextView exerciseName;
    @BindView(R.id.exercise_desc)
    TextView exerciseDesc;
    @BindView(R.id.minutes)
    WheelPicker minutesPicker;
    @BindView(R.id.seconds)
    WheelPicker secondsPicker;

    private Exercise exercise = new Exercise();

    public ExerciseFormFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_exercise_form, container, false);
        ButterKnife.bind(this, rootView);
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            values.add(String.format(Locale.getDefault(), "%02d", i));
        }
        minutesPicker.setData(values);
        secondsPicker.setData(values);
        updateUI();
        return rootView;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
    }

    private void updateUI() {
        exerciseName.setText(exercise.getName());
        exerciseDesc.setText(exercise.getDescription());
        int minutes = exercise.getDuration() / 60;
        int seconds = exercise.getDuration() - minutes * 60;
        minutesPicker.setSelectedItemPosition(minutes);
        secondsPicker.setSelectedItemPosition(seconds);
    }

    public boolean validate() {
        String name = exerciseName.getText().toString();
        int duration = getDuration();
        if (name.isEmpty()) {
            Snackbar.make(getView(), R.string.form_err_name_required, BaseTransientBottomBar.LENGTH_LONG).show();
            return false;
        }
        if (duration == 0) {
            Snackbar.make(getView(), R.string.form_err_duration, BaseTransientBottomBar.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private int getDuration() {
        int minutes = minutesPicker.getCurrentItemPosition();
        int seconds = secondsPicker.getCurrentItemPosition();
        return minutes * 60 + seconds;
    }

    public Exercise getExercise() {
        exercise.setName(exerciseName.getText().toString());
        exercise.setDescription(exerciseDesc.getText().toString());
        exercise.setDuration(getDuration());
        return exercise;
    }
}
