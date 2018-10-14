package studio.microworld.hypernote.ui.account;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.support.widget.ProgressDialogHelper;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */

public abstract class AccountActivity
        extends BaseActivity
        implements View.OnTouchListener
{
    @BindView(R.id.et_username)
    public EditText mUsername;

    @BindView(R.id.et_password)
    public EditText mPassword;

    @BindView(R.id.iv_username_clear)
    public ImageView mClearUsername;

    @BindView(R.id.iv_password_clear)
    public ImageView mClearPassword;

    @BindView(R.id.rl_root_layout)
    public View mRootView;

    ProgressDialogHelper progressHelper = new ProgressDialogHelper(this);

    @Override
    protected void onLoadActionBar(ActionBar mActionBar)
    {
        if (mActionBar != null)
        {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (null != AccountActivity.this.getCurrentFocus())
        {
            //点击空白位置 隐藏软键盘
            InputMethodManager mInputMethodManager
                    = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager
                    .hideSoftInputFromWindow(AccountActivity.this
                            .getCurrentFocus()
                            .getWindowToken(), 0);
        }
        return false;
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        TextClearBinder.bind(mUsername, mClearUsername);
        TextClearBinder.bind(mPassword, mClearPassword);
        mRootView.setOnTouchListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
