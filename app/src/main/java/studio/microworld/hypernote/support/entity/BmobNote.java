package studio.microworld.hypernote.support.entity;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;

/**
 * Created by Mr.小世界 on 2018/9/12.
 */

public final class BmobNote extends BmobObject implements NoteObserver
{
    private BmobUser user;

    private String title;

    private String content;

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Override
    public int getId()
    {
        return getObjectId().hashCode();
    }

    @Override
    public String getTitle()
    {
        return title;
    }

    @Override
    public String getContent()
    {
        return content;
    }

    @Override
    public final int getFolderId()
    {
        return FolderObserver.NETWORK_FOLDER_ID;
    }

    @Override
    public final int getType()
    {
        return NORMAL_NOTE_TYPE;
    }
}
