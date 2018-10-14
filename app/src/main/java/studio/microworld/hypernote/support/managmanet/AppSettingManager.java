package studio.microworld.hypernote.support.managmanet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;


import java.lang.ref.WeakReference;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.adapter.NoteListAdapter;
import studio.microworld.hypernote.support.pref.AccountInfo;
import studio.microworld.hypernote.support.pref.UserSettings;
import studio.microworld.hypernote.support.utlis.GsonUtil;

/**
 * Created by Mr.小世界 on 2018/9/14.
 */

public final class AppSettingManager
{
    private static SharedPreferences sharedPreferences;

    private static WeakReference<Context> mContext;

    public static void initialize(Context context)
    {
        mContext = new WeakReference<>(context.getApplicationContext());
        sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(context.getApplicationContext());
    }

    private AppSettingManager()
    {
    }

    public static boolean isFirstLaunch()
    {
        return sharedPreferences.getBoolean(getString(R.string.is_first_key), true);
    }

    public static void triggerFirstLaunch()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getString(R.string.is_first_key), false);
        editor.apply();
    }

    public static void setUserSettings(UserSettings userSettings)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getString(R.string.select_folder_id_key), userSettings.selectFolderId);
        editor.putBoolean(getString(R.string.is_lock_key), userSettings.isUseRecovery);
        editor.putBoolean(getString(R.string.is_recovery_key), userSettings.isUseRecovery);
        editor.putString(getString(R.string.lock_password_key), userSettings.lockPassword);
        editor.putInt(getString(R.string.note_list_style_key), userSettings.noteListStyle);
        editor.apply();
    }

    public static UserSettings getUserSettings()
    {
        UserSettings userSettings = new UserSettings();
        int select = sharedPreferences.getInt(getString(R.string.select_folder_id_key),
                Integer.MIN_VALUE);
        if (select != Integer.MIN_VALUE)
        {
            userSettings.selectFolderId = select;
            userSettings.isLocked
                    = sharedPreferences.getBoolean(getString(R.string.is_lock_key), false);
            userSettings.isUseRecovery
                    = sharedPreferences.getBoolean(getString(R.string.is_recovery_key), true);
            userSettings.lockPassword
                    = sharedPreferences.getString(getString(R.string.lock_password_key), null);
            userSettings.noteListStyle = sharedPreferences.getInt(getString(R.string.note_list_style_key),
                    NoteListAdapter.LINER_LAYOUT);
            return userSettings;
        }
        return userSettings;
    }

    public static String getWallpaperTime()
    {
        return sharedPreferences.getString(getString(R.string.wallpaper_time_key), null);
    }

    public static void setWallpaperTime(String time)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.wallpaper_time_key), time);
        editor.apply();
    }

    public static void setAccountInfo(AccountInfo accountInfo)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.username_key), accountInfo.username);
        editor.putString(getString(R.string.password_key), accountInfo.password);
        editor.apply();
    }

    public static void clearAccountInfo()
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(getString(R.string.username_key));
        editor.remove(getString(R.string.password_key));
        editor.apply();
    }

    public static AccountInfo getAccountInfo()
    {
        String username = sharedPreferences.getString(getString(R.string.username_key), null);
        if (username != null)
        {
            AccountInfo info = new AccountInfo();
            info.username = username;
            info.password = sharedPreferences.getString(getString(R.string.password_key), null);
            return info;
        }
        return null;
    }

    public static SharedPreferences getSharedPreferences()
    {
        return sharedPreferences;
    }

    private static String getString(@StringRes int id)
    {
        return mContext.get().getString(id);
    }
}
