package com.example.microphone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import android.os.CountDownTimer;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    static {
        System.loadLibrary("native-lib");
    }

    private int grantResults[];
    int freq = 0;
    double vol = 0;
    int length = 0;
    Worker task;
    Activity av;
    TextView tv,countdownField;
    int counter = 0;
    String fname = "";
    Vibrator vibrator;
    CountDownTimer ttimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},1);
        onRequestPermissionsResult(1,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.RECORD_AUDIO},grantResults);

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        av = this;
        tv = (TextView)findViewById(R.id.textView1);
        countdownField = (TextView)findViewById(R.id.textView4);
        Constants.statusField=(TextView)findViewById(R.id.textView5);

        Constants.lineChart_imu = (LineChart)findViewById(R.id.linechart_imu);
        Constants.lineChart_mic = (LineChart)findViewById(R.id.linechart_mic);
        Constants.startButton = (Button)findViewById(R.id.button);
        Constants.stopButton = (Button)findViewById(R.id.button2);
        Constants.checkButton = (Button)findViewById(R.id.button3);
        Constants.startButton.setEnabled(true);
        Constants.stopButton.setEnabled(false);
        Constants.checkButton.setEnabled(true);
        Constants.checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File dirFile = new File(getExternalFilesDir(null).toString());
                File[] files = dirFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        return pathname.isFile() && pathname.getName().toLowerCase().contains("pcg");
                    }
                });

                if (files != null && files.length > 0) {
                    File mostRecent = files[0];
                    for (int i = 1; i < files.length; i++) {
                        if (files[i].lastModified() > mostRecent.lastModified()) {
                            mostRecent = files[i];
                        }
                    }

                    long sizeInBytes = mostRecent.length();
                    double sizeInMB = sizeInBytes / (1024.0 * 1024.0);
                    Log.e("FileInfo", mostRecent.getName() +
                            ", Size: " + String.format("%.2f", sizeInMB) + " MB");
                    Toast.makeText(av, mostRecent.getName() +
                            ", Size: " + String.format("%.2f", sizeInMB) + " MB", Toast.LENGTH_LONG).show();
                } else {
                    Log.e("FileInfo", "No matching files found.");
                }
            }
        });
        Constants.startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineDataX=new ArrayList<>();
                lineDataY=new ArrayList<>();
                lineDataZ=new ArrayList<>();
                fname=System.currentTimeMillis()+"";
                Constants.statusField.setText("");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText(fname);
                    }
                });

                closeKeyboard();
                task = new Worker(av,av,freq,vol,length, 8000,fname,vibrator);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                Constants.startButton.setEnabled(false);
                Constants.stopButton.setEnabled(true);

                String s = Constants.lengthEt.getText().toString();
                int size = 500*Integer.parseInt(s);
                Constants.acc_ts=new ArrayList<>(size);
                Constants.accx=new ArrayList<>(size);
                Constants.accy=new ArrayList<>(size);
                Constants.accz=new ArrayList<>(size);
                Constants.gyro_ts=new ArrayList<>(size);
                Constants.gyrox=new ArrayList<>(size);
                Constants.gyroy=new ArrayList<>(size);
                Constants.gyroz=new ArrayList<>(size);
                counter=0;
                Constants.start=true;

                ttimer=new CountDownTimer((Integer.parseInt(s)) * 1000, 100) {

                    public void onTick(long millisUntilFinished) {
                        countdownField.setText(1 + (millisUntilFinished / 1000) + "");
                    }

                    public void onFinish() {
                        countdownField.setText("0");
                        Constants.start=false;
                    }
                }.start();
            }
        });
        Constants.stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task.cancel(true);
                Constants.startButton.setEnabled(true);
                Constants.stopButton.setEnabled(false);
                FileOperations.writetofile(av,fname);

                Constants.start=false;
                ttimer.cancel();
            }
        });

        Constants.freqEt = (EditText)findViewById(R.id.editTextNumber);
        Constants.volEt = (EditText)findViewById(R.id.editTextNumber2);
        Constants.lengthEt = (EditText)findViewById(R.id.editTextNumber3);

        Context c= this;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        freq=prefs.getInt("freq",200);
        Constants.freqEt.setText(freq+"");
        Constants.freqEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence cs, int start,
                                      int before, int count) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
                String s = Constants.freqEt.getText().toString();
                if (Utils.isInteger(s)) {
                    freq=Integer.parseInt(s);
                    editor.putInt("freq", freq);
                    editor.commit();
                }
            }
        });
        vol=prefs.getFloat("vol", 0.0f);
        String volText = vol+"";
        Constants.volEt.setText(volText.substring(0,3));
        Constants.volEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence cs, int start,
                                      int before, int count) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
                String s = Constants.volEt.getText().toString();
                if (Utils.isDouble(s)) {
                    vol=Double.parseDouble(s);
                    editor.putFloat("vol", (float)vol);
                    editor.commit();
                }
            }
        });
        length=prefs.getInt("length",30);
        Constants.lengthEt.setText(length+"");
        Constants.lengthEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence cs, int start,
                                      int before, int count) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(c).edit();
                String s = Constants.lengthEt.getText().toString();
                if (Utils.isInteger(s)) {
                    length=Integer.parseInt(s);
                    editor.putInt("length", length);
                    editor.commit();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    public void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (Constants.start) {
            if (sensorEvent.sensor.equals(accelerometer)) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                Constants.acc_ts.add(System.currentTimeMillis());
                Constants.accx.add(x);
                Constants.accy.add(y);
                Constants.accz.add(z);
                graphData(new float[]{x,y,z});
//                gotacc=true;
            }
            else if (sensorEvent.sensor.equals(gyroscope)) {
                float x = sensorEvent.values[0];
                float y = sensorEvent.values[1];
                float z = sensorEvent.values[2];
                Constants.gyro_ts.add(System.currentTimeMillis());
                Constants.gyrox.add(x);
                Constants.gyroy.add(y);
                Constants.gyroz.add(z);
//                graphData(new float[]{x,y,z});
//                gotacc=true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    int lim=500;
    List<Entry> lineDataX;
    List<Entry> lineDataY;
    List<Entry> lineDataZ;
    public void graphData(float[] values) {
        lineDataX.add(new Entry(counter,values[0]));
        lineDataY.add(new Entry(counter,values[1]));
        lineDataZ.add(new Entry(counter,values[2]));
        if (lineDataX.size()>lim) {
            lineDataX.remove(0);
            lineDataY.remove(0);
            lineDataZ.remove(0);
        }
        counter+=1;

        LineDataSet data1 = new LineDataSet(lineDataX, "x");
        LineDataSet data2 = new LineDataSet(lineDataY, "y");
        LineDataSet data3 = new LineDataSet(lineDataZ, "z");
        data1.setDrawCircles(false);
        data2.setDrawCircles(false);
        data3.setDrawCircles(false);
        data1.setColor(((MainActivity)this).getResources().getColor(R.color.red));
        data2.setColor(((MainActivity)this).getResources().getColor(R.color.green));
        data3.setColor(((MainActivity)this).getResources().getColor(R.color.blue));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        data.add(data2);
        data.add(data3);

        LineData lineData = new LineData(data);
        Constants.lineChart_imu.setData(lineData);
//        lineChart.getAxisLeft().setAxisMaximum(90);
//        lineChart.getAxisLeft().setAxisMinimum(-90);
        Constants.lineChart_imu.notifyDataSetChanged();
        Constants.lineChart_imu.invalidate();
    }
}