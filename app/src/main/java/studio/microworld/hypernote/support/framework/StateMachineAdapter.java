package studio.microworld.hypernote.support.framework;

import android.support.annotation.LayoutRes;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.Collection;

/**
 * Created by Mr.小世界 on 2018/9/16.
 */

public abstract class StateMachineAdapter<T,S extends BaseState>
        extends BaseQuickAdapter<T,BaseViewHolder>
        implements StateMachine<S>
{
    protected StateMachineAdapter(@LayoutRes int id)
    {
        super(id);
    }

    private StateMachine<S> stateMachine = new StateMachineHelper<S>();

    public S getCurrentState()
    {
        return stateMachine.getCurrentState();
    }

    @Override
    public <V extends S> void registerState(V v)
    {
        stateMachine.registerState(v);
    }

    public void changeState(Class<? extends S> target)
    {
        stateMachine.changeState(target);
    }

    @Override
    public Collection<S> getStates()
    {
        return stateMachine.getStates();
    }
}
