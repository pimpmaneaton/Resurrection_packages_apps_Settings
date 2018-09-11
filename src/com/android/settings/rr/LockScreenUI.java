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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenUI extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "LockScreenSecurity";
    private static final String LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR =  "lock_screen_visualizer_custom_color";

    private ColorPickerPreference mVisualizerColor;
    private ListPreference mAmbientTicker;

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rr_ls_ui);

        final ContentResolver resolver = getActivity().getContentResolver();

        mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.ambient_ticker_footer);

        mAmbientTicker = (ListPreference) findPreference("force_ambient_for_media");
        int mode = Settings.System.getIntForUser(resolver,
                Settings.System.FORCE_AMBIENT_FOR_MEDIA, 0, UserHandle.USER_CURRENT);
        mAmbientTicker.setValue(Integer.toString(mode));
        mAmbientTicker.setSummary(mAmbientTicker.getEntry());
        mAmbientTicker.setOnPreferenceChangeListener(this);

        // Visualizer custom color
        mVisualizerColor = (ColorPickerPreference) findPreference(LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR);
        int visColor = Settings.System.getInt(resolver,
                Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, 0xff1976D2);
        String visColorHex = String.format("#%08x", (0xff1976D2 & visColor));
        mVisualizerColor.setNewPreviewColor(visColor);
        mVisualizerColor.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) 		{
        final ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mAmbientTicker) {
            int mode = Integer.valueOf((String) objValue);
            int index = mAmbientTicker.findIndexOfValue((String) objValue);
            mAmbientTicker.setSummary(
                    mAmbientTicker.getEntries()[index]);
            Settings.System.putIntForUser(resolver, Settings.System.FORCE_AMBIENT_FOR_MEDIA,
                    mode, UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mVisualizerColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(objValue)));
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.LOCK_SCREEN_VISUALIZER_CUSTOM_COLOR, intHex);
            return true;
        }
        return false;
    }
}
