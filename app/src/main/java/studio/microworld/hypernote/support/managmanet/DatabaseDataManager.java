package studio.microworld.hypernote.support.managmanet;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.orhanobut.logger.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.litepal.LitePal;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.adapter.FolderListAdapter;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.entity.DatabaseFolder;
import studio.microworld.hypernote.support.entity.DatabaseNote;
import studio.microworld.hypernote.support.utlis.UriUtil;

/**
 * Created by Mr.小世界 on 2018/9/13.
 */

public abstract class DatabaseDataManager extends AsyncDataManager
{

    protected final WeakReference<Context> mContext;

    private final FolderListAdapter folderListAdapter;

    protected DatabaseDataManager(Context context)
    {
        super();
        mContext = new WeakReference<>(context.getApplicationContext());
        LitePal.getDatabase();
        if (AppSettingManager.isFirstLaunch())
        {
            DatabaseFolder folder = new DatabaseFolder();
            folder.setName(DatabaseFolder.DEFAULT_FOLDER_NAME);
            folder.save();

            folder = new DatabaseFolder();
            folder.setName("生活");
            folder.save();

            folder = new DatabaseFolder();
            folder.setName("工作");
            folder.save();

            folder = new DatabaseFolder();
            folder.setName("学习");
            folder.save();

            DatabaseFolder databaseFolder = LitePal.where("name = ?",
                    DatabaseFolder.DEFAULT_FOLDER_NAME).findFirst(DatabaseFolder.class);

            String now = TimeUtils.getNowString();
            DatabaseNote note = new DatabaseNote();
            note.setContent(getString(R.string.launch_note1));
            note.setType(DatabaseNote.NORMAL_NOTE_TYPE);
            note.setTitle("欢迎使用");
            note.setFolderId(databaseFolder.getId());
            note.setCreatedAt(now);
            note.setUpdatedAt(now);
            note.save();

            note = new DatabaseNote();
            note.setContent(getString(R.string.launch_note2));
            note.setType(DatabaseNote.NORMAL_NOTE_TYPE);
            note.setTitle("小提示");
            note.setFolderId(databaseFolder.getId());
            note.setCreatedAt(now);
            note.setUpdatedAt(now);
            note.save();
        }
        folderListAdapter = new FolderListAdapter();
        List<FolderObserver> folders = loadFolders();
        for (DatabaseFolder folder : convertFolderListToDatabaseType(folders))
        {
            int count = LitePal.where("folderId = ? and type = ?",
                    Integer.toString(folder.getId()),
                    Integer.toString(DatabaseNote.NORMAL_NOTE_TYPE))
                    .count(DatabaseNote.class);
            folder.setNoteCount(count);
        }
        folderListAdapter.setNewData(folders);
    }

    public FolderObserver getFolderById(int id)
    {
        switch (id)
        {
            case EntityTypeHelper.ALL_NORMAL_FOLDER_ID:
            {
                return EntityTypeHelper.ALL_NORMAL_FOLDER;
            }
            case EntityTypeHelper.NETWORK_FOLDER_ID:
            {
                return EntityTypeHelper.NETWORK_FOLDER;
            }
            case EntityTypeHelper.PRIVATE_FOLDER_ID:
            {
                return EntityTypeHelper.PRIVATE_FOLDER;
            }
            case EntityTypeHelper.RECOVERY_FOLDER_ID:
            {
                return EntityTypeHelper.RECOVERY_FOLDER;
            }
            default:
            {
                for (FolderObserver folderObserver : folderListAdapter.getData())
                {
                    if (folderObserver.getId() == id)
                    {
                        return folderObserver;
                    }
                }
                throw new NullPointerException();
            }
        }
    }

    public FolderListAdapter getFolderListAdapter()
    {
        return folderListAdapter;
    }

    @Override
    protected List<NoteObserver> removeNotes(List<NoteObserver> noteObservers)
    {
        for (DatabaseNote note : convertNoteListToDatabaseType(noteObservers))
        {
            FileUtils.deleteDir(getTargetDir(note.getId()));
            note.delete();
        }
        return noteObservers;
    }

