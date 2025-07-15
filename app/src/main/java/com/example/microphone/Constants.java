package com.example.microphone;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import java.util.ArrayList;

public class Constants {
    static Button startButton, stopButton;
    static EditText freqEt,volEt,lengthEt;
    static TextView statusField;
    static LineChart lineChart_imu,lineChart_mic;
    static short[] samples;
    static short[] temp;
    static ArrayList<Long> acc_ts;
    static ArrayList<Float> accx;
    static ArrayList<Float> accy;
    static ArrayList<Float> accz;
    static ArrayList<Long> gyro_ts;
    static ArrayList<Float> gyrox;
    static ArrayList<Float> gyroy;
    static ArrayList<Float> gyroz;
    static boolean start=false;
}
