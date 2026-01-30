package com.example.sensor_detector;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private List<Sensor> sensorList;

    private Spinner spinnerSensors;
    private TextView tvDetails, tvValues;


    private Sensor currentSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerSensors = findViewById(R.id.spinnerSensors);
        tvDetails = findViewById(R.id.tvDetails);
        tvValues = findViewById(R.id.tvValues);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);


        if (sensorList == null || sensorList.isEmpty()) {
            tvDetails.setText("No sensors found on this device.");
            return;
        }

        // Build list of names for Spinner
        ArrayList<String> names = new ArrayList<>();
        for (Sensor s : sensorList) {
            names.add(s.getName() + "  (Type: " + s.getType() + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
        );
        spinnerSensors.setAdapter(adapter);

        spinnerSensors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                Log.d("mainccc", "onItemSelected: "+sensorList.get(position) +  position);
                selectSensor(sensorList.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        // Select first sensor by default
        selectSensor(sensorList.get(0));
    }

    private void selectSensor(@NonNull Sensor sensor) {
        // Unregister previous
        if (currentSensor != null) {
            sensorManager.unregisterListener(this, currentSensor);
        }

        currentSensor = sensor;

        // Show sensor details
        String details = "Name: " + sensor.getName() +
                "\nVendor: " + sensor.getVendor() +
                "\nType: " + sensor.getType() +
                "\nString Type: " + sensor.getStringType() +
                "\nVersion: " + sensor.getVersion() +
                "\nMax Range: " + sensor.getMaximumRange() +
                "\nResolution: " + sensor.getResolution() +
                "\nPower (mA): " + sensor.getPower() +
                "\nMin Delay (µs): " + sensor.getMinDelay();

        tvDetails.setText(details);
        tvValues.setText("Waiting for sensor data...");

        // Register listener
        // Options: SENSOR_DELAY_NORMAL, UI, GAME, FASTEST
        sensorManager.registerListener(this, currentSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentSensor != null) {
            sensorManager.registerListener(this, currentSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentSensor != null) {
            sensorManager.unregisterListener(this, currentSensor);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // event.values can have 1..N values depending on sensor
        StringBuilder sb = new StringBuilder();
        sb.append("Timestamp (ns): ").append(event.timestamp).append("\n\n");

        sb.append("Values:\n");
        for (int i = 0; i < event.values.length; i++) {
            sb.append(String.format(Locale.US, "  [%d] = %.6f\n", i, event.values[i]));
        }

        // Optional: show accuracy
        sb.append("\nAccuracy: ").append(event.accuracy);

        tvValues.setText(sb.toString());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not always used
    }
}