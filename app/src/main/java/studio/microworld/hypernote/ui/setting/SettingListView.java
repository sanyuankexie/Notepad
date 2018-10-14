package studio.microworld.hypernote.ui.setting;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import studio.microworld.hypernote.R;

/**
 * Created by Mr.小世界 on 2018/9/27.
 */

public final class SettingListView extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setting);

    }
}
