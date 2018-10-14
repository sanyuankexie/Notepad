package studio.microworld.hypernote.support.adapter;


import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;


import java.util.List;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.observer.FolderObserver;
import studio.microworld.hypernote.support.managmanet.EntityTypeHelper;
import studio.microworld.hypernote.support.framework.BaseState;
import studio.microworld.hypernote.support.framework.StateMachineAdapter;
import studio.microworld.hypernote.support.utlis.DrawableUtil;

/**
 * Created by Mr.小世界 on 2018/9/13.
 */

public final class FolderListAdapter
        extends StateMachineAdapter<FolderObserver,FolderListAdapter.State>
{
    public static final int NO_SELECT = -1;

    private int select = NO_SELECT;

    public FolderListAdapter()
    {
        super(R.layout.item_folder);
        registerState(new DefaultState());
        registerState(new EditState());
    }

    public int getSelect()
    {
        return select;
    }

    public void setSelect(int select)
    {
        this.select = select;
    }

    @Override
    protected void convert(BaseViewHolder helper, FolderObserver item)
    {
        getCurrentState().onUpdate(helper, item);
    }

    @Override
    public void setNewData(@Nullable List<FolderObserver> data)
    {
        super.setNewData(data);
    }

    public abstract class State extends BaseState
    {
        @Override
        public void onEnter()
        {

        }


        @Override
        public void onExit()
        {

        }


        public void onUpdate(BaseViewHolder helper, FolderObserver item)
        {
            helper.addOnClickListener(R.id.rl_folder_root);
            helper.addOnLongClickListener(R.id.rl_folder_root);
            helper.addOnClickListener(R.id.ib_delete_action);
            helper.addOnLongClickListener(R.id.ib_delete_action);
            helper.addOnClickListener(R.id.tv_folder_list_title);
            helper.addOnLongClickListener(R.id.tv_folder_list_title);
            TextView tv = helper.getView(R.id.tv_folder_list_title);
            if (!EntityTypeHelper.isDefaultFolder(item))
            {
                helper.setVisible(R.id.ib_delete_action, onSetDeleteButton());
                onSetText(tv);
            }
            helper.setText(R.id.tv_folder_list_title, item.getName())
                    .setText(R.id.tv_folder_list_count,
                            Integer.toString(item.getNoteCount()));

            RelativeLayout rlItem = helper.getView(R.id.rl_folder_root);
            TextView tvTitle = helper.getView(R.id.tv_folder_list_title);
            TextView tvCount = helper.getView(R.id.tv_folder_list_count);
            ImageView ivIcon = helper.getView(R.id.iv_folder_list_ic);
            //文件夹被选中
            if (select == helper.getLayoutPosition() - getHeaderLayoutCount())
            {
                rlItem.setSelected(true);
                int selectColor = mContext
                        .getResources()
                        .getColor(R.color.colorPrimary);
                tvTitle.setTextColor(selectColor);
                tvCount.setTextColor(selectColor);
                ivIcon.setBackgroundDrawable(DrawableUtil
                        .getIcFolderSelectedDrawable(selectColor));
            } else
            {
                rlItem.setSelected(false);
                tvTitle.setTextColor(mContext.getResources()
                        .getColor(R.color.colorBlackAlpha87));
                tvCount.setTextColor(mContext.getResources()
                        .getColor(R.color.colorBlackAlpha54));
                ivIcon.setBackgroundResource(R.drawable.ic_folder_un_selected);
            }
        }

        public abstract void onSetText(TextView textView);

        public abstract boolean onSetDeleteButton();

    }

    public final class DefaultState extends State
    {
        @Override
        public void onSetText(TextView textView)
        {
            textView.getPaint().setFlags(0); // 中划线
        }

        @Override
        public boolean onSetDeleteButton()
        {
            return false;
        }
    }

    public final class EditState extends State
    {
        @Override
        public void onSetText(TextView textView)
        {
            textView.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG); // 中划线
        }

        @Override
        public boolean onSetDeleteButton()
        {
            return true;
        }
    }
}
