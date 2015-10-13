package com.dlosf.sim.util;

import alphabetsoup.waypointgraph.Waypoint;

/**
 * Created by eshen on 10/12/15.
 */
public class JsonBot  extends JsonWaypoint {
    private float bucketPickupSetdownTime;
    private float maxAcceleration;
    private float maxVelocity;
    private float collisionPenaltyTime;

    public JsonBot() {
        super();
    }

    public JsonBot(float x, float y, float radius, float bucketPickupSetdownTime, float maxAcceleration, float maxVelocity, float collisionPenaltyTime) {
        super(x, y, radius);
        this.bucketPickupSetdownTime = bucketPickupSetdownTime;
        this.maxAcceleration = maxAcceleration;
        this.maxVelocity = maxVelocity;
        this.collisionPenaltyTime = collisionPenaltyTime;
    }


    public float getBucketPickupSetdownTime() {
        return bucketPickupSetdownTime;
    }

    public void setBucketPickupSetdownTime(float bucketPickupSetdownTime) {
        this.bucketPickupSetdownTime = bucketPickupSetdownTime;
    }

    public float getCollisionPenaltyTime() {
        return collisionPenaltyTime;
    }

    public void setCollisionPenaltyTime(float collisionPenaltyTime) {
        this.collisionPenaltyTime = collisionPenaltyTime;
    }

    public float getMaxAcceleration() {
        return maxAcceleration;
    }

    public void setMaxAcceleration(float maxAcceleration) {
        this.maxAcceleration = maxAcceleration;
    }

    public float getMaxVelocity() {
        return maxVelocity;
    }

    public void setMaxVelocity(float maxVelocity) {
        this.maxVelocity = maxVelocity;
    }
}
