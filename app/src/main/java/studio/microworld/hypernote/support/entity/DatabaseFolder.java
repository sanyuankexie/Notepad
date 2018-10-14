package studio.microworld.hypernote.support.entity;


import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import studio.microworld.hypernote.support.observer.FolderObserver;

/**
 * Created by Mr.小世界 on 2018/9/12.
 */

public final class DatabaseFolder extends LitePalSupport implements FolderObserver
{
    private int id;

    private String name;

    @Column(ignore = true)
    private int noteCount;

    @Override
    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getNoteCount()
    {
        return noteCount;
    }

    public void setNoteCount(int noteCount)
    {
        this.noteCount = noteCount;
    }
}
