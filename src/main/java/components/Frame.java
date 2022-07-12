package components;

// 动画的一个状态
public class Frame {
    // 这一个状态的材质
    public Sprite sprite;
    // 这一个状态持续的时间
    public float frameTime;

    public Frame() {

    }

    public Frame(Sprite sprite, float frameTime) {
        this.sprite = sprite;
        this.frameTime = frameTime;
    }
}
