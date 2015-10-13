package com.dlosf.sim.util;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by eshen on 10/7/15.
 */
public class JsonWordStation extends JsonWaypoint {


    private int capacity;
    private float bucketToLetterTime;
    private float wordCompletionTime;


    public JsonWordStation() {
        super();
    }

    public JsonWordStation(float x, float y, float radius, int capacity, float bucketToLetterTime, float wordCompletionTime) {
        super(x, y, radius);
        this.capacity = capacity;
        this.bucketToLetterTime = bucketToLetterTime;
        this.wordCompletionTime = wordCompletionTime;

    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public float getBucketToLetterTime() {
        return bucketToLetterTime;
    }

    public void setBucketToLetterTime(float bucketToLetterTime) {
        this.bucketToLetterTime = bucketToLetterTime;
    }

    public float getWordCompletionTime() {
        return wordCompletionTime;
    }

    public void setWordCompletionTime(float wordCompletionTime) {
        this.wordCompletionTime = wordCompletionTime;
    }
}
