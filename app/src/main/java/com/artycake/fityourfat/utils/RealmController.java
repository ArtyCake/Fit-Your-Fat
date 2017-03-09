package com.artycake.fityourfat.utils;

import android.content.Context;

import com.artycake.fityourfat.models.Workout;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by artycake on 2/27/17.
 */
public class RealmController {
    private static RealmController instance;
    private Realm realm;

    public static RealmController getInstance(Context context) {
        if (instance == null) {
            instance = new RealmController(context);
        }
        return instance;
    }

    private RealmController(Context context) {
        Realm.init(context);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);
        realm = Realm.getDefaultInstance();
    }

    public Realm getRealm() {
        return realm;
    }

    public Workout getWorkout(int id) {
        return realm.where(Workout.class).equalTo("id", id).findFirst();
    }

    public RealmResults<Workout> getWorkouts() {
        return realm.where(Workout.class).findAll();
    }

    public int getNewWorkoutId() {
        RealmResults<Workout> workouts = realm.where(Workout.class).findAllSorted("id", Sort.DESCENDING);
        if (workouts.size() == 0) {
            return 1;
        }
        return workouts.first().getId() + 1;
    }
}
