package com.wingsandbeer.whatsmypitch;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Tuner extends AppCompatActivity {
    private int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
    private int BytesPerElement = 2; // 2 bytes in 16bit format

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;

    private void permissionCheckAndProceed() {
        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO}, 1);
    }

    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        for (int i=0; i<permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                this.finish();
                System.exit(0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions. Only do this once app starts.
        permissionCheckAndProceed();

        setContentView(R.layout.activity_tuner);
        TextView tv = (TextView) findViewById(R.id.content);
        tv.setText("Welcome to super awesome Tuner!!!");
    }

    public void tunerButtonPress(View view) {
        TextView tv = (TextView) findViewById(R.id.content);

        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);

        if(isRecording) {
            tv.setText("Analysis Ended!");
            stopRecordingAnalysis();
            Button i = (Button)findViewById(R.id.tuner_button);
            i.setText(R.string.button_tuner_stop);
        } else {
            tv.setText("Analysis Started...");
            startRecordingAnalysis();
            Button i = (Button)findViewById(R.id.tuner_button);
            i.setText(R.string.button_tuner_start);
        }
    }

    private void startRecordingAnalysis() {

        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

        recorder.startRecording();
        isRecording = true;
        recordingThread = new Thread(new Runnable() {
            public void run() {
                analyzeAudioData();
            }
        }, "AudioRecorder Thread");
        recordingThread.start();
    }

    private void stopRecordingAnalysis() {

        // stops the recording activity
        if (null != recorder) {
            isRecording = false;
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }

    private void analyzeAudioData(){
        short sData[] = new short[BufferElements2Rec];
        int pitchLoc;
        int pitchHz;
        int lastValidPitch = 0;
        while (isRecording) {
            /* gets the voice output from microphone to byte format */
            recorder.read(sData, 0, BufferElements2Rec);
            Complex[] input_complex = new Complex[BufferElements2Rec];
            for(int i = 0; i<BufferElements2Rec; i++){
                input_complex[i] = new Complex(sData[i]);
            }

            Complex[] output = FFT.fft(input_complex);

            pitchLoc = maxLoc(output);
            pitchHz = pitchLoc*RECORDER_SAMPLERATE/BufferElements2Rec;

            if(pitchHz > RECORDER_SAMPLERATE/2){
                System.out.println(lastValidPitch);
            }else{
                System.out.println(pitchHz);
                lastValidPitch = pitchHz;
            }

        }
    }

    private static int maxLoc(Complex[] cmp) {
        double maxVal = 0;
        int maxInd = -1;
        double magnitude;
        for (int ktr = 0; ktr < cmp.length; ktr++) {
            magnitude = cmp[ktr].abs();
            if(magnitude > maxVal) {
                maxVal = magnitude;
                maxInd = ktr;
            }
        }
        return maxInd;
    }
}
