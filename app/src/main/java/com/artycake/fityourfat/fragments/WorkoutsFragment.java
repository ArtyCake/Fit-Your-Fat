package com.artycake.fityourfat.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.fityourfat.R;
import com.artycake.fityourfat.activities.MainActivity;
import com.artycake.fityourfat.activities.WorkoutFormActivity;
import com.artycake.fityourfat.adapters.WorkoutsAdapter;
import com.artycake.fityourfat.models.Workout;
import com.artycake.fityourfat.utils.RealmController;
import com.artycake.fityourfat.utils.UserPrefs;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutsFragment extends Fragment {
    @BindView(R.id.workouts_list)
    RecyclerView workoutsList;

    private List<Workout> workouts = new ArrayList<>();
    private WorkoutsAdapter adapter;

    public WorkoutsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_workouts, container, false);
        ButterKnife.bind(this, view);
        workouts.addAll(RealmController.getInstance(getContext()).getWorkouts());
        adapter = new WorkoutsAdapter(workouts);
        adapter.setOnItemClick(new WorkoutsAdapter.OnItemClick() {
            @Override
            public void onEditClick(Workout workout) {
                Intent intent = new Intent(getActivity(), WorkoutFormActivity.class);
                intent.putExtra(WorkoutFormActivity.ID, workout.getId());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(final Workout workout) {
                RealmController.getInstance(getContext()).deleteWorkout(workout);
                workouts.remove(workout);
            }

            @Override
            public void onClick(Workout workout) {
                UserPrefs.getInstance(getContext()).putPreferences(UserPrefs.CURRENT_WORKOUT, workout.getId());
                ((MainActivity) getActivity()).toTimer();
            }
        });
        workoutsList.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        workoutsList.setLayoutManager(layoutManager);
        return view;
    }

    @OnClick(R.id.action_add)
    public void add() {
        startActivity(new Intent(getActivity(), WorkoutFormActivity.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        workouts.clear();
        workouts.addAll(RealmController.getInstance(getContext()).getWorkouts());
        adapter.notifyDataSetChanged();
    }
}
