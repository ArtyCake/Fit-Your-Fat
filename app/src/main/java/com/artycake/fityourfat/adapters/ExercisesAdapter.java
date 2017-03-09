package com.artycake.fityourfat.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.holders.ExerciseHolder;
import com.artycake.fityourfat.models.Exercise;

import java.util.List;

/**
 * Created by artycake on 3/9/17.
 */

public class ExercisesAdapter extends RecyclerView.Adapter<ExerciseHolder> {
    private List<Exercise> exercises;
    private OnItemClick onItemClick;

    public ExercisesAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    @Override
    public ExerciseHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseHolder(view);
    }

    @Override
    public void onBindViewHolder(final ExerciseHolder holder, int position) {
        holder.updateUI(exercises.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onClick(exercises.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return exercises.size();
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemClick {
        void onClick(Exercise exercise);
    }
}
