package com.minervaheavyindustries.sunrisealarm;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends AppCompatActivity {

    MediaPlayer mp;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        Button stopAlarm = (Button)findViewById(R.id.stop_alarm);

        Bundle intent = getIntent().getExtras();
        final String ringtoneName = intent.getString("ringtone");

        if (!(ringtoneName == null) && !ringtoneName.equals("silent")){
            Uri ringtoneUri = Uri.parse(ringtoneName);
            Log.d("AlarmActivity", ringtoneUri.toString());
            mp = MediaPlayer.create(this, ringtoneUri);
            mp.setLooping(true);
            mp.start();
        }


        //sendNotification();
        stopAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (!ringtoneName.equals("silent") && mp != null){ mp.stop(); }
                Intent backToMain = new Intent(AlarmActivity.this, MainActivity.class);
                backToMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(backToMain);
            }
        });

    }

    /*protected void sendNotification(){
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder alarmNotification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.alarm_clock)
                .setContentTitle("It's sunrise!")
                .setContentText("Tap to turn off the alarm")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1, alarmNotification.build());
        Log.d("AlarmActivity", "Notification Sent");
    }*/

}
