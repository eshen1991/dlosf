package com.dlosf.sim.util;

import alphabetsoup.framework.Bucket;
import alphabetsoup.framework.Letter;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by eshen on 10/4/15.
 */
public class JsonBucket extends JsonWaypoint {

    private String letters;
    private int capacity;

    public JsonBucket() {
        super();
    }

    public  JsonBucket(Bucket bucket) {
        super(bucket.getX(), bucket.getY(),bucket.getRadius());
        this.capacity = bucket.getCapacity();
        Letter[] letters =  bucket.getLetters().toArray(new Letter[bucket.getLetters().size()]);
        this.letters = JsonHelper.convertLettersToString(letters);
    }

    public JsonBucket(float x, float y, float radius, String letters, int capacity) {
        super(x, y, radius);
        this.letters = letters;
        this.capacity = capacity;

    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getLetters() {
        return letters;
    }

    public void setLetters(String letters) {
        this.letters = letters;
    }
}
