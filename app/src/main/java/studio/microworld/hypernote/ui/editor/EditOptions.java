package studio.microworld.hypernote.ui.editor;

import java.io.Serializable;

/**
 * Created by Mr.小世界 on 2018/9/2.
 */

public final class EditOptions implements Serializable
{
    public int actionType = TYPE_ADD;

    public String updatedAt = "";

    public String createdAt = "";

    public String content = "";

    public String title = "";

    public int noteId = 0;

    public int position = 0;

    public boolean editable = true;

    public final static int TYPE_ADD = 0;

    public final static int TYPE_UPDATE = 1;
}
