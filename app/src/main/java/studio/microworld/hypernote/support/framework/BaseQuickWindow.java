package studio.microworld.hypernote.support.framework;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDialog;
import android.view.Window;
import android.view.WindowManager;

import butterknife.ButterKnife;
import studio.microworld.hypernote.R;

/**
 * Created by Mr.小世界 on 2018/9/10.
 */

//轻量级的窗口,不需要开启活动,并且可以传递引用和回调
public abstract class BaseQuickWindow extends AppCompatDialog
{
    public BaseQuickWindow(Context context)
    {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setCanceledOnTouchOutside(false);
        setContentView(onLoadLayout());
        ButterKnife.bind(this);
        onLoadLayoutAfter();
    }

    @LayoutRes
    protected abstract int onLoadLayout();

    protected abstract void onLoadLayoutAfter();

}
