package com.dlosf.sim.util;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by eshen on 10/12/15.
 */
public class JsonWaypoint {
    private float x = Float.NaN;
    private float y = Float.NaN;
    private float radius;
    private String uuid;
    private HashMap<String, Float> paths = new HashMap<String, Float>();

    public JsonWaypoint() {
        this.uuid = UUID.randomUUID().toString();
    }

    public JsonWaypoint(float x, float y, float radius) {
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.uuid = UUID.randomUUID().toString();
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public HashMap<String, Float> getPaths() {
        return paths;
    }

    public void setPaths(HashMap<String, Float> paths) {
        this.paths = paths;
    }
}
