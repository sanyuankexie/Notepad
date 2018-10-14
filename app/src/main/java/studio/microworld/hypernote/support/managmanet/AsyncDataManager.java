package studio.microworld.hypernote.support.managmanet;

import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.v4.util.Pair;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.utlis.AsyncCallback;
import studio.microworld.hypernote.support.utlis.CheckUtil;

/**
 * Created by Mr.小世界 on 2018/9/13.
 */

public abstract class AsyncDataManager
{
    //-----------------Note crud-----------------------------------------

    //remove
    protected abstract List<NoteObserver> removeNotes(List<NoteObserver> noteObservers);

    @UiThread
    public final void removeNotesAsync(final List<NoteObserver> noteObservers,
                                       final AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return removeNotes(noteObservers);
            }
        }, callback);
    }

    //create
    protected abstract List<NoteObserver>
    createNewNotes(Map<String, Pair<String, Integer>> newNotes,
                   FolderObserver folderObserver);

    protected final NoteObserver createNewNote(String title,
                                               String content,
                                               int type,
                                               FolderObserver folderObserver)
    {
        return getSingleResult(createNewNotes(getSingleMapTask(title, Pair.create(content, type)),
                folderObserver));
    }

    @UiThread
    public final void createNewNoteAsync(final String title,
                                         final String content,
                                         final int type,
                                         final FolderObserver folderObserver,
                                         final AsyncCallback<NoteObserver> callback)
    {
        runAsync(new Task<NoteObserver>()
        {
            @Override
            public NoteObserver doInBackground()
            {
                return createNewNote(title, content, type, folderObserver);
            }
        }, callback);
    }


    protected abstract NoteObserver updateNote(NoteObserver noteObserver, String title, String content);

    @UiThread
    public final void updateNoteAsync(final NoteObserver noteObserver,
                                      final String title,
                                      final String content,
                                      final AsyncCallback<NoteObserver> callback)
    {
        runAsync(new Task<NoteObserver>()
        {
            @Override
            public NoteObserver doInBackground()
            {
                return updateNote(noteObserver, title, content);
            }
        }, callback);
    }

    protected abstract List<NoteObserver> moveNotes(List<NoteObserver> noteObservers,
                                                    FolderObserver folderObserver);


    @UiThread
    public final void moveNotesAsync(final List<NoteObserver> noteObservers,
                                     final FolderObserver folderObserver,
                                     final AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return moveNotes(noteObservers, folderObserver);
            }
        }, callback);
    }


    protected abstract List<NoteObserver> lockNotes(List<NoteObserver> noteObservers);

    protected abstract List<NoteObserver> unlockNotes(List<NoteObserver> noteObservers);

    @UiThread
    public final void lockNotesAsync(final List<NoteObserver> noteObservers,
                                     final AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return lockNotes(noteObservers);
            }
        }, callback);
    }

    @UiThread
    public final void unlockNotesAsync(final List<NoteObserver> noteObservers,
                                       final AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return unlockNotes(noteObservers);
            }
        }, callback);
    }


    protected abstract List<NoteObserver> uploadNotes(List<NoteObserver> noteObservers);

    protected abstract List<NoteObserver> downloadNotes(List<NoteObserver> noteObservers);

    @UiThread
    public final void uploadNotesAsync(final List<NoteObserver> noteObservers,
                                       AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return uploadNotes(noteObservers);
            }
        }, callback);
    }

    @UiThread
    public final void downloadNotesAsync(final List<NoteObserver> noteObservers,
                                         AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return downloadNotes(noteObservers);
            }
        }, callback);
    }

    protected abstract List<NoteObserver> recoveryNotes(List<NoteObserver> noteObservers);

    protected abstract List<NoteObserver> reductionNotes(List<NoteObserver> noteObservers);

    @UiThread
    public final void recoveryNotesAsync(final List<NoteObserver> noteObservers,
                                         AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return recoveryNotes(noteObservers);
            }
        }, callback);
    }

    @UiThread
    public final void reductionNotesAsync(final List<NoteObserver> noteObservers,
                                          AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return reductionNotes(noteObservers);
            }
        }, callback);
    }

    //select
    protected abstract List<NoteObserver> loadNotesByFolder(FolderObserver folderObserver);

    @UiThread
    public final void loadNotesByFolderAsync(final FolderObserver folderObserver,
                                             final AsyncCallback<List<NoteObserver>> callback)
    {
        runAsync(new Task<List<NoteObserver>>()
        {
            @Override
            public List<NoteObserver> doInBackground()
            {
                return loadNotesByFolder(folderObserver);
            }
        }, callback);
    }

    //---------------------folder crud---------------------------------------


    //create
    protected abstract List<FolderObserver>
    createNewFolders(List<String> names);

    @UiThread
    public final void createNewFoldersAsync(final List<String> names,
                                            final AsyncCallback<List<FolderObserver>> callback)
    {
        runAsync(new Task<List<FolderObserver>>()
        {
            @Override
            public List<FolderObserver> doInBackground()
            {
                return createNewFolders(names);
            }
        }, callback);
    }

    @UiThread
    public void createNewFolderAsync(String name,
                                     AsyncCallback<FolderObserver> callback)
    {
        createNewFoldersAsync(getSingleListTask(name),
                getSingleCallback(callback));
    }

    //update
    protected abstract List<FolderObserver> updateFolders(Map<FolderObserver, String> newNames);

    @UiThread
    public final void updateFoldersAsync(final Map<FolderObserver, String> newNames,
                                         final AsyncCallback<List<FolderObserver>> callback)
    {
        runAsync(new Task<List<FolderObserver>>()
        {
            @Override
            public List<FolderObserver> doInBackground()
            {
                return updateFolders(newNames);
            }
        }, callback);

    }

    @UiThread
    public final void updateFolderAsync(FolderObserver folderObserver,
                                        String name,
                                        AsyncCallback<FolderObserver> callback)
    {
        updateFoldersAsync(getSingleMapTask(folderObserver, name), getSingleCallback(callback));
    }

    //select
    protected abstract List<FolderObserver> loadFolders();

    @UiThread
    public final void loadFoldersAsync(final AsyncCallback<List<FolderObserver>> callback)
    {
        runAsync(new Task<List<FolderObserver>>()
        {
            @Override
            public List<FolderObserver> doInBackground()
            {
                return loadFolders();
            }
        }, callback);
    }

    //remove
    protected abstract List<FolderObserver> removeFolders(List<FolderObserver> folderObservers);

    @UiThread
    public final void removeFoldersAsync(final List<FolderObserver> folderObservers,
                                         final AsyncCallback<List<FolderObserver>> callback)
    {
        runAsync(new Task<List<FolderObserver>>()
        {
            @Override
            public List<FolderObserver> doInBackground()
            {
                return removeFolders(folderObservers);
            }
        }, callback);
    }

    @UiThread
    public final void removeFolderAsync(final FolderObserver folderObserver,
                                        final AsyncCallback<FolderObserver> callback)
    {
        removeFoldersAsync(getSingleListTask(folderObserver), getSingleCallback(callback));
    }

    protected abstract List<FolderObserver> recoveryFolders(List<FolderObserver> folderObservers);

    @UiThread
    public final void recoveryFoldersAsync(final List<FolderObserver> folderObservers,
                                         final AsyncCallback<List<FolderObserver>> callback)
    {
        runAsync(new Task<List<FolderObserver>>()
        {
            @Override
            public List<FolderObserver> doInBackground()
            {
                return recoveryFolders(folderObservers);
            }
        }, callback);
    }

    @UiThread
    public final void recoveryFolderAsync(final FolderObserver folderObserver,
                                        final AsyncCallback<FolderObserver> callback)
    {
        recoveryFoldersAsync(getSingleListTask(folderObserver), getSingleCallback(callback));
    }

    //-----------------------static&&utils------------------------------

    private final ExecutorService executor;

    protected AsyncDataManager()
    {
        executor = Executors.newSingleThreadExecutor();
    }

    private static <T> AsyncCallback<List<T>> getSingleCallback(final AsyncCallback<T> callback)
    {
        return callback == null ? null : new AsyncCallback<List<T>>()
        {
            @Override
            public void onResult(List<T> result)
            {
                CheckUtil.safeCallback(callback, getSingleResult(result));
            }
        };
    }

    private static <T> List<T> getSingleListTask(final T val)
    {
        return new ArrayList<T>()
        {
            {
                add(val);
            }
        };
    }

    private static <K, V> Map<K, V> getSingleMapTask(final K key, final V val)
    {
        return new HashMap<K, V>()
        {
            {
                put(key, val);
            }
        };
    }

    private static <T> T getSingleResult(List<T> list)
    {
        return list == null ? null : list.get(0);
    }

    private interface Task<T>
    {
        T doInBackground();
    }

    private static final class BatchTaskBundle<T>
    {
        private AsyncCallback<T> callback;

        private Task<T> task;

        private T result;

        BatchTaskBundle(Task<T> task, AsyncCallback<T> callback)
        {
            this.task = task;
            this.callback = callback;
        }

        void run()
        {
            result = task.doInBackground();
        }

        static public <T> BatchTaskBundle<T> getNewBundle(Task<T> task, AsyncCallback<T> callback)
        {
            if (cache.isEmpty())
            {
                return new BatchTaskBundle<>(task, callback);
            } else
            {
                BatchTaskBundle batchTaskBundle = cache.pop();
                batchTaskBundle.task = task;
                batchTaskBundle.callback = callback;
                return batchTaskBundle;
            }
        }

        static final Stack<BatchTaskBundle> cache = new Stack<>();

        static public void recovery(BatchTaskBundle bundle)
        {
            bundle.callback = null;
            bundle.result = null;
            bundle.task = null;
            cache.push(bundle);
        }
    }

    private static final class BatchAsyncTask<T> extends AsyncTask<BatchTaskBundle<T>,
            Void, BatchTaskBundle<T>>
    {
        @Override
        protected BatchTaskBundle<T> doInBackground(BatchTaskBundle<T>... params)
        {
            BatchTaskBundle<T> bundle = params[0];
            bundle.run();
            return bundle;
        }

        @Override
        protected void onPostExecute(BatchTaskBundle<T> bundle)
        {
            CheckUtil.safeCallback(bundle.callback, bundle.result);
            BatchTaskBundle.recovery(bundle);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void runAsync(Task<T> task, AsyncCallback<T> callback)
    {
        new BatchAsyncTask<T>().executeOnExecutor(executor, BatchTaskBundle.getNewBundle(task, callback));
    }
}
