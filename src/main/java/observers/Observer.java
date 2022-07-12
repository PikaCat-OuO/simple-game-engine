package observers;

import observers.events.Event;
import pikacat.GameObject;

// 观察者
public interface Observer {
    // 某个游戏对象的某个事件
    void onNotify(GameObject gameObject, Event event);
}
