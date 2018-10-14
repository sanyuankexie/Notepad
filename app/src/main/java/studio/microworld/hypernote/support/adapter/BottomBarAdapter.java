package studio.microworld.hypernote.support.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import studio.microworld.hypernote.R;
import studio.microworld.hypernote.support.observer.FolderObserver;


/**
 * Created by Mr.小世界 on 2018/8/30.
 */

public final class BottomBarAdapter
        extends BaseQuickAdapter<FolderObserver,
                BaseViewHolder>
{
    public BottomBarAdapter()
    {
        super(R.layout.item_note_bottom_folder);
    }

    @Override
    protected void convert(BaseViewHolder helper, FolderObserver item)
    {
        helper.setText(R.id.tv_folder_title_bottom_sheet, item.getName());
    }
}
