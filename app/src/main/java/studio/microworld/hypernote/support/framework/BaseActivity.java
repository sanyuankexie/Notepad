package studio.microworld.hypernote.support.framework;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import org.jsoup.Connection;

import butterknife.ButterKnife;
import studio.microworld.hypernote.R;

/**
 * Created by Mr.小世界 on 2018/8/22.
 */

public abstract class BaseActivity extends AppCompatActivity
{
    protected Toolbar mToolbar;

    protected ActionBar mActionBar;

    @Override
    protected final void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.onLoadLayoutBefore();
        this.setContentView(onLoadLayout());
        ButterKnife.bind(this);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null)
        {
            this.setSupportActionBar(mToolbar);
            mActionBar = getSupportActionBar();
            if (mActionBar != null)
            {
                this.onLoadActionBar(mActionBar);
            }
        }
        this.onLoadLayoutAfter();
    }

    protected void onLoadLayoutBefore()
    {
    }

    protected abstract void onLoadLayoutAfter();

    protected abstract
    @LayoutRes
    int onLoadLayout();

    protected void onLoadActionBar(ActionBar mActionBar)
    {

    }

    public static boolean isOnMainThread()
    {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

}
