package studio.microworld.hypernote.support.framework;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;


/**
 * Created by Mr.小世界 on 2018/8/22.
 */

public abstract class BaseFragment extends Fragment
{
    //缓存Fragment view
    private View mRootView;
    private boolean mIsMulti = false;

    @Nullable
    @Override
    public View
    onCreateView(LayoutInflater inflater,
                 @Nullable ViewGroup container,
                 @Nullable Bundle savedInstanceState)
    {
        if (mRootView == null)
        {
            mRootView = inflater.inflate(onLoadLayout(), container, false);
            ButterKnife.bind(this, mRootView);
            onLoadLayoutAfter();
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null)
        {
            parent.removeView(mRootView);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (getUserVisibleHint() && mRootView != null && !mIsMulti)
        {
            mIsMulti = true;
            onLoadActivityAfter();
        }
    }

    /**
     * 绑定布局文件
     *
     * @return 布局id
     */
    protected abstract
    @LayoutRes
    int onLoadLayout();

    /**
     * 初始化视图控件
     *
     * @describe
     */
    protected abstract void onLoadLayoutAfter();

    /**
     * 更新视图控件
     *
     * @describe
     */
    protected abstract void onLoadActivityAfter();
}
