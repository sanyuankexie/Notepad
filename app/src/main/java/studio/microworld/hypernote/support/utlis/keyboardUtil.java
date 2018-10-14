package studio.microworld.hypernote.support.utlis;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Mr.小世界 on 2018/8/24.
 */

public final class keyboardUtil
{
    //    获取焦点并弹出键盘
    public static void openKeyborad(View view)
    {
//        获取 接受焦点的资格
        view.setFocusable(true);
//        获取 焦点可以响应点触的资格
        view.setFocusableInTouchMode(true);
//        请求焦点
        view.requestFocus();
//        弹出键盘
        InputMethodManager manager
                = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.toggleSoftInput(0, 0);
        manager.showSoftInput(view, 0);
    }

    //　关闭键盘
    public static void closeKeyboard(View view)
    {
        InputMethodManager manager
                = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager.isActive())
        {
            manager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
