package studio.microworld.hypernote.support.utlis;

import com.google.gson.Gson;

/**
 * Created by Mr.小世界 on 2018/8/27.
 */

public final class GsonUtil
{
    private static Gson gson;

    public static Gson getGson()
    {
        if (gson == null)
        {
            gson = new Gson();
        }
        return gson;
    }
}
