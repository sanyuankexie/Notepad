package studio.microworld.hypernote.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import studio.microworld.hypernote.R;
import studio.microworld.hypernote.RC;
import studio.microworld.hypernote.support.adapter.BottomBarAdapter;
import studio.microworld.hypernote.support.adapter.NoteListAdapter;
import studio.microworld.hypernote.support.pref.UserSettings;
import studio.microworld.hypernote.support.managmanet.AppSettingManager;
import studio.microworld.hypernote.support.managmanet.EntityTypeHelper;
import studio.microworld.hypernote.support.managmanet.NoteDataManager;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.utlis.AsyncCallback;
import studio.microworld.hypernote.support.utlis.DrawableUtil;
import studio.microworld.hypernote.support.widget.AlertDialogHelper;
import studio.microworld.hypernote.support.widget.ProgressDialogHelper;
import studio.microworld.hypernote.support.framework.BaseActivity;
import studio.microworld.hypernote.ui.editor.EditOptions;
import studio.microworld.hypernote.ui.editor.EditorActivity;
import studio.microworld.hypernote.ui.qrcode.QRScanActivity;

/**
 * Created by Mr.小世界 on 2018/9/18.
 */

public final class HostActivity
        extends BaseActivity
        implements View.OnClickListener,
        HostBinder,
        SearchView.OnQueryTextListener,
        SearchView.OnCloseListener,
        MenuItemCompat.OnActionExpandListener,
        DialogInterface.OnDismissListener
{

    //-------------------- Bind View -----------------------------
    @BindView(R.id.rv_note_list)
    RecyclerView mRvNoteList;   // 便签列表

    @BindView(R.id.tv_note_list_to_privacy)
    TextView mTvToPrivacy;  // 设为私密

    @BindView(R.id.tv_note_list_delete)
    TextView mTvDelete;   // 删除

    @BindView(R.id.tv_note_list_move)
    TextView mTvMove;   // 移动

    @BindView(R.id.fab_note_list_add)
    FloatingActionButton mFabAdd;  // 添加便签

    @BindView(R.id.rl_note_list_bottom_bar)
    RelativeLayout mRlBottomBar;   // 多选操作的bottomBar

    @BindView(R.id.root_layout)
    DrawerLayout mDrawer;

    private LinearLayout mLoading;
    private TextView mEmpty;


    private MenuItem mSearchMenu;
    private MenuItem mShowModeMenu;
    private MenuItem mCheckAllMenu;
    private MenuItem mQRScanMenu;

    //------------------ view helper-------------------------------------------

    private ProgressDialogHelper progressDialogHelper = new ProgressDialogHelper(this);

    private AlertDialogHelper alertDialogHelper = new AlertDialogHelper(this);

    //--------------- activity data ---------------------------------

    private enum StateType
    {
        Normal,
        MultiSelect,
        Loading,
        Search,
    }

    public static final int OBJECT_ANIMATION_TIME = 400;

    private StateType currentState = StateType.Normal;

    private UserSettings userSettings;

    private List<NoteObserver> searchCacheList;

    private NoteListAdapter noteListAdapter;

    private NoteDataManager noteDataManager = NoteDataManager.getInstance();

    private BottomBarAdapter bottomBarAdapter;

    private NavigationBinder navigation;

    //------------------ Base method --------------------------------------

    @Override
    protected void onLoadActionBar(ActionBar mActionBar)
    {
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_format_list_bulleted_white_24dp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main_toolbar, menu);
        //---------------------------------------------------------
        mSearchMenu = menu.findItem(R.id.menu_note_search);
        SearchView searchView = (SearchView)
                MenuItemCompat.getActionView(mSearchMenu);
        searchView.setSubmitButtonEnabled(true);//显示提交按钮
        searchView.setQueryHint(getString(R.string.main_search));//设置默认无内容时的文字提示
        SearchView.SearchAutoComplete mSearchAutoComplete
                = (SearchView.SearchAutoComplete) searchView
                .findViewById(R.id.search_src_text);
        mSearchAutoComplete.setHintTextColor(getResources()
                .getColor(android.R.color.white));//设置提示文字颜色
        mSearchAutoComplete.setTextColor(getResources()
                .getColor(android.R.color.white));//设置内容文字颜色
        searchView.setOnQueryTextListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            MenuItemCompat.setOnActionExpandListener(mSearchMenu, this);
        } else
        {
            searchView.setOnCloseListener(this);
        }
        //----------------------------------------------------

        mShowModeMenu = menu.findItem(R.id.menu_note_show_mode);
        layoutStyleViewUpdate(userSettings.noteListStyle);

        //------------------------------------------------------

        mQRScanMenu = menu.findItem(R.id.menu_qrcode);

        //----------------------------------------------------

        mCheckAllMenu = menu.findItem(R.id.menu_note_check_all);

        //---------------------------------------------------
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (currentState == StateType.MultiSelect)
        {
            mQRScanMenu.setVisible(false);
            mSearchMenu.setVisible(false);
            mShowModeMenu.setVisible(false);
            mCheckAllMenu.setVisible(true);
            mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);

        } else
        {
            mSearchMenu.setVisible(true);
            mShowModeMenu.setVisible(true);
            mCheckAllMenu.setVisible(false);
            mQRScanMenu.setVisible(true);
            mToolbar.setNavigationIcon(R.drawable.ic_format_list_bulleted_white_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_note_show_mode:
            {
                changeLayoutStyleViewUpdate();
            }
            break;
            case R.id.menu_note_check_all:
            {
                changeSelectAllNotesViewUpdate();
            }
            break;
            case R.id.menu_note_search:
            {
                searchViewUpdate(true);
            }
            break;
            case R.id.menu_qrcode:
            {
                QRScanActivity.openQRScanForResult(this);
            }
            break;
            case android.R.id.home:
            {
                if (currentState == StateType.Normal)
                {
                    mDrawer.openDrawer(GravityCompat.START);
                } else if (currentState == StateType.MultiSelect)
                {
                    multiSelectViewUpdate(false);
                }
            }
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onLoadLayoutAfter()
    {
        loadUserSetting();
        loadAdapter();
        loadRecyclerView();
    }

    @Override
    protected int onLoadLayout()
    {
        return R.layout.activity_main;
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            //点击添加按钮跳转到Note Editor
            case R.id.fab_note_list_add:
            {
                EditOptions config = new EditOptions();
                config.actionType = EditOptions.TYPE_ADD;
                config.noteId = noteDataManager.getNextDatabaseNoteId();
                EditorActivity.openEditor(this, config);
            }
            break;
            //点击底部栏的删除按钮显示删除对话框
            case R.id.tv_note_list_delete:
            {
                displayMultiSelectRemoveDialog();
            }
            break;
            case R.id.tv_note_list_move:
            {
                if (EntityTypeHelper.isRecoveryFolder(navigation.getSelect()))
                {
                    //若是废纸篓,则为还原按键
                    displayMultiSelectReductionDialog();
                } else
                {
                    //否则为移动按键
                    displayMultiSelectMoveView();
                }
            }
            break;
            case R.id.tv_note_list_to_privacy:
            {
                //加密/解密按键
                displayMultiSelectPrivateDialog();
            }
            break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (isDrawerViewOpen())
        {//　侧滑菜单已打开
            drawerViewUpdate(false);
        } else if (currentState == StateType.MultiSelect)
        {//　已显示多选菜单
            multiSelectViewUpdate(false);
        } else
        {// 默认
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Logger.d("" + requestCode + " " + resultCode);
        switch (requestCode)
        {
            //只有MainActivity保存了实体类型和绑定访问器
            //所以所有编辑的apply操作都必须返回MainActivity进行
            case RC.request_code.REQUEST_CODE_TO_NOTE:
            {
                if (resultCode == RESULT_OK)
                {
                    onEditorResult((EditOptions)
                            data.getSerializableExtra(RC.activity_note_editor.OPTIONS_KEY));
                }
            }
            break;
            case RC.request_code.REQUEST_CODE_TO_QRSCANER:
            {
                if (resultCode == RESULT_OK)
                {
                    String id = data.getStringExtra(RC.activity_qrscan.NOTE_URL_PROTOCOL);
                }
            }
            break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public HostBinder bindNavigation(NavigationBinder navigation)
    {
        this.navigation = navigation;
        mDrawer.addDrawerListener(navigation);
        return this;
    }

    @Override
    public void displayFolderNotes(final FolderObserver folderObserver)
    {
        if (folderObserver == null)
        {
            drawerViewUpdate(false);
            return;
        }
        if (EntityTypeHelper.isRecoveryFolder(folderObserver))
        {
            floatButtonViewUpdate(false);
        } else
        {
            floatButtonViewUpdate(true);
        }
        lockDrawerViewUpdate(false);
        loadingFolderViewUpdate(true);
        setTitle(folderObserver.getName());
        noteDataManager.loadNotesByFolderAsync(folderObserver, new AsyncCallback<List<NoteObserver>>()
        {
            @Override
            public void onResult(List<NoteObserver> noteObservers)
            {
                if (noteObservers != null)
                {
                    setDataNoteToViewUpdate(noteObservers);
                } else
                {
                    setDataNoteToViewUpdate(Collections.EMPTY_LIST);
                    Logger.d(navigation.getSelect().getName());
                }
                loadingFolderViewUpdate(false);
                lockDrawerViewUpdate(true);
            }
        });
    }

    @Override
    public NoteDataManager getDataManager()
    {
        return noteDataManager;
    }

    @Override
    public AlertDialogHelper getAlertDialog()
    {
        return alertDialogHelper;
    }

    @Override
    public ProgressDialogHelper getProgressDialog()
    {
        return progressDialogHelper;
    }

    @Override
    public UserSettings getUserSettings()
    {
        return userSettings;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item)
    {
        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item)
    {
        return onClose();
    }

    @Override
    public boolean onClose()
    {
        searchViewUpdate(false);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        if (currentState == StateType.Search)
        {

            if (TextUtils.isEmpty(newText))
            {
                noteListAdapter.setNewData(searchCacheList);
            } else
            {
                String lowerCaseQuery = newText.toLowerCase();
                List<NoteObserver> list = new ArrayList<>();
                //　此处使用倒叙进行检索，这样搜索出来的顺序是正序
                for (int i = searchCacheList.size() - 1; i >= 0; i--)
                {
                    if (searchCacheList.get(i).getContent().toLowerCase().contains(lowerCaseQuery))
                    {
                        list.add(searchCacheList.get(i));
                    }
                }
                noteListAdapter.setNewData(list);
                Logger.d(newText);
            }
        }
        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        bottomBarAdapter.getData().clear();
        bottomBarAdapter.setOnItemClickListener(null);
    }

    //--------------------------load-----------------------------------

    private View loadEmptyView()
    {
        View empty = LayoutInflater.from(this).inflate(R.layout.include_main_empty, null, false);
        mEmpty = (TextView) empty.findViewById(R.id.tv_recycler_view_empty);
        mLoading = (LinearLayout) empty.findViewById(R.id.ll_loading);
        return empty;
    }

    private void loadUserSetting()
    {
        userSettings = (UserSettings) getIntent()
                .getSerializableExtra(getString(R.string.user_setting_key));
    }

    private void loadAdapter()
    {
        bottomBarAdapter = new BottomBarAdapter();
        noteListAdapter = new NoteListAdapter();
        noteListAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener()
        {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter,
                                         View view, int position)
            {
                //先判断是否为多选状态,是多选则多选
                if (currentState == StateType.MultiSelect)
                {
                    if (noteListAdapter.getIsSelect(position))
                    {
                        selectNoteViewUpdate(position, false);
                    } else
                    {
                        selectNoteViewUpdate(position, true);
                    }
                }//若查看回收站,便签不可打开需要恢复
                else if (EntityTypeHelper.isRecoveryFolder(navigation.getSelect()))
                {
                    displaySingleClickReductionDialog(((NoteListAdapter) adapter)
                            .getData()
                            .get(position));
                } else
                {
                    openNoteToEdit(position);
                }
            }
        });
        noteListAdapter.setOnItemChildLongClickListener(new BaseQuickAdapter.OnItemChildLongClickListener()
        {
            @Override
            public boolean onItemChildLongClick(BaseQuickAdapter adapter,
                                                View view, int position)
            {
                //搜索模式下不允许长按事件
                if (currentState != StateType.Search)
                {
                    multiSelectViewUpdate(true);
                    // 选中当前便签并刷新
                    selectNoteViewUpdate(position, true);
                }
                return true;
            }
        });
        noteListAdapter.setEmptyView(loadEmptyView());
    }

    private void loadRecyclerView()
    {
        mRvNoteList.setAdapter(noteListAdapter);
        if (userSettings.noteListStyle == NoteListAdapter.LINER_LAYOUT)
        {
            noteListAdapter.setLayout(userSettings.noteListStyle);
            mRvNoteList.setLayoutManager(new LinearLayoutManager(this));
        } else
        {
            noteListAdapter.setLayout(userSettings.noteListStyle);
            mRvNoteList.setLayoutManager(new StaggeredGridLayoutManager(2,
                    StaggeredGridLayoutManager.VERTICAL));
        }
        noteListAdapter.notifyDataSetChanged();
    }

    //---------------------- update view ---------------------------------

    public void lockDrawerViewUpdate(boolean lock)
    {
        if (lock)
        {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        } else
        {
            drawerViewUpdate(false);
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED); //关闭手势滑动
        }
    }

    public void loadingFolderViewUpdate(boolean enable)
    {
        if (enable)
        {
            currentState = StateType.Loading;
            noteListAdapter.getData().clear();
            noteListAdapter.notifyDataSetChanged();
            mEmpty.setVisibility(View.GONE);
            mLoading.setVisibility(View.VISIBLE);
        } else
        {
            currentState = StateType.Normal;
            mEmpty.setVisibility(View.VISIBLE);
            mLoading.setVisibility(View.GONE);
        }
    }

    public void searchViewUpdate(boolean enable)
    {
        if (enable && currentState == StateType.Normal)
        {
            currentState = StateType.Search;
            mShowModeMenu.setVisible(false);
            mQRScanMenu.setVisible(false);
            if (searchCacheList == null)
            {
                searchCacheList = new ArrayList<>();
            }
            searchCacheList.addAll(noteListAdapter.getData());
            floatButtonViewUpdate(false);
            lockDrawerViewUpdate(false);
        } else if (!enable && currentState == StateType.Search)
        {
            currentState = StateType.Normal;
            mShowModeMenu.setVisible(true);
            mQRScanMenu.setVisible(true);
            if (searchCacheList != null)
            {
                noteListAdapter.setNewData(searchCacheList);
                searchCacheList = null;
            }
            floatButtonViewUpdate(true);
            lockDrawerViewUpdate(true);
        }
    }

    public void toolbarItemViewUpdate()
    {
        // 更新toolbar菜单，
        // 系统会去调用onPrepareOptionsMenu（MenuItem item)方法
        supportInvalidateOptionsMenu();
    }

    public void multiSelectViewUpdate(boolean enable)
    {
        if (enable)
        {
            if (currentState == StateType.Normal)
            {
                currentState = StateType.MultiSelect;

                noteListAdapter.setMultiSelectEnable(true);

                // 更新toolbar菜单，
                toolbarItemViewUpdate();
                // 隐藏添加按钮
                floatButtonViewUpdate(false);
                // 显示BottomBar
                bottomBarViewUpdate(true);

                lockDrawerViewUpdate(false);

            }
        } else
        {
            if (currentState == StateType.MultiSelect)
            {
                currentState = StateType.Normal;

                noteListAdapter.selectAll(false);

                noteListAdapter.setMultiSelectEnable(false);
                // 更新toolbar菜单，
                toolbarItemViewUpdate();
                // 显示添加按钮
                floatButtonViewUpdate(true);
                // 显示BottomBar
                bottomBarViewUpdate(false);

                lockDrawerViewUpdate(true);

                setTitle(navigation.getSelect().getName());
            }
        }
    }

    private final AnimatorListenerAdapter[] animatorListeners
            = new AnimatorListenerAdapter[]{
            new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationStart(Animator animation)
                {

                }

                @Override
                public void onAnimationEnd(Animator animation)
                {
                    DrawableUtil.setTextButtonEnabled(mTvDelete, true);
                    DrawableUtil.setTextButtonEnabled(mTvMove, true);
                    DrawableUtil.setTextButtonEnabled(mTvToPrivacy, true);
                    switch (navigation.getSelect().getId())
                    {
                        case EntityTypeHelper.NETWORK_FOLDER_ID:
                        {

                        }
                        break;
                        case EntityTypeHelper.PRIVATE_FOLDER_ID:
                        {
                            mTvToPrivacy.setText("移除私密");
                            mTvDelete.setText("删除");
                            mTvToPrivacy.setVisibility(View.VISIBLE);
                            mTvDelete.setVisibility(View.VISIBLE);
                            mTvMove.setVisibility(View.GONE);
                        }
                        break;
                        case EntityTypeHelper.RECOVERY_FOLDER_ID:
                        {
                            mTvDelete.setText("删除");
                            mTvMove.setText("恢复");
                            mTvToPrivacy.setVisibility(View.GONE);
                            mTvDelete.setVisibility(View.VISIBLE);
                            mTvMove.setVisibility(View.VISIBLE);
                        }
                        break;
                        default:
                        {
                            mTvToPrivacy.setText("设为私密");
                            mTvDelete.setText("删除");
                            mTvMove.setText("移动");
                            mTvToPrivacy.setVisibility(View.VISIBLE);
                            mTvDelete.setVisibility(View.VISIBLE);
                            mTvMove.setVisibility(View.VISIBLE);
                        }
                        break;
                    }
                }
            },
            new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mRlBottomBar.setVisibility(View.GONE);
                }
            },
            new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mFabAdd.setEnabled(true);
                }
            },
            new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mFabAdd.setVisibility(View.GONE);
                }
            }
    };

    public void bottomBarViewUpdate(boolean enable)
    {
        if (enable)
        {
            mRlBottomBar.setVisibility(View.VISIBLE);
            // bottom bar进行一个上移的动画
            ObjectAnimator animator = ObjectAnimator
                    .ofFloat(mRlBottomBar, "translationY", SizeUtils.dp2px(56), 0);
            animator.setDuration(OBJECT_ANIMATION_TIME);
            animator.addListener(animatorListeners[0]);
            animator.start();
        } else
        {
            // 下移动画
            DrawableUtil.setTextButtonEnabled(mTvDelete, false);
            DrawableUtil.setTextButtonEnabled(mTvMove, false);
            DrawableUtil.setTextButtonEnabled(mTvToPrivacy, false);
            ObjectAnimator animator = ObjectAnimator
                    .ofFloat(mRlBottomBar, "translationY", SizeUtils.dp2px(56));
            animator.setDuration(OBJECT_ANIMATION_TIME);
            animator.addListener(animatorListeners[1]);
            animator.start();
        }
    }

    public void floatButtonViewUpdate(boolean enable)
    {
        if (enable && !EntityTypeHelper.isRecoveryFolder(navigation.getSelect()))
        {
            if (mFabAdd.getVisibility() == View.GONE)
            {
                mFabAdd.setVisibility(View.VISIBLE);
                ObjectAnimator animator = ObjectAnimator
                        .ofFloat(mFabAdd, "translationY", SizeUtils.dp2px(0));
                animator.addListener(animatorListeners[2]);
                animator.setDuration(OBJECT_ANIMATION_TIME);
                animator.start();
            }
        } else
        {
            if (mFabAdd.getVisibility() == View.VISIBLE)
            {
                ObjectAnimator animator = ObjectAnimator
                        .ofFloat(mFabAdd, "translationY", SizeUtils.dp2px(80));
                animator.setDuration(OBJECT_ANIMATION_TIME);
                animator.addListener(animatorListeners[3]);
                animator.start();
            }
        }
    }

    public void layoutStyleViewUpdate(int style)
    {
        switch (style)
        {
            case NoteListAdapter.GRID_LAYOUT:
            {
                userSettings.noteListStyle = style;
                noteListAdapter.setLayout(style);
                mRvNoteList.setLayoutManager(
                        new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
                mShowModeMenu.setIcon(getResources()
                        .getDrawable(R.drawable.ic_border_all_white_24dp));
                AppSettingManager.setUserSettings(userSettings);
                noteListAdapter.notifyDataSetChanged();
            }
            break;
            case NoteListAdapter.LINER_LAYOUT:
            {
                userSettings.noteListStyle = style;
                noteListAdapter.setLayout(style);
                mRvNoteList.setLayoutManager(new LinearLayoutManager(this));
                mShowModeMenu.setIcon(getResources()
                        .getDrawable(R.drawable.ic_format_list_bulleted_white_24dp));
                AppSettingManager.setUserSettings(userSettings);
                noteListAdapter.notifyDataSetChanged();
            }
            break;
        }
    }

    public void changeLayoutStyleViewUpdate()
    {
        if (userSettings.noteListStyle == NoteListAdapter.LINER_LAYOUT)
        {
            Logger.d("grid --> linear");
            layoutStyleViewUpdate(NoteListAdapter.GRID_LAYOUT);
        } else
        {
            Logger.d("linear --> grid");
            layoutStyleViewUpdate(NoteListAdapter.LINER_LAYOUT);
        }
    }

    //选择便签并更新视图
    private void selectNoteViewUpdate(int position, boolean select)
    {
        noteListAdapter.selectItem(position, select);
        multiSelectCountViewUpdate(noteListAdapter.getSelectCount());
    }

    void multiSelectCountViewUpdate(int count)
    {
        setTitle("已选择: " + count);
    }

    private void addNoteToViewUpdate(NoteObserver handler)
    {
        noteListAdapter.addData(handler);
        mRvNoteList.scrollToPosition(0);
    }

    private void changeNoteViewUpdate(NoteObserver handler)
    {
        noteListAdapter.notifyItemChanged(noteListAdapter
                .getData().indexOf(handler));
    }

    private void removeNotesViewUpdate(List<NoteObserver> handlers)
    {
        Logger.d(handlers.size());
        for (NoteObserver handler : handlers)
        {
            noteListAdapter.remove(noteListAdapter.getData().indexOf(handler));
        }
    }

    void setDataNoteToViewUpdate(List<NoteObserver> list)
    {
        noteListAdapter.setNewData(list);
    }

    void changeSelectAllNotesViewUpdate()
    {
        if (noteListAdapter.isSelectAll())
        {
            noteListAdapter.selectAll(false);
            multiSelectCountViewUpdate(0);
        } else
        {
            noteListAdapter.selectAll(true);
            multiSelectCountViewUpdate(noteListAdapter.getData().size());
        }
    }

    public void drawerViewUpdate(boolean enable)
    {
        if (enable)
        {
            mDrawer.openDrawer(GravityCompat.START);
        } else
        {
            mDrawer.closeDrawer(GravityCompat.START);
        }
    }


    //--------------------------dialogs ----------------------------------

    private final AsyncCallback<List<NoteObserver>> removeNotesCallback
            = new AsyncCallback<List<NoteObserver>>()
    {
        @Override
        public void onResult(List<NoteObserver> handlers)
        {
            if (handlers != null)
            {
                removeNotesViewUpdate(handlers);
            }
            progressDialogHelper.finish();
        }
    };

    //显示多选删除对话框
    private void displayMultiSelectRemoveDialog()
    {
        final DialogInterface.OnClickListener removeListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                    {
                        progressDialogHelper.display("删除中...");
                        List<NoteObserver> select = noteListAdapter.getSelectedItems();
                        multiSelectViewUpdate(false);
                        navigation.removeNotes(
                                select,
                                removeNotesCallback);
                    }
                    break;
                }
            }
        };
        DialogInterface.OnClickListener recoveryListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch (which)
                {
                    case DialogInterface.BUTTON_POSITIVE:
                    {
                        if (!EntityTypeHelper.isRecoveryFolder(navigation.getSelect()))
                        {
                            progressDialogHelper.display("正在移动到回收站...");
                            List<NoteObserver> select = noteListAdapter.getSelectedItems();
                            multiSelectViewUpdate(false);
                            navigation.recoveryNotes(
                                    select,
                                    removeNotesCallback);
                        } else
                        {
                            removeListener.onClick(dialog, which);
                        }
                    }
                    break;
                }
            }
        };
        alertDialogHelper.display("删除便签",
                "确定删除选中的便签吗？", userSettings.isUseRecovery ? recoveryListener : removeListener);
    }

    //显示加密/解密对话框
    private void displayMultiSelectPrivateDialog()
    {
        //私密-->解密
        if (EntityTypeHelper.isPrivateFolder(navigation.getSelect()))
        {
            alertDialogHelper.display("移除加密",
                    "把这些便签移除加密",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                {
                                    progressDialogHelper.display("移动中...");
                                    List<NoteObserver> select = noteListAdapter.getSelectedItems();
                                    multiSelectViewUpdate(false);
                                    navigation.unlockNotes(select,
                                            removeNotesCallback);
                                }
                                break;
                            }
                        }
                    });
        }//普通-->加密
        else if (EntityTypeHelper.isAllNormalFolder(navigation.getSelect())
                || EntityTypeHelper.isDatabeseFolder(navigation.getSelect()))
        {
            alertDialogHelper.display("加密",
                    "加密这些便签",
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                {
                                    progressDialogHelper.display("移动中...");
                                    List<NoteObserver> select = noteListAdapter.getSelectedItems();
                                    multiSelectViewUpdate(false);
                                    navigation.lockNotes(select,
                                            removeNotesCallback);
                                }
                                break;
                            }
                        }
                    });
        }
    }

    //显示还原对话框
    private void displaySingleClickReductionDialog(final NoteObserver noteHandler)
    {
        alertDialogHelper.display("恢复此便签",
                "无法直接打开便签，是否恢复至原有便签夹？",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                            {
                                progressDialogHelper.display("移动中...");
                                List<NoteObserver> arrayList = new ArrayList<>();
                                arrayList.add(noteHandler);
                                navigation.reductionNotes(arrayList,
                                        removeNotesCallback);
                            }
                            break;
                        }
                    }
                });
    }

    //显示多选还原对话框
    private void displayMultiSelectReductionDialog()
    {
        alertDialogHelper.display("还原",
                "是否还原这些便签？",
                new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        switch (which)
                        {
                            case DialogInterface.BUTTON_POSITIVE:
                            {
                                progressDialogHelper.display("还原中...");
                                //bottomBarViewUpdate(false);
                                List<NoteObserver> select = noteListAdapter.getSelectedItems();
                                multiSelectViewUpdate(false);
                                navigation.reductionNotes(select, removeNotesCallback);
                            }
                        }
                    }
                });
    }

    //显示底部信息
    private void displayBottomMessage(String message)
    {
        Snackbar.make(mFabAdd, message,
                Snackbar.LENGTH_SHORT).setAction("Action", null).show();
    }

    //显示多选移动选择
    private void displayMultiSelectMoveView()
    {
        final BottomSheetDialog dialog
                = new BottomSheetDialog(this);
        // 获取contentView
        ViewGroup contentView =
                (ViewGroup) ((ViewGroup) findViewById(android.R.id.content))
                        .getChildAt(0);
        View root = LayoutInflater.from(this)
                .inflate(R.layout.include_main_bottom_sheet_folder,
                        contentView, false);
        RecyclerView recyclerView
                = (RecyclerView) root.findViewById(R.id.recycler_bottom_sheet_folder);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        dialog.setContentView(root);
        FolderObserver currentFolder = navigation.getSelect();
        List<FolderObserver> selectItem = navigation.getFolderList();
        selectItem.remove(currentFolder);
        bottomBarAdapter.setNewData(selectItem);
        recyclerView.setAdapter(bottomBarAdapter);
        dialog.setOnDismissListener(this);
        bottomBarAdapter.setOnItemClickListener(
                new BottomBarAdapter.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position)
                    {
                        progressDialogHelper.display("移动中...");
                        final FolderObserver folder
                                = bottomBarAdapter.getData().get(position);
                        List<NoteObserver> select = noteListAdapter.getSelectedItems();
                        multiSelectViewUpdate(false);
                        navigation.moveNotes(select, folder,
                                new AsyncCallback<List<NoteObserver>>()
                                {
                                    @Override
                                    public void onResult(List<NoteObserver> handlers)
                                    {
                                        if (handlers != null)
                                        {
                                            if (!EntityTypeHelper.isAllNormalFolder(navigation.getSelect()))
                                            {
                                                removeNotesViewUpdate(handlers);
                                            }
                                            displayBottomMessage("已将" + handlers.size()
                                                    + "条便签移动到"
                                                    + folder.getName()
                                            );
                                        }
                                        progressDialogHelper.finish();
                                    }
                                });
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    //-----------------------internal action-------------------------

    private void onEditorResult(EditOptions options)
    {
        progressDialogHelper.display("保存中...");
        if (options.actionType == EditOptions.TYPE_ADD)
        {
            navigation.createNewNote(options.title,
                    options.content,
                    new AsyncCallback<NoteObserver>()
                    {
                        @Override
                        public void onResult(NoteObserver handler)
                        {
                            if (handler != null)
                            {
                                addNoteToViewUpdate(handler);
                            }
                            progressDialogHelper.finish();
                        }
                    });
        } else
        {
            NoteObserver handler = noteListAdapter.getData().get(options.position);
            noteDataManager.updateNoteAsync(handler,
                    options.title,
                    options.content,
                    new AsyncCallback<NoteObserver>()
                    {
                        @Override
                        public void onResult(NoteObserver noteHandler)
                        {
                            if (noteHandler != null)
                            {
                                changeNoteViewUpdate(noteHandler);
                            }
                            progressDialogHelper.finish();
                        }
                    });
        }
    }

    private void openNoteToEdit(int position)
    {
        EditOptions config = new EditOptions();
        config.actionType = EditOptions.TYPE_UPDATE;
        NoteObserver handler = noteListAdapter.getData().get(position);
        config.title = handler.getTitle();
        config.content = handler.getContent();
        config.noteId = handler.getId();
        config.position = position;
        config.createdAt = handler.getCreatedAt();
        config.updatedAt = handler.getUpdatedAt();
        EditorActivity.openEditor(HostActivity.this, config);
    }

    private boolean isDrawerViewOpen()
    {
        return mDrawer.isDrawerOpen(GravityCompat.START);
    }
}
