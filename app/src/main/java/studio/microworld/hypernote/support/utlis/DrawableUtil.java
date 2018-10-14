package studio.microworld.hypernote.support.utlis;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.Utils;

import studio.microworld.hypernote.R;


/**
 * Created by miaoyongyong on 2017/2/23.
 */

public final class DrawableUtil
{

//    便签夹 图标 的样式
    public static GradientDrawable getIcFolderSelectedDrawable(@ColorInt int color){
        GradientDrawable gradientDrawable=new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setSize(SizeUtils.dp2px(24), SizeUtils.dp2px(24));
        gradientDrawable.setBounds(0,0, SizeUtils.dp2px(24), SizeUtils.dp2px(24));
        gradientDrawable.setColor(color);
        return gradientDrawable;
    }

    public static void setTextButtonEnabled(TextView view,boolean enabled)
    {
        view.setEnabled(enabled);
        if (enabled)
        {
            view.setTextColor(Utils.getContext().getResources().getColor(R.color.white));
        } else
        {
            view.setTextColor(Utils.getContext().getResources().getColor(R.color.colorWhiteAlpha30));
        }
    }
}
