package studio.microworld.hypernote.support.managmanet;

import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;

/**
 * Created by Mr.小世界 on 2018/9/16.
 */
public final class EntityTypeHelper implements FolderObserver
{
    public static boolean isDefaultFolder(FolderObserver folder)
    {
        return FolderObserver.DEFAULT_FOLDER_NAME.equals(folder.getName());
    }

    public static boolean isAbstractFolder(FolderObserver folder)
    {
        return EntityTypeHelper.class.equals(folder.getClass());
    }

    public static boolean isDatabeseFolder(FolderObserver folderObserver)
    {
        return !isAbstractFolder(folderObserver);
    }

    public static boolean isNetworkFolder(FolderObserver folder)
    {
        return folder.getId() == FolderObserver.NETWORK_FOLDER_ID;
    }

    public static boolean isRecoveryFolder(FolderObserver handler)
    {
        return handler.getId() == FolderObserver.RECOVERY_FOLDER_ID;
    }

    public static boolean isPrivateFolder(FolderObserver handler)
    {
        return handler.getId() == FolderObserver.PRIVATE_FOLDER_ID;
    }

    public static boolean isAllNormalFolder(FolderObserver handler)
    {
        return handler.getId() == FolderObserver.ALL_NORMAL_FOLDER_ID;
    }

    public static boolean isNetworkNote(NoteObserver noteObserver)
    {
        return noteObserver.getFolderId() == FolderObserver.NETWORK_FOLDER_ID;
    }

    public static boolean isLocalNote(NoteObserver handler)
    {
        return isLocalNoteType(handler.getType());
    }

    public static boolean isNormalNote(NoteObserver handler)
    {
        return handler.getType() == NoteObserver.NORMAL_NOTE_TYPE;
    }

    public static boolean isPrivateNote(NoteObserver handler)
    {
        return handler.getType() == NoteObserver.PRIVATE_NOTE_TYPE;
    }

    public static boolean isRecoveryNote(NoteObserver handler)
    {
        return handler.getType() == NoteObserver.RECOVERY_NOTE_TYPE;
    }

    public static boolean isRecoveryPrivateNote(NoteObserver handler)
    {
        return handler.getType() == NoteObserver.RECOVERY_PRIVATE_NOTE_TYPE;
    }

    private static boolean isLocalNoteType(int type)
    {
        return type == NoteObserver.NORMAL_NOTE_TYPE
                || type == NoteObserver.RECOVERY_NOTE_TYPE
                || type == NoteObserver.PRIVATE_NOTE_TYPE
                || type == NoteObserver.RECOVERY_PRIVATE_NOTE_TYPE;
    }

    public static boolean isLegalNoteType(int type)
    {
        return isLocalNoteType(type)
                || type == NoteObserver.NETWORK_NOTE_TYPE;
    }

    private String name;

    private int id;

    private EntityTypeHelper(String name, int id)
    {
        this.name = name;
        this.id = id;
    }

    public static final FolderObserver ALL_NORMAL_FOLDER;

    public static final FolderObserver PRIVATE_FOLDER;

    public static final FolderObserver NETWORK_FOLDER;

    public static final FolderObserver RECOVERY_FOLDER;

    static
    {
        ALL_NORMAL_FOLDER = new EntityTypeHelper(ALL_NORMAL_FOLDER_NAME, ALL_NORMAL_FOLDER_ID);
        PRIVATE_FOLDER = new EntityTypeHelper(PRIVATE_FOLDER_NAME, PRIVATE_FOLDER_ID);
        NETWORK_FOLDER = new EntityTypeHelper(NETWORK_FOLDER_NAME, NETWORK_FOLDER_ID);
        RECOVERY_FOLDER = new EntityTypeHelper(RECOVERY_FOLDER_NAME, RECOVERY_FOLDER_ID);
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public int getId()
    {
        return id;
    }

    @Override
    public int getNoteCount()
    {
        return Integer.MIN_VALUE;
    }
}
