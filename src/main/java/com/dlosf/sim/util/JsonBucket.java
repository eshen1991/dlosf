package com.dlosf.sim.util;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Letter;

/**
 * Created by eshen on 10/4/15.
 */
public class JsonBucket {
    private float x;
    private float y;
    private float radius;
    private String letters;
    private int capacity;

    public JsonBucket() {
    }

    public  JsonBucket(Bucket bucket) {
        this.x = bucket.getX();
        this.y = bucket.getY();
        this.capacity = bucket.getCapacity();

        Letter[] letters =  bucket.getLetters().toArray(new Letter[bucket.getLetters().size()]);
        this.letters = SimulationWorldStaticInitializer.convertLettersToString(letters);

    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }
}
