package studio.microworld.hypernote.ui.main;

import studio.microworld.hypernote.support.pref.UserSettings;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.managmanet.NoteDataManager;
import studio.microworld.hypernote.support.widget.AlertDialogHelper;
import studio.microworld.hypernote.support.widget.ProgressDialogHelper;


/**
 * Created by Mr.小世界 on 2018/8/28.
 */

public interface HostBinder
{
    //绑定导航栏
    HostBinder bindNavigation(NavigationBinder navigation);

    //显示文件夹的内容
    void displayFolderNotes(FolderObserver handler);

    //的到数据访问器
    NoteDataManager getDataManager();

    AlertDialogHelper getAlertDialog();

    ProgressDialogHelper getProgressDialog();

    UserSettings getUserSettings();
}
