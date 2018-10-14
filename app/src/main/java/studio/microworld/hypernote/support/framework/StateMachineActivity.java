package studio.microworld.hypernote.support.framework;

import java.util.Collection;

/**
 * Created by Mr.小世界 on 2018/9/16.
 */

public abstract class StateMachineActivity<T extends BaseState>
        extends BaseActivity
        implements StateMachine<T>
{

    private StateMachine<T> stateMachine = new StateMachineHelper<T>();

    public T getCurrentState()
    {
        return stateMachine.getCurrentState();
    }

    @Override
    public <V extends T> void registerState(V v)
    {
        stateMachine.registerState(v);
    }

    public void changeState(Class<? extends T> target)
    {
        stateMachine.changeState(target);
    }

    @Override
    public Collection<T> getStates()
    {
        return stateMachine.getStates();
    }
}
