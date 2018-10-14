package studio.microworld.hypernote.ui.account;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Mr.小世界 on 2018/8/24.
 */
public final class TextClearBinder
        implements TextWatcher,
        View.OnClickListener
{
    private TextView text;

    private ImageView image;

    private TextClearBinder(TextView text, ImageView image)
    {
        this.text = text;
        this.image = image;
    }

    public static void bind(final EditText et, final ImageView iv)
    {
        TextClearBinder temp = new TextClearBinder(et, iv);
        et.addTextChangedListener(temp);
        iv.setOnClickListener(temp);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        //如果有输入内容长度大于0那么显示clear按钮
        String str = s + "";
        if (s.length() > 0)
        {
            image.setVisibility(View.VISIBLE);
        } else
        {
            image.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v)
    {
        text.setText("");
    }
}
