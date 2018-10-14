package studio.microworld.hypernote.support.utlis;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.blankj.utilcode.util.Utils;
import com.orhanobut.logger.Logger;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by Mr.小世界 on 2018/8/24.
 */

public final class DeviceUtil
{
    private static final String PREFS_FILE = "device_id";

    private static final String PREFS_DEVICE_ID = "device_id";

    private static UUID uuid;

    private static String deviceType = "0";

    private static final String TYPE_ANDROID_ID = "1";

    private static final String TYPE_DEVICE_ID = "2";

    private static final String TYPE_RANDOM_UUID = "3";

    private static void initialize()
    {
        synchronized (DeviceUtil.class)
        {
            Context context = Utils.getContext();
            if (uuid == null)
            {
                final SharedPreferences prefs
                        = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
                final String id = prefs.getString(PREFS_DEVICE_ID, null);
                if (id != null)
                {
                    uuid = UUID.fromString(id);
                } else
                {
                    final String androidId
                            = Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.ANDROID_ID);
                    try
                    {
                        if (!"9774d56d682e549c".equals(androidId))
                        {
                            deviceType = TYPE_ANDROID_ID;
                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                        } else
                        {
                            final String deviceId
                                    = ((TelephonyManager) context
                                    .getSystemService(Context.TELEPHONY_SERVICE))
                                    .getDeviceId();
                            if (deviceId != null
                                    && !"0123456789abcdef".equals(deviceId.toLowerCase())
                                    && !"000000000000000".equals(deviceId.toLowerCase()))
                            {
                                deviceType = TYPE_DEVICE_ID;
                                uuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
                            } else
                            {
                                deviceType = TYPE_RANDOM_UUID;
                                uuid = UUID.randomUUID();
                            }
                        }
                    } catch (UnsupportedEncodingException e)
                    {
                        deviceType = TYPE_RANDOM_UUID;
                        uuid = UUID.randomUUID();
                    } finally
                    {
                        uuid = UUID.fromString(deviceType + uuid.toString());
                    }
                    prefs.edit().putString(PREFS_DEVICE_ID, uuid.toString()).commit();
                }
            }
        }
    }

    public static UUID getDeviceUuid()
    {
        if (uuid == null)
        {
            initialize();
        }
        Logger.d("------>获取的设备ID号为：" + uuid.toString());
        return uuid;
    }
}
