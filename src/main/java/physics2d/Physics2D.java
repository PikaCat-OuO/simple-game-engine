package physics2d;

import components.Ground;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import pikacat.Transform;
import pikacat.Window;

public class Physics2D {
    // 定义重力，y方向-9.8m/s^2
    private final Vec2 GRAVITY = new Vec2(0, -9.8f);

    // 用这个重力创建一个世界
    private World world = new World(GRAVITY);

    public Physics2D() {
        world.setContactListener(new ContactListener());
    }

    // 添加一个游戏对象到物理系统里面
    public void add(GameObject gameObject) {
        // 判断这个游戏物品有没有物理实体对象，有的话就加入到物理系统中
        RigidBody2D rigidBody = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody != null && rigidBody.getRawBody() == null) {
            Transform transform = gameObject.transform;

            // 物理世界中的实体的定义
            BodyDef bodyDef = new BodyDef();
            bodyDef.angle = (float) (Math.toRadians(transform.rotation));
            bodyDef.position.set(transform.position.x, transform.position.y);
            bodyDef.angularDamping = rigidBody.getAngularDamping();
            bodyDef.linearDamping = rigidBody.getLinearDamping();
            bodyDef.fixedRotation = rigidBody.isFixedRotation();
            bodyDef.userData = rigidBody.gameObject;
            bodyDef.bullet = rigidBody.isContinuesCollision();
            bodyDef.type = rigidBody.getBodyType();
            bodyDef.gravityScale = rigidBody.gravityScale;
            bodyDef.angularVelocity = rigidBody.angularVelocity;

            // 在物理世界中创建实体
            Body body = this.world.createBody(bodyDef);
            body.m_mass = rigidBody.getMass();
            rigidBody.setRawBody(body);

            // 物理世界的碰撞体
            CircleCollider circleCollider;
            Box2DCollider box2DCollider;
            PillboxCollider pillboxCollider;

            if ((circleCollider = gameObject.getComponent(CircleCollider.class)) != null) {
                addCircleCollider(rigidBody, circleCollider);
            }
            if ((box2DCollider = gameObject.getComponent(Box2DCollider.class)) != null) {
                addBox2DCollider(rigidBody, box2DCollider);
            }
            if ((pillboxCollider = gameObject.getComponent(PillboxCollider.class)) != null) {
                addPillBoxCollider(rigidBody, pillboxCollider);
            }
        }
    }

    public void destoryGameObject(GameObject gameObject) {
        RigidBody2D rigidBody2D = gameObject.getComponent(RigidBody2D.class);
        if (rigidBody2D != null) {
            this.world.destroyBody(rigidBody2D.getRawBody());
            rigidBody2D.setRawBody(null);
        }
    }

    public void update(float deltaTime) {
        // 物理世界随着真实的时间更新
        world.step(deltaTime, 8, 3);
    }

    public void resetBox2DCollider(RigidBody2D rigidBody, Box2DCollider box2DCollider) {
        Body body = rigidBody.getRawBody();
        if (body == null) {
            return;
        }
        int size = fixtureListSize(body);

        for (int i = 0; i < size; ++i) {
            body.destroyFixture(body.getFixtureList());
        }

        addBox2DCollider(rigidBody, box2DCollider);
        body.resetMassData();
    }

    public void resetCircleCollider(RigidBody2D rigidBody, CircleCollider circleCollider) {
        Body body = rigidBody.getRawBody();
        if (body == null) {
            return;
        }
        int size = fixtureListSize(body);

        for (int i = 0; i < size; ++i) {
            body.destroyFixture(body.getFixtureList());
        }

        addCircleCollider(rigidBody, circleCollider);
        body.resetMassData();
    }

    public void resetPillboxCollider(RigidBody2D rigidBody, PillboxCollider pillboxCollider) {
        Body body = rigidBody.getRawBody();
        if (body == null) {
            return;
        }
        int size = fixtureListSize(body);

        for (int i = 0; i < size; ++i) {
            body.destroyFixture(body.getFixtureList());
        }

        addPillBoxCollider(rigidBody, pillboxCollider);
        body.resetMassData();
    }

    // 添加一个盒子碰撞
    private void addBox2DCollider(RigidBody2D rigidBody, Box2DCollider box2DCollider) {
        Body body = rigidBody.getRawBody();
        assert body != null : "错误：无法加载物理实体";

        PolygonShape polygonShape = new PolygonShape();
        Vector2f halfSize = new Vector2f(box2DCollider.getHalfSize());
        Vector2f offset = box2DCollider.getOffset();
        Vector2f origin = new Vector2f(box2DCollider.getOrigin());
        polygonShape.setAsBox(halfSize.x, halfSize.y, new Vec2(offset.x, offset.y), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = rigidBody.getFriction();
        fixtureDef.userData = box2DCollider.gameObject;
        fixtureDef.isSensor = rigidBody.isSensor();
        body.createFixture(fixtureDef);
    }

    // 添加一个盒子碰撞
    private void addCircleCollider(RigidBody2D rigidBody, CircleCollider circleCollider) {
        Body body = rigidBody.getRawBody();
        assert body != null : "错误：无法加载物理实体";

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(circleCollider.getRadius());
        circleShape.m_p.set(circleCollider.getOffset().x, circleCollider.getOffset().y);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = rigidBody.getFriction();
        fixtureDef.userData = circleCollider.gameObject;
        fixtureDef.isSensor = rigidBody.isSensor();
        body.createFixture(fixtureDef);
    }

    public void addPillBoxCollider(RigidBody2D rigidBody, PillboxCollider pillboxCollider) {
        Body body = rigidBody.getRawBody();
        assert body != null : "错误：无法加载物理实体";

        addBox2DCollider(rigidBody, pillboxCollider.getBox());
        addCircleCollider(rigidBody, pillboxCollider.getBottomCircle());
    }

    public RayCastInfo rayCast(GameObject requestingObject, Vector2f point1, Vector2f point2) {
        RayCastInfo callback = new RayCastInfo(requestingObject);
        // 世界碰撞检测，如果发生了碰撞就调用callback
        world.raycast(callback, new Vec2(point1.x, point1.y), new Vec2(point2.x, point2.y));
        return callback;
    }

    private int fixtureListSize(Body body) {
        int size = 0;

        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            ++size;
            fixture = fixture.m_next;
        }

        return size;
    }

    public void setIsSensor(RigidBody2D rigidBody2D) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            fixture.m_isSensor = true;
            fixture = fixture.m_next;
        }
    }

    public void setNotSensor(RigidBody2D rigidBody2D) {
        Body body = rigidBody2D.getRawBody();
        if (body == null) {
            return;
        }

        Fixture fixture = body.getFixtureList();
        while (fixture != null) {
            fixture.m_isSensor = false;
            fixture = fixture.m_next;
        }
    }

    public boolean isLocked() {
        return this.world.isLocked();
    }

    public Vec2 getGravity() {
        return this.world.getGravity();
    }

    // 检查是否在地面上
    public static boolean checkOnGround(GameObject gameObject, float innerPlayerWidth, float height) {
        return leftOnGround(gameObject, innerPlayerWidth, height) ||
                rightOnGround(gameObject, innerPlayerWidth, height);
    }

    // 检查左边是不是在地面上
    public static boolean leftOnGround(GameObject gameObject, float innerPlayerWidth, float height) {
        // 中心距离地面的偏移
        Vector2f rayCastBeginLeft = new Vector2f(gameObject.transform.position)
                .sub(innerPlayerWidth / 2.0f, 0.0f);
        Vector2f rayCastEndLeft = new Vector2f(rayCastBeginLeft).add(0.0f, height);
        RayCastInfo rayCastInfoLeft = Window.getPhysics().rayCast(gameObject, rayCastBeginLeft, rayCastEndLeft);

        return (rayCastInfoLeft.hit && rayCastInfoLeft.hitObject != null &&
                rayCastInfoLeft.hitObject.getComponent(Ground.class) != null);
    }

    // 检查右边是不是在地面上
    public static boolean rightOnGround(GameObject gameObject, float innerPlayerWidth, float height) {
        Vector2f rayCastBeginRight = new Vector2f(gameObject.transform.position)
                .add(innerPlayerWidth / 2.0f, 0.0f);
        Vector2f rayCastEndRight = new Vector2f(rayCastBeginRight).add(0.0f, height);
        RayCastInfo rayCastInfoRight = Window.getPhysics().rayCast(gameObject, rayCastBeginRight, rayCastEndRight);

        return (rayCastInfoRight.hit && rayCastInfoRight.hitObject != null &&
                        rayCastInfoRight.hitObject.getComponent(Ground.class) != null);
    }
}
