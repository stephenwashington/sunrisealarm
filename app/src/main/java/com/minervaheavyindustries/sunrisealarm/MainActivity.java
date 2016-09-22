package com.minervaheavyindustries.sunrisealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TextView alarmStatus;
    TextView ringtoneText;
    Context context;
    Intent intent;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        intent = new Intent(this.context, AlarmReceiver.class);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmStatus = (TextView)findViewById(R.id.alarm_status);
        ringtoneText = (TextView)findViewById(R.id.ringtone_setting);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), uri);
        intent.putExtra("ringtone", uri.toString());
        setRingtoneText(r.getTitle(getApplicationContext()));

        Button setAlarm = (Button)findViewById(R.id.set_alarm);
        Button unsetAlarm = (Button)findViewById(R.id.unset_alarm);
        Button setAlarmSound = (Button)findViewById(R.id.set_alarm_sound);

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
    }

    private void setAlarmStatus(String s) {
        alarmStatus.setText(s);
        Log.d("MainActivity", s);
    }

    private void setRingtoneText(String s) {
        ringtoneText.setText(s);
        Log.d("MainActivity", s);
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
