package minervaheavyindustries.sunrisealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    AlarmManager alarmManager;
    TextView alarmStatus;
    Context context;
    PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;

        // initialize alarm manager
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

        // initialize text update box
        alarmStatus = (TextView)findViewById(R.id.alarm_status);

        // create instance of calendar

        final Calendar calendar = Calendar.getInstance();

        // initialize buttons
        Button setAlarm = (Button)findViewById(R.id.set_alarm);
        Button unsetAlarm = (Button)findViewById(R.id.unset_alarm);

        // create intent for AlarmReceiver
        Intent intent = new Intent(this.context, AlarmReceiver.class);

        // listener for setAlarm
        setAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 50);
                setAlarmStatus("Alarm set for " + String.valueOf(calendar.getTime()));
            }
        });

        //listener for unsetAlarm
        unsetAlarm.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                alarmManager.cancel(pendingIntent);
                setAlarmStatus("Alarm off!");
            }
        });

        //create a pending intent that delays the intent until the specified calendar time
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //set the alarm manager
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);


    }

    private void setAlarmStatus(String s) {
        alarmStatus.setText(s);
    }
}
