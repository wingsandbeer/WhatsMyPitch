package com.wingsandbeer.whatsmypitch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class Tuner extends AppCompatActivity {
    private static final String LOG_TAG = "WhatsMyPitch::Tuner";

    private boolean granted_record_permission = false;
    private boolean granted_rw_storage_permission = false;

    private void permissionCheckAndProceed() {
        // Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
        granted_rw_storage_permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        granted_record_permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;

        ActivityCompat.requestPermissions(
                this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO}, 1);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        for (int i=0; i<permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                this.finish();
                System.exit(0);
            } else {
                switch(permissions[i]) {
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        granted_rw_storage_permission = true;
                        break;
                    case Manifest.permission.RECORD_AUDIO:
                        granted_record_permission = true;
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check permissions. Only do this once app starts.
        permissionCheckAndProceed();

        // Wait for results from the permissions.
        int counter = 0;
        while (!granted_rw_storage_permission || !granted_record_permission) {
            // If no response received within 100 seconds, quit.
            if (counter == 1000) {
                this.finish();
                System.exit(1);
            }
            counter++;
            try {
                Thread.sleep(100);
            } catch(InterruptedException e) {
                Log.e(LOG_TAG, "wait for permissions interrupted.");
                return;
            }
        }

        setContentView(R.layout.activity_tuner);

        TextView tv = (TextView) findViewById(R.id.content);
        tv.setText("Welcome to super awesome Tuner!!!");
    }

    public void recordButtonPress(View view) {}
    public void playButtonPress(View view) {}
    public void tunerButtonPress(View view) {}
}
