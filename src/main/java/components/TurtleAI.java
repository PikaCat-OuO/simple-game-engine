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

public class TurtleAI extends Component {
    private transient boolean goingRight = false;
    private transient RigidBody2D rigidBody;
    private transient float walkSpeed = 0.6f;
    private transient Vector2f velocity = new Vector2f();
    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);
    private transient boolean onGround = false;
    private transient boolean isDead = false;
    private transient boolean isMoving = false;
    private transient StateMachine stateMachine;
    // 防止第一次跳到乌龟头上的时候乌龟头顶碰撞被触发两次
    private transient float movingDebounce = 0.0f;

    @Override
    public void start() {
        this.stateMachine = this.gameObject.getComponent(StateMachine.class);
        this.rigidBody = this.gameObject.getComponent(RigidBody2D.class);
        this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
    }

    @Override
    public void update(float deltaTime) {
        // 如果乌龟在摄像机之外，不更新
        Camera camera = Window.getCurrentScene().getCamera();
        if (this.gameObject.transform.position.x >
                camera.position.x + camera.getProjectionSize().x * camera.getZoom()) {
            return;
        }

        this.movingDebounce -= deltaTime;

        // 如果乌龟没有死，或者死了，但是处于移动状态
        if (!isDead || isMoving) {
            if (goingRight) {
                this.gameObject.transform.scale.x = -0.25f;
                this.velocity.x = walkSpeed;
                this.acceleration.x = 0.0f;
            } else {
                this.gameObject.transform.scale.x = 0.25f;
                this.velocity.x = -walkSpeed;
                this.acceleration.x = 0.0f;
            }
        }
        // 死了，就一个龟壳不动
        else {
            this.velocity.x = 0;
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

        // 如果乌龟超过了摄像机左边或摄像机下面就销毁
        if (this.gameObject.transform.position.y < camera.position.y ||
                this.gameObject.transform.position.x < camera.position.x - 1.0f) {
            this.gameObject.destroy();
        }
    }

    public boolean leftOnGround() {
        return Physics2D.leftOnGround(this.gameObject, Settings.GRID_WIDTH * 0.7f, -0.2f);
    }

    public boolean rightOnGround() {
        return Physics2D.rightOnGround(this.gameObject, Settings.GRID_WIDTH * 0.7f, -0.2f);
    }

    public void checkOnGround() {
        this.onGround = leftOnGround() && rightOnGround();
    }

    public void stomp() {
        this.isDead = true;
        this.isMoving = false;
        this.velocity.zero();
        this.rigidBody.setVelocity(this.velocity);
        this.rigidBody.setAngularVelocity(0.0f);
        this.rigidBody.setGravityScale(0.0f);
        this.stateMachine.trigger("squashMe");
        AssetPool.getSound("assets/sounds/bump.ogg").play();
    }

    @Override
    public void preSolve(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        GoombaAI goomba = collidingGameObject.getComponent(GoombaAI.class);
        if (goomba != null && isDead && isMoving) {
            goomba.stomp();
            // 不再往下检测碰撞
            contact.setEnabled(false);
            AssetPool.getSound("assets/sounds/kick.ogg").play();
        }

        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            // 正常的乌龟被踩头顶，进入死亡状态
            if (!isDead && !playerController.isDead() &&
                    !playerController.isHurtInvincible() && contactNormal.y > 0.58f) {
                playerController.enemyBounce();
                stomp();
                walkSpeed *= 3.0f;
            }
            // 马里奥碰到正常的乌龟或飞翔的龟壳，死亡
            else if (movingDebounce < 0 && (!isDead || isMoving) && !playerController.isDead() &&
                    !playerController.isInvincible() && contactNormal.y < 0.58f) {
                playerController.die();
                if (!playerController.isDead()) {
                    contact.setEnabled(false);
                }
            }
            else if (movingDebounce < 0 && !playerController.isDead() && !playerController.isHurtInvincible()) {
                // 马里奥头踩静止或飞翔的龟壳
                if (contactNormal.y > 0.58f) {
                    playerController.enemyBounce();
                    isMoving = !isMoving;
                }
                // 马里奥碰到静止的龟壳
                else {
                    isMoving = true;
                }
                goingRight = contactNormal.x < 0;
                movingDebounce = 0.32f;
            } else if (!playerController.isDead() && playerController.isHurtInvincible()) {
                contact.setEnabled(false);
            }
        }
        // 碰到其他物品
        else if (Math.abs(contactNormal.y) < 0.1f && !collidingGameObject.isDead() &&
                collidingGameObject.getComponent(MushroomAI.class) == null) {
            // 改变方向
            goingRight = contactNormal.x < 0;
            if (isMoving && isDead) {
                // 飞翔的龟壳要播放声音
                AssetPool.getSound("assets/sounds/bump.ogg").play();
            }
        }

        // 被火球击中
        if (collidingGameObject.getComponent(Fireball.class) != null) {
            if (!isDead) {
                stomp();
                walkSpeed *= 3.0f;
            } else {
                isMoving = !isMoving;
                goingRight = contactNormal.x < 0.0f;
            }
            collidingGameObject.getComponent(Fireball.class).disappear();
            // 不再往下处理
            contact.setEnabled(false);
        }

        // 如果没有死就要检测悬崖
        if (!isDead) {
            // 检查左边是不是悬崖
            if (!leftOnGround()) {
                this.goingRight = true;
            }

            // 检查右边是不是悬崖
            if (!rightOnGround()) {
                this.goingRight = false;
            }
        }
    }
}
