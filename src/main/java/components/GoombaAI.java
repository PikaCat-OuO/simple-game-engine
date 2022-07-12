package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.Physics2D;
import physics2d.components.RigidBody2D;
import pikacat.Camera;
import pikacat.GameObject;
import pikacat.Window;
import util.AssetPool;
import util.Settings;

public class GoombaAI extends Component {

    private transient boolean goingRight = false;
    private transient RigidBody2D rigidBody;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f();
    private transient boolean onGround = false;
    private transient boolean isDead = false;
    private transient float timeToKill = 0.5f;
    private transient StateMachine stateMachine;

    @Override
    public void start() {
        this.stateMachine = gameObject.getComponent(StateMachine.class);
        this.rigidBody = gameObject.getComponent(RigidBody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float deltaTime) {
        Camera camera = Window.getCurrentScene().getCamera();

        // 懒惰更新，如果玩家没到那里，就不更新
        if (this.gameObject.transform.position.x >
                camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
            return;
        }

        if (isDead) {
            timeToKill -= deltaTime;
            if (timeToKill <= 0) {
                this.gameObject.destroy();
            }
            this.rigidBody.setVelocity(new Vector2f());
            return;
        }

        if (goingRight) {
            this.velocity.x = walkSpeed;
        } else {
            this.velocity.x = -walkSpeed;
        }

        checkOnGround();
        if (onGround) {
            this.acceleration.y = 0;
            this.velocity.y = 0;
        } else {
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }

        this.velocity.y += this.acceleration.y * deltaTime;
        this.velocity.y = Math.max(Math.min(this.velocity.y, terminalVelocity.y), -terminalVelocity.y);
        this.rigidBody.setVelocity(this.velocity);

        // 如果超过了摄像机左边或摄像机下面就销毁
        if (this.gameObject.transform.position.y < camera.position.y ||
                this.gameObject.transform.position.x < camera.position.x - 1.0f) {
            this.gameObject.destroy();
        }
    }

    public boolean leftOnGround() {
        return Physics2D.leftOnGround(this.gameObject, Settings.GRID_WIDTH * 0.7f, -0.14f);
    }

    public boolean rightOnGround() {
        return Physics2D.rightOnGround(this.gameObject, Settings.GRID_WIDTH * 0.7f, -0.14f);
    }

    public void checkOnGround() {
        this.onGround = leftOnGround() && rightOnGround();
    }

    @Override
    public void preSolve(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        if (isDead) {
            return;
        }

        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            if (!playerController.isDead() && !playerController.isHurtInvincible() && contactNormal.y > 0.58) {
                // 马里奥没有死亡，马里奥不在受伤保护状态内，冲击到goomba的头部，马里奥弹开
                playerController.enemyBounce();
                // goomba死亡
                stomp();
            } else if (!playerController.isDead() && !playerController.isInvincible()) {
                // 马里奥撞击goomba，并且不在受伤保护和无敌保护下，马里奥死亡
                playerController.die();
                if (!playerController.isDead()) {
                    contact.setEnabled(false);
                }
            } else if (!playerController.isDead() && playerController.isInvincible()) {
                if (!playerController.isDead()) {
                    contact.setEnabled(false);
                }
            }
        }
        // 撞击到非玩家
        else if (Math.abs(contactNormal.y) < 0.1f){
            this.goingRight = contactNormal.x < 0;
        }

        // 被火球击中
        if (collidingGameObject.getComponent(Fireball.class) != null) {
            stomp();
            collidingGameObject.getComponent(Fireball.class).disappear();
        }

        // 检查左边是不是悬崖
        if (!leftOnGround()) {
            this.goingRight = true;
        }

        // 检查右边是不是悬崖
        if (!rightOnGround()) {
            this.goingRight = false;
        }
    }

    public void stomp() {
        stomp(true);
    }

    public void stomp(boolean playSound) {
        this.isDead = true;
        this.velocity.zero();
        this.rigidBody.setVelocity(new Vector2f());
        this.rigidBody.setAngularVelocity(0.0f);
        this.rigidBody.setGravityScale(0.0f);
        this.rigidBody.setIsSensor();
        this.stateMachine.trigger("squashMe");
        if (playSound) {
            AssetPool.getSound("assets/sounds/bump.ogg").play();
        }
    }
}
