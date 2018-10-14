package studio.microworld.hypernote.support.managmanet;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;


import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.UploadFileListener;
import my.util.Tuple2;
import studio.microworld.hypernote.support.entity.AppUser;
import studio.microworld.hypernote.support.entity.BmobNote;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;

/**
 * Created by Mr.小世界 on 2018/9/13.
 */

//NoteData数据资源的访问器实现了网络和本地的统一管理
//对外使用观察器接口封闭实现
//分析便签内容,还有更新磁盘引用
//Async方法外部调用,非Async内部调用
public final class NoteDataManager extends DatabaseDataManager
{
    private final Lock lock;
    private final Condition condition;

    private static NoteDataManager instance;

    public static void initialize(Context context)
    {
        instance = new NoteDataManager(context);
    }

    private NoteDataManager(Context context)
    {
        super(context);
        lock = new ReentrantLock();
        condition = lock.newCondition();
    }

    public static NoteDataManager getInstance()
    {
        if (instance == null)
        {
            throw new NullPointerException("没有初始化!");
        } else
        {
            return instance;
        }
    }

    @Override
    protected List<NoteObserver> loadNotesByFolder(FolderObserver folderObserver)
    {
        if (EntityTypeHelper.isNetworkFolder(folderObserver))
        {
            return safeConvertAll(loadInternal());
        }
        return super.loadNotesByFolder(folderObserver);
    }


    @Override
    protected List<NoteObserver> uploadNotes(List<NoteObserver> noteObservers)
    {
        List<NoteObserver> newNotes = new ArrayList<>(noteObservers.size());
//        for (NoteObserver noteObserver : noteObservers)
//        {
//            Document document = Jsoup.parse(noteObserver.getContent());
//            Elements elements = document.getElementsByAttribute("src");
//            List<File> strings = new ArrayList<>(elements.size());
//            for (Element element : elements)
//            {
//                strings.add(new File(element.attr("src")));
//            }
//            List<File> files =
//            for (int i = 0; i < files.size(); i++)
//            {
//                File file = files.get(i);
//                elements.get(i).attr("src", file == null ? "" : file.getAbsolutePath());
//            }
//            newNotes.add(createNewNote(noteObserver.getTitle(),
//                    documentToContent(document),
//                    NoteObserver.NORMAL_NOTE_TYPE, getDefaultDatabaseFolder()));
//        }
        return newNotes;
    }

    @Override
    protected List<NoteObserver> downloadNotes(List<NoteObserver> noteObservers)
    {
        List<NoteObserver> newNotes = new ArrayList<>(noteObservers.size());
        for (NoteObserver noteObserver : noteObservers)
        {
            Document document = Jsoup.parse(noteObserver.getContent());
            Elements elements = document.getElementsByAttribute("src");
            List<String> strings = new ArrayList<>(elements.size());
            for (Element element : elements)
            {
                strings.add(element.attr("src"));
            }
            List<File> files = downloadFileInternal(getTargetDir(getNextDatabaseNoteId()), strings);
            for (int i = 0; i < files.size(); i++)
            {
                File file = files.get(i);
                elements.get(i).attr("src", file == null ? "" : file.getAbsolutePath());
            }
            newNotes.add(createNewNote(noteObserver.getTitle(),
                    documentToContent(document),
                    NoteObserver.NORMAL_NOTE_TYPE, getDefaultDatabaseFolder()));
        }
        return newNotes;
    }

    @SuppressWarnings("unchecked")
    private List<BmobNote> convertNoteListToBmobType(List<NoteObserver> noteObservers)
    {
        return (List) noteObservers;
    }

    private List<BmobNote> loadInternal()
    {
        AppUser user = null;
        if ((user = BmobUser.getCurrentUser(AppUser.class)) == null)
        {
            return null;
        }
        lock.lock();
        BmobQuery<BmobNote> bmobQuery = new BmobQuery<>();
        bmobQuery.setLimit(500);
        bmobQuery.addWhereEqualTo("user", user);
        class FindListenerAdapter extends FindListener<BmobNote>
        {
            private List<BmobNote> result;
            @Override
            public void done(List<BmobNote> list, BmobException e)
            {
                lock.lock();
                result = list;
                condition.signalAll();
                lock.unlock();
            }
        }
        FindListenerAdapter adapter = new FindListenerAdapter();
        bmobQuery.findObjects(adapter);
        try
        {
            condition.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }
        return adapter.result;
    }

    private List<File> downloadFileInternal(File saveDir,List<String> urls)
    {
        lock.lock();
        final List<File> files = new ArrayList<>();
        for (String url : urls)
        {
            String name = UUID.randomUUID().toString();
            BmobFile bmobFile = new BmobFile(name, "", url);
            final File path = new File(saveDir, name);
            bmobFile.download(path, new DownloadFileListener()
            {
                @Override
                public void done(String s, BmobException e)
                {
                    lock.lock();
                    if (e == null)
                    {
                        files.add(path);
                    } else
                    {
                        files.add(null);
                    }
                    condition.signalAll();
                    lock.unlock();
                }

                @Override
                public void onProgress(Integer integer, long l)
                {
                }
            });
            try
            {
                condition.await();
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        lock.unlock();
        return files;
    }

    private List<BmobNote> createInternal(List<Tuple2<String,String>> data)
    {
        lock.lock();
        BmobBatch bmobBatch = new BmobBatch();
        final List<BmobNote> doing = new ArrayList<>(data.size());
        for (Tuple2<String, String> tuple : data)
        {
            String title = tuple.item1;
            String content = tuple.item2;
            BmobNote bmobNote = new BmobNote();
            bmobNote.setTitle(title);
            bmobNote.setContent(content);
            doing.add(bmobNote);
        }
        bmobBatch.insertBatch((List) doing);
        final List<BmobNote> result = new ArrayList<>(data.size());
        bmobBatch.doBatch(new QueryListListener<BatchResult>()
        {
            @Override
            public void done(List<BatchResult> list, BmobException e)
            {
                lock.lock();
                for (int i = 0; i < list.size(); i++)
                {
                    if (list.get(i).isSuccess())
                    {
                        result.add(doing.get(i));
                    } else
                    {
                        result.add(null);
                    }
                }
                condition.signalAll();
                lock.unlock();
            }
        });
        try
        {
            condition.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        } finally
        {
            lock.unlock();
        }
        return result;
    }

    private List<Boolean> deleteInternal(List<BmobNote> list)
    {
        lock.lock();
        final List<Boolean> result = new ArrayList<>(list.size());
        BmobBatch bmobBatch = new BmobBatch();
        bmobBatch.deleteBatch((List) list);
        bmobBatch.doBatch(new QueryListListener<BatchResult>()
        {
            @Override
            public void done(List<BatchResult> list, BmobException e)
            {
                lock.lock();
                for (BatchResult r : list)
                {
                    result.add(r.isSuccess());
                }
                condition.signalAll();
                lock.unlock();
            }
        });
        try
        {
            condition.await();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        finally
        {
            lock.unlock();
        }
        return result;
    }
}
