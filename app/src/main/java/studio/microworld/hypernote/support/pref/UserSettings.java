package studio.microworld.hypernote.support.pref;

import java.io.Serializable;

import studio.microworld.hypernote.support.adapter.NoteListAdapter;
import studio.microworld.hypernote.support.managmanet.EntityTypeHelper;

/**
 * Created by Mr.小世界 on 2018/9/14.
 */

public final class UserSettings implements Serializable
{
    // 隐私密码
    public String lockPassword = EMPTY_LOCK_PASSWORD;

    // 是否已启用废纸篓
    public boolean isUseRecovery = true;

    // 是否已设置隐私密码
    public boolean isLocked = false;

    public int selectFolderId = EntityTypeHelper.ALL_NORMAL_FOLDER_ID;

    public int noteListStyle = NoteListAdapter.LINER_LAYOUT;

    public final static String EMPTY_LOCK_PASSWORD = "";

}
