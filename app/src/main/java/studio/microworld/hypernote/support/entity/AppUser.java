package studio.microworld.hypernote.support.entity;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

/**
 * Created by Mr.小世界 on 2018/10/2.
 */

public final class AppUser extends BmobUser
{
    private BmobFile head;

    public BmobFile getHead()
    {
        return head;
    }

    public void setHead(BmobFile head)
    {
        this.head = head;
    }

}
