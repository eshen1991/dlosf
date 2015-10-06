package com.dlosf.sim.util;

import alphabetsoup.framework.Circle;

/**
 * Created by eshen on 10/5/15.
 */
public class JsonBucketCirclePair {
    private JsonBucket bucket;
    private Circle   circle;

    public JsonBucketCirclePair() {
    }

    public JsonBucketCirclePair(JsonBucket bucket, Circle circle) {
        this.bucket = bucket;
        this.circle = circle;
    }

    public JsonBucket getBucket() {
        return bucket;
    }

    public void setBucket(JsonBucket bucket) {
        this.bucket = bucket;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }
}
