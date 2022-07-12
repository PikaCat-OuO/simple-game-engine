package physics2d.components;

import components.Component;
import org.joml.Vector2f;
import pikacat.Window;

public class PillboxCollider extends Component {
    // 碰撞体偏移中心点的距离
    protected Vector2f offset = new Vector2f();

    // 碰撞体
    private transient Box2DCollider box = new Box2DCollider();
    private transient CircleCollider bottomCircle = new CircleCollider();

    // 下一帧时候需要重置物品
    private transient boolean resetFixtureNextFrame = false;

    // 这个碰撞体的宽高
    public float width;
    public float height;

    @Override
    public void start() {
        this.box.gameObject = this.gameObject;
        this.bottomCircle.gameObject = this.gameObject;
        reCalculateColliders();
    }

    @Override
    public void update(float deltaTime) {
        if (this.resetFixtureNextFrame) {
            resetFixture();
        }
    }

    @Override
    public void editorUpdate(float deltaTime) {
        box.editorUpdate(deltaTime);
        bottomCircle.editorUpdate(deltaTime);
        reCalculateColliders();

        if (this.resetFixtureNextFrame) {
            resetFixture();
        }
    }

    // 重置物品
    public void resetFixture() {
        if (Window.getPhysics().isLocked()) {
            this.resetFixtureNextFrame = true;
            return;
        }

        this.resetFixtureNextFrame = false;

        if (gameObject != null) {
            RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
            if (rigidBody != null) {
                Window.getPhysics().resetPillboxCollider(rigidBody, this);
            }
        }
    }

    // 根据宽高计算下面三个碰撞体的宽高
    public void reCalculateColliders() {
        float circleRadius = width / 2.0f;
        float boxHeight = height - circleRadius;

        box.setHalfSize(new Vector2f(width - 0.03f, boxHeight).div(2f));
        box.setOffset(new Vector2f(offset).add(new Vector2f(0, (height - boxHeight) / 2.0f)));

        bottomCircle.setRadius(circleRadius);
        bottomCircle.setOffset(new Vector2f(offset).sub(0, (height - 2 * circleRadius) / 2.0f));
    }

    public Box2DCollider getBox() {
        return box;
    }

    public CircleCollider getBottomCircle() {
        return bottomCircle;
    }

    public void setWidth(float width) {
        this.width = width;
        reCalculateColliders();
        resetFixture();
    }

    public void setHeight(float height) {
        this.height = height;
        reCalculateColliders();
        resetFixture();
    }
}
