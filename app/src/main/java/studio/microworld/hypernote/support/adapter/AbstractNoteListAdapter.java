package studio.microworld.hypernote.support.adapter;

import android.support.annotation.IdRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.blankj.utilcode.util.TimeUtils;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.observer.NoteObserver;
import studio.microworld.hypernote.support.framework.BaseState;
import studio.microworld.hypernote.support.framework.StateMachineAdapter;
import studio.microworld.hypernote.support.utlis.DateUtil;

/**
 * Created by Mr.小世界 on 2018/9/17.
 */

abstract class AbstractNoteListAdapter
        extends StateMachineAdapter<NoteObserver,AbstractNoteListAdapter.State>
{

    protected List<Boolean> mCheckList = new ArrayList<>();

    private int mCheckCount = 0;

    @Override
    public void remove(@IntRange(from = 0L) int position)
    {
        if (mCheckList.get(position))
        {
            mCheckCount--;
        }
        mCheckList.remove(position);
        super.remove(position);
    }

    public boolean isSelectAll()
    {
        return mCheckCount == getData().size();
    }

    protected AbstractNoteListAdapter()
    {
        super(R.layout.item_note);
    }

    public abstract void setMultiSelectEnable(boolean multiSelectEnable);

    @NonNull
    public List<NoteObserver> getSelectedItems()
    {
        List<NoteObserver> result = new ArrayList<>();
        List<NoteObserver> handlers = getData();
        for (int i = 0; i < handlers.size(); i++)
        {
            if (getIsSelect(i))
            {
                result.add(handlers.get(i));
            }
        }
        return result;
    }

    public boolean getIsSelect(int position)
    {
        return mCheckList.get(position);
    }

    public void selectAll(boolean enable)
    {
        if (enable)
        {
            for (int i = 0; i < mCheckList.size(); i++)
            {
                mCheckList.set(i, true);
            }
            mCheckCount = getData().size();

        } else
        {
            for (int i = 0; i < mCheckList.size(); i++)
            {
                mCheckList.set(i, false);
            }
            mCheckCount = 0;
        }
        notifyDataSetChanged();
    }

    public void selectItem(int position, boolean selected)
    {
        if (mCheckList.get(position) == selected)
        {
            return;
        }
        mCheckList.set(position, selected);
        if (selected)
        {
            mCheckCount++;
        } else
        {
            mCheckCount--;
        }
        this.notifyItemChanged(position);
    }

    public int getSelectCount()
    {
        return mCheckCount;
    }

    //add list
    @Override
    public void addData(@NonNull Collection<? extends NoteObserver> newData)
    {
        for (int i = 0; i < newData.size(); i++)
        {
            mCheckList.add(false);
        }
        super.addData(newData);
    }

    //add single
    @Override
    public void addData(@NonNull NoteObserver data)
    {
        mCheckList.add(0, false);
        super.addData(0, data);
    }

    public void setNewData(@Nullable List<NoteObserver> data)
    {
        mCheckList.clear();
        mCheckCount = 0;
        if (data != null)
        {
            for (int i = 0; i < data.size(); i++)
            {
                mCheckList.add(false);
            }
        }
        super.setNewData(data);
    }

    @Override
    protected void convert(BaseViewHolder helper, NoteObserver item)
    {
        getCurrentState().onUpdate(helper, item);
    }

    protected abstract static class State extends BaseState
    {
        @Override
        public void onEnter()
        {

        }

        @Override
        public void onExit()
        {

        }

        public abstract void onUpdate(BaseViewHolder helper, NoteObserver item);

        @NonNull
        public abstract String onParseTitle(NoteObserver item);

        public final void parseText(TextView textView, NoteObserver item)
        {
            String text = onParseTitle(item);
            if (!TextUtils.isEmpty(item.getContent()))
            {
                String temp = item.getContent().replaceAll("<img.*?>", "[图片]")
                        .replaceAll("<audio.*?>", "[音频]")
                        .replaceAll("<.*?>", " ")
                        .replaceAll("<.*?", "")
                        .replaceAll("\n", " ")
                        .replaceAll(" +", " ");
                if (temp.length() < 20)
                {
                    text += temp;
                } else
                {
                    text += temp.substring(0, 20);
                }
            }
            textView.setText(text);
        }

        @IdRes
        public abstract int onLoadCheckBox();

        public void onSetCheckBox(CheckBox checkBox, int position)
        {
            checkBox.setVisibility(View.INVISIBLE);
            checkBox.setChecked(false);
        }

        public void setCheckBox(BaseViewHolder helper)
        {
            int position = helper.getLayoutPosition();
            CheckBox checkBox = helper.getView(onLoadCheckBox());
            onSetCheckBox(checkBox, position);
        }

        @IdRes
        public abstract int onLoadTimeTextView();

        private void setNoteTimeInfo(BaseViewHolder helper,
                                     long time,
                                     SimpleDateFormat format)
        {
            helper.setText(onLoadTimeTextView(),
                    TimeUtils.millis2String(time, format));
        }


        public void setNoteTime(BaseViewHolder helper, String time)
        {
            // 系统当前时间，用于与便签的修改时间进行对比
            long nowTime = TimeUtils.getNowMills();
            long lTime = TimeUtils.string2Millis(time);
            if (DateUtil.isInSameDay(nowTime, lTime))  // 同一天
            {
                setNoteTimeInfo(helper, lTime, new SimpleDateFormat("HH:mm"));
            } else if (DateUtil.isInSameYear(nowTime, lTime))  // 同一年
            {
                setNoteTimeInfo(helper, lTime, new SimpleDateFormat("MM-dd HH:mm"));
            } else // 其他
            {
                setNoteTimeInfo(helper, lTime, new SimpleDateFormat("yyyy-MM-dd HH:mm"));
            }
        }
    }
}
