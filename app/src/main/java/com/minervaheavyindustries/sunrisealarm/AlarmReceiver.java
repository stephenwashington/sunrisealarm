package com.minervaheavyindustries.sunrisealarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        String ringtone = intent.getExtras().getString("ringtone");
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.putExtra("ringtone", ringtone);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);

    }
}
