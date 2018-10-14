package studio.microworld.hypernote.support.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.UiThread;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.json.WallpaperArray;
import studio.microworld.hypernote.support.managmanet.AppSettingManager;
import studio.microworld.hypernote.support.utlis.AsyncCallback;

/**
 * Created by Mr.小世界 on 2018/9/15.
 */

public final class ThemeManager
{

    private static Handler mHandler;

    private static WeakReference<Context> mContext;

    public static void initialize(Context context)
    {
        mContext = new WeakReference<>(context.getApplicationContext());
        mHandler = new Handler(Looper.getMainLooper());
    }

    @UiThread
    public static void loadWallpaperAsync(final AsyncCallback<Bitmap> callback)
    {
        final Runnable loadDefaultImage = new Runnable()
        {
            @Override
            public void run()
            {
                callback.onResult(BitmapFactory
                        .decodeResource(mContext.get().getResources(),
                                R.drawable.bk_animation));
            }
        };
        final Runnable loadWebImage = new Runnable()
        {
            @Override
            public void run()
            {
                String url = mContext.get().getString(R.string.bing_url);
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(3, TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder().url(url).build();
                client.newCall(request).enqueue(new Callback()
                {
                    @Override
                    public void onFailure(Call call, IOException e)
                    {
                        mHandler.post(loadDefaultImage);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException
                    {
                        String content = response.body().string();
                        final WallpaperArray wallpaperArray
                                = new Gson()
                                .fromJson(content, WallpaperArray.class);
                        final String url = wallpaperArray.toString();
                        mHandler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Glide.with(mContext.get())
                                        .load(url)
                                        .asBitmap()
                                        .priority(Priority.IMMEDIATE)
                                        .into(new SimpleTarget<Bitmap>()
                                        {
                                            @Override
                                            public void onResourceReady(final Bitmap resource,
                                                                        GlideAnimation<? super Bitmap>
                                                                                glideAnimation)
                                            {
                                                callback.onResult(resource);
                                                final String now = TimeUtils.getNowString();
                                                final String before
                                                        = AppSettingManager.getWallpaperTime();
                                                AppSettingManager.setWallpaperTime(now);
                                                if (before != null)
                                                {
                                                    FileUtils.deleteFile(new File(mContext.get()
                                                            .getCacheDir(),
                                                            before + ".jpg"));
                                                }
                                                File target = new File(mContext.get().getCacheDir(),
                                                        now + ".jpg");
                                                try
                                                {
                                                    if (!target.exists())
                                                    {
                                                        target.createNewFile();
                                                    }
                                                    FileOutputStream outputStream
                                                            = new FileOutputStream(target);
                                                    resource.compress(Bitmap.CompressFormat.WEBP,
                                                            50, outputStream);
                                                    outputStream.flush();
                                                    outputStream.close();
                                                } catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                            }
                        });
                    }
                });
            }
        };
        String time = AppSettingManager.getWallpaperTime();
        if (NetworkUtils.isConnected())
        {
            //不是今天或者没保存
            if (time == null || !TimeUtils.isToday(time))
            {
                Logger.d("不是今天或者没保存" + time);
                loadWebImage.run();
            } else//保存了
            {
                final File target = new File(mContext.get().getCacheDir(), time
                        + ".jpg");
                if (target.exists())
                {
                    Logger.d("保存了");
                    Glide.with(mContext.get())
                            .load(target)
                            .asBitmap()
                            .priority(Priority.IMMEDIATE)
                            .into(new SimpleTarget<Bitmap>()
                            {
                                @Override
                                public void onResourceReady(Bitmap resource,
                                                            GlideAnimation<? super Bitmap>
                                                                    glideAnimation)
                                {
                                    callback.onResult(resource);
                                }
                            });
                } else
                {
                    Logger.d("保存了但被删除了");
                    loadWebImage.run();
                }
            }
        } else
        {
            Logger.d("无网络");
            loadDefaultImage.run();
        }
    }
}
