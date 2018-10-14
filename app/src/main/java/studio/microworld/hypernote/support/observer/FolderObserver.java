package studio.microworld.hypernote.support.observer;

/**
 * Created by Mr.小世界 on 2018/9/12.
 */
//用于访问数据的观察者
public interface FolderObserver
{
    String getName();

    int getId();

    int getNoteCount();

    int ALL_NORMAL_FOLDER_ID = -1;

    int NETWORK_FOLDER_ID = -2;

    int PRIVATE_FOLDER_ID = -3;

    int RECOVERY_FOLDER_ID = -4;

    String DEFAULT_FOLDER_NAME = "默认文件夹";

    String ALL_NORMAL_FOLDER_NAME = "所有便签";

    String NETWORK_FOLDER_NAME = "网络";

    String PRIVATE_FOLDER_NAME = "私密";

    String RECOVERY_FOLDER_NAME = "回收站";
}
