package observers.events;

public class Event {
    // 事件的类型
    public EventType eventType;

    // 事件携带的信息
    public String message;

    public Event() {
        this.eventType = EventType.USER_EVENT;
    }

    public Event(EventType eventType) {
        this.eventType = eventType;
    }

    public Event(EventType eventType, String message) {
        this.eventType = eventType;
        this.message = message;
    }
}
