package studio.microworld.hypernote.support.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.UiThread;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.blankj.utilcode.util.TimeUtils;
import com.orhanobut.logger.Logger;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.utlis.UriUtil;

public final class NoteText extends WebView
{
    //-------------------interface&enum--------------------------------
    public enum Type
    {
        BOLD,
        ITALIC,
        SUBSCRIPT,
        SUPERSCRIPT,
        STRIKETHROUGH,
        UNDERLINE,
        H1,
        H2,
        H3,
        H4,
        H5,
        H6,
        ORDEREDLIST,
        UNORDEREDLIST,
        JUSTIFYCENTER,
        JUSTIFYFULL,
        JUSTUFYLEFT,
        JUSTIFYRIGHT
    }

    public interface OnTextChangeListener
    {
        void onTextChange(String text);
    }

    public interface OnDecorationStateListener
    {
        void onStateChangeListener(String text, List<Type> types);
    }

    public interface OnAfterInitialLoadListener
    {
        void onAfterInitialLoad(boolean isReady);
    }

    public interface OnHtmlTagClickListener
    {
        void onTagClick(String id, String src);
    }

    protected class EditorWebViewClient extends WebViewClient
    {
        @Override
        public void onPageFinished(WebView view, String url)
        {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            if (mLoadListener != null)
            {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            String decode;
            try
            {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e)
            {
                // No handling
                return false;
            }

            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0)
            {
                callback(decode);
                return true;
            } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0)
            {
                stateCheck(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    //------------------------data-----------------------------------------------

    private static final String SETUP_HTML = "file:///android_asset/html/editor.html";
    private static final String CALLBACK_SCHEME = "re-callback://";
    private static final String STATE_SCHEME = "re-state://";
    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mTextChangeListener;
    private OnDecorationStateListener mDecorationStateListener;
    private OnAfterInitialLoadListener mLoadListener;
    private OnHtmlTagClickListener mImageClickListener;
    private int contentWidth = 0;


    //---------------------------init------------------------------------------

    public NoteText(Context context)
    {
        this(context, null);
    }

    public NoteText(Context context, AttributeSet attrs)
    {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public NoteText(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        setVerticalScrollBarEnabled(true);
        setHorizontalScrollBarEnabled(false);
        WebSettings settings = getSettings();
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(createWebViewClient());
        loadUrl(SETUP_HTML);
        addJavascriptInterface(this, "NoteText");
        setHeight(250);//起始编辑设置高度
        setEditorFontSize(18);//设置字体大小
        setPadding(25, 10, 25, 0);
        setPlaceholder("输入内容...");
        setBackgroundColor(getResources().getColor(R.color.background));
        applyAttributes(context, attrs);
    }

    protected EditorWebViewClient createWebViewClient()
    {
        return new EditorWebViewClient();
    }

    //----------------------action----------------------------------------------


    public int getContentWidth()
    {
        return contentWidth;
    }

    public void setOnTextChangeListener(OnTextChangeListener listener)
    {
        mTextChangeListener = listener;
    }

    public void setOnImageClickListener(OnHtmlTagClickListener listener)
    {
        mImageClickListener = listener;
    }

    public void setOnDecorationChangeListener(OnDecorationStateListener listener)
    {
        mDecorationStateListener = listener;
    }

    public void setOnInitialLoadListener(OnAfterInitialLoadListener listener)
    {
        mLoadListener = listener;
    }

    @Override
    public void setBackgroundColor(int color)
    {
        super.setBackgroundColor(color);
        exec("javascript:RE.setBackgroundColor(\'" + convertHexColorString(color) + "\');");
    }

    public void setContent(String contents)
    {
        if (contents == null)
        {
            contents = "";
        }
        try
        {
            exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
        } catch (UnsupportedEncodingException e)
        {

        }
        mContents = contents;
    }

    public String getContent()
    {
        return mContents;
    }

    public void setEditorFontColor(int color)
    {
        String hex = convertHexColorString(color);
        exec("javascript:RE.setBaseTextColor('" + hex + "');");
    }

    public void setEditorFontSize(int px)
    {
        exec("javascript:RE.setBaseFontSize('" + px + "px');");
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom)
    {
        super.setPadding(left, top, right, bottom);
        exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
                + "px');");
    }

    @Override
    public void setPaddingRelative(int start, int top, int end, int bottom)
    {
        // still not support RTL.
        setPadding(start, top, end, bottom);
    }

    @Override
    public void setBackgroundResource(int resid)
    {
        Bitmap bitmap = decodeResource(resid);
        String base64 = toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    @Override
    public void setBackground(Drawable background)
    {
        Bitmap bitmap = toBitmap(background);
        String base64 = toBase64(bitmap);
        bitmap.recycle();

        exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
    }

    public void setBackground(String url)
    {
        exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
    }

    public void setWidth(int px)
    {
        exec("javascript:RE.setWidth('" + px + "px');");
    }

    public void setHeight(int px)
    {
        exec("javascript:RE.setHeight('" + px + "px');");
    }

    public void setPlaceholder(String placeholder)
    {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }

    public void setInputEnabled(Boolean inputEnabled)
    {
        exec("javascript:RE.setInputEnabled(" + inputEnabled + ")");
    }

    public void loadCSS(String cssFile)
    {
        String jsCSSImport = "(function() {" +
                "    var head  = document.getElementsByTagName(\"head\")[0];" +
                "    var link  = document.createElement(\"link\");" +
                "    link.rel  = \"stylesheet\";" +
                "    link.type = \"text/css\";" +
                "    link.href = \"" + cssFile + "\";" +
                "    link.media = \"all\";" +
                "    head.appendChild(link);" +
                "}) ();";
        exec("javascript:" + jsCSSImport + "");
    }

    public void undo()
    {
        exec("javascript:RE.undo();");
    }

    public void redo()
    {
        exec("javascript:RE.redo();");
    }

    public void setBold()
    {
        exec("javascript:RE.setBold();");
    }

    public void setItalic()
    {
        exec("javascript:RE.setItalic();");
    }

    public void setStrikeThrough()
    {
        exec("javascript:RE.setStrikeThrough();");
    }

    public void setUnderline()
    {
        exec("javascript:RE.setUnderline();");
    }

    public void setTextColor(int color)
    {
        exec("javascript:RE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextColor('" + hex + "');");
    }

    public void setTextBackgroundColor(int color)
    {
        exec("javascript:RE.prepareInsert();");
        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextBackgroundColor('" + hex + "');");
    }

    public void setFontSize(int fontSize)
    {
        if (fontSize > 7 || fontSize < 1)
        {
            Log.e("RichEditor", "Font size should have a value between 1-7");
        }
        exec("javascript:RE.setFontSize('" + fontSize + "');");
    }

    public void removeFormat()
    {
        exec("javascript:RE.removeFormat();");
    }

    public void setHeading(int heading)
    {
        exec("javascript:RE.setHeading('" + heading + "');");
    }

    public void setIndent()
    {
        exec("javascript:RE.setIndent();");
    }

    public void setOutdent()
    {
        exec("javascript:RE.setOutdent();");
    }

    public void setAlignLeft()
    {
        exec("javascript:RE.setJustifyLeft();");
    }

    public void setAlignCenter()
    {
        exec("javascript:RE.setJustifyCenter();");
    }

    public void setAlignRight()
    {
        exec("javascript:RE.setJustifyRight();");
    }

    public void setNumbers()
    {
        exec("javascript:RE.setNumbers();");
    }

    @UiThread
    public void insertAudio(Uri uri)
    {
        String id = UUID.randomUUID().toString();
        Element element = new Element(Tag.valueOf("audio"), SETUP_HTML);
        element.attr("id", id);
        element.attr("src", uri.toString());
        String html = element.toString();
        prepareInsert();
        Logger.d(html);
        insertHTML(html);
    }

    @UiThread
    public void deleteElementById(String id)
    {
        exec("javascript:RE.deleteElementById('" + id + "');");
    }

    @UiThread
    public void insertImage(Uri uri)
    {
        String id = UUID.randomUUID().toString();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String srcRealPath = UriUtil.getRealPathFromUri(getContext(), uri);
        BitmapFactory.decodeFile(srcRealPath, options);
        Element element = new Element(Tag.valueOf("img"), SETUP_HTML);
        if (getContentWidth() < options.outWidth)
        {
            element.attr("width", "100%");
        }
        element.attr("id", id);
        element.addClass("img-thumbnail");
        String src = uri.toString();
        element.attr("src", src);
        element.attr("onclick",
                "javascript:NoteText.onTagClick(this.id,this.src)");
        String html = element.toString();
        prepareInsert();
        Logger.d(html);
        insertHTML(html);
    }

    public void insertTodo()
    {
        String now = TimeUtils.getNowString();
        Element element = new Element(Tag.valueOf("input"), SETUP_HTML);
        element.attr("type", "checkbox");
        prepareInsert();
        insertHTML(element.toString());
    }

    public void insertRawText(String text)
    {
        prepareInsert();
        insertHTML(text);
    }

    public void focus()
    {
        requestFocus();
        exec("javascript:RE.focus();");
    }

    @Override
    public void clearFocus()
    {
        exec("javascript:RE.blurFocus();");
        super.clearFocus();
    }


    //------------------------internal----------------------------------

    protected void exec(final String trigger)
    {
        if (isReady)
        {
            load(trigger);
        } else
        {
            postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    exec(trigger);
                }
            }, 100);
        }
    }

