package physics2d.components;

import components.Component;
import org.joml.Vector2f;
import renderer.DebugDraw;

// 盒子碰撞
public class Box2DCollider extends Component {
    // 碰撞体偏移中心点的距离
    protected Vector2f offset = new Vector2f();
    // 半边长
    private Vector2f halfSize = new Vector2f(1);
    // 箱形模型的中点
    private Vector2f origin = new Vector2f();

    @Override
    public void editorUpdate(float deltaTime) {
        Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.offset);
        DebugDraw.addBox2D(center, this.halfSize, this.gameObject.transform.rotation);
    }

    public Vector2f getOffset() {
        return offset;
    }

    public void setOffset(Vector2f offset) {
        this.offset.set(offset);
    }

    public Vector2f getHalfSize() {
        return halfSize;
    }

    public void setHalfSize(Vector2f halfSize) {
        this.halfSize.set(halfSize);
    }

    public Vector2f getOrigin() {
        return origin;
    }
}
