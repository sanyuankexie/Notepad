package studio.microworld.hypernote.ui.account;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;

import butterknife.BindView;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.LogInListener;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.utlis.CheckUtil;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */

public final class LogInActivity
        extends AccountActivity
        implements View.OnClickListener
{
    @BindView(R.id.civ_head)
    public ImageView mHead;

    @BindView(R.id.btn_sigup)
    public Button mSignup;

    @BindView(R.id.btn_login)
    public Button mLogin;

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_login;
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        super.onLoadLayoutAfter();
        Glide.with(this)
                .load(R.drawable.bk_my_name_is_van)
                .into(mHead);
        mSignup.setOnClickListener(this);
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_login:
            {
                userLogIn(mUsername.getText().toString(),
                        mPassword.getText().toString());
            }break;
            case R.id.btn_sigup:
            {
                SignUpActivity.openSignUpForResult(this);
            }break;
        }
    }

    public void userLogIn(String username, String password)
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
            if (TextUtils.isEmpty(password))
            {
                ToastUtils.showShort("密码为空");
                //mView.showEmptyPasswordToast();
                return;
            } else
            {
                if (!CheckUtil.isPassword(password))
                {
                    ToastUtils.showShort("密码不合法");
                    //mView.showErorrPasswordToast();
                    return;
                }
                progressHelper.display("登录中...");
                //mView.openProgressDialog();
                BmobUser.loginByAccount(username
                        , password
                        , new LogInListener<BmobUser>()
                        {
                            @Override
                            public void done(final BmobUser bmobUser,
                                             final BmobException e)
                            {
                                runOnUiThread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        progressHelper.finish();
                                        if (e == null)
                                        {
                                            ToastUtils.showShort("登录成功");
                                        } else
                                        {
                                            ToastUtils.showShort("登录失败");
                                        }
                                    }
                                });
                            }
                        });
            }
        }
    }

}
