package renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Line2D {
    private Vector2f from;
    private Vector2f to;
    private Vector3f color;

    // 这条线多久后消失，可以形成视觉暂留的效果
    private int lifetime;

    public Line2D(Vector2f from, Vector2f to, Vector3f color, int lifetime) {
        this.from = from;
        this.to = to;
        this.color = color;
        this.lifetime = lifetime;
    }

    public int beginFrame() {
        // 一条线的生存时间是由帧来决定的，单位是帧
        --this.lifetime;
        return this.lifetime;
    }

    public Vector2f getFrom() {
        return from;
    }

    public Vector2f getTo() {
        return to;
    }

    public Vector3f getColor() {
        return color;
    }

    public int getLifetime() {
        return lifetime;
    }
}
