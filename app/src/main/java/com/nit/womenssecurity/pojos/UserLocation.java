package com.nit.womenssecurity.pojos;

public class UserLocation {

    private String userId;
    private long time;
    private double lat;
    private double lon;

    public UserLocation() {
    }

    public UserLocation(String userId, long time, double lat, double lon) {
        this.userId = userId;
        this.time = time;
        this.lat = lat;
        this.lon = lon;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}
