package com.minervaheavyindustries.sunrisealarm;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

// http://stackoverflow.com/questions/1974193/slider-on-my-preferencescreen
public class SeekBarPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    private SeekBar seekBar;
    private TextView valueText;
    private Context mContext;
    private int defaultVal, maxValue, value = 0;

    public SeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        defaultVal = 60;
        maxValue = 120;
    }

    @Override
    protected View onCreateDialogView() {

        LinearLayout.LayoutParams params;
        LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(6, 6, 6, 6);

        valueText = new TextView(mContext);
        valueText.setGravity(Gravity.CENTER_HORIZONTAL);
        valueText.setTextSize(24);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(valueText, params);

        seekBar = new SeekBar(mContext);
        seekBar.setOnSeekBarChangeListener(this);
        layout.addView(seekBar, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        if (shouldPersist()) {
            value = getPersistedInt(defaultVal);
        }

        seekBar.setMax(maxValue);
        seekBar.setProgress(value);

        return layout;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);
        seekBar.setMax(maxValue);
        seekBar.setProgress(value);
    }

    @Override
    protected void onSetInitialValue(boolean restore, Object defaultValue) {
        super.onSetInitialValue(restore, defaultValue);
        if (restore) {
            value = shouldPersist() ? getPersistedInt(defaultVal) : 60;
        } else {
            value = (Integer) defaultValue;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seek, int value, boolean fromTouch) {
        String t = String.valueOf(value - 60);
        valueText.setText(t.concat(" " + ((value == 59 || value == 61) ? "minute" : "minutes")));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seek) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seek) {
    }

    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);
        Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (shouldPersist()) {
            value = seekBar.getProgress();
            persistInt(seekBar.getProgress());
            callChangeListener(Integer.valueOf(seekBar.getProgress()));
        }
        (getDialog()).dismiss();
    }

}
