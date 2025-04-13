package com.example.microphone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class OfflineRecorder extends Thread {

    AudioRecord rec;
    int minbuffersize;
    boolean recording;
    int count;
    Context context;
    String filename;
    int fs;
    int freq;

    public OfflineRecorder(int microphone, int fs, int bufferLen, Context context, String filename, int freq) {
        this.context = context;
        this.filename = filename;
        this.fs = fs;
        this.freq = freq;
        int channels = AudioFormat.CHANNEL_IN_MONO;

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(this.fs)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelIndexMask(0b000001111)  // Configuring to record from the front microphone
                .build();

        minbuffersize = AudioRecord.getMinBufferSize(fs, channels, AudioFormat.ENCODING_PCM_16BIT);

//        int buffersize = AudioRecord.getMinBufferSize(fs,
//                AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        rec = new AudioRecord.Builder()
//                .setAudioSource(MediaRecorder.AudioSource.CAMCORDER)  // try using the rear mic
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(minbuffersize)
                .build();
        Constants.temp = new short[minbuffersize];
        Constants.samples = new short[bufferLen*4];
    }

    int graph_length=32000;
    public void run() {
        int bytesread;
        graph_setup();
        rec.startRecording();
        recording=true;
//        int factor=graph_length/Constants.temp.length;
        while(recording) {
            bytesread = rec.read(Constants.temp, 0, minbuffersize);

            if((count-graph_length)%graph_length==0 && count >= graph_length) {
                process(count-graph_length);
            }

//            Log.e("asdf","counter "+count+","+Constants.samples.length+","+minbuffersize);
            for (int i = 0; i < bytesread; i++) {
                if (count >= Constants.samples.length) {
                    recording = false;
                    FileOperations.writeToDisk(context,filename);
                    break;
                } else {
                    Constants.samples[count] = Constants.temp[i];
                    count += 1;
                }
            }
        }
    }

    public void graph_setup() {
        Constants.lineChart_mic.getXAxis().setAxisMinimum(0);
        Constants.lineChart_mic.getXAxis().setAxisMaximum(graph_length);
        Constants.lineChart_mic.getAxisLeft().setAxisMinimum(-5000);
        Constants.lineChart_mic.getAxisLeft().setAxisMaximum(5000);
    }

    public void process(int idx) {
//        double[]out=fftnative_short(Constants.temp,Constants.temp.length);
        List<Entry> lineData=new ArrayList<>();
        Log.e("mydebug",(idx+1)+"");
        for(int i = 1; i < graph_length; i+=4) {
            lineData.add(new Entry(i, (float) Constants.samples[idx+i]));
        }
        LineDataSet data1 = new LineDataSet(lineData, "");
        data1.setDrawCircles(false);
        data1.setColor(context.getResources().getColor(R.color.red));
        List<ILineDataSet> data = new ArrayList<>();
        data.add(data1);
        Constants.lineChart_mic.setData(new LineData(data));
        Constants.lineChart_mic.notifyDataSetChanged();
        Constants.lineChart_mic.invalidate();
    }

    public void halt() {
        if (rec.getState() == AudioRecord.STATE_INITIALIZED||
                rec.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            rec.stop();
        }
        rec.release();
        recording = false;
        FileOperations.writeToDisk(context,filename);
    }

    public static native double[] fftnative_short(short[] data, int N);

}