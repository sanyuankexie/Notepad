package studio.microworld.hypernote.ui.editor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.widget.NoteText;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.support.utlis.AsyncCallback;
import studio.microworld.hypernote.support.utlis.keyboardUtil;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */

//网络便签只能引用网络图片,否则图片会被自动移除
public final class EditorActivity
        extends SpeakerActivity
        implements NoteText.OnTextChangeListener,
        NoteText.OnHtmlTagClickListener,
        NoteText.OnAfterInitialLoadListener,
        View.OnClickListener,
        View.OnFocusChangeListener
{

    //-----------------------bind view------------------------------

    @BindView(R.id.ib_back)
    ImageButton mBack;

    @BindView(R.id.root_layout)
    LinearLayout mRootLayout;

    @BindView(R.id.et_title_edit)
    EditText mEditTitle;

    @BindView(R.id.ll_editor_root)
    LinearLayout mEditorRoot;

    @BindView(R.id.ll_edit_tools_bar)
    LinearLayout mHvToolsBar;

    @BindView(R.id.tv_time)
    TextView mTimeText;

    @BindView(R.id.nt_editor)
    NoteText mNoteText;

    @BindView(R.id.ll_progress)
    LinearLayout mProgress;

    //----------------------activity data ------------------------------

    private EditOptions config;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINA);

    //----------------------base method----------------------------------------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data)
    {
        switch (requestCode)
        {
            case RC.request_code.REQUEST_CODE_TO_PHOTO:
            {
                if (resultCode == RESULT_OK)
                {
                    mNoteText.insertImage(data.getData());
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onTagClick(String id, String src)
    {
        ImageBrowserActivity.openImageBrowser(this, src);
    }

    @Override
    public void onClick(final View v)
    {
        switch (v.getId())
        {
            case R.id.ib_back:
            {
                if (mNoteText.hasFocus())
                {
                    mNoteText.clearFocus();
                    keyboardUtil.closeKeyboard(mRootLayout);
                    return;
                }
                onResult();
                finish();
            }
            break;
        }
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        super.onLoadLayoutAfter();
        loadIntent();
        loadEditor();
        loadEditToolsBar();
        loadOtherView();
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_note_editor;
    }

    @Override
    public void onTextChange(String text)
    {

    }

    @Override
    public void onAfterInitialLoad(boolean isReady)
    {
        mNoteText.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mProgress.setVisibility(View.GONE);
                Animation animation = new AlphaAnimation(0.1f, 1.0f);
                animation.setDuration(750);//动画的持续的时间
                animation.setAnimationListener(new Animation.AnimationListener()
                {
                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                        mNoteText.setVisibility(View.VISIBLE);
                        mNoteText.setEnabled(false);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        mNoteText.setEnabled(true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {

                    }
                });
                mNoteText.startAnimation(animation);
            }
        }, 100);
    }

    @Override
    public void onBackPressed()
    {
        if (mNoteText.hasFocus())
        {
            mNoteText.clearFocus();
            return;
        }
        onResult();
        finish();
    }

    @Override
    protected void onDestroy()
    {
        mNoteText.setOnTextChangeListener(null);
        mNoteText.setOnFocusChangeListener(null);
        mNoteText.setOnImageClickListener(null);
        mEditorRoot.removeView(mNoteText);
        mNoteText.stopLoading();
        mNoteText.getSettings().setJavaScriptEnabled(false);
        mNoteText.clearHistory();
        mNoteText.removeAllViews();
        mNoteText.destroy();
        mNoteText = null;
        super.onDestroy();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
            case R.id.nt_editor:
            {
                if (hasFocus)
                {
                    updateEditEnable(true);
                    mTimeText.setVisibility(View.GONE);
                } else
                {
                    updateEditEnable(false);
                    mTimeText.setVisibility(View.VISIBLE);
                }
            }
            break;
        }
    }

    //-----------------------load data---------------------------------------------

    private void loadEditToolsBar()
    {
        View.OnClickListener onClickListener = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                updateView(v);
                switch (v.getId())
                {
                    case R.id.action_undo:
                    {
                        mNoteText.undo();
                    }
                    break;
                    case R.id.action_redo:
                    {
                        mNoteText.redo();
                    }
                    break;
                    case R.id.action_insert_speech:
                    {
                        displaySpeaker(new AsyncCallback<String>()
                        {
                            @Override
                            public void onResult(String s)
                            {
                                if (s == null)
                                {
                                    ToastUtils.showShort("无网络连接");
                                } else if ("".equals(s))
                                {
                                    ToastUtils.showShort("识别结果为空");
                                } else
                                {
                                    mNoteText.insertRawText(s);
                                }
                            }
                        });
                    }
                    break;
                    case R.id.action_insert_image:
                    {
                        Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
                        getImage.addCategory(Intent.CATEGORY_OPENABLE);
                        getImage.setType("image/*");
                        startActivityForResult(getImage, RC.request_code.REQUEST_CODE_TO_PHOTO);
                    }
                    case R.id.action_bold:
                    {
                        mNoteText.setBold();
                    }
                    break;
                    case R.id.action_italic:
                    {
                        mNoteText.setItalic();
                    }
                    break;
                    case R.id.action_strikethrough:
                    {
                        mNoteText.setStrikeThrough();
                    }
                    break;
                    case R.id.action_underline:
                    {
                        mNoteText.setUnderline();
                    }
                    break;
                    case R.id.action_heading1:
                    {
                        mNoteText.setHeading(1);
                    }
                    break;
                    case R.id.action_heading2:
                    {
                        mNoteText.setHeading(2);
                    }
                    break;
                    case R.id.action_heading3:
                    {
                        mNoteText.setHeading(3);
                    }
                    break;
                    case R.id.action_indent:
                    {
                        mNoteText.setIndent();
                    }
                    break;
                    case R.id.action_outdent:
                    {
                        mNoteText.setOutdent();
                    }
                    break;
                    case R.id.action_align_left:
                    {
                        mNoteText.setAlignLeft();
                    }
                    break;
                    case R.id.action_align_center:
                    {
                        mNoteText.setAlignCenter();
                    }
                    break;
                    case R.id.action_align_right:
                    {
                        mNoteText.setAlignRight();
                    }
                    break;
                    case R.id.action_insert_numbers:
                    {
                        mNoteText.setNumbers();
                    }
                    break;
                }
            }

            private void updateView(final View v)
            {
                Object val = v.getTag(R.id.edit_tools_bar_animation);
                if (val == null || !((Boolean) val))
                {
                    v.setTag(R.id.edit_tools_bar_animation, true);
                    final Drawable drawable = v.getBackground();
                    v.setBackgroundColor(getResources().getColor(R.color.colorBlackAlpha54));
                    v.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            v.setTag(R.id.edit_tools_bar_animation, false);
                            v.setBackgroundDrawable(drawable);
                        }
                    }, 100);
                }
            }

        };
        findViewById(R.id.action_undo).setOnClickListener(onClickListener);
        findViewById(R.id.action_redo).setOnClickListener(onClickListener);
        findViewById(R.id.action_insert_speech).setOnClickListener(onClickListener);
        findViewById(R.id.action_bold).setOnClickListener(onClickListener);
        findViewById(R.id.action_italic).setOnClickListener(onClickListener);
        findViewById(R.id.action_strikethrough).setOnClickListener(onClickListener);
        findViewById(R.id.action_underline).setOnClickListener(onClickListener);
        findViewById(R.id.action_heading1).setOnClickListener(onClickListener);
        findViewById(R.id.action_heading2).setOnClickListener(onClickListener);
        findViewById(R.id.action_heading3).setOnClickListener(onClickListener);
        findViewById(R.id.action_indent).setOnClickListener(onClickListener);
        findViewById(R.id.action_outdent).setOnClickListener(onClickListener);
        findViewById(R.id.action_align_left).setOnClickListener(onClickListener);
        findViewById(R.id.action_align_center).setOnClickListener(onClickListener);
        findViewById(R.id.action_align_right).setOnClickListener(onClickListener);
        findViewById(R.id.action_insert_numbers).setOnClickListener(onClickListener);
        findViewById(R.id.action_insert_image).setOnClickListener(onClickListener);
        mHvToolsBar.setHorizontalScrollBarEnabled(false);
    }

    private void loadEditor()
    {
        mNoteText.setOnTextChangeListener(this);
        if (config.actionType == EditOptions.TYPE_UPDATE)
        {
            mTimeText.setText(RC.activity_note_editor.UPDATE_TIME_TEXT
                    + TimeUtils.millis2String(TimeUtils.string2Millis(config.updatedAt), dateFormat));
            mNoteText.setContent(config.content);
            mEditTitle.setText(config.title);
        } else
        {
            mTimeText.setText(RC.activity_note_editor.UPDATE_TIME_TEXT + RC.activity_note_editor.NEW_NOTE_TEXT);
            mNoteText.setContent("");
            mEditTitle.setText("");
        }
        if (!config.editable)
        {
            mNoteText.setInputEnabled(false);
            mEditTitle.setEnabled(false);
        }
        mNoteText.setOnInitialLoadListener(this);
        mNoteText.setOnFocusChangeListener(this);
        mNoteText.setOnImageClickListener(this);
    }

    private void loadIntent()
    {
        Intent intent = getIntent();
        config = (EditOptions)
                intent.getSerializableExtra(RC.activity_note_editor.OPTIONS_KEY);
    }

    private void loadOtherView()
    {
        mBack.setOnClickListener(this);
    }

    //-------------------------- Open ----------------------------------------

    public static void openEditor(BaseActivity activity, EditOptions config)
    {
        Intent intent = new Intent(activity, EditorActivity.class);
        intent.putExtra(RC.activity_note_editor.OPTIONS_KEY, config);
        Logger.d(config.content);
        activity.startActivityForResult(intent, RC.request_code.REQUEST_CODE_TO_NOTE);
    }

    //----------------------internal -------------------------------------

    private void onResult()
    {
        String title = mEditTitle.getText().toString();
        String content = mNoteText.getContent();
        if (!(TextUtils.isEmpty(title) && TextUtils.isEmpty(content)))
        {
            boolean hasUpdate = false;
            if (!content.equals(config.content))
            {
                hasUpdate = true;
                config.content = content;
            }
            if (!title.equals(config.title))
            {
                hasUpdate = true;
                config.title = title;
            }
            if (hasUpdate)
            {
                Intent intent = new Intent();
                intent.putExtra(RC.activity_note_editor.OPTIONS_KEY, config);
                setResult(Activity.RESULT_OK, intent);
            } else
            {
                if (config.actionType == EditOptions.TYPE_ADD)
                {
                }
                setResult(Activity.RESULT_CANCELED);
            }
        } else
        {
            if (config.actionType == EditOptions.TYPE_ADD)
            {
            }
            setResult(Activity.RESULT_CANCELED);
        }
    }

    //------------------update------------------------------------

    private void updateEditEnable(boolean enable)
    {
        if (enable)
        {
            updateEditToolsBarEnable(true);
        } else
        {
            updateEditToolsBarEnable(false);
            mNoteText.scrollTo(0, 0);
        }
    }

    private void updateEditToolsBarEnable(boolean enable)
    {
        if (enable && config.editable)
        {
            mHvToolsBar.setVisibility(View.VISIBLE);
        } else
        {
            mHvToolsBar.setVisibility(View.GONE);
        }
    }
}