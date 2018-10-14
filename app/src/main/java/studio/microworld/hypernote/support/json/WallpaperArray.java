package studio.microworld.hypernote.support.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;


/**
 * Created by Mr.小世界 on 2018/9/14.
 */

public final class WallpaperArray
{
    @SerializedName("images")
    public List<Wallpaper> images;

    public class Wallpaper
    {
        @SerializedName("url")
        public String partUrl;//这里的链接还需要加上微软的http://cn.bing.com在前面
    }

    @Override
    public String toString()
    {
        return "http://cn.bing.com" + images.get(0).partUrl;
    }
}
