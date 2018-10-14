package studio.microworld.hypernote.support.utlis;

import android.support.annotation.UiThread;

/**
 * Created by Mr.小世界 on 2018/8/28.
 */

//访问器的回调操作,异步执行,但在Main Thread上回调
public interface AsyncCallback<Result>
{
    @UiThread
    void onResult(Result result);
}
