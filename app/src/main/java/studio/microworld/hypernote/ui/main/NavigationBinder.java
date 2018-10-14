package studio.microworld.hypernote.ui.main;


import android.support.v4.widget.DrawerLayout;

import java.util.List;

import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.utlis.AsyncCallback;

/**
 * Created by Mr.小世界 on 2018/8/28.
 */

public interface NavigationBinder extends DrawerLayout.DrawerListener
{
    List<FolderObserver> getFolderList();

    //操作note
    void createNewNote(String title,
                       String content,
                       AsyncCallback<NoteObserver> callback);

    void removeNotes(List<NoteObserver> handler,
                     AsyncCallback<List<NoteObserver>> callback);

    void moveNotes(List<NoteObserver> noteHandler,
                   FolderObserver noteFolderHandler,
                   AsyncCallback<List<NoteObserver>> callback);

    void recoveryNotes(List<NoteObserver> noteObservers,
                       AsyncCallback<List<NoteObserver>> callback);

    void reductionNotes(List<NoteObserver> noteObservers,
                        AsyncCallback<List<NoteObserver>> callback);

    void lockNotes(List<NoteObserver> noteObservers,
                   AsyncCallback<List<NoteObserver>> callback);

    void unlockNotes(List<NoteObserver> noteObservers,
                     AsyncCallback<List<NoteObserver>> callback);

    FolderObserver getSelect();

}
