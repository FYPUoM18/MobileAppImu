package com.example.fypimu;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

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
    private static final int MAX_QUEUE_SIZE = 10;

    // Create queues for each sensor data
    private Queue<String> acceQueue = new LinkedList<>();
    private Queue<String> gameRvQueue = new LinkedList<>();
    private Queue<String> gyroQueue = new LinkedList<>();

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
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                // Gyroscope data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the gyroscope data as needed
                // Add gyroscope data to the queue
                String gyroData = String.format("%.15e %.15e %.15e", x, y, z);
                gyroQueue.offer(gyroData);
                // Write gyroscope data to file when the queue reaches the maximum size
                if (gyroQueue.size() >= MAX_QUEUE_SIZE) {
                    writeDataToFile("gyro.txt", gyroQueue);
                    gyroQueue.clear();
                }
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Accelerometer data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the accelerometer data as needed
                // Add accelerometer data to the queue
                String accelData = String.format("%.15e %.15e %.15e", x, y, z);
                acceQueue.offer(accelData);
                // Write accelerometer data to file when the queue reaches the maximum size
                if (acceQueue.size() >= MAX_QUEUE_SIZE) {
                    writeDataToFile("acce.txt", acceQueue);
                    acceQueue.clear();
                }
            } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                // Rotation vector data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the rotation vector data as needed
                // Add rotation vector data to the queue
                String rotationVectorData = String.format("%.15e %.15e %.15e", x, y, z);
                gameRvQueue.offer(rotationVectorData);
                // Write rotation vector data to file when the queue reaches the maximum size
                if (gameRvQueue.size() >= MAX_QUEUE_SIZE) {
                    writeDataToFile("game_rv.txt", gameRvQueue);
                    gameRvQueue.clear();
                }
            }

            // Display the current set of data
            displayDataQueue();
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Handle accuracy changes here if needed
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

        // Update UI
        startReadingButton.setVisibility(View.GONE);
        stopReadingButton.setVisibility(View.VISIBLE);
        dataTextView.setText("Reading data...");
        isReadingData = true;
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
        dataTextView.setText("");
        isReadingData = false;
    }

    private String getFilePath(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }
        return downloadsDir.getAbsolutePath() + File.separator + fileName;
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
