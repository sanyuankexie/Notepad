package studio.microworld.hypernote;

import java.util.regex.Pattern;

/**
 * Created by Mr.小世界 on 2018/8/23.
 */
//模仿R,常量引用,为什么不用R.string ?  因为这样好看...
public final class RC
{
    public final static class activity_note_editor
    {
        public final static String OPTIONS_KEY = "config";

        public final static String NEW_NOTE_TEXT = "----年--月--日 --:--:--";

        public final static String UPDATE_TIME_TEXT = "修改于: ";

        public final static String CREATE_TIME_TEXT="创建于: ";
    }

    public final static class request_code
    {

        public static final int REQUEST_CODE_TO_PERMISSIONS = 1000;//前往请求权限

        public static final int REQUEST_CODE_TO_PHOTO = 1002;   //前往图库

        public static final int REQUEST_CODE_TO_NOTE = 1007;//从便签编辑器返回(编辑)

        public static final int REQUEST_CODE_TO_QRSCANER = 1009;//去到二维码扫描

        public static final int REQUEST_CODE_TO_SPEECH = 1010;//去到百度语音识别

        public static final int REQUEST_CODE_TO_SETTING =1011;
    }

    public final static class activity_launch
    {
        public static final String DOT_PNG = ".png";

        public static final String INTERNAL_DATA_KEY = "internal_data";

        public static final String USER_DATA_KEY = "user_data";

        public static final int DEFAULT_FOLDER_ID_NO_SAVE = -1000;

        public static final String DEFAULT_FOLDER_ID_KEY = "default_folder_id";

        public static final String IS_FIRST_LAUNCH_KET = "is_first_launch";

        public static final String IMAGE_TIME_KEY = "image_time";
    }

    public final static class activity_main
    {

        public static final String MAIN_ACTIVITY_STATE_KEY = "main_activity_state";

        //这些特殊文件夹的ID也是他们的的position
        public static final int ALL_NORMAL_FOLDER_ID = -1;

        public static final int PRIVATE_FOLDER_ID = -2;

        public static final int RECOVERY_FOLDER_ID = -3;

        public static final int NETWORK_FOLDER_ID = -4;

        public static final String DEFAULT_FOLDER_NAME = "默认收藏夹";

        public static final String ALL_NORMAL_FOLDER_NAME = "所有便签";

        public static final String NETWORK_FOLDER_NAME = "网络";

        public static final String PRIVATE_FOLDER_NAME = "私密";

        public static final String RECOVERY_FOLDER_NAME = "废纸篓";
    }

    public final static class activity_setting
    {


    }

    public final static class activity_image_viewer
    {
        public static final String IMAGE_ID_KEY = "image_id";

        public static final String IMAGE_URI_KEY = "image_uri";
    }

    public final static class activity_qrscan
    {
        public static final String INTENT_EXTRA_KEY_SCAN = "hyper_note_qrscan";

        public static final String NOTE_URL_PROTOCOL = "note";
    }

    public final static class activity_folder_editor
    {
        public static final String CREATES_KEY = "creates";

        public static final String UPDATES_KEY = "updates";

        public static final String REMOVES_KEY = "removes";
    }

    public final static class activity_account
    {
        public static final String USERNAME_KEY="username";

        public static final String PASSWORD_KEY="password";
    }

}


