package com.minervaheavyindustries.sunrisealarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MainActivity extends PreferenceActivity {

    private static Intent intent;
    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        intent = new Intent(this, AlarmReceiver.class);
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);

    }

    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState){
            Log.d("MainActivity", "onCreate");
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
            addPreferencesFromResource(R.xml.preferences);
            updateAllSummaries();
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d("MainActivity", "onResume");
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            updateAllSummaries();
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        public void updateAllSummaries(){
            updateSummary("ringtone");
            updateSummary("offset");
            updateSummary("location");
        }



        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            Log.d("MainActivity", "SharedPreferenceChanged - " + key);
            switch(key){
                case "ringtone":
                    updateSummary("ringtone");
                case "offset":
                    updateSummary("offset");
                case "location":
                    updateSummary("location");
                case "alarm":
                    updateSummary("alarm");
                default:
                    break;
            }
        }

        public void showToast(String message){
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }

        public void updateSummary(String key){
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();

            switch(key){
                case "alarm":
                    boolean alarmStatus = sp.getBoolean("alarm", false);
                    if (alarmStatus){
                        updateAlarm();
                        showToast("Alarm Set");
                    } else {
                        if (pendingIntent != null){ alarmManager.cancel(pendingIntent); }
                        showToast("Alarm Unset");
                    }

                case "ringtone":
                    String ringtonePref = sp.getString("ringtone", "");
                    RingtonePreference rp = (RingtonePreference)findPreference("ringtone");

                    if (!ringtonePref.equals("")){
                        Uri ringtoneUri = Uri.parse(ringtonePref);
                        Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                        String ringtoneName = ringtone.getTitle(getActivity());
                        rp.setSummary(ringtoneName);
                    } else {
                        rp.setSummary("Silent");
                    }
                    updateAlarm();

                case "offset":
                    SeekBarPreference sbp = (SeekBarPreference)findPreference("offset");
                    int sunriseOffset = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("offset", 60) - 60;
                    String offsetSummaryMessage;
                    String minute = "minutes";

                    if (sunriseOffset == 1 || sunriseOffset == -1) minute = "minute";
                    if (sunriseOffset == 0){
                        offsetSummaryMessage = "No offset";
                    } else if (sunriseOffset > 0){
                        offsetSummaryMessage = String.valueOf(sunriseOffset) + " " + minute + " after sunrise";
                    } else {
                        offsetSummaryMessage = String.valueOf(-sunriseOffset) + " " + minute + " before sunrise";
                    }
                    sbp.setSummary(offsetSummaryMessage);
                    updateAlarm();

                case "location":
                    LocationPreference lp = (LocationPreference)findPreference("location");
                    String location = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location", "38.897096, -77.036545");
                    Log.d("MainActivity", location);
                    lp.setSummary(location);
                    updateAlarm();
                default:
                    break;
            }
        }

        private void updateAlarm(){
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            SwitchPreference switchPreference = (SwitchPreference)findPreference("alarm");

            if (pendingIntent != null){
                alarmManager.cancel(pendingIntent);
            }
            // set the alarm sound
            String ringtonePref = sp.getString("ringtone", "");
            Uri ringtoneUri = Uri.parse(ringtonePref);
            intent.putExtra("ringtone", ringtoneUri.toString());

            //Get the alarm time
            DateTime now = DateTime.now();
            String location = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location", "38.897096, -77.036545");
            String[] latlong = location.split(",");
            double latitude = Double.valueOf(latlong[0].replaceAll("\\s+",""));
            double longitude = Double.valueOf(latlong[1].replaceAll("\\s+",""));
            DateTime sunriseUTC = SunriseCalculator.getSunrise(latitude, longitude, now);

            // Localize, set offset
            DateTime sunriseLocal = sunriseUTC.withZone(now.getZone());
            int sunriseOffset = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("offset", 60) - 60;
            sunriseLocal = sunriseLocal.plusMinutes(sunriseOffset);

            // Set the alarm itself
            pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, sunriseLocal.getMillis(), pendingIntent);

            // Update Preference Summary
            DateTimeFormatter formatter = DateTimeFormat.forPattern("E, yyyy-MM-dd HH:mm:ss Z");
            switchPreference.setSummary(sunriseLocal.toString(formatter));
        }

    }

}
