package com.artycake.fityourfat.models;

import android.content.Context;

import com.artycake.fityourfat.utils.RealmController;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by artycake on 3/8/17.
 */

public class Workout extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private int warmUpTime = 0;
    private int laps;
    private RealmList<Exercise> exercises = new RealmList<>();
    @Ignore
    private int index = 0;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWarmUpTime() {
        return warmUpTime;
    }

    public void setWarmUpTime(int warmUpTime) {
        this.warmUpTime = warmUpTime;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public RealmList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(RealmList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Exercise nextExercise() {
        index++;
        if (index >= getExercises().size()) {
            return null;
        }
        return getExercises().get(index);
    }
}
