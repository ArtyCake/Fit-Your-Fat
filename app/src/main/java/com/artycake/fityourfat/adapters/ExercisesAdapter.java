package com.artycake.fityourfat.adapters;

import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.drag.ItemTouchHelperAdapter;
import com.artycake.fityourfat.drag.OnStartDragListener;
import com.artycake.fityourfat.holders.ExerciseHolder;
import com.artycake.fityourfat.models.Exercise;

import java.util.Collections;
import java.util.List;

/**
 * Created by artycake on 3/9/17.
 */

public class ExercisesAdapter extends RecyclerView.Adapter<ExerciseHolder> implements ItemTouchHelperAdapter {
    private List<Exercise> exercises;
    private OnItemClick onItemClick;
    private OnStartDragListener onStartDragListener;

    public ExercisesAdapter(List<Exercise> exercises) {
        this.exercises = exercises;
    }

    public void setOnStartDragListener(OnStartDragListener onStartDragListener) {
        this.onStartDragListener = onStartDragListener;
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
        holder.dragIcon.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    if (onStartDragListener != null) {
                        onStartDragListener.onStartDrag(holder);
                    }
                }
                return false;
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(exercises, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        exercises.remove(position);
        notifyItemRemoved(position);
    }

    public interface OnItemClick {
        void onClick(Exercise exercise);
    }
}