    private void load(String trigger)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            evaluateJavascript(trigger, null);
        } else
        {
            loadUrl(trigger);
        }
    }

    private void callback(String text)
    {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        if (mTextChangeListener != null)
        {
            mTextChangeListener.onTextChange(mContents);
        }
    }

    private void stateCheck(String text)
    {
        String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
        List<Type> types = new ArrayList<>();
        for (Type type : Type.values())
        {
            if (TextUtils.indexOf(state, type.name()) != -1)
            {
                types.add(type);
            }
        }

        if (mDecorationStateListener != null)
        {
            mDecorationStateListener.onStateChangeListener(state, types);
        }
    }

    private void applyAttributes(Context context, AttributeSet attrs)
    {
        final int[] attrsArray = new int[]{
                android.R.attr.gravity
        };
        TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

        int gravity = ta.getInt(0, NO_ID);
        switch (gravity)
        {
            case Gravity.LEFT:
                exec("javascript:RE.setTextAlign(\"left\")");
                break;
            case Gravity.RIGHT:
                exec("javascript:RE.setTextAlign(\"right\")");
                break;
            case Gravity.TOP:
                exec("javascript:RE.setVerticalAlign(\"top\")");
                break;
            case Gravity.BOTTOM:
                exec("javascript:RE.setVerticalAlign(\"bottom\")");
                break;
            case Gravity.CENTER_VERTICAL:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                break;
            case Gravity.CENTER_HORIZONTAL:
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
            case Gravity.CENTER:
                exec("javascript:RE.setVerticalAlign(\"middle\")");
                exec("javascript:RE.setTextAlign(\"center\")");
                break;
        }

        ta.recycle();
    }

    protected void prepareInsert()
    {
        exec("javascript:RE.prepareInsert();");
    }

    protected void insertHTML(String html)
    {
        exec("javascript:RE.insertHTML('" + html + "');");
    }

    private static String toBase64(Bitmap bitmap)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private Bitmap toBitmap(Drawable drawable)
    {
        if (drawable instanceof BitmapDrawable)
        {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private Bitmap decodeResource(int resId)
    {
        return BitmapFactory.decodeResource(getResources(), resId);
    }

    private String convertHexColorString(int color)
    {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    //-----------------JavascriptInterface----------------

    @JavascriptInterface
    public void updateContentWidth(final int width)
    {
        post(new Runnable()
        {
            @Override
            public void run()
            {
                contentWidth = width;
            }
        });
    }

    @JavascriptInterface
    public void onTagClick(final String id, final String src)
    {
        post(new Runnable()
        {
            @Override
            public void run()
            {
                if (mImageClickListener != null)
                {
                    Logger.d("onTagClick id:" + id + " " + src);
                    mImageClickListener.onTagClick(id, src);
                }
            }
        });
    }
}