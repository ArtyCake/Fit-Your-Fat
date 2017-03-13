package com.artycake.fityourfat.fragments;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

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
    private int lastMinutes = 0;
    private int lastSeconds = 0;
    private boolean minutesScrolled = false;
    private boolean secondsScrolled = false;

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
        minutesPicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                if (lastMinutes == position && !minutesScrolled) {
                    openDialog(picker);
                }
                minutesScrolled = false;
                lastMinutes = position;
            }
        });
        minutesPicker.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int offset) {
                if (offset > 0) {
                    minutesScrolled = true;
                }
            }

            @Override
            public void onWheelSelected(int position) {
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
            }
        });
        secondsPicker.setOnItemSelectedListener(new WheelPicker.OnItemSelectedListener() {
            @Override
            public void onItemSelected(WheelPicker picker, Object data, int position) {
                if (lastSeconds == position && !secondsScrolled) {
                    openDialog(picker);
                }
                secondsScrolled = false;
                lastSeconds = position;
            }
        });
        secondsPicker.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int offset) {
                if (offset > 0) {
                    secondsScrolled = true;
                }
            }

            @Override
            public void onWheelSelected(int position) {
            }

            @Override
            public void onWheelScrollStateChanged(int state) {
            }
        });
        updateUI();
        return rootView;
    }

    private void openDialog(final WheelPicker picker) {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        alert.setTitle("Enter value");
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(picker.getCurrentItemPosition()));
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                if (value.isEmpty()) {
                    return;
                }
                int position = Integer.valueOf(value);
                picker.setSelectedItemPosition(position);
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.cancel();
            }
        });
        alert.show();
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
