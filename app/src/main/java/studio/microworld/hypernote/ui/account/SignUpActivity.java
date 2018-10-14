package studio.microworld.hypernote.ui.account;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;

import butterknife.BindView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.support.utlis.CheckUtil;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */

public final class SignUpActivity
        extends AccountActivity
        implements View.OnClickListener
{
    @BindView(R.id.et_email)
    public EditText mEmail;

    @BindView(R.id.iv_email_clear)
    public ImageView mClearEmail;

    @BindView(R.id.btn_sigup)
    public Button mSignup;


    public static void openSignUpForResult(BaseActivity context)
    {
        Intent intent = new Intent(context, SignUpActivity.class);
        context.startActivityForResult(intent, 0);
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_signup;
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        super.onLoadLayoutAfter();
        TextClearBinder.bind(mEmail, mClearEmail);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_sigup:
            {
                userSignUp(mUsername.getText().toString()
                        , mEmail.getText().toString()
                        , mPassword.getText().toString());
            }
            break;
        }
    }

    private void userSignUp(String username, String email, String password)
    {
        if (TextUtils.isEmpty(username))
        {
            ToastUtils.showShort("用户名为空");
            //mView.showEmptyUsernameToast();
            return;
        } else
        {
            if (!CheckUtil.isUsername(username))
            {
                ToastUtils.showShort("用户名不合法");
                //mView.showErorrUsernameToast();
                return;
            }
            if (TextUtils.isEmpty(email))
            {
                ToastUtils.showShort("电子邮箱为空");
                //mView.showEmptyEmailTaost();
                return;
            } else
            {
                if (!CheckUtil.isEmail(email))
                {
                    ToastUtils.showShort("电子邮箱不合法");
                    //mView.showErorrEmailToast();
                    return;
                }
                if (TextUtils.isEmpty(password))
                {
                    ToastUtils.showShort("密码为空");
                    //mView.showEmptyPasswordToast();
                } else
                {
                    if (CheckUtil.isPassword(password))
                    {
                        ToastUtils.showShort("密码不合法");
                        //mView.showErorrPasswordToast();
                        return;
                    }
                    //mView.openProgressDialog();
                    progressHelper.display("注册中...");
                    BmobUser bu = new BmobUser();
                    bu.setEmail(email);
                    bu.setUsername(username);
                    bu.setPassword(password);
                    bu.signUp(new SaveListener<BmobUser>()
                    {
                        @Override
                        public void done(final BmobUser s, final BmobException e)
                        {
                            runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    progressHelper.finish();
                                    //mView.closeProgressDialog();
                                    if (e == null)
                                    {
                                        ToastUtils.showShort("注册成功");
                                    } else
                                    {
                                        ToastUtils.showShort("注册失败");
                                        //mView.showFailToast();
                                    }
                                }
                            });
                        }
                    });
                }
            }
        }
    }
}
