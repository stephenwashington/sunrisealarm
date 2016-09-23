package com.minervaheavyindustries.sunrisealarm;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import android.support.design.widget.Snackbar;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TextView alarmStatus;
    TextView ringtoneText;
    TextView locationText;
    Context context;
    Intent intent;
    PendingIntent pendingIntent;
    Location location;
    private View mLayout;

    private static final int COARSE_LOCATION = 0;
    private static final int FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        intent = new Intent(this.context, AlarmReceiver.class);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmStatus = (TextView)findViewById(R.id.alarm_status);
        ringtoneText = (TextView)findViewById(R.id.ringtone_setting);
        locationText = (TextView)findViewById(R.id.location_text);
        location = LocationUtilities.getLocation(context);
        if (location != null){
            setLocationText(String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
        } else {
            Log.d("MainActivity", "Location is null!");
        }

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
        intent.putExtra("ringtone", uri.toString());
        setRingtoneText(r.getTitle(getApplicationContext()));

        Button setAlarm = (Button)findViewById(R.id.set_alarm);
        Button unsetAlarm = (Button)findViewById(R.id.unset_alarm);
        Button setAlarmSound = (Button)findViewById(R.id.set_alarm_sound);
        Button getLocationButton = (Button)findViewById(R.id.get_location_button);


        setAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.SECOND, 5);
                setAlarmStatus("Alarm set for " + String.valueOf(calendar.getTime()));
                pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        });

        unsetAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                alarmManager.cancel(pendingIntent);
                setAlarmStatus("Alarm off!");
            }
        });

        setAlarmSound.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent2 = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent2.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Ringtone");
                intent2.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
                intent2.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                intent2.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,RingtoneManager.TYPE_ALARM);
                startActivityForResult(intent2, 999);
            }
        });

        getLocationButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestCoarseLocationPermission();
                } else if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestFineLocationPermission();
                } else{
                    Log.d("MainActivity","Location permissions granted");
                    location = LocationUtilities.getLocation(getApplicationContext());
                    if (location == null){
                        Log.d("MainActivity", "location came back null!");
                    } else {
                        setLocationText(String.valueOf(location.getLatitude()) + ", " + String.valueOf(location.getLongitude()));
                    }


                }
            }
        });
    }

    private void setLocationText(String s){
        locationText.setText(s);
        Log.d("MainActivity", s);
    }

    private void setAlarmStatus(String s) {
        alarmStatus.setText(s);
        Log.d("MainActivity", s);
    }

    private void setRingtoneText(String s) {
        ringtoneText.setText(s);
        Log.d("MainActivity", s);
    }

    private void requestCoarseLocationPermission(){
        Log.d("MainActivity","Coarse Location Permission has not been granted. Requesting permission...");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
            Snackbar.make(mLayout, "Sunrise Alarm needs your location to calculate the sunrise", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Okay", new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
                        }
                    }).show();
        } else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
        }
    }

    private void requestFineLocationPermission(){
        Log.d("MainActivity", "Fine Location Permission has not been granted. Requesting permission...");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Snackbar.make(mLayout, "Sunrise Alarm needs your location to calculate the sunrise", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Okay", new View.OnClickListener(){
                        @Override
                        public void onClick(View v){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
                        }
                    }).show();
        } else{
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
        }
    }

    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == FINE_LOCATION){
            Log.d("MainActivity", "Recieved response for fine location");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("MainActivity", "Permission granted for fine location");
                Snackbar.make(mLayout, "Permission granted for fine location", Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d("MainActivity", "Permission denied for fine location");
                Snackbar.make(mLayout, "Permission denied for fine location", Snackbar.LENGTH_SHORT).show();
            }
        } else if (requestCode == COARSE_LOCATION){
            Log.d("MainActivity", "Received response for coarse location");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d("MainActivity", "Permission granted for coarse location");
                Snackbar.make(mLayout, "Permission granted for coarse location", Snackbar.LENGTH_SHORT).show();
            } else {
                Log.d("MainActivity", "Permission denied for coarse location");
                Snackbar.make(mLayout, "Permission denied for coarse location", Snackbar.LENGTH_SHORT).show();
            }
        } else{
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = (Uri)data.getExtras().get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
            intent.putExtra("ringtone", uri.toString());
            setRingtoneText(r.getTitle(getApplicationContext()));
        }
    }
}
