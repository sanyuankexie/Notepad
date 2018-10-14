package studio.microworld.hypernote.ui.main;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.adapter.FolderListAdapter;
import studio.microworld.hypernote.support.managmanet.AppSettingManager;
import studio.microworld.hypernote.support.managmanet.EntityTypeHelper;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.utlis.AsyncCallback;
import studio.microworld.hypernote.support.utlis.CheckUtil;
import studio.microworld.hypernote.support.utlis.DrawableUtil;
import studio.microworld.hypernote.support.framework.BaseFragment;
import studio.microworld.hypernote.ui.setting.SettingActivity;

/**
 * Created by Mr.小世界 on 2018/9/18.
 */

public final class NavigationBar
        extends BaseFragment
        implements NavigationBinder,
        View.OnClickListener,
        BaseQuickAdapter.OnItemChildClickListener,
        BaseQuickAdapter.OnItemChildLongClickListener,
        LockQuickWindow.OnLockVerifyListener
{
    //---------------------初始化---------------------------------
    @BindView(R.id.rv_note_list_folder)
    RecyclerView mRvNoteFolder;   // 便签夹列表

    @BindView(R.id.ll_folder_list_setting)
    LinearLayout mLlToSetting;//导航到设置界面

    private RelativeLayout mRlAllFolder;
    private ImageView mIvAllIcon;
    private TextView mTvAllTitle;
    private TextView mTvAllCount;

    private TextView tvToEdit;
    private RelativeLayout mRlAddFolder;

    private ImageView mIvPrimaryIcon;
    private TextView mTvPrimaryTitle;

    private View mHeaderView1;
    private View mHeaderView2;
    private View mFooterView;

    private RelativeLayout mRlRecycleFolder;

    private ImageView mIvRecycleIcon;
    private TextView mTvRecycleTitle;
    private FolderListAdapter folderListAdapter;


    private FolderObserver abstractSelect = null;
    private HostBinder host;


    private View loadHeaderView1()
    {
        mHeaderView1 = LayoutInflater
                .from(getActivity())
                .inflate(R.layout.include_main_nav_handler, null, false);
        return mHeaderView1;
    }

    private View loadHeaderView2()
    {

        mHeaderView2 = LayoutInflater.from(getActivity())
                .inflate(R.layout.include_main_nav_hearder_2, null, false);

        mRlAllFolder = (RelativeLayout) mHeaderView2
                .findViewById(R.id.rl_folder_all);
        mIvAllIcon = (ImageView) mHeaderView2
                .findViewById(R.id.iv_folder_all_icon);
        mTvAllTitle = (TextView) mHeaderView2
                .findViewById(R.id.tv_folder_all_title);
        mTvAllCount = (TextView) mHeaderView2
                .findViewById(R.id.tv_folder_all_count);

        tvToEdit = (TextView) mHeaderView2
                .findViewById(R.id.tv_folder_to_edit);


        mRlAllFolder.setOnClickListener(this);
        tvToEdit.setOnClickListener(this);

        return mHeaderView2;
    }

    /**
     * 获取FolderList的footer
     */
    private View loadFooterView()
    {
        mFooterView = LayoutInflater.from(getActivity())
                .inflate(R.layout.include_main_nav_footer, null, false);

        RelativeLayout mRlPrimaryFolder = (RelativeLayout) mFooterView
                .findViewById(R.id.rl_folder_private);
        mIvPrimaryIcon = (ImageView) mFooterView
                .findViewById(R.id.img_folder_privacy_icon);
        mTvPrimaryTitle = (TextView) mFooterView
                .findViewById(R.id.tv_folder_privacy_title);

        mRlRecycleFolder = (RelativeLayout) mFooterView
                .findViewById(R.id.rl_folder_recovery);
        mIvRecycleIcon = (ImageView) mFooterView
                .findViewById(R.id.img_folder_recycle_bin_ic);
        mTvRecycleTitle = (TextView) mFooterView
                .findViewById(R.id.tv_folder_recycle_bin_title);
        mRlAddFolder = (RelativeLayout) mFooterView
                .findViewById(R.id.rl_folder_add);

        mRlPrimaryFolder.setOnClickListener(this);
        mRlRecycleFolder.setOnClickListener(this);
        mRlAddFolder.setOnClickListener(this);

        return mFooterView;
    }

    private void loadAdapterToView()
    {
        folderListAdapter = host.getDataManager().getFolderListAdapter();
        folderListAdapter.addHeaderView(mHeaderView1);
        folderListAdapter.addHeaderView(mHeaderView2);
        folderListAdapter.addFooterView(mFooterView);
        mRvNoteFolder.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRvNoteFolder.setAdapter(folderListAdapter);
        mLlToSetting.setOnClickListener(this);
        folderListAdapter.setOnItemChildClickListener(this);
        folderListAdapter.setOnItemChildLongClickListener(this);
        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
        FolderObserver folderObserver = host.getDataManager()
                .getFolderById(host.getUserSettings().selectFolderId);
        List<Integer> list = new ArrayList<>();
        list.add(EntityTypeHelper.ALL_NORMAL_FOLDER_ID);
        list.add(EntityTypeHelper.NETWORK_FOLDER_ID);
        list.add(EntityTypeHelper.PRIVATE_FOLDER_ID);
        list.add(EntityTypeHelper.RECOVERY_FOLDER_ID);
        if (list.contains(folderObserver.getId()))
        {
            selectAbstractNoCheck(folderObserver);
        } else
        {
            selectNormal(folderListAdapter.getData().indexOf(folderObserver));
        }
    }

    private void loadHostBinder()
    {
        host = ((HostBinder) getActivity()).bindNavigation(this);
    }

    //---------------Navigation Interface----------------------------------

    @Override
    public List<FolderObserver> getFolderList()
    {
        return new ArrayList<>(folderListAdapter.getData());
    }

    @Override
    public void createNewNote(String title,
                              String content,
                              final AsyncCallback<NoteObserver> callback)
    {
        final FolderObserver folderObserver = getSelect();
        host.getDataManager()
                .createNewNoteAsync(title,
                        content,
                        (EntityTypeHelper.isPrivateFolder(folderObserver)
                                ? NoteObserver.PRIVATE_NOTE_TYPE
                                : NoteObserver.NORMAL_NOTE_TYPE),
                        ((EntityTypeHelper.isAllNormalFolder(folderObserver)
                                ||EntityTypeHelper.isPrivateFolder(folderObserver)) ?
                                host.getDataManager().getDefaultDatabaseFolder() : folderObserver),
                        new AsyncCallback<NoteObserver>()
                        {
                            @Override
                            public void onResult(NoteObserver noteObserver)
                            {
                                folderNoteCountViewUpdate(folderObserver);
                                if (!EntityTypeHelper.isAllNormalFolder(folderObserver))
                                {
                                    folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                                }
                                CheckUtil.safeCallback(callback, noteObserver);
                            }
                        });
    }

    @Override
    public void removeNotes(List<NoteObserver> handler,
                            final AsyncCallback<List<NoteObserver>> callback)
    {
        host.getDataManager().removeNotesAsync(handler, new AsyncCallback<List<NoteObserver>>()
        {
            @Override
            public void onResult(List<NoteObserver> noteObservers)
            {
                CheckUtil.safeCallback(callback, noteObservers);
            }
        });
    }

    @Override
    public void moveNotes(List<NoteObserver> noteHandler,
                          final FolderObserver dest,
                          final AsyncCallback<List<NoteObserver>> callback)
    {
        final FolderObserver src = getSelect();
        host.getDataManager()
                .moveNotesAsync(noteHandler,
                        dest,
                        new AsyncCallback<List<NoteObserver>>()
                        {
                            @Override
                            public void onResult(List<NoteObserver> noteObservers)
                            {
                                if (EntityTypeHelper.isAllNormalFolder(src))
                                {
                                    folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                                    folderListAdapter.notifyDataSetChanged();
                                } else
                                {
                                    folderNoteCountViewUpdate(src);
                                    folderNoteCountViewUpdate(dest);
                                }
                                CheckUtil.safeCallback(callback, noteObservers);
                            }
                        });
    }

    @Override
    public void recoveryNotes(List<NoteObserver> noteObservers,
                              final AsyncCallback<List<NoteObserver>> callback)
    {
        host.getDataManager().recoveryNotesAsync(noteObservers,
                new AsyncCallback<List<NoteObserver>>()
                {
                    @Override
                    public void onResult(List<NoteObserver> noteObservers)
                    {
                        folderListAdapter.notifyDataSetChanged();
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, noteObservers);
                    }
                });
    }

    @Override
    public void reductionNotes(List<NoteObserver> noteObservers,
                               final AsyncCallback<List<NoteObserver>> callback)
    {
        host.getDataManager().reductionNotesAsync(noteObservers,
                new AsyncCallback<List<NoteObserver>>()
                {
                    @Override
                    public void onResult(List<NoteObserver> noteObservers)
                    {
                        folderListAdapter.notifyDataSetChanged();
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, noteObservers);
                    }
                });
    }

    @Override
    public void lockNotes(List<NoteObserver> noteObservers,
                          final AsyncCallback<List<NoteObserver>> callback)
    {
        host.getDataManager().lockNotesAsync(noteObservers,
                new AsyncCallback<List<NoteObserver>>()
                {
                    @Override
                    public void onResult(List<NoteObserver> noteObservers)
                    {
                        folderListAdapter.notifyDataSetChanged();
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, noteObservers);
                    }
                });
    }

    @Override
    public void unlockNotes(List<NoteObserver> noteObservers,
                            final AsyncCallback<List<NoteObserver>> callback)
    {
        host.getDataManager().unlockNotesAsync(noteObservers,
                new AsyncCallback<List<NoteObserver>>()
                {
                    @Override
                    public void onResult(List<NoteObserver> noteObservers)
                    {
                        folderListAdapter.notifyDataSetChanged();
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, noteObservers);
                    }
                });
    }

    @Override
    public FolderObserver getSelect()
    {
        if (folderListAdapter.getSelect() != FolderListAdapter.NO_SELECT)
        {
            return folderListAdapter.getData().get(folderListAdapter.getSelect());
        } else
        {
            if (abstractSelect == null)
            {
                return EntityTypeHelper.ALL_NORMAL_FOLDER;
            }
            return abstractSelect;
        }
    }

    //------------------base override -------------------------------------------

    @Override
    public void onVerifySuccess()
    {
        noSelect();
        abstractSelect = EntityTypeHelper.PRIVATE_FOLDER;
        selectPrivateFolderViewUpdate(true);
        host.displayFolderNotes(EntityTypeHelper.PRIVATE_FOLDER);
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.fragment_main_nav;
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        loadHeaderView1();
        loadHeaderView2();
        loadFooterView();
    }

    @Override
    protected void onLoadActivityAfter()
    {
        loadHostBinder();
        loadAdapterToView();
        if (host.getUserSettings().isUseRecovery)
        {
            mRlRecycleFolder.setVisibility(View.VISIBLE);
        } else
        {
            mRlRecycleFolder.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDrawerSlide(View drawerView,
                              float slideOffset)
    {

    }

    @Override
    public void onDrawerOpened(View drawerView)
    {

    }

    @Override
    public void onDrawerClosed(View drawerView)
    {
        enableEditModeViewUpdate(false);
    }

    @Override
    public void onDrawerStateChanged(int newState)
    {

    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.rl_folder_all:
            {
                selectAbstract(EntityTypeHelper.ALL_NORMAL_FOLDER);
            }
            break;
            case R.id.rl_folder_private:
            {
                selectAbstract(EntityTypeHelper.PRIVATE_FOLDER);
            }
            break;
            case R.id.rl_folder_recovery:
            {
                selectAbstract(EntityTypeHelper.RECOVERY_FOLDER);
            }
            break;
            case R.id.ll_folder_list_setting:
            {
                SettingActivity.openSetting(this);
            }
            break;
            case R.id.tv_folder_to_edit:
            {
                if (isEditMode())
                {
                    enableEditModeViewUpdate(false);
                } else
                {
                    enableEditModeViewUpdate(true);
                }
            }
            break;
            case R.id.rl_folder_add:
            {
                displayCreateFolderDialog();
            }
            break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case RC.request_code.REQUEST_CODE_TO_SETTING:
            {
                host.getUserSettings().isUseRecovery
                        = AppSettingManager.getSharedPreferences()
                        .getBoolean(getString(R.string.is_recovery_key), true);
                if (host.getUserSettings().isUseRecovery)
                {
                    mRlRecycleFolder.setVisibility(View.VISIBLE);
                } else
                {
                    mRlRecycleFolder.setVisibility(View.GONE);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemChildClick(final BaseQuickAdapter adapter, View view, final int position)
    {
        if (!isEditMode())
        {
            Logger.d("folder选中" + position);
            //点击时选中文件夹
            selectNormal(position);
        } else
        {
            switch (view.getId())
            {
                case R.id.ib_delete_action:
                {
                    Logger.d("删除操作");
                    displayDeleteFolderDialog(position);
                }
                break;
                case R.id.tv_folder_list_title:
                {
                    displayFolderRenameDialog(position);
                }
                break;
            }
        }
    }

    @Override
    public boolean onItemChildLongClick(BaseQuickAdapter adapter, View view, int position)
    {
        if (!isEditMode())
        {
            enableEditModeViewUpdate(true);
        } else
        {
            switch (view.getId())
            {
                case R.id.ib_delete_action:
                {
                    Logger.d("删除操作");
                    displayDeleteFolderDialog(position);
                }
                break;
                case R.id.tv_folder_list_title:
                {
                    displayFolderRenameDialog(position);
                }
                break;
            }
        }
        return true;
    }

    //---------------------------view update -----------------------------------------------

    private void enableEditModeViewUpdate(boolean enable)
    {
        if (enable)
        {
            folderListAdapter.changeState(FolderListAdapter.EditState.class);
            tvToEdit.setText("退出编辑模式");
            mRlAddFolder.setVisibility(View.VISIBLE);
            folderListAdapter.notifyDataSetChanged();
        } else
        {
            folderListAdapter.changeState(FolderListAdapter.DefaultState.class);
            tvToEdit.setText("编辑便签夹");
            mRlAddFolder.setVisibility(View.GONE);
            folderListAdapter.notifyDataSetChanged();
        }
    }

    private void selectAllFolderViewUpdate(boolean selected)
    {
        mRlAllFolder.setSelected(selected);
        if (selected)
        {
            mIvAllIcon.setBackgroundDrawable(DrawableUtil
                    .getIcFolderSelectedDrawable(getResources()
                            .getColor(R.color.colorPrimary)));
            mTvAllTitle.setTextColor(getResources()
                    .getColor(R.color.colorPrimary));
            mTvAllCount.setTextColor(getResources()
                    .getColor(R.color.colorPrimary));
        } else
        {
            mIvAllIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
            mTvAllTitle.setTextColor(getResources()
                    .getColor(R.color.colorBlackAlpha87));
            mTvAllCount.setTextColor(getResources()
                    .getColor(R.color.colorBlackAlpha54));
        }
    }

    private void selectNetworkFolderViewUpdate(boolean selected)
    {
        mRlAllFolder.setSelected(selected);
    }

    private void selectPrivateFolderViewUpdate(boolean selected)
    {
        mRlAllFolder.setSelected(selected);
        if (selected)
        {
            mIvPrimaryIcon.setBackgroundDrawable(DrawableUtil
                    .getIcFolderSelectedDrawable(getResources()
                            .getColor(R.color.colorPrimary)));
            mTvPrimaryTitle.setTextColor(getResources()
                    .getColor(R.color.colorPrimary));
        } else
        {
            mIvPrimaryIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
            mTvPrimaryTitle.setTextColor(getResources()
                    .getColor(R.color.colorBlackAlpha87));
        }
    }

    private void selectRecoveryFolderViewUpdate(boolean selected)
    {
        mRlAllFolder.setSelected(selected);
        if (selected)
        {
            mIvRecycleIcon.setBackgroundDrawable(DrawableUtil
                    .getIcFolderSelectedDrawable(getResources()
                            .getColor(R.color.colorPrimary)));
            mTvRecycleTitle.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else
        {
            mIvRecycleIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
            mTvRecycleTitle.setTextColor(getResources().getColor(R.color.colorBlackAlpha87));
        }
    }

    private void folderNoteCountViewUpdate(FolderObserver folderObserver)
    {
        if (EntityTypeHelper.isAllNormalFolder(folderObserver))
        {
            int count = 0;
            for (FolderObserver note : folderListAdapter.getData())
            {
                count += note.getNoteCount();
            }
            mTvAllCount.setText(Integer.toString(count));
            folderListAdapter.notifyDataSetChanged();
            return;
        } else if (EntityTypeHelper.isDatabeseFolder(folderObserver))
        {
            for (int i = 0; i < folderListAdapter.getData().size(); i++)
            {
                FolderObserver folder = folderListAdapter.getData().get(i);
                if (folderObserver == folder)
                {
                    //i 是List的position
                    folderListAdapter.notifyItemChanged(i + folderListAdapter.getHeaderLayoutCount());
                    return;
                }
            }
        }
    }

    //-------------------- internal action -------------------------------


    //如果删除了当前选中的文件夹
    private void removeFolder(final int position,
                              final AsyncCallback<FolderObserver> callback)
    {
        final FolderObserver currentFolder = getSelect();
        final FolderObserver folderObserver = folderListAdapter.getData().get(position);
        host.getDataManager().removeFolderAsync(folderObserver,
                new AsyncCallback<FolderObserver>()
                {
                    @Override
                    public void onResult(FolderObserver folderObserver)
                    {
                        if (folderObserver.equals(currentFolder))
                        {
                            selectAbstract(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        }
                        folderListAdapter.remove(position);
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, folderObserver);
                    }
                });
    }

    private void recoveryFolder(final int position,
                              final AsyncCallback<FolderObserver> callback)
    {
        final FolderObserver currentFolder = getSelect();
        final FolderObserver folderObserver = folderListAdapter.getData().get(position);
        host.getDataManager().recoveryFolderAsync(folderObserver,
                new AsyncCallback<FolderObserver>()
                {
                    @Override
                    public void onResult(FolderObserver folderObserver)
                    {
                        if (folderObserver.equals(currentFolder))
                        {
                            selectAbstract(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        }
                        folderListAdapter.remove(position);
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, folderObserver);
                    }
                });
    }

    private void updateFolder(final int position,
                              String name,
                              final AsyncCallback<FolderObserver> callback)
    {
        FolderObserver folderObserver = folderListAdapter.getData().get(position);
        host.getDataManager().updateFolderAsync(folderObserver, name,
                new AsyncCallback<FolderObserver>()
                {
                    @Override
                    public void onResult(FolderObserver result)
                    {
                        folderListAdapter.notifyItemChanged(position
                                + folderListAdapter.getHeaderLayoutCount());
                        folderNoteCountViewUpdate(EntityTypeHelper.ALL_NORMAL_FOLDER);
                        CheckUtil.safeCallback(callback, result);
                    }
                });
    }

    private void addFolder(String name, final AsyncCallback<FolderObserver> callback)
    {
        host.getDataManager().createNewFolderAsync(name,
                new AsyncCallback<FolderObserver>()
                {
                    @Override
                    public void onResult(FolderObserver handler)
                    {
                        folderListAdapter.addData(handler);
                        CheckUtil.safeCallback(callback, handler);
                    }
                });
    }

    private boolean isEditMode()
    {
        return folderListAdapter.getCurrentState()
                .getClass()
                .equals(FolderListAdapter.EditState.class);
    }

    private void selectNormal(int position)
    {
        noSelect();
        FolderObserver folderObserver = folderListAdapter.getData().get(position);
        if (folderObserver.equals(getSelect()))
        {
            host.displayFolderNotes(null);
        } else
        {
            if (isEditMode())
            {
                enableEditModeViewUpdate(false);
            }
            folderListAdapter.setSelect(position);
            folderListAdapter.notifyItemChanged(position
                    + folderListAdapter.getHeaderLayoutCount());
            host.displayFolderNotes(folderObserver);
            host.getUserSettings().selectFolderId = folderObserver.getId();
            AppSettingManager.setUserSettings(host.getUserSettings());
        }
    }

    private void selectAbstractNoCheck(@NonNull FolderObserver folderObserver)
    {
        if (EntityTypeHelper.isAllNormalFolder(folderObserver))
        {
            noSelect();
            abstractSelect = folderObserver;
            selectAllFolderViewUpdate(true);
            host.displayFolderNotes(folderObserver);
            host.getUserSettings().selectFolderId = folderObserver.getId();
            AppSettingManager.setUserSettings(host.getUserSettings());
        } else if (EntityTypeHelper.isRecoveryFolder(folderObserver))
        {
            noSelect();
            abstractSelect = folderObserver;
            selectRecoveryFolderViewUpdate(true);
            host.displayFolderNotes(folderObserver);
            host.getUserSettings().selectFolderId = folderObserver.getId();
            AppSettingManager.setUserSettings(host.getUserSettings());
        } else if (EntityTypeHelper.isPrivateFolder(folderObserver))
        {
            LockQuickWindow.openLockWindow(getContext(), host.getUserSettings().lockPassword, this);
        } else if (EntityTypeHelper.isNetworkFolder(folderObserver))
        {
            noSelect();
            abstractSelect = folderObserver;
            selectNetworkFolderViewUpdate(true);
            host.displayFolderNotes(folderObserver);
        }
    }

    private void selectAbstract(@NonNull FolderObserver folderObserver)
    {
        if (EntityTypeHelper.isAbstractFolder(folderObserver))
        {
            if (folderObserver.equals(getSelect()))
            {
                host.displayFolderNotes(null);
                return;
            }
            if (isEditMode())
            {
                enableEditModeViewUpdate(false);
            }
            selectAbstractNoCheck(folderObserver);
        } else
        {
            throw new IllegalArgumentException();
        }
    }

    private void noSelect()
    {
        if (folderListAdapter.getSelect() != FolderListAdapter.NO_SELECT)
        {
            folderListAdapter.setSelect(FolderListAdapter.NO_SELECT);
        } else
        {
            FolderObserver folderObserver = getSelect();
            if (EntityTypeHelper.isAbstractFolder(folderObserver))
            {
                if (EntityTypeHelper.isAllNormalFolder(folderObserver))
                {
                    selectAllFolderViewUpdate(false);
                } else if (EntityTypeHelper.isPrivateFolder(folderObserver))
                {
                    selectPrivateFolderViewUpdate(false);
                } else if (EntityTypeHelper.isRecoveryFolder(folderObserver))
                {
                    selectRecoveryFolderViewUpdate(false);
                } else if (EntityTypeHelper.isNetworkFolder(folderObserver))
                {
                    selectNetworkFolderViewUpdate(false);
                }
                abstractSelect = null;
            } else
            {
                throw new IllegalArgumentException();
            }
        }
    }

    private boolean checkNewName(String newName)
    {
        for (FolderObserver handler : folderListAdapter.getData())
        {
            if (handler.getName().equals(newName) || TextUtils.isEmpty(newName))
            {
                return false;
            }
        }
        return true;
    }

    //--------------- dialogs ----------------------------------------------

    private void displayDeleteFolderDialog(final int position)
    {
        final FolderObserver folderObserver = folderListAdapter.getData().get(position);
        final boolean isRecovery = host.getUserSettings().isUseRecovery;
        host.getAlertDialog().display("警告", isRecovery ?
                        "删除文件夹会将文件夹下包括私密便签的所有便签移动到回收站" :
                        "删除文件夹会删除文件夹下包括私密便签的所有便签",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                            {
                                host.getProgressDialog().display("删除中...");
                                AsyncCallback<FolderObserver> callback
                                        = new AsyncCallback<FolderObserver>()
                                {
                                    @Override
                                    public void onResult(FolderObserver folderObserver)
                                    {
                                        host.getProgressDialog().finish();
                                    }
                                };
                                if (isRecovery)
                                {
                                    recoveryFolder(position, callback);
                                } else
                                {
                                    removeFolder(position, callback);
                                }
                            }
                        }
                    }
                });
    }

    private void displayCreateFolderDialog()
    {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.include_main_folder_rename, null);
        final EditText editText = (EditText) v.findViewById(R.id.et_folder_rename);
        Logger.d("创建新文件夹");
        new AlertDialog.Builder(getActivity())
                .setTitle("创建新文件夹")
                .setView(v)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                final String newName = editText.getText().toString();
                                if (checkNewName(newName))
                                {
                                    host.getProgressDialog().display("创建中...");
                                    addFolder(newName, new AsyncCallback<FolderObserver>()
                                    {
                                        @Override
                                        public void onResult(FolderObserver handler)
                                        {
                                            host.getProgressDialog().finish();
                                        }
                                    });
                                } else
                                {
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            ToastUtils.showShort("文件夹名字不能重复或为空");
                                        }
                                    }, 100);
                                }
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }

    private void displayFolderRenameDialog(final int position)
    {
        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.include_main_folder_rename, null);
        final EditText editText = (EditText) v.findViewById(R.id.et_folder_rename);
        Logger.d("修改名字");
        new AlertDialog.Builder(getActivity())
                .setTitle("修改文件夹名字")
                .setView(v)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                String newName = editText.getText().toString();
                                if (checkNewName(newName))
                                {
                                    host.getProgressDialog().display("更改中...");
                                    updateFolder(position,
                                            editText.getText()
                                                    .toString(),
                                            new AsyncCallback<FolderObserver>()
                                            {
                                                @Override
                                                public void onResult(FolderObserver handler)
                                                {
                                                    host.getProgressDialog().finish();
                                                }
                                            });
                                } else
                                {
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            ToastUtils.showShort("文件夹名字不能重复或为空");
                                        }
                                    }, 100);
                                }
                            }
                        })
                .setNegativeButton("取消", null)
                .show();
    }
}
