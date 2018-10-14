package studio.microworld.hypernote.ui.qrcode;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.activity.CaptureActivity;

import studio.microworld.hypernote.RC;

public final class QRScanActivity extends CaptureActivity
{
    public static void openQRScanForResult(Activity activity)
    {
        activity.startActivityForResult(new Intent(activity, QRScanActivity.class),
                RC.request_code.REQUEST_CODE_TO_QRSCANER);
    }

}
