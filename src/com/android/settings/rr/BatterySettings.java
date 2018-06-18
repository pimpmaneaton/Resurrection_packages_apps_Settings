/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.rr;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.graphics.Color;
import android.provider.Settings;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.settings.rr.Preferences.SystemSettingMasterSwitchPreference;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

public class BatterySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private SystemSettingMasterSwitchPreference mBatteryBarPosition;
    private static final String BATTERY_STYLE = "battery_style";
    private static final String BATTERY_PERCENT = "show_battery_percent";
    private static final String PREF_BATT_BAR_COLOR = "statusbar_battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "statusbar_battery_bar_charging_color";
    private static final String STATUSBAR_BATTERY_LOW_COLOR_WARNING = "statusbar_battery_bar_battery_low_color";
    private static final String STATUS_BAR_BAR_LOW_COLOR = "statusbar_battery_bar_low_color";
    private static final String STATUS_BAR_BAR_HIGH_COLOR = "statusbar_battery_bar_high_color";

    private ListPreference mBatteryIconStyle;
    private ListPreference mBatteryPercentage;

    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;
    private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
    private ColorPickerPreference mBatteryBarBatteryHighColor;

    static final int DEFAULT = 0xffffffff;
    static final int highColor = 0xff99CC00;
    static final int lowColor = 0xffff4444;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rr_battery);

        int intColor;
        String hexColor;

        ContentResolver resolver = getActivity().getContentResolver();
        int batteryStyle = Settings.Secure.getInt(getActivity().getContentResolver(),
                Settings.Secure.STATUS_BAR_BATTERY_STYLE, 0);

        mBatteryIconStyle = (ListPreference) findPreference(BATTERY_STYLE);
        mBatteryIconStyle.setValue(Integer.toString(batteryStyle));
        mBatteryIconStyle.setOnPreferenceChangeListener(this);
        int valueIndex = mBatteryIconStyle.findIndexOfValue(String.valueOf(batteryStyle));
        mBatteryIconStyle.setSummary(mBatteryIconStyle.getEntries()[valueIndex]);
        int showPercent = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SHOW_BATTERY_PERCENT, 1);

        mBatteryPercentage = (ListPreference) findPreference(BATTERY_PERCENT);
        mBatteryPercentage.setValue(Integer.toString(showPercent));
                valueIndex = mBatteryPercentage.findIndexOfValue(String.valueOf(showPercent));
        mBatteryPercentage.setSummary(mBatteryPercentage.getEntries()[valueIndex]);
        mBatteryPercentage.setOnPreferenceChangeListener(this);
        boolean hideForcePercentage = batteryStyle == 6; /*text or hidden style*/
        mBatteryPercentage.setEnabled(!hideForcePercentage);

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
               Settings.System.STATUSBAR_BATTERY_BAR_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarColor.setNewPreviewColor(intColor);

        mBatteryBarBatteryLowColorWarn =
               (ColorPickerPreference) findPreference(STATUSBAR_BATTERY_LOW_COLOR_WARNING);
        mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
               Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(intColor);

        mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
               Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarChargingColor.setNewPreviewColor(intColor);

        mBatteryBarBatteryLowColor =
               (ColorPickerPreference) findPreference(STATUS_BAR_BAR_LOW_COLOR);
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
               Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, lowColor);
        hexColor = String.format("#%08x", (0xffff4444 & intColor));
        mBatteryBarBatteryLowColor.setNewPreviewColor(intColor);

        mBatteryBarBatteryHighColor = (ColorPickerPreference) findPreference(STATUS_BAR_BAR_HIGH_COLOR);
        mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
               Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, highColor);
        hexColor = String.format("#%08x", (0xff99CC00 & intColor));
        mBatteryBarBatteryHighColor.setNewPreviewColor(intColor);

        setHasOptionsMenu(true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mBatteryIconStyle) {
            int value = Integer.valueOf((String) newValue);
            Settings.Secure.putInt(getContentResolver(),
                    Settings.Secure.STATUS_BAR_BATTERY_STYLE, value);
                    int valueIndex = mBatteryIconStyle
                             .findIndexOfValue((String) newValue);
                     mBatteryIconStyle
                             .setSummary(mBatteryIconStyle.getEntries()[valueIndex]);
                    boolean hideForcePercentage = value == 7;/*text or hidden style*/
            mBatteryPercentage.setEnabled(!hideForcePercentage);
            return true;
         } else  if (preference == mBatteryPercentage) {
            int value = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
            Settings.System.SHOW_BATTERY_PERCENT, value);
            int valueIndex = mBatteryPercentage
                    .findIndexOfValue((String) newValue);
            mBatteryPercentage
                    .setSummary(mBatteryPercentage.getEntries()[valueIndex]);
            return true;
         } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
           return true;
         } else if (preference == mBatteryBarBatteryLowColorWarn) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, intHex);
            return true;
         }  else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, intHex);
            return true;
         } else if (preference == mBatteryBarBatteryLowColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, intHex);
            return true;
         } else if (preference == mBatteryBarBatteryHighColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, intHex);
         }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.colors_reset_title);
        alertDialog.setMessage(R.string.colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
	ContentResolver resolver = getActivity().getContentResolver();
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, DEFAULT);
        mBatteryBarColor.setNewPreviewColor(DEFAULT);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR, DEFAULT);
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(DEFAULT);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, DEFAULT);
        mBatteryBarChargingColor.setNewPreviewColor(DEFAULT);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, lowColor);
        mBatteryBarBatteryLowColor.setNewPreviewColor(lowColor);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, highColor);
        mBatteryBarBatteryHighColor.setNewPreviewColor(highColor);
	}

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

}
