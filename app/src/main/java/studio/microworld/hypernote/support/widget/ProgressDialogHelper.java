package studio.microworld.hypernote.support.widget;

import android.app.ProgressDialog;
import android.content.Context;

import com.orhanobut.logger.Logger;


public final class ProgressDialogHelper
{
    private ProgressDialog mProgressDialog;
    final private Context mContext;

    public ProgressDialogHelper(Context context)
    {
        this.mContext = context;
    }

    public void display(String message)
    {
        if (mProgressDialog != null)
        {
            throw new RuntimeException();
        }
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public void finish()
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
