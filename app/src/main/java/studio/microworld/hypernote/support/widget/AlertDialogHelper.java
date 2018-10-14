package studio.microworld.hypernote.support.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Mr.小世界 on 2018/9/5.
 */

public final class AlertDialogHelper
{
    private final Context context;

    public AlertDialogHelper(Context context)
    {
        this.context = context;
    }

    public void display(String title,
                         String message,
                         DialogInterface.OnClickListener listener)
    {
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", listener)
                .setNegativeButton("取消", listener)
                .show();
    }
}
