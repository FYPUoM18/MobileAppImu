package com.example.fypimu;

public class SensorData {
    private long timestamp;
    private float gyro_x;
    private float gyro_y;
    private float gyro_z;
    private float gamerv_x;
    private float gamerv_y;
    private float gamerv_z;
    private float gamerv_w;
    private float acce_x;
    private float acce_y;
    private String deviceId;
    private String building;

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }
    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }



    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getGyro_x() {
        return gyro_x;
    }

    public void setGyro_x(float gyro_x) {
        this.gyro_x = gyro_x;
    }

    public float getGyro_y() {
        return gyro_y;
    }

    public void setGyro_y(float gyro_y) {
        this.gyro_y = gyro_y;
    }

    public float getGyro_z() {
        return gyro_z;
    }

    public void setGyro_z(float gyro_z) {
        this.gyro_z = gyro_z;
    }

    public float getGamerv_x() {
        return gamerv_x;
    }

    public void setGamerv_x(float gamerv_x) {
        this.gamerv_x = gamerv_x;
    }

    public float getGamerv_y() {
        return gamerv_y;
    }

    public void setGamerv_y(float gamerv_y) {
        this.gamerv_y = gamerv_y;
    }

    public float getGamerv_z() {
        return gamerv_z;
    }

    public void setGamerv_z(float gamerv_z) {
        this.gamerv_z = gamerv_z;
    }

    public float getGamerv_w() {
        return gamerv_w;
    }

    public void setGamerv_w(float gamerv_w) {
        this.gamerv_w = gamerv_w;
    }

    public float getAcce_x() {
        return acce_x;
    }

    public void setAcce_x(float acce_x) {
        this.acce_x = acce_x;
    }

    public float getAcce_y() {
        return acce_y;
    }

    public void setAcce_y(float acce_y) {
        this.acce_y = acce_y;
    }

    public float getAcce_z() {
        return acce_z;
    }

    public void setAcce_z(float acce_z) {
        this.acce_z = acce_z;
    }

    private float acce_z;
}
