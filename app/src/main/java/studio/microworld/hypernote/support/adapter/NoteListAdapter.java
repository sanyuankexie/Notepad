package studio.microworld.hypernote.support.adapter;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.Locale;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.managmanet.EntityTypeHelper;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.utlis.DateUtil;

/**
 * Created by Mr.小世界 on 2018/9/18.
 */

public final class NoteListAdapter extends AbstractNoteListAdapter
{

    public NoteListAdapter()
    {
        super();
        registerState(new LinearState());
        registerState(new GridState());
        registerState(new LinearMultiSelectState());
        registerState(new GridMultiSelectState());
    }

    public static final int LINER_LAYOUT = 0;

    public static final int GRID_LAYOUT = 1;

    private final SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("MM月", Locale.CHINA);

    private final SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy年MM月", Locale.CHINA);

    public void setLayout(int layout)
    {
        Class stateType = getCurrentState().getClass();
        if (layout == LINER_LAYOUT)
        {
            if (GridState.class.equals(stateType))
            {
                changeState(LinearState.class);
            } else if (GridMultiSelectState.class.equals(stateType))
            {
                changeState(LinearMultiSelectState.class);
            }
        } else if (layout == GRID_LAYOUT)
        {
            if (LinearState.class.equals(stateType))
            {
                changeState(GridState.class);
            } else if (LinearMultiSelectState.class.equals(stateType))
            {
                changeState(GridMultiSelectState.class);
            }
        }
    }

    @Override
    public void setMultiSelectEnable(boolean multiSelectEnable)
    {
        Class stateType = getCurrentState().getClass();
        Logger.d(stateType);
        if (multiSelectEnable)
        {
            if (stateType.equals(GridState.class))
            {
                changeState(GridMultiSelectState.class);
            } else if (stateType.equals(LinearState.class))
            {
                changeState(LinearMultiSelectState.class);
            }
        } else
        {
            if (stateType.equals(GridMultiSelectState.class))
            {
                changeState(GridState.class);
            } else if (stateType.equals(LinearMultiSelectState.class))
            {
                changeState(LinearState.class);
            }
        }
        Logger.d(getCurrentState().getClass());
    }

    public class GridState extends State
    {

        @Override
        public void onUpdate(BaseViewHolder helper, NoteObserver item)
        {
            helper.addOnClickListener(R.id.cv_note_list_grid);
            helper.addOnLongClickListener(R.id.cv_note_list_grid);
            helper.setVisible(R.id.ll_note_list_linear, false);
            helper.setVisible(R.id.cv_note_list_grid, true);

            TextView tvContent = helper.getView(R.id.tv_note_list_grid_content);
            if (EntityTypeHelper.isRecoveryPrivateNote(item))
            {
                helper.setText(R.id.tv_note_list_grid_content,
                        mContext.getResources()
                                .getString(R.string.note_private_and_recovery));
            } else
            {
                parseText(tvContent, item);
            }
            // 设置便签的时间显示
            setNoteTime(helper, item.getUpdatedAt());
            // 设置多选按钮
            setCheckBox(helper);
        }

        @NonNull
        @Override
        public String onParseTitle(NoteObserver item)
        {
            String text = "";
            if (TextUtils.isEmpty(item.getTitle()))
            {
                text += "[空标题]\n";
            } else
            {
                text += "[" + item.getTitle() + "]\n";
            }
            return text;
        }

        @Override
        public int onLoadCheckBox()
        {
            return R.id.cb_note_list_grid_check;
        }

        @Override
        public int onLoadTimeTextView()
        {
            return R.id.tv_note_list_grid_time;
        }
    }

    public class GridMultiSelectState extends GridState
    {
        @Override
        public void onSetCheckBox(CheckBox checkBox, int position)
        {
            checkBox.setVisibility(View.VISIBLE);
            if (mCheckList.get(position))
            {
                checkBox.setChecked(true);
            } else
            {
                checkBox.setChecked(false);
            }
        }
    }

    private class LinearState extends State
    {

