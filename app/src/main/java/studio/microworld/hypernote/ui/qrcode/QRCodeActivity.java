package studio.microworld.hypernote.ui.qrcode;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.mylhyl.zxing.scanner.encode.QREncode;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.json.QRCode;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.support.utlis.GsonUtil;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */

public final class QRCodeActivity extends BaseActivity
{
    @BindView(R.id.iv_qrcode)
    ImageView mQRCode;

    @BindView(R.id.pb_progress)
    ProgressBar mProgressBar;

    public static void openQRCode(Activity activity,String guid)
    {
        Intent intent = new Intent(activity, QRCodeActivity.class);
        intent.putExtra("guid", GsonUtil.getGson().toJson(new QRCode(guid.trim())));
        activity.startActivity(intent);
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        Intent intent = getIntent();
        final String guid = intent.getStringExtra("guid");
        mQRCode.setVisibility(View.GONE);
        new AsyncTask<String,Integer,Bitmap>()
        {
            @Override
            protected Bitmap doInBackground(String... params)
            {
                final Bitmap bitmap = new QREncode.Builder(QRCodeActivity.this)
                        .setColor(QRCodeActivity.this.getResources()
                                .getColor(R.color.colorPrimary))
                        .setContents(params[0]).build().encodeAsBitmap();
                return bitmap;
            }
            @Override
            protected void onPostExecute(Bitmap bitmap)
            {
                mProgressBar.setVisibility(View.GONE);
                mQRCode.setVisibility(View.VISIBLE);
                mQRCode.setImageBitmap(bitmap);
            }
        }.execute(guid);
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_qrcode;
    }
}
