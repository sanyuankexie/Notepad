package studio.microworld.hypernote.support.framework;

import java.util.Collection;

/**
 * Created by Mr.小世界 on 2018/9/16.
 */

public interface StateMachine<T extends BaseState>
{
    T getCurrentState();

    <V extends T> void registerState(V v);

    void changeState(Class<? extends T> target);

    Collection<T> getStates();
}