        @Override
        public void onUpdate(BaseViewHolder helper, NoteObserver item)
        {
            helper.addOnClickListener(R.id.ll_note_list_line);
            helper.addOnLongClickListener(R.id.ll_note_list_line);

            // 显示竖排布局，隐藏网格布局
            helper.setVisible(R.id.cv_note_list_grid, false);
            helper.setVisible(R.id.ll_note_list_linear, true);

            TextView tvContent = helper.getView(R.id.tv_note_list_linear_content);
            if (EntityTypeHelper.isRecoveryPrivateNote(item))
            {
                helper.setText(R.id.tv_note_list_linear_content,
                        mContext.getResources()
                                .getString(R.string.note_private_and_recovery));
            } else
            {
                parseText(tvContent, item);
            }
            // 设置便签的时间显示
            setNoteTime(helper, item.getUpdatedAt());

            // 设置便签的分组显示
            setLinearLayoutGroup(helper,
                    TimeUtils.string2Millis(item.getCreatedAt()));
            // 设置多选按钮
            setCheckBox(helper);
        }

        @Override
        public int onLoadCheckBox()
        {
            return R.id.cb_note_list_liear_check;
        }

        @Override
        public int onLoadTimeTextView()
        {
            return R.id.tv_note_list_linear_time;
        }

        @NonNull
        @Override
        public String onParseTitle(NoteObserver item)
        {
            String text = "";
            if (TextUtils.isEmpty(item.getTitle()))
            {
                text += "[空标题]:";
            } else
            {
                text += "[" + item.getTitle() + "]:";
            }
            return text;
        }

        private void setLinearLayoutGroup(BaseViewHolder helper, long time)
        {

            // 当前position
            int position = helper.getLayoutPosition();

            // 如果是列表第一项,或者与上一个便签的创建时间不是在同一月，显示分组信息
            if (position == 0 || !DateUtil.isInSameMonth(time, TimeUtils
                    .string2Millis(getData()
                            .get(position - 1).getCreatedAt())))
            {
                showLinearLayoutGroup(true, helper, time);
                return;
            }
            showLinearLayoutGroup(false, helper, time);
        }

        private void showLinearLayoutGroup(boolean isShow,
                                           BaseViewHolder helper,
                                           long time)
        {
            // 有分组的列，marginTop为8dp,否则，为0dp
            LinearLayout ll = helper.getView(R.id.ll_note_list_linear);
            LinearLayout.LayoutParams params
                    = (LinearLayout.LayoutParams) ll.getLayoutParams();
            if (isShow)
            {
                helper.setVisible(R.id.tv_note_list_linear_month, true);
                setLinearGroupStyle(helper, time);

                params.setMargins(SizeUtils.dp2px(0),
                        SizeUtils.dp2px(8),
                        SizeUtils.dp2px(0),
                        SizeUtils.dp2px(0));
                ll.setLayoutParams(params);

            } else
            {
                helper.setVisible(R.id.tv_note_list_linear_month, false);
                params.setMargins(SizeUtils.dp2px(0),
                        SizeUtils.dp2px(0),
                        SizeUtils.dp2px(0),
                        SizeUtils.dp2px(0));
                ll.setLayoutParams(params);
            }
        }

        private void setLinearGroupStyle(BaseViewHolder helper, long time)
        {
            long nowTime = TimeUtils.getNowMills();

            if (DateUtil.isInSameYear(nowTime, time))
            { // 如果同一年 显示为：x月
                helper.setText(R.id.tv_note_list_linear_month,
                        TimeUtils.millis2String(time, simpleDateFormat1));
            } else
            { //否则 显示为：xxxx年x月
                helper.setText(R.id.tv_note_list_linear_month,
                        TimeUtils.millis2String(time, simpleDateFormat2));
            }
        }
    }

    private class LinearMultiSelectState extends LinearState
    {

        @Override
        public void onSetCheckBox(CheckBox checkBox, int position)
        {
            checkBox.setVisibility(View.VISIBLE);
            if (mCheckList.get(position))
            {
                checkBox.setChecked(true);
            } else
            {
                checkBox.setChecked(false);
            }
        }
    }
}