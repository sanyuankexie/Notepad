package studio.microworld.hypernote.support.framework;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Mr.小世界 on 2018/9/16.
 */

public final class StateMachineHelper<T extends BaseState> implements StateMachine<T>
{
    private final Map<Class<? extends BaseState>, T> stateTable = new HashMap<>();

    private T currentState;

    public T getCurrentState()
    {
        return currentState;
    }

    @Override
    public <V extends T> void registerState(V v)
    {
        if (currentState == null)
        {
            currentState = v;
        }
        stateTable.put(v.getClass(), v);
    }

    public void changeState(Class<? extends T> target)
    {
        currentState.onExit();
        T state = stateTable.get(target);
        state.onEnter();
        currentState = state;
    }

    @Override
    public Collection<T> getStates()
    {
        return stateTable.values();
    }
}
