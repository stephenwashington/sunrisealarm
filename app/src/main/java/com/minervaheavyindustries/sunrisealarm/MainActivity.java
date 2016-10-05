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

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class MainActivity extends PreferenceActivity {

    private static AlarmManager alarmManager;
    private static int ALARM_ID = 1117;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefsFragment()).commit();
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
    }

    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            updateSummary("ringtone");
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
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

        public void updateSummary(String key){
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            boolean alarmStatus = sp.getBoolean("alarm", false);

            switch(key){
                case "alarm":
                    if (alarmStatus){
                        updateAlarm();
                    } else {
                        cancelAlarm();
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
                    if (alarmStatus) { updateAlarm(); }

                case "offset":
                    SeekBarPreference sbp = (SeekBarPreference)findPreference("offset");
                    int sunriseOffset = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("offset", 60) - 60;
                    String offsetSummaryMessage;
                    String minute = "minutes";

                    if (sunriseOffset == 1 || sunriseOffset == -1) minute = "minute";
                    if (sunriseOffset == 0){
                        offsetSummaryMessage = "No offset";
                    } else if (sunriseOffset > 0){
                        offsetSummaryMessage = String.valueOf(sunriseOffset) + " " + minute + " " + R.string.after_sunrise;
                    } else {
                        offsetSummaryMessage = String.valueOf(-sunriseOffset) + " " + minute + " " + R.string.before_sunrise;
                    }
                    sbp.setSummary(offsetSummaryMessage);
                    if (alarmStatus) { updateAlarm(); }

                case "location":
                    LocationPreference lp = (LocationPreference)findPreference("location");
                    String location = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location", "38.897096, -77.036545");
                    lp.setSummary(location);
                    if (alarmStatus) { updateAlarm(); }
                default:
                    break;
            }
        }

        private void cancelAlarm(){
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), ALARM_ID, intent, 0);
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        private void updateAlarm(){
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            SwitchPreference switchPreference = (SwitchPreference)findPreference("alarm");
            Intent intent = new Intent(getActivity(), AlarmReceiver.class);

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

            PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), ALARM_ID, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager.set(AlarmManager.RTC_WAKEUP, sunriseLocal.getMillis(), pendingIntent);

            // Update Preference Summary
            DateTimeFormatter formatter = DateTimeFormat.forPattern("E, yyyy-MM-dd HH:mm:ss Z");
            switchPreference.setSummary(sunriseLocal.toString(formatter));
        }

    }

}
