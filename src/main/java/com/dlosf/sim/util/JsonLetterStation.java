package com.dlosf.sim.util;

import alphabetsoup.framework.Letter;

import java.util.*;

/**
 * Created by eshen on 10/7/15.
 */
public class JsonLetterStation extends JsonWaypoint{

    private int capacity;
    private int bundleSize;
    private float letterToBucketTime;



    public JsonLetterStation() {
       super();
    }

    public JsonLetterStation(float x, float y, float radius, int capacity, int bundleSize, float letterToBucketTime) {
        super(x, y, radius);
        this.capacity = capacity;
        this.bundleSize = bundleSize;
        this.letterToBucketTime = letterToBucketTime;


    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getBundleSize() {
        return bundleSize;
    }

    public void setBundleSize(int bundleSize) {
        this.bundleSize = bundleSize;
    }

    public float getLetterToBucketTime() {
        return letterToBucketTime;
    }

    public void setLetterToBucketTime(float letterToBucketTime) {
        this.letterToBucketTime = letterToBucketTime;
    }
}
