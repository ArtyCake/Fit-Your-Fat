package com.artycake.fityourfat.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.holders.WorkoutHolder;
import com.artycake.fityourfat.models.Workout;

import java.util.List;

/**
 * Created by artycake on 3/9/17.
 */

public class WorkoutsAdapter extends RecyclerView.Adapter<WorkoutHolder> {
    private List<Workout> workouts;
    private OnItemClick onItemClick;

    public WorkoutsAdapter(List<Workout> workouts) {
        this.workouts = workouts;
    }

    @Override
    public WorkoutHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_workout, parent, false);
        return new WorkoutHolder(view);
    }

    @Override
    public void onBindViewHolder(final WorkoutHolder holder, int position) {
        holder.updateUI(workouts.get(position));
        holder.setOnEditClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onEditClick(workouts.get(holder.getAdapterPosition()));
                }
            }
        });
        holder.setOnDeleteClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onDeleteClick(workouts.get(holder.getAdapterPosition()));
                }
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onClick(workouts.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return workouts.size();
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemClick {
        void onEditClick(Workout workout);

        void onDeleteClick(Workout workout);

        void onClick(Workout workout);
    }
}
