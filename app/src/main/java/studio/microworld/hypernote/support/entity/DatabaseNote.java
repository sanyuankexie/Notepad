package studio.microworld.hypernote.support.entity;


import org.litepal.annotation.Encrypt;
import org.litepal.crud.LitePalSupport;

import studio.microworld.hypernote.support.observer.NoteObserver;

/**
 * Created by Mr.小世界 on 2018/9/12.
 */

public final class DatabaseNote extends LitePalSupport implements NoteObserver
{

    private String createdAt;

    private String updatedAt;

    @Encrypt(algorithm = AES)
    private String title;

    @Encrypt(algorithm = AES)
    private String content;

    private int id;

    private int folderId;

    private int type;

    public void setType(int type)
    {
        this.type = type;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public void setUpdatedAt(String updatedAt)
    {
        this.updatedAt = updatedAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public void setFolderId(int folderId)
    {
        this.folderId = folderId;
    }

    @Override
    public int getId()
    {
        return id;
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
    public String getUpdatedAt()
    {
        return updatedAt;
    }

    @Override
    public String getCreatedAt()
    {
        return createdAt;
    }

    @Override
    public int getFolderId()
    {
        return folderId;
    }

    @Override
    public int getType()
    {
        return type;
    }

    private static String getNoteTypeName(int type)
    {
        switch (type)
        {
            case NoteObserver.NETWORK_NOTE_TYPE:
            {
                return "网络便签";
            }
            case NoteObserver.NORMAL_NOTE_TYPE:
            {
                return "普通便签";
            }
            case NoteObserver.PRIVATE_NOTE_TYPE:
            {
                return "私密便签";
            }
            case NoteObserver.RECOVERY_NOTE_TYPE:
            {
                return "已回收便签";
            }
            case NoteObserver.RECOVERY_PRIVATE_NOTE_TYPE:
            {
                return "已回收私密便签";
            }
        }
        return null;
    }

    @Override
    public String toString()
    {
        return " Note Info : "
                + " Id= " + id
                + " FolderId = " + folderId
                + " Type = " + getNoteTypeName(type)
                + " Title = " + title;
    }
}
