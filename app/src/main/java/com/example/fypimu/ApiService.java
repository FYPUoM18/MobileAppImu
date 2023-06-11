package com.example.fypimu;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/sensor-data")
    Call<Object> uploadSensorData(@Body List<SensorData> sensorData);
}
