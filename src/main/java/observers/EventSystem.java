package observers;

import observers.events.Event;
import pikacat.GameObject;

import java.util.ArrayList;
import java.util.List;

public class EventSystem {
    // 所有注册到这个事件系统的游戏对象
    private static List<Observer> observers = new ArrayList<>();

    public static void addObserver(Observer observer) {
        observers.add(observer);
    }

    public static void notify(GameObject gameObject, Event event) {
        for (Observer observer : observers) {
            observer.onNotify(gameObject, event);
        }
    }
}
