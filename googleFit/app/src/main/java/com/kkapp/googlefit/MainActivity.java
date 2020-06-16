package com.kkapp.googlefit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This enum is used to define actions that can be performed after a successful sign in to Fit.
 * One of these values is passed to the Fit sign-in, and returned in a successful callback, allowing
 * subsequent execution of the desired action.
 */


public class MainActivity extends AppCompatActivity {
    public static final String TAG = "BasicSensorsApi";
    private static final int REQUEST_OAUTH_REQUEST_CODE = 1;
//    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
//    private OnDataPointListener mListener;
//    private OnDataPointListener mListene;

    TextView steps,distance,calories;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (savedInstanceState != null) {
//            mSensorsStepCount = savedInstanceState.getInt("sensor_steps");
//        }
        steps=findViewById(R.id.steps);
        distance=findViewById(R.id.distance);
        calories=findViewById(R.id.calories);
//Get the firebase authentication done first
        FitnessOptions fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                        .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                        .addDataType(DataType.TYPE_DISTANCE_DELTA)
                        .addDataType(DataType.AGGREGATE_ACTIVITY_SUMMARY)
                        .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    REQUEST_OAUTH_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }

//        subscribe();
    }
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Thank you for you subscription", Toast.LENGTH_SHORT);
                                } else {
                                    Toast.makeText(
                                            MainActivity.this,
                                            "Authorization Failed",
                                            Toast.LENGTH_SHORT);
                                    Log.d(
                                            "USER AUTHORIZATION",
                                            "There was a problem subscribing.",
                                            task.getException());
                                }
                            }
                        });
        getTodayStepsCount();
//        getTodayCalories();
        getTodayDistance();
        readLastWeekSteps();
    }
    private void getTodayStepsCount() {
        Fitness.getHistoryClient(
                this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                long total =
                                        dataSet.isEmpty()
                                                ? 0
                                                : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                steps.setText(String.valueOf(total));

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Authorization Failed. Unable to get step Count",
                                        Toast.LENGTH_SHORT);
                                steps.setText(0);

                            }
                        });
    }
    private void getTodayDistance(){
        Fitness.getHistoryClient(
                this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                Log.d("Distance:", dataSet.getDataPoints().toString());
                                float total = dataSet.isEmpty()
                                        ? 0 : dataSet.getDataPoints().get(0).getValue(
                                        Field.FIELD_DISTANCE).asFloat();
                                int roundTotal = Math.round(total);
                                distance.setText(String.valueOf(roundTotal)+" m");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Authorization Failed. Unable to get step Count",
                                        Toast.LENGTH_SHORT);
                                Log.d("DISTANCE", "Failed" );
                                distance.setText(0+" m");
                            }
                        });
    }
    private void getTodayCalories(){
        Fitness.getHistoryClient(
                this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                float total = dataSet.isEmpty()
                                        ? 0 : dataSet.getDataPoints().get(0).getValue(
                                        Field.FIELD_CALORIES).asFloat();
                                int roundTotal = Math.round(total);
                                calories.setText(String.valueOf(roundTotal)+" Cal");
                            }
                        }
                )
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(
                                        MainActivity.this,
                                        "Authorization Failed. Unable to get step Count",
                                        Toast.LENGTH_SHORT);
                                calories.setText(0+" Cal");

                            }
                        });
    }
    protected void readLastWeekSteps() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest dataReadRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();
        Task<DataReadResponse> response = Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readData(dataReadRequest).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        int p =0;
                        if (dataReadResponse.getBuckets().size() > 0) {
                            for (Bucket bucket : dataReadResponse.getBuckets()) {
                                DataSet dataSet = bucket.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
                                //Loop through Bucket to get last 7 days of data
                                //dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt()
                                p+=dataSet.isEmpty()?0:dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                calories.setText(String.valueOf(p));
                            }
                            Log.i("HElooooooooooo","K");
                        }
                    }
                });

    }
}