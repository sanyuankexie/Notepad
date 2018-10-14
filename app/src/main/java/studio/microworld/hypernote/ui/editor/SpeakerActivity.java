package studio.microworld.hypernote.ui.editor;

import android.content.Intent;
import android.media.MediaRecorder;

import com.baidu.aip.asrwakeup3.core.recog.MyRecognizer;
import com.baidu.aip.asrwakeup3.core.recog.listener.ChainRecogListener;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.baidu.voicerecognition.android.ui.DigitalDialogInput;
import com.blankj.utilcode.util.NetworkUtils;;

import java.util.ArrayList;
import java.util.HashMap;

import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.support.utlis.AsyncCallback;

/**
 * Created by Mr.小世界 on 2018/9/8.
 */

//封装百度语音敷衍又恶心Demo的代码
public abstract class SpeakerActivity
        extends BaseActivity
{
    private MyRecognizer myRecognizer;
    private AsyncCallback<String> speechCallback;

    @Override
    protected void onLoadLayoutAfter()
    {
        ChainRecogListener chainRecogListener = new ChainRecogListener();
        myRecognizer = new MyRecognizer(this, chainRecogListener);
        BaiduASRDigitalDialog.setInput(new DigitalDialogInput(myRecognizer,
                chainRecogListener,
                new HashMap<String, Object>()));
    }

    protected final void displaySpeaker(AsyncCallback<String> callback)
    {
        speechCallback = callback;
        if (NetworkUtils.isConnected())
        {
            Intent intent = new Intent(this, BaiduASRDigitalDialog.class);
            startActivityForResult(intent, RC.request_code.REQUEST_CODE_TO_SPEECH);
        } else
        {
            if (speechCallback != null)
            {
                callback.onResult(null);
            }
        }
    }

    @Override
    public void finish()
    {
        super.finish();
        BaiduASRDigitalDialog.setInput(null);
        myRecognizer.release();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == RC.request_code.REQUEST_CODE_TO_SPEECH)
        {
            if (speechCallback != null)
            {
                if (resultCode == RESULT_OK)
                {
                    ArrayList<String> results = data.getStringArrayListExtra("results");
                    if (results != null && results.size() > 0)
                    {
                        speechCallback.onResult(results.get(0));
                    }
                } else
                {
                    speechCallback.onResult("");
                }
                //清理引用
                speechCallback = null;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
