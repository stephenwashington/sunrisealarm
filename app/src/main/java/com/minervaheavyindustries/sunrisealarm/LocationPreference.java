package com.minervaheavyindustries.sunrisealarm;


import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.preference.EditTextPreference;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class LocationPreference extends EditTextPreference {

    private Context mContext;
    private Activity mActivity;
    private String value;
    private static final int COARSE_LOCATION = 0;
    private static final int FINE_LOCATION = 1;

    public LocationPreference(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        mContext = context;
        mActivity = (Activity)context;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder){
        super.onPrepareDialogBuilder(builder);
        builder.setNeutralButton(R.string.location_neutral, this);
        final EditText editText = getEditText();
        SharedPreferences sp = getPreferenceManager().getSharedPreferences();

        if (editText.getText().toString().equals("")){
            editText.setText(sp.getString("location", "38.897096, -77.036545"));
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentInput = editText.getText().toString();
                if (validInput(currentInput)){
                    value = currentInput;
                }

            }
        });
    }

    private boolean validInput(String s){
        return s.matches("(-)?\\d+[.]\\d+, (-)?\\d+[.]\\d+");
    }

    @Override
    public void onClick(DialogInterface dialog, int id){
        switch (id) {
            case DialogInterface.BUTTON_NEUTRAL:
                Log.d("LocationPreference", "Acquiring new GPS...");
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestCoarseLocationPermission();
                } else if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    requestFineLocationPermission();
                } else{
                    Log.d("LocationPreference", "Location permissions granted");
                    Location loc = null;
                    for (int i = 0; i < 2; i++){
                        loc = LocationUtilities.getLocation(mContext);
                    }
                    if (loc != null){
                        double latitude = loc.getLatitude();
                        double longitude = loc.getLongitude();
                        value = String.format(Locale.US, "%.5f, %.5f", latitude, longitude);
                        this.setText(value);
                    } else {
                        Log.d("LocationPreference", "Location came back null!");
                        Toast.makeText(getContext(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case DialogInterface.BUTTON_POSITIVE:
                this.setText(value);
                break;
            default:
                break;
        }
    }

    private void requestCoarseLocationPermission(){
        Log.d("LocationPreference","Coarse Location Permission has not been granted. Requesting permission...");
        if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)){
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, COARSE_LOCATION);
        }
    }

    private void requestFineLocationPermission(){
        Log.d("LocationPreference", "Fine Location Permission has not been granted. Requesting permission...");
        if (!ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)){
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION);
        }
    }

    @TargetApi(23)
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == FINE_LOCATION){
            Log.d("LocationPreference", "Recieved response for fine location");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), R.string.location_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.location_denied, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == COARSE_LOCATION){
            Log.d("LocationPreference", "Received response for coarse location");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getContext(), R.string.location_granted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.location_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
