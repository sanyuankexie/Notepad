package studio.microworld.hypernote.ui.setting;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.framework.BaseActivity;

/**
 * Created by Mr.小世界 on 2018/8/28.
 */

public final class SettingActivity extends BaseActivity
{

    public static void openSetting(Fragment fragment)
    {
        fragment.startActivityForResult(new Intent(fragment.getActivity(), SettingActivity.class),
                RC.request_code.REQUEST_CODE_TO_SETTING);
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        setTitle("设置");
    }

    @Override
    protected void onLoadActionBar(ActionBar mActionBar)
    {
        mActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_setting;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                finish();
            }break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        finish();
        super.onBackPressed();
    }
}
