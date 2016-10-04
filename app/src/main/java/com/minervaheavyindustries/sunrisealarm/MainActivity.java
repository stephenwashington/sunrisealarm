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

import java.util.Calendar;

/*TODO:
- Fix bug where getting location for non-UTC timezones skips ahead two weeks
*/
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
            updateAllSummaries(false);
        }

        @Override
        public void onResume() {
            super.onResume();
            Log.d("MainActivity", "onResume");
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            updateAllSummaries(false);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        public void updateAllSummaries(boolean makeToast){
            updateSummary("ringtone", makeToast);
            updateSummary("offset", makeToast);
            updateSummary("location", makeToast);
        }



        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            Log.d("MainActivity", "SharedPreferenceChanged!");
            switch(key){
                case "ringtone":
                    updateSummary("ringtone", true);
                case "offset":
                    updateSummary("offset", true);
                case "location":
                    updateSummary("location", true);
                case "alarm":
                    updateSummary("alarm", true);
                default:
                    break;
            }
        }

        public void showToast(String message){
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }

        public void updateSummary(String key, boolean makeToast){
            SharedPreferences sp = getPreferenceManager().getSharedPreferences();
            Log.d("updateSummary", key + " " + String.valueOf(makeToast));

            if (key.equals("alarm")){
                SwitchPreference switchPreference = (SwitchPreference)findPreference("alarm");
                boolean alarmStatus = sp.getBoolean("alarm", false);
                if (alarmStatus){
                    // set the alarm sound
                    String ringtonePref = sp.getString("ringtone", "");
                    Uri ringtoneUri = Uri.parse(ringtonePref);
                    intent.putExtra("ringtone", ringtoneUri.toString());

                    //Get the alarm time
                    Calendar now = Calendar.getInstance();
                    String location = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location", "38.897096, -77.036545");
                    String[] latlong = location.split(",");
                    double latitude = Double.valueOf(latlong[0].replaceAll("\\s+",""));
                    double longitude = Double.valueOf(latlong[1].replaceAll("\\s+",""));
                    Calendar sunriseUTC = SunriseCalculator.getSunrise(latitude, longitude, now);

                    // Localize, set offset
                    Calendar sunriseLocal = Calendar.getInstance();
                    sunriseLocal.setTimeInMillis(sunriseUTC.getTimeInMillis());
                    int sunriseOffset = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getInt("offset", 60) - 60;
                    sunriseLocal.add(Calendar.MINUTE, sunriseOffset);

                    // Set the alarm itself
                    pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    alarmManager.set(AlarmManager.RTC_WAKEUP, sunriseLocal.getTimeInMillis(), pendingIntent);

                    // Update Preference Summary
                    switchPreference.setSummary(sunriseLocal.getTime().toString());

                    //Notify User
                    if (makeToast){ showToast("Alarm Set"); }
                } else {
                    alarmManager.cancel(pendingIntent);
                    if (makeToast){ showToast("Alarm Unset"); }
                }

            }
            if (key.equals("ringtone")){
                String ringtonePref = sp.getString("ringtone", "");
                RingtonePreference rp = (RingtonePreference)findPreference("ringtone");

                if (ringtonePref.equals("")){
                    Uri ringtoneUri = Uri.parse(ringtonePref);
                    Ringtone ringtone = RingtoneManager.getRingtone(getActivity(), ringtoneUri);
                    String ringtoneName = ringtone.getTitle(getActivity());
                    rp.setSummary(ringtoneName);
                    if (makeToast){ showToast("Ringtone set to " + ringtoneName); }
                } else {
                    rp.setSummary("Silent");
                    if (makeToast){ showToast("Ringtone set to Silent"); }
                }



            }
            if (key.equals("offset")){
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
                if (makeToast) { showToast("Offset set to " + offsetSummaryMessage);}

            }

            if (key.equals("location")){
                LocationPreference lp = (LocationPreference)findPreference("location");
                String location = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString("location", "38.897096, -77.036545");
                Log.d("MainActivity", location);
                lp.setSummary(location);
                if (makeToast) { showToast("Location set to " + location); }
            }

        }

    }

}
