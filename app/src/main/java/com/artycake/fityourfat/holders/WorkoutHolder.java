package com.artycake.fityourfat.holders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.models.Workout;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by artycake on 3/9/17.
 */

public class WorkoutHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.workout_name)
    TextView workoutName;
    @BindView(R.id.workout_laps)
    TextView workoutLaps;
    @BindView(R.id.workout_edit)
    ImageButton workoutEdit;
    @BindView(R.id.workout_delete)
    ImageButton workoutDelete;

    public WorkoutHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateUI(Workout workout) {
        workoutName.setText(workout.getName());
        workoutLaps.setText(itemView.getContext().getResources().getString(R.string.workout_laps, workout.getLaps()));
    }

    public void setOnEditClick(View.OnClickListener onEditClick) {
        workoutEdit.setOnClickListener(onEditClick);
    }

    public void setOnDeleteClick(View.OnClickListener onDeleteClick) {
        workoutDelete.setOnClickListener(onDeleteClick);
    }

    public void enableDelete(boolean enable) {
        workoutDelete.setEnabled(enable);
    }
}
