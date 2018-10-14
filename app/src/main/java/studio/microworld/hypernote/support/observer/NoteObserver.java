package studio.microworld.hypernote.support.observer;

/**
 * Created by Mr.小世界 on 2018/9/12.
 */

//用于访问数据的观察者
public interface NoteObserver
{
    int getId();

    String getTitle();

    String getContent();

    String getUpdatedAt();

    String getCreatedAt();

    int getFolderId();

    int getType();

    int NORMAL_NOTE_TYPE = 1;

    int NETWORK_NOTE_TYPE = 2;

    int PRIVATE_NOTE_TYPE = 4;

    int RECOVERY_NOTE_TYPE = 8;

    int RECOVERY_PRIVATE_NOTE_TYPE = RECOVERY_NOTE_TYPE | PRIVATE_NOTE_TYPE;

}
