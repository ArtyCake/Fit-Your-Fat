package com.artycake.fityourfat.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Exercise;
import com.artycake.fityourfat.utils.TextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by artycake on 3/9/17.
 */

public class ExerciseHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.exercise_name)
    TextView exerciseName;
    @BindView(R.id.exercise_time)
    TextView exerciseTime;

    public ExerciseHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateUI(Exercise exercise) {
        exerciseName.setText(exercise.getName());
        exerciseTime.setText(TextHelper.formatTime(exercise.getDuration()));
    }
}
