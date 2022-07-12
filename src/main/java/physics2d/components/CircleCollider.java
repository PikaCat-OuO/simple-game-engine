package physics2d.components;

import components.Component;
import org.joml.Vector2f;
import pikacat.Window;
import renderer.DebugDraw;

// 圆形碰撞
public class CircleCollider extends Component {
    // 碰撞体偏移中心点的距离
    protected Vector2f offset = new Vector2f();

    private transient boolean resetFixtureNextFrame = false;

    // 半径
    private float radius = 1f;

    @Override
    public void editorUpdate(float deltaTime) {
        Vector2f center = new Vector2f(this.gameObject.transform.position).add(this.offset);
        DebugDraw.addCircle2D(center, this.radius);

        if (resetFixtureNextFrame) {
            this.resetFixture();
        }
    }

    @Override
    public void update(float deltaTime) {
        if (resetFixtureNextFrame) {
            this.resetFixture();
        }
    }

    public void resetFixture() {
        if (Window.getPhysics().isLocked()) {
            resetFixtureNextFrame = true;
            return;
        }
        resetFixtureNextFrame = false;

        if (gameObject != null) {
            RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
            if (rigidBody != null) {
                Window.getPhysics().resetCircleCollider(rigidBody, this);
            }
        }
    }

    public Vector2f getOffset() {
        return offset;
    }

    public void setOffset(Vector2f offset) {
        this.resetFixtureNextFrame = true;
        this.offset.set(offset);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
}
