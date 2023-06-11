package com.example.fypimu;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor gyroSensor;
    private Sensor accelSensor;
    private Sensor rotationVectorSensor;
    private Button startReadingButton;
    private Button stopReadingButton;
    private TextView dataTextView;
    private Queue<String> dataQueue;
    private boolean isReadingData = false;

    // Define the maximum queue size
    private static final int MAX_QUEUE_SIZE = 100;

    // Create queues for each sensor data
    private Queue<String> acceQueue = new LinkedList<>();
    private String lastAcceVal = "0 0 0";
    private Queue<String> gameRvQueue = new LinkedList<>();
    private String lastGameRvVal = "0 0 0 0";
    private Queue<String> gyroQueue = new LinkedList<>();
    private String lastGyroVal = "0 0 0";

    private String currentSessionFileFolder = UUID.randomUUID().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize sensor manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get required sensors
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        // Get references to the buttons and text view
        startReadingButton = findViewById(R.id.startReadingButton);
        stopReadingButton = findViewById(R.id.stopReadingButton);
        dataTextView = findViewById(R.id.dataTextView);

        // Initialize data queue
        dataQueue = new LinkedList<>();

        // Set click listener for the start reading button
        startReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startReadingData();
            }
        });

        // Set click listener for the stop reading button
        stopReadingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopReadingData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister sensor listeners
        stopReadingData();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isReadingData) {
            long timestamp = System.currentTimeMillis();
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // Gyroscope data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the gyroscope data as needed
                // Add gyroscope data to the queue
                String gyroDataTime = timestamp + " " + x  + " " +  y + " "  + z;
                lastGyroVal =  x  + " " +  y + " "  + z;
                gyroQueue.offer(gyroDataTime);
                acceQueue.offer(timestamp + " " + lastAcceVal);
                gameRvQueue.offer(timestamp + " " + lastGameRvVal);
                // Write gyroscope data to file when the queue reaches the maximum size
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Accelerometer data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                String accelDataTime = timestamp  +" " + x  + " " +  y + " "  + z;
                lastAcceVal =  x  + " " +  y + " "  + z;
                gyroQueue.offer(timestamp + " " + lastGyroVal);
                gameRvQueue.offer(timestamp + " " + lastGameRvVal);
                acceQueue.offer(accelDataTime);
                // Write accelerometer data to file when the queue reaches the maximum size

            } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {

                // Rotation vector data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                float w = event.values[3];
                // Process the rotation vector data as needed
                // Add rotation vector data to the queue
                String rotationVectorData = String.format("%.15e %.15e %.15e %.15e",w, x, y, z);
                String rotationVectorDataTime = timestamp  +" "+ w +" " + x  + " " +  y + " "+ z;
                lastGameRvVal = w +" " + x  + " " +  y + " "+ z;
                gyroQueue.offer(timestamp + " " + lastGyroVal);
                acceQueue.offer(timestamp + " " + lastAcceVal);
                gameRvQueue.offer(rotationVectorDataTime);
                // Write rotation vector data to file when the queue reaches the maximum size

            }
            if (gameRvQueue.size() >= MAX_QUEUE_SIZE) {
//                    writeDataToFile("fyp_game_rv.txt", gameRvQueue);
                gameRvQueue.clear();
                acceQueue.clear();
                gyroQueue.clear();
            }

            // Display the current set of data
            displayDataQueue();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes here if needed
    }

    private List<String> readFileToString(String fileName) {
        String filePath = getFilePath(fileName);
        StringBuilder stringBuilder = new StringBuilder();
        ArrayList readData = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = reader.readLine()) != null) {
                readData.add(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return readData;
    }



    private void startReadingData() {
        // Register sensor listeners
        if (gyroSensor != null) {
            sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (accelSensor != null) {
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        // Start a periodic data reading task
        sendDataHandler.postDelayed(sendDataRunnable, (long) 00.1); // Delayed start with 4779 milliseconds (4.779615 seconds)

        // Update UI
        startReadingButton.setVisibility(View.GONE);
        stopReadingButton.setVisibility(View.VISIBLE);
        dataTextView.setText("Reading data...");
        isReadingData = true;
    }

    private Handler sendDataHandler = new Handler();
    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            // Write sensor data to files
            writeDataToFile("fyp_gyro.txt", gyroQueue);
            writeDataToFile("fyp_acce.txt", acceQueue);
            writeDataToFile("fyp_game_rv.txt", gameRvQueue);

            // Schedule the next execution after the specified interval
            sendDataHandler.postDelayed(this, (long) 00.1);
        }
    };

    private void sendDataToServer() {
        // Create an instance of the ApiService using the ApiManager
        ApiService apiService = ApiManager.getApiService();

        List<String> gyroData = readFileToString("fyp_gyro.txt");
        List<String> accelData = readFileToString("fyp_acce.txt");
        List<String> rotationVectorData = readFileToString("fyp_game_rv.txt");
        Toast.makeText(MainActivity.this, "Failed to send data to server" + gyroData.size() + " " + accelData.size() + " "
                + rotationVectorData.size(), Toast.LENGTH_SHORT).show();

        List<SensorData> sensorData = new ArrayList<>();

//        for (int i = 0; i < gyroData.size(); i++) {
//            SensorData newSensorDataToWrite = new SensorData();
//            String[] splittedGyro = gyroData.get(i).split(" ");
//            newSensorDataToWrite.setTimestamp(Long.parseLong(splittedGyro[0]));
//            newSensorDataToWrite.setGyro_x(Float.parseFloat(splittedGyro[1]));
//            newSensorDataToWrite.setGyro_y(Float.parseFloat(splittedGyro[2]));
//            newSensorDataToWrite.setGyro_z(Float.parseFloat(splittedGyro[3]));
//
//            String[] splittedAccelData = accelData.get(i).split(" ");
//            newSensorDataToWrite.setAcce_x(Float.parseFloat(splittedAccelData[1]));
//            newSensorDataToWrite.setAcce_y(Float.parseFloat(splittedAccelData[2]));
//            newSensorDataToWrite.setAcce_z(Float.parseFloat(splittedAccelData[3]));
//
//            String[] splitteRotationVectorData= rotationVectorData.get(i).split(" ");
//            newSensorDataToWrite.setGamerv_x(Float.parseFloat(splitteRotationVectorData[1]));
//            newSensorDataToWrite.setGamerv_y(Float.parseFloat(splitteRotationVectorData[2]));
//            newSensorDataToWrite.setGamerv_z(Float.parseFloat(splitteRotationVectorData[3]));
//            newSensorDataToWrite.setGamerv_w(Float.parseFloat(splitteRotationVectorData[4]));
//
//            sensorData.add(newSensorDataToWrite);
//        }

        // Call the appropriate API method with the data you want to send
//        Call<Object> call = apiService.uploadSensorData(sensorData);
//
//        // Execute the API call
//        call.enqueue(new Callback<Object>() {
//            @Override
//            public void onResponse(Call<Object> call, Response<Object> response) {
//                if (response.isSuccessful()) {
//                    // API call successful
//                    Object data = response.body();
//                    // Handle the response data as needed
//                } else {
//                    // API call failed
//                    Toast.makeText(MainActivity.this, "Failed to send data to server", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Object> call, Throwable t) {
//                // Error occurred during API call
//                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private void stopReadingData() {
        // Unregister sensor listeners
        sensorManager.unregisterListener(this);

        // Clear the queues
        acceQueue.clear();
        gyroQueue.clear();
        gameRvQueue.clear();

        // Update UI
        stopReadingButton.setVisibility(View.GONE);
        startReadingButton.setVisibility(View.VISIBLE);
//        dataTextView.setText("");
        sendDataToServer();
        isReadingData = false;
    }

    private String getFilePath(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        return downloadsDir.getAbsolutePath() + File.separator+ fileName;
    }

    private void writeDataToFile( String fileName,Queue<String> queue) {
        // Check if external storage is available
        String filePath = getFilePath(fileName);

        if (isExternalStorageWritable()) {


            try {
                // Create a FileWriter and write data to the file
                FileWriter writer = new FileWriter(filePath, true); // Append mode
                while (!queue.isEmpty()) {
                    String data = queue.poll();
                    writer.append(data).append("\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error writing data to file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "External storage is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayDataQueue() {
        // Clear the data text view
        dataTextView.setText("");

        // Create a SpannableStringBuilder to apply spannable text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        // Append the accelerometer data
        spannableStringBuilder.append("Accelerometer Data:\n");
        for (String data : acceQueue) {
            spannableStringBuilder.append(data).append("\n");
        }
        spannableStringBuilder.append("\n");

        // Append the gyroscope data
        spannableStringBuilder.append("Gyroscope Data:\n");
        for (String data : gyroQueue) {
            spannableStringBuilder.append(data).append("\n");
        }
        spannableStringBuilder.append("\n");

        // Append the rotation vector data
        spannableStringBuilder.append("Rotation Vector Data:\n");
        for (String data : gameRvQueue) {
            spannableStringBuilder.append(data).append("\n");
        }

        // Set the spannable text to the TextView
        dataTextView.setText(spannableStringBuilder);

        // Scroll to the top of the TextView
        dataTextView.scrollTo(0, 0);
    }


    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }



}