    @Override
    protected List<NoteObserver> createNewNotes(Map<String, Pair<String, Integer>> newNotes,
                                                FolderObserver folderObserver)
    {
        String now = TimeUtils.getNowString();
        List<DatabaseNote> notes = new ArrayList<>();
        for (Map.Entry<String, Pair<String, Integer>> entry : newNotes.entrySet())
        {
            DatabaseNote dbNote = new DatabaseNote();
            String title = entry.getKey();
            int type = entry.getValue().second;
            String content = entry.getValue().first;
            File folder = getTargetDir(getNextDatabaseNoteId());
            Document document = getDocumentFormContent(content);
            Map<File, List<Element>> mapping = analysisCreateReference(document);
            for (Map.Entry<File, List<Element>> entry1 : mapping.entrySet())
            {
                File target = new File(folder, UUID.randomUUID().toString());
                if (FileUtils.copyFile(entry1.getKey(), target))
                {
                    String src = "file://" + target.getAbsolutePath();
                    for (Element element : entry1.getValue())
                    {
                        element.attr("src", src);
                    }
                } else
                {
                    Logger.d("copy failed:" + entry1.getKey().getAbsolutePath()
                            + "\n" + target.getAbsolutePath());
                    for (Element element : entry1.getValue())
                    {
                        element.attr("src", "");
                    }
                }
            }
            dbNote.setTitle(title);
            dbNote.setType(type);
            dbNote.setFolderId(folderObserver.getId());
            dbNote.setContent(documentToContent(document));
            dbNote.setCreatedAt(now);
            dbNote.setUpdatedAt(now);
            notes.add(dbNote);
        }
        LitePal.saveAll(notes);
        return safeConvertAll(notes);
    }

