package com.artycake.fityourfat.models;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by artycake on 3/8/17.
 */

public class Exercise extends RealmObject {
    @PrimaryKey
    private int id;
    private String name;
    private int color;
    private String description;
    private int duration;
    private boolean repsMode = false;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isRepsMode() {
        return repsMode;
    }

    public void setRepsMode(boolean repsMode) {
        this.repsMode = repsMode;
    }
}
