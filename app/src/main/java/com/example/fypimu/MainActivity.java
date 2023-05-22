package com.example.fypimu;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

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
    private Handler sendDataHandler;
    private Runnable sendDataRunnable;
    private boolean isReadingData = false;

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

        // Initialize handler and runnable for sending data every 20 seconds
        sendDataHandler = new Handler();
        sendDataRunnable = new Runnable() {
            @Override
            public void run() {
                sendDataToServer();
                sendDataHandler.postDelayed(this, 20000); // 20 seconds delay
            }
        };

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
        // Unregister sensor listeners and remove any pending data send tasks
        stopReadingData();
        sendDataHandler.removeCallbacks(sendDataRunnable);
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
                String gyroData = "Gyroscope Data:\nX: " + x + "\nY: " + y + "\nZ: " + z;
                dataQueue.offer(gyroData);
            } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Accelerometer data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the accelerometer data as needed
                // Add accelerometer data to the queue
                String accelData = "Accelerometer Data:\nX: " + x + "\nY: " + y + "\nZ: " + z;
                dataQueue.offer(accelData);
            } else if (event.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
                // Rotation vector data
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                // Process the rotation vector data as needed
                // Add rotation vector data to the queue
                String rotationVectorData = "Rotation Vector Data:\nX: " + x + "\nY: " + y + "\nZ: " + z;
                dataQueue.offer(rotationVectorData);
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

        // Start sending data to the server every 20 seconds
        sendDataHandler.postDelayed(sendDataRunnable, 2000);

        // Update UI
        startReadingButton.setVisibility(View.GONE);
        stopReadingButton.setVisibility(View.VISIBLE);
        dataTextView.setText("Reading data...");
        isReadingData = true;
    }

    private void stopReadingData() {
        // Unregister sensor listeners
        sensorManager.unregisterListener(this);

        // Stop sending data to the server
        sendDataHandler.removeCallbacks(sendDataRunnable);

        // Clear the data queue
        dataQueue.clear();

        // Update UI
        stopReadingButton.setVisibility(View.GONE);
        startReadingButton.setVisibility(View.VISIBLE);
        dataTextView.setText("");
        isReadingData = false;
    }

    private void sendDataToServer() {
        // Check if there is data in the queue
        if (!dataQueue.isEmpty()) {
            // Send the data to the server
            String data = dataQueue.poll();
            // Implement your logic to send the data to the server here
            // If the data is sent successfully, remove it from the queue
//            Toast.makeText(MainActivity.this, "Data sent: " + data, Toast.LENGTH_SHORT).show();

            System.out.println("data"+data);
            // Display the updated data queue
            displayDataQueue();
        }
    }

    private void displayDataQueue() {
        // Clear the data text view
        dataTextView.setText("");

        // Create a SpannableStringBuilder to apply spannable text
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();

        // Append the data sets in the queue
        LinkedList<String> dataList = new LinkedList<>(dataQueue);
        for (int i = dataList.size() - 1; i >= 0; i--) {
            String data = dataList.get(i);
            if (i == dataList.size() - 1) {
                // Apply different color to the latest data
                spannableStringBuilder.append(data + "\n\n");
                int startIndex = spannableStringBuilder.length() - data.length() - 2; // Adjust index to exclude the newlines
                int endIndex = spannableStringBuilder.length() - 2; // Adjust index to exclude the newlines
                spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                spannableStringBuilder.append(data + "\n\n");
            }
        }

        // Set the spannable text to the TextView
        dataTextView.setText(spannableStringBuilder);

        // Scroll to the top of the TextView
        dataTextView.scrollTo(0, 0);
    }
}