    @Override
    protected NoteObserver updateNote(NoteObserver noteObserver, String title, String content)
    {
        DatabaseNote dbNote = convertSingleToDatabaseType(noteObserver);
        if (dbNote.getTitle().equals(title)
                && dbNote.getContent().equals(content))
        {
            //直接返回
            return null;
        }
        //先获取现有的所有引用,只有md5能唯一标记一个文件,所以使用md5作为key
        File dir = getTargetDir(dbNote.getId());//获取note指向的文件夹
        HashMap<String, File> beforeRef = new HashMap<>();
        for (File file : dir.listFiles())
        {
            beforeRef.put(FileUtils.getFileMD5ToString(file), file);
        }
        //解析html获取现有引用
        Document document = getDocumentFormContent(content);
        Elements elements = document.getElementsByAttribute("src");//找出所有src(引用)
        HashSet<File> beforeRefNowRef = new HashSet<>();
        HashSet<File> files = new HashSet<>(beforeRef.values());
        HashMap<String, File> newRefMd5map = new HashMap<>();
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            Uri uri = Uri.parse(element.attr("src"));
            File current = new File(UriUtil.getRealPathFromUri(mContext.get(),
                    uri));
            if (files.contains(current))//有引用的文件
            {
                if (!beforeRefNowRef.contains(current))
                {
                    beforeRefNowRef.add(current);
                }
            } else//新的引用
            {
                //检查md5
                String md5 = FileUtils.getFileMD5ToString(current);
                File bFile = beforeRef.get(md5);
                if (bFile == null)
                {
                    bFile = newRefMd5map.get(md5);
                }
                if (bFile != null)//确认过是引用过的
                {
                    String ref = "file://" + bFile.getAbsolutePath();
                    Logger.d("ref :" + ref);
                    element.attr("src", ref);
                    if (!beforeRefNowRef.contains(bFile))
                    {
                        beforeRefNowRef.add(bFile);
                    }
                } else//确认是新的
                {
                    File target = new File(dir, UUID.randomUUID().toString());
                    if (FileUtils.copyFile(current, target))
                    {
                        newRefMd5map.put(md5, target);
                        String src = "file://" + target.getAbsolutePath();
                        Logger.d("create :" + src + "\nmd5:" + md5);
                        element.attr("src", src);
                    } else
                    {
                        element.attr("src", "");
                    }
                }
            }
        }
        files.removeAll(beforeRefNowRef);
        for (File file : files)
        {
            Logger.d("del :" + file.getAbsolutePath());
            FileUtils.deleteFile(file);
        }
        String parseContent = documentToContent(document);
        dbNote.setTitle(title);
        dbNote.setContent(parseContent);
        dbNote.setUpdatedAt(TimeUtils.getNowString());
        dbNote.save();
        return dbNote;
    }

    @Override
    protected List<NoteObserver> moveNotes(List<NoteObserver> noteObservers,
                                           FolderObserver folderObserver)
    {
        List<DatabaseNote> list = new ArrayList<>();
        for (DatabaseNote databaseNote : convertNoteListToDatabaseType(noteObservers))
        {
            if (databaseNote.getFolderId() == folderObserver.getId())
            {
                continue;
            }
            DatabaseFolder src = getDatabaseFolderByDatabaseNote(databaseNote);
            src.setNoteCount(src.getNoteCount() - 1);
            databaseNote.setFolderId(folderObserver.getId());
            DatabaseFolder dst = convertSingleToDatabaseType(folderObserver);
            dst.setNoteCount(dst.getNoteCount() + 1);
            list.add(databaseNote);
        }
        LitePal.saveAll(list);
        return safeConvertAll(list);
    }

    @Override
    protected List<NoteObserver> lockNotes(List<NoteObserver> noteObservers)
    {
        List<DatabaseNote> list = new ArrayList<>();
        for (DatabaseNote databaseNote : convertNoteListToDatabaseType(noteObservers))
        {
            if (EntityTypeHelper.isNormalNote(databaseNote))
            {
                DatabaseFolder databaseFolder = getDatabaseFolderByDatabaseNote(databaseNote);
                databaseFolder.setNoteCount(databaseFolder.getNoteCount() - 1);
                databaseNote.setType(DatabaseNote.PRIVATE_NOTE_TYPE);
                list.add(databaseNote);
            }
        }
        LitePal.saveAll(list);
        return safeConvertAll(list);
    }

    @Override
    protected List<NoteObserver> unlockNotes(List<NoteObserver> noteObservers)
    {
        List<DatabaseNote> list = new ArrayList<>();
        for (DatabaseNote databaseNote : convertNoteListToDatabaseType(noteObservers))
        {
            if (EntityTypeHelper.isPrivateNote(databaseNote))
            {
                DatabaseFolder databaseFolder = getDatabaseFolderByDatabaseNote(databaseNote);
                databaseFolder.setNoteCount(databaseFolder.getNoteCount() + 1);
                databaseNote.setType(DatabaseNote.NORMAL_NOTE_TYPE);
                list.add(databaseNote);
            }
        }
        LitePal.saveAll(list);
        return safeConvertAll(list);
    }

    @Override
    protected List<NoteObserver> recoveryNotes(List<NoteObserver> noteObservers)
    {
        List<DatabaseNote> list = new ArrayList<>();
        for (DatabaseNote databaseNote : convertNoteListToDatabaseType(noteObservers))
        {
            if (EntityTypeHelper.isNormalNote(databaseNote))
            {
                DatabaseFolder folder = getDatabaseFolderByDatabaseNote(databaseNote);
                folder.setNoteCount(folder.getNoteCount() - 1);
                databaseNote.setType(DatabaseNote.RECOVERY_NOTE_TYPE);
            } else if (EntityTypeHelper.isPrivateNote(databaseNote))
            {
                databaseNote.setType(DatabaseNote.RECOVERY_PRIVATE_NOTE_TYPE);
            }
            list.add(databaseNote);
        }
        LitePal.saveAll(list);
        return safeConvertAll(list);
    }

    @Override
    protected List<NoteObserver> reductionNotes(List<NoteObserver> noteObservers)
    {
        List<DatabaseNote> list = new ArrayList<>();
        for (DatabaseNote databaseNote : convertNoteListToDatabaseType(noteObservers))
        {
            if (EntityTypeHelper.isRecoveryPrivateNote(databaseNote))
            {
                databaseNote.setType(DatabaseNote.PRIVATE_NOTE_TYPE);
            } else if (EntityTypeHelper.isRecoveryNote(databaseNote))
            {
                DatabaseFolder folder = getDatabaseFolderByDatabaseNote(databaseNote);
                folder.setNoteCount(folder.getNoteCount() + 1);
                databaseNote.setType(DatabaseNote.NORMAL_NOTE_TYPE);
            }
            list.add(databaseNote);
        }
        LitePal.saveAll(list);
        return safeConvertAll(list);
    }

    @Override
    protected List<NoteObserver> loadNotesByFolder(FolderObserver folderObserver)
    {
        if (EntityTypeHelper.isPrivateFolder(folderObserver))
        {
            return safeConvertAll(LitePal.where("type = ?",
                    Integer.toString(DatabaseNote.PRIVATE_NOTE_TYPE))
                    .find(DatabaseNote.class));
        } else if (EntityTypeHelper.isRecoveryFolder(folderObserver))
        {
            return safeConvertAll(LitePal.where("type = ?",
                    Integer.toString(DatabaseNote.RECOVERY_NOTE_TYPE))
                    .find(DatabaseNote.class));
        } else if (EntityTypeHelper.isDatabeseFolder(folderObserver))
        {
            return safeConvertAll(LitePal.where("type = ? and folderId = ?",
                    Integer.toString(DatabaseNote.NORMAL_NOTE_TYPE),
                    Integer.toString(folderObserver.getId()))
                    .find(DatabaseNote.class));
        } else if (EntityTypeHelper.isAllNormalFolder(folderObserver))
        {
            return safeConvertAll(LitePal.where("type = ?",
                    Integer.toString(DatabaseNote.NORMAL_NOTE_TYPE))
                    .find(DatabaseNote.class));
        } else
        {
            return null;
        }
    }

    @Override
    protected List<FolderObserver> createNewFolders(List<String> names)
    {
        List<DatabaseFolder> folders = new ArrayList<>();
        for (String name : names)
        {
            DatabaseFolder dbFolder = new DatabaseFolder();
            dbFolder.setName(name);
            folders.add(dbFolder);
        }
        LitePal.saveAll(folders);
        return safeConvertAll(folders);
    }

    @Override
    protected List<FolderObserver> loadFolders()
    {
        return safeConvertAll(LitePal.findAll(DatabaseFolder.class));
    }

    @Override
    protected List<FolderObserver> removeFolders(List<FolderObserver> folderHandlers)
    {
        for (DatabaseFolder dbFolder : convertFolderListToDatabaseType(folderHandlers))
        {
            removeNotes(loadNotesByFolder(dbFolder));
            dbFolder.delete();
        }
        return folderHandlers;
    }

    @Override
    protected List<FolderObserver> recoveryFolders(List<FolderObserver> folderObservers)
    {
        int defaultFolderId = getDefaultDatabaseFolder().getId();
        List<DatabaseNote> noteObservers = new ArrayList<>();
        for (DatabaseFolder dbFolder : convertFolderListToDatabaseType(folderObservers))
        {
            for (DatabaseNote databaseNote : convertNoteListToDatabaseType(loadNotesByFolder(dbFolder)))
            {
                databaseNote.setFolderId(defaultFolderId);
                noteObservers.add(databaseNote);
            }
            dbFolder.delete();
        }
        LitePal.saveAll(noteObservers);
        return folderObservers;
    }


    @Override
    protected List<FolderObserver> updateFolders(Map<FolderObserver, String> newNames)
    {
        List<DatabaseFolder> dbFolders = new ArrayList<>();
        for (Map.Entry<DatabaseFolder, String> entry : convertFolderMapToDatabaseType(newNames).entrySet())
        {
            DatabaseFolder dbFolder = entry.getKey();
            dbFolder.setName(entry.getValue());
            dbFolders.add(dbFolder);
        }
        LitePal.saveAll(dbFolders);
        return safeConvertAll(dbFolders);
    }



    //----------------------static&&utils----------------------------------


    @SuppressWarnings("unchecked")
    protected static <T, V extends T> List<T> safeConvertAll(List<V> list)
    {
        return list == null ? null : (List<T>) list;
    }

    protected static DatabaseNote convertSingleToDatabaseType(NoteObserver handler)
    {
        return (DatabaseNote) handler;
    }

    protected static DatabaseFolder convertSingleToDatabaseType(FolderObserver handler)
    {
        return (DatabaseFolder) handler;
    }

    @SuppressWarnings("unchecked")
    protected static <T> Map<DatabaseFolder, T> convertFolderMapToDatabaseType(Map<FolderObserver, T> map)
    {
        return map == null ? null : (Map) map;
    }

    @SuppressWarnings("unchecked")
    protected static List<DatabaseFolder> convertFolderListToDatabaseType(List<FolderObserver> folderHandlers)
    {
        return folderHandlers == null ? null : (List) folderHandlers;
    }

    @SuppressWarnings("unchecked")
    protected static List<DatabaseNote> convertNoteListToDatabaseType(List<NoteObserver> noteHandlers)
    {
        return noteHandlers == null ? null : (List) noteHandlers;
    }

    private DatabaseFolder getDatabaseFolderByDatabaseNote(DatabaseNote dbNote)
    {
        List<DatabaseFolder> list = convertFolderListToDatabaseType(folderListAdapter.getData());
        for (int i = 0; i < list.size(); i++)
        {
            DatabaseFolder folderHandler = list.get(i);
            if (dbNote.getFolderId() == folderHandler.getId())
            {
                return folderHandler;
            }
        }
        throw new NullPointerException();
    }


    public DatabaseFolder getDefaultDatabaseFolder()
    {
        for (DatabaseFolder databaseNote : convertFolderListToDatabaseType(folderListAdapter.getData()))
        {
            if (databaseNote.getName().equals(DatabaseFolder.DEFAULT_FOLDER_NAME))
            {
                return databaseNote;
            }
        }
        throw new NullPointerException();
    }

    public int getNextDatabaseNoteId()
    {
        return LitePal.max(DatabaseNote.class, "id", Integer.TYPE) + 1;
    }

    @NonNull
    private String getString(@StringRes int id)
    {
        return mContext.get().getResources().getString(id);
    }

    protected File getTargetDir(int id)
    {
        return mContext.get().getExternalFilesDir("notes/" + Integer.toString(id));
    }

    protected Document getDocumentFormContent(String content)
    {
        return Jsoup.parse(getString(R.string.html_before) + content + getString(R.string.html_after));
    }

    protected Map<File, List<Element>> analysisCreateReference(Document document)
    {
        Elements elements = document.getElementsByAttribute("src");
        //name --> elements
        HashMap<String, List<Element>> refs = new HashMap<>();//先按引用名分组
        for (int i = 0; i < elements.size(); i++)
        {
            Element element = elements.get(i);
            String src = element.attr("src");
            List<Element> elements1 = refs.get(src);
            if (elements1 == null)
            {
                elements1 = new ArrayList<>();
                refs.put(src, elements1);
            }
            elements1.add(element);
        }
        //md5 --> file
        HashMap<String, File> md5set = new HashMap<>();
        //file --> elements
        HashMap<File, List<Element>> maping = new HashMap<>();
        for (Map.Entry<String, List<Element>> entry1 : refs.entrySet())
        {
            String raw = entry1.getKey();
            String real = UriUtil.getRealPathFromUri(mContext.get(),
                    Uri.parse(raw));
            String md5 = FileUtils.getFileMD5ToString(real);
            File target = md5set.get(md5);
            if (target == null)
            {
                target = new File(real);
                md5set.put(md5, target);
                maping.put(target, entry1.getValue());
            } else
            {
                maping.get(target).addAll(entry1.getValue());
            }
        }
        Logger.d(maping.size());
        return maping;
    }

    protected static String documentToContent(Document document)
    {
        return document.body().html();
    }

}
