package studio.microworld.hypernote.ui.editor;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;

import com.github.chrisbanes.photoview.PhotoView;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.framework.BaseActivity;

/**
 * Created by Mr.小世界 on 2018/9/20.
 */

public final class ImageBrowserActivity extends BaseActivity
{

    @BindView(R.id.pv_photo)
    PhotoView photoView;

    public static void openImageBrowser(BaseActivity activity, String uri)
    {
        Intent intent = new Intent(activity, ImageBrowserActivity.class);
        intent.putExtra(activity.getString(R.string.image_uri_key), uri);
        activity.startActivityForResult(intent,
                RC.request_code.REQUEST_CODE_TO_PHOTO);
    }

    @Override
    protected void onLoadActionBar(ActionBar mActionBar)
    {

    }

    @Override
    protected void onLoadLayoutAfter()
    {
        loadIntent();
    }

    private void loadIntent()
    {
        Intent intent = getIntent();
        photoView.setImageURI(Uri.parse(intent.getStringExtra(getString(R.string.image_uri_key))));
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_image;
    }
}
