package com.example.fypimu;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {

    static String SERVER_IP = "192.168.8.161"; // Replace with the actual IP address of the server
    static int SERVER_PORT = 8000; // Replace with the port number your server is running on
    static String BASE_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + "/";

    private static Retrofit retrofit;
    private static ApiService apiService;


    public static ApiService getApiService() {

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
        if (apiService == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}

