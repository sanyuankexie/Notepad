package studio.microworld.hypernote.ui.launch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobConfig;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.pref.UserSettings;
import studio.microworld.hypernote.support.managmanet.AppSettingManager;
import studio.microworld.hypernote.support.managmanet.NoteDataManager;
import studio.microworld.hypernote.support.theme.ThemeManager;
import studio.microworld.hypernote.support.utlis.AsyncCallback;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.ui.main.HostActivity;

/**
 * Created by Mr.小世界 on 2018/8/22.
 */

//第一个Activity,这里会异步加载程序需要的资源
public final class LaunchActivity
        extends BaseActivity
{

    @BindView(R.id.iv_launch_image)
    ImageView mLaunchImage;

    private final String request[] = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };

    private final Lock wallpaperLock = new ReentrantLock();

    private final Lock requestLock = new ReentrantLock();

    //--------------------------base method------------------------

    @Override
    protected void onLoadLayoutBefore()
    {
        super.onLoadLayoutBefore();
        //去除title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //去掉Activity上面的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //优先初始化Context
        Context context = this.getApplicationContext();
        Utils.init(context);
        AppSettingManager.initialize(context);
        ThemeManager.initialize(context);
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        requestPermissions();
        loadLaunchImage();
        loadAppComponent();
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_launch;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case RC.request_code.REQUEST_CODE_TO_PERMISSIONS:
            {
                boolean test = true;
                // 权限申请
                for (int i = 0; i < grantResults.length; i++)
                {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        test = false;
                        ToastUtils.showShort("请至权限中心打开本应用的所需的权限");
                        jumpToSystemSetting();
                        System.exit(1);
                    }
                }
                if (test)
                {
                    requestLock.unlock();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    //----------------------internal----------------------------------------

    private void loadLaunchImage()
    {
        wallpaperLock.lock();
        ThemeManager.loadWallpaperAsync(new AsyncCallback<Bitmap>()
        {
            @Override
            public void onResult(Bitmap bitmap)
            {
                if (!isFinishing())
                {
                    mLaunchImage.setImageBitmap(bitmap);
                    wallpaperLock.unlock();
                }
            }
        });
    }

    private void requestPermissions()
    {
        requestLock.lock();
        final ArrayList<String> toApplyList = new ArrayList<>();
        for (String perm : request)
        {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat
                    .checkSelfPermission(Utils.getContext(), perm))
            {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        if (!toApplyList.isEmpty())
        {
            String[] toApplys = new String[toApplyList.size()];
            toApplyList.toArray(toApplys);
            ActivityCompat.requestPermissions(this, toApplys,
                    RC.request_code.REQUEST_CODE_TO_PERMISSIONS);
        } else
        {
            requestLock.unlock();
        }
    }

    private void loadAppComponent()
    {
        new AsyncTask<Void, Void, Intent>()
        {
            @Override
            protected Intent doInBackground(Void... params)
            {
                Context context = getApplicationContext();


                //自v3.4.7版本开始,设置BmobConfig,
                //允许设置请求超时时间、文件分片上传时每片的大小、文件的过期时间(单位为秒)，
                BmobConfig config = new BmobConfig.Builder(context)
                        ////请求超时时间（单位为秒）：默认15s
                        .setConnectTimeout(10)
                        ////设置appkey
                        .setApplicationId(LaunchActivity.this.getString(R.string.bmob_appid))
                        .build();
                Bmob.initialize(config);


                ///litepal
                LitePal.initialize(context);
                //note data
                NoteDataManager.initialize(context);


                AppSettingManager.triggerFirstLaunch();
                Intent intent = new Intent(LaunchActivity.this,
                        HostActivity.class);
                UserSettings userSettings = AppSettingManager.getUserSettings();
                intent.putExtra(getString(R.string.user_setting_key),userSettings);
                requestLock.lock();
                try
                {
                    wallpaperLock.tryLock(500, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                return intent;
            }

            @Override
            protected void onPostExecute(Intent intent)
            {
                jumpToMain(intent);
            }
        }.execute();
    }

    private void jumpToSystemSetting()
    {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9)
        {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", this.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8)
        {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", this.getPackageName());
        }
        this.startActivity(intent);
    }

    private void jumpToMain(Intent intent)
    {
        startActivity(intent);
        finish();
    }

}