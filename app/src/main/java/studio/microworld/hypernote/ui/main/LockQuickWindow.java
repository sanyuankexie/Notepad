package studio.microworld.hypernote.ui.main;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.widget.LockView;
import studio.microworld.hypernote.support.framework.BaseQuickWindow;

/**
 * Created by Mr.小世界 on 2018/9/10.
 */

public final class LockQuickWindow
        extends BaseQuickWindow
        implements LockView.OnDrawFinishedListener,
        View.OnClickListener
{

    public interface OnLockVerifyListener
    {
        void onVerifySuccess();
    }

    @BindView(R.id.ll_title_layout)
    LinearLayout mTitleLayout;

    @BindView(R.id.lv_lock)
    LockView lockView;

    @BindView(R.id.iv_title_image)
    ImageView mTitleImage;

    @BindView(R.id.tv_title_text)
    TextView mTitleText;

    @BindView(R.id.btn_back)
    Button back;

    @BindView(R.id.tv_use_num_password)
    TextView mUseNum;

    private OnLockVerifyListener onLockVerifyListener;

    private final String password;

    private LockQuickWindow(Context context, String password)
    {
        super(context);
        this.password = password;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_back:
            {
                dismiss();
            }break;
            case R.id.tv_use_num_password:
            {

            }break;
        }
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.window_lock;
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        lockView.setOnDrawFinishedListener(this);
        back.setOnClickListener(this);
        mUseNum.setOnClickListener(this);
    }

    public void setOnLockVerifyListener(OnLockVerifyListener onLockVerifyListener)
    {
        this.onLockVerifyListener = onLockVerifyListener;
    }

    @Override
    public boolean onDrawFinished(List<Integer> passPositions)
    {
        StringBuilder stringBuilder = new StringBuilder(9);
        for (Integer integer : passPositions)
        {
            stringBuilder.append(integer);
        }
        if (toMD5(stringBuilder.toString()).equals(password))
        {
            onLockVerifyListener.onVerifySuccess();
            return true;
        }
        mTitleText.setText("请重试");
        ObjectAnimator animator = ObjectAnimator.ofFloat(mTitleLayout,
                "translationX",
                -SizeUtils.dp2px(8),
                SizeUtils.dp2px(8), 0);
        animator.setDuration(200);
        animator.start();
        return false;
    }

    private static String toMD5(String sourceStr)
    {
        String result = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(sourceStr.getBytes());
            byte b[] = md.digest();
            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++)
            {
                i = b[offset];
                if (i < 0)
                {
                    i += 256;
                }
                if (i < 16)
                {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException e)
        {
            System.out.println(e);
        }
        return result;
    }

    //----------------------------------------------------------------

    public static void openLockWindow(Context activity,
                                      String password,
                                      OnLockVerifyListener listener)
    {
        final LockQuickWindow window = new LockQuickWindow(activity, password);
        window.setOnLockVerifyListener(listener);
        window.show();
    }
}
