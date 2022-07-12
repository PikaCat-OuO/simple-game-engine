package physics2d.components;

import components.Component;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector2f;
import pikacat.Window;

// 物理世界的真实模型在JAVA中的映射
public class RigidBody2D extends Component {
    // 速度以及阻力
    private Vector2f velocity = new Vector2f();
    private float angularDamping = 0.8f;
    private float linearDamping = 0.9f;

    // 质量
    private float mass = 0;

    // 物体的类型
    private BodyType bodyType = BodyType.DYNAMIC;

    // 阻力
    private float friction = 0.1f;

    public float angularVelocity = 0.0f;

    public float gravityScale = 1.0f;

    // 这个物品不会参与任何碰撞，相当于装饰品
    private boolean isSensor = false;

    private boolean fixedRotation = false;

    // 这个适合用在高速运动的物体中
    private boolean continuesCollision = true;

    // Box2D中的实体
    private transient Body rawBody = null;

    @Override
    public void update(float deltaTime) {
        if (rawBody != null) {
            if (bodyType == BodyType.DYNAMIC || bodyType == BodyType.KINEMATIC) {
                // 物理世界中的物品与真实物品的位置相对应
                this.gameObject.transform.position.set(rawBody.getPosition().x, rawBody.getPosition().y);
                this.gameObject.transform.rotation = (float) Math.toDegrees(rawBody.getAngle());

                // 物理世界中的速度与真实物品的速度对应
                Vec2 val = rawBody.getLinearVelocity();
                this.velocity.set(val.x, val.y);
            } else if (this.bodyType == BodyType.STATIC) {
                // 如果是静止的物品，那么物理引擎需要适应真实物品的位置
                Vector2f position = this.gameObject.transform.position;
                this.rawBody.setTransform(new Vec2(position.x, position.y),
                        this.gameObject.transform.rotation);
            }
        }
    }

    // 用力推动
    public void addVelocity(Vector2f forceToAdd) {
        if (rawBody != null) {
            rawBody.applyForceToCenter(new Vec2(forceToAdd.x, forceToAdd.y));
        }
    }

    public void addImpulse(Vector2f impulse) {
        if (rawBody != null) {
            rawBody.applyLinearImpulse(new Vec2(velocity.x, velocity.y), rawBody.getWorldCenter());
        }
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
        if (rawBody != null) {
            this.rawBody.setLinearVelocity(new Vec2(velocity.x, velocity.y));
        }
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        if (rawBody != null) {
            rawBody.setAngularVelocity(angularVelocity);
        }
    }

    public void setGravityScale(float gravityScale) {
        this.gravityScale = gravityScale;
        if (rawBody != null) {
            rawBody.setGravityScale(gravityScale);
        }
    }

    public void setIsSensor() {
        this.isSensor = true;
        if (rawBody != null) {
            Window.getPhysics().setIsSensor(this);
        }
    }

    public boolean isSensor() {
        return this.isSensor;
    }

    public void setNotSensor() {
        this.isSensor = false;
        if (rawBody != null) {
            Window.getPhysics().setNotSensor(this);
        }
    }

    public void setPosition(Vector2f position) {
        if (rawBody != null) {
            this.rawBody.setTransform(new Vec2(position.x, position.y), this.gameObject.transform.rotation);
        }
    }

    public float getAngularDamping() {
        return angularDamping;
    }

    public void setAngularDamping(float angularDamping) {
        this.angularDamping = angularDamping;
    }

    public float getLinearDamping() {
        return linearDamping;
    }

    public void setLinearDamping(float linearDamping) {
        this.linearDamping = linearDamping;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public BodyType getBodyType() {
        return bodyType;
    }

    public void setBodyType(BodyType bodyType) {
        this.bodyType = bodyType;
    }

    public boolean isFixedRotation() {
        return fixedRotation;
    }

    public void setFixedRotation(boolean fixedRotation) {
        this.fixedRotation = fixedRotation;
    }

    public boolean isContinuesCollision() {
        return continuesCollision;
    }

    public void setContinuesCollision(boolean continuesCollision) {
        this.continuesCollision = continuesCollision;
    }

    public Body getRawBody() {
        return rawBody;
    }

    public void setRawBody(Body rawBody) {
        this.rawBody = rawBody;
    }

    public float getFriction() {
        return friction;
    }
}
