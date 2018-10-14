package studio.microworld.hypernote.support.utlis;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Mr.小世界 on 2018/8/21.
 */

public final class CheckUtil
{
    public static boolean isEmail(String string)
    {
        if (TextUtils.isEmpty(string))
        {
            return false;
        }
        final String regEx1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern p;
        Matcher m;
        p = Pattern.compile(regEx1);
        m = p.matcher(string);
        if (m.matches())
        {
            return true;
        } else
        {
            return false;
        }
    }

    public static boolean isUsername(String s)
    {
        if (TextUtils.isEmpty(s))
        {
            return false;
        }
        if (s.length() < 6)
        {
            return false;
        } else
        {
            return true;
        }
    }

    public static boolean isPassword(String s)
    {
        if (TextUtils.isEmpty(s))
        {
            return false;
        }
        if (s.length() < 5)
        {
            return false;
        } else
        {
            return true;
        }
    }

    public static <T> void safeCallback(final AsyncCallback<T> callback, T result)
    {
        if (callback != null)
        {
            callback.onResult(result);
        }
    }

}
