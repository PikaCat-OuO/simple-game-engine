package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.Physics2D;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import pikacat.Window;
import util.Settings;

public class Fireball extends Component {
    public transient boolean goingRight = false;
    private transient RigidBody2D rigidBody;
    private transient float fireballSpeed = 1.7f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient float lifeTime = 4.0f;

    // 当前的场景中有多少个火球
    private static int fireballCount = 0;

    // 当前场上的火球不能多于4个
    public static boolean canSpawn() {
        return fireballCount < 4;
    }

    @Override
    public void start() {
        ++fireballCount;
        this.rigidBody = this.gameObject.getComponent(RigidBody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float deltaTime) {
        lifeTime -= deltaTime;
        if (lifeTime <= 0) {
            this.disappear();
            return;
        }

        if (goingRight) {
            this.velocity.x = fireballSpeed;
        } else {
            this.velocity.x = -fireballSpeed;
        }

        checkOnGround();
        if (onGround) {
            this.acceleration.y = 1.5f;
            this.velocity.y = 2.5f;
        } else {
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }

        this.velocity.y += this.acceleration.y * deltaTime;
        this.velocity.y = Math.max(Math.min(this.velocity.y, terminalVelocity.y), -terminalVelocity.y);
        this.rigidBody.setVelocity(this.velocity);
    }

    @Override
    public void preSolve(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        if (collidingGameObject.getComponent(PlayerController.class) != null ||
                collidingGameObject.getComponent(Fireball.class) != null) {
            contact.setEnabled(false);
        }
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        if (Math.abs(contactNormal.x) > 0.8f) {
            this.goingRight = contactNormal.x < 0;
        }
    }

    public void disappear() {
        --fireballCount;
        this.gameObject.destroy();
    }

    public void checkOnGround() {
        this.onGround = Physics2D.checkOnGround(this.gameObject,
                Settings.GRID_WIDTH * 0.7f, -0.09f);
    }
}
