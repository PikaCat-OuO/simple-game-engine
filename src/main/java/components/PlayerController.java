package components;

import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import org.joml.Vector4f;
import physics2d.Physics2D;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import pikacat.KeyListener;
import pikacat.Prefabs;
import pikacat.Window;
import util.AssetPool;
import util.Settings;

import static org.lwjgl.glfw.GLFW.*;

public class PlayerController extends Component {
    private enum PlayerState {
        SMALL, BIG, FIRE, INVINCIBLE
    }

    public float walkSpeed = 1.9f;
    public float jumpBoost = 1.0f;
    public float jumpImpulse = 3.0f;
    public float slowDownForce = 0.05f;
    public Vector2f terminalVelocity = new Vector2f(2.1f, 3.1f);

    private PlayerState playerState = PlayerState.SMALL;
    public transient boolean onGround = false;
    private transient float groundDebounce = 0.0f;
    // 离开地面后有多少时间可以跳
    private transient float groundDebounceTime = 0.1f;

    private transient RigidBody2D rigidBody;
    private transient StateMachine stateMachine;
    private transient SpriteRenderer spriteRenderer;
    private transient float bigJumpBoostFactor = 1.05f;
    private transient float playerWidth = 0.25f;

    // 还可以跳的时间
    private transient float jumpTime = 0;

    private transient Vector2f acceleration = new Vector2f();
    private transient Vector2f velocity = new Vector2f();
    private transient boolean isDead = false;
    private transient float enemyBounce = 0;
    private transient float hurtInvincibleTimeLeft = 0;

    // 死亡上升最大高度
    private transient float deadMaxHeight = 0;
    // 死亡下降最低点
    private transient float deadMinHeight = 0;
    // 当前是否处于死亡上升状态
    private transient boolean deadGoingUp = false;

    // 是否已经胜利了
    private transient boolean playWinAnimation = false;
    // 胜利动画播放的总时长
    private transient float timeToCastle = 4.5f;
    // 马里奥胜利时在地面上走动的时间
    private transient float walkTime = 2.2f;


    // 死亡闪烁时间
    private transient float blinkTime = 0.0f;


    // 是否在升级
    private transient boolean isPowerUp = false;

    @Override
    public void start() {
        this.rigidBody = gameObject.getComponent(RigidBody2D.class);
        this.stateMachine = gameObject.getComponent(StateMachine.class);
        this.rigidBody.setGravityScale(0.0f);
        this.spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
    }

    @Override
    public void update(float deltaTime) {
        // 如果播放胜利动画
        if (playWinAnimation) {
            checkOnGround();
            if (!onGround) {
                // 如果不在地面上就下落，首先转向
                this.gameObject.transform.scale.x = -0.25f;
                this.gameObject.transform.position.y -= deltaTime;
                Window.getCurrentScene().getGameObjectByName("flagTop").transform.position.y =
                        this.gameObject.transform.position.y;
                this.stateMachine.trigger("stopRunning");
                this.stateMachine.trigger("stopJumping");
            } else {
                if (this.walkTime > 0) {
                    // 如果已经在地面上了，就播放开始走入城堡，先转向
                    this.gameObject.transform.scale.x = 0.25f;
                    this.gameObject.transform.position.x += deltaTime;
                    stateMachine.trigger("startRunning");
                }
                if (!AssetPool.getSound("assets/sounds/stage_clear.ogg").isPlaying()) {
                    AssetPool.getSound("assets/sounds/stage_clear.ogg").play();
                }

                walkTime -= deltaTime;
                timeToCastle -= deltaTime;

                if (timeToCastle <= 0) {
                    EventSystem.notify(null, new Event(EventType.GAME_ENGINE_START_PLAY));
                }
            }

            return;
        }
        if (isDead) {
            Vector2f position = this.gameObject.transform.position;
            // 死亡上升状态
            if (position.y < deadMaxHeight && deadGoingUp) {
                position.y += deltaTime * walkSpeed / 2.0f;
            }
            // 已经上升到了最高点
            else if (position.y >= deadMaxHeight && deadGoingUp) {
                deadGoingUp = false;
            }
            // 如果我们不处于死亡上升状态就开始下降
            else if (!deadGoingUp && position.y > deadMinHeight) {
                // 让物理引擎帮我们下降
                this.rigidBody.setBodyType(BodyType.KINEMATIC);
                this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
                this.velocity.y += this.acceleration.y * deltaTime;
                // 最大速度限制
                this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y),
                        -this.terminalVelocity.y);

                this.rigidBody.setVelocity(this.velocity);
                this.rigidBody.setAngularVelocity(0);
            }
            // 如果已经达到了最低点，回到开始界面
            else if (!deadGoingUp && position.y <= deadMinHeight) {
                EventSystem.notify(null, new Event(EventType.GAME_ENGINE_START_PLAY));
            }
            return;
        }

        // 如果现在正处于受伤无敌状态
        if (hurtInvincibleTimeLeft > 0) {
            hurtInvincibleTimeLeft -= deltaTime;
            blinkTime -= deltaTime;

            // 受伤闪烁
            if (blinkTime <= 0) {
                blinkTime = 0.2f;
                spriteRenderer.setColor(new Vector4f(1, 1, 1, 1 - spriteRenderer.getColor().w));
            }
        }
        // 受伤无敌时间过了就不再闪烁了
        else if (spriteRenderer.getColor().w == 0) {
            spriteRenderer.setColor(new Vector4f(1, 1, 1, 1));
        }

        // 向右边走
        if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW_KEY_D)) {
            // 设置人物方向
            this.gameObject.transform.scale.x = playerWidth;
            // 设置加速度
            this.acceleration.x = walkSpeed;

            if (this.velocity.x < 0) {
                // 转身慢慢停下
                this.stateMachine.trigger("switchDirection");
                this.velocity.x += slowDownForce;
            } else {
                // 开始跑步
                stateMachine.trigger("startRunning");
            }
        }
        // 向左边走
        else if ((KeyListener.isKeyPressed(GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW_KEY_A)) &&
                this.gameObject.transform.position.x >= Window.getCurrentScene().getCamera().position.x + 0.25f) {
            // 设置人物方向
            this.gameObject.transform.scale.x = -playerWidth;
            // 设置加速度
            this.acceleration.x = -walkSpeed;

            if (this.velocity.x > 0) {
                // 转身慢慢停下
                this.stateMachine.trigger("switchDirection");
                this.velocity.x -= slowDownForce;
            } else {
                // 开始跑步
                stateMachine.trigger("startRunning");
            }
        }
        // 增加摩擦力
        else {
            this.acceleration.x = 0;
            if (this.velocity.x > 0) {
                this.velocity.x = Math.max(0, this.velocity.x - slowDownForce);
            } else if (this.velocity.x < 0) {
                this.velocity.x = Math.min(0, this.velocity.x + slowDownForce);
            } else {
                this.stateMachine.trigger("stopRunning");
            }
        }

        // 射击火球
        if (KeyListener.keyBeginPress(GLFW_KEY_E) && playerState == PlayerState.FIRE && Fireball.canSpawn()) {
            // 生成的火球的位置
            Vector2f position = new Vector2f(this.gameObject.transform.position)
                    .add(this.gameObject.transform.scale.x > 0 ?
                            new Vector2f(0.26f, 0) :
                            new Vector2f(-0.26f, 0));

            // 生成火球
            GameObject fireball = Prefabs.generateFireball(position);
            // 火球的方向
            fireball.getComponent(Fireball.class).goingRight = this.gameObject.transform.scale.x > 0;

            Window.getCurrentScene().addGameObjectToScene(fireball);
        }

        // 检查是否在地面上
        checkOnGround();

        // 如果不再地面上就跳转到跳跃模式
        if (!onGround) {
            stateMachine.trigger("jump");
        } else {
            stateMachine.trigger("stopJumping");
        }

        // 跳跃，一直按着空格键
        if (KeyListener.isKeyPressed(GLFW_KEY_SPACE) && (jumpTime > 0 || onGround || groundDebounce > 0)) {
            // 如果在地面上或者刚离地
            if ((onGround || groundDebounce > 0) && jumpTime <= 0) {
                AssetPool.getSound("assets/sounds/jump-small.ogg").play();
                jumpTime = 0.47f;
                this.velocity.y = jumpImpulse;
            }
            // 如果正在跳跃那么就衰减跳跃时间并且慢慢加速
            else if (jumpTime > 0) {
                jumpTime -= deltaTime;
                this.velocity.y = jumpTime * 27.0f * jumpBoost;
            }
            // 跳跃时间用完了，设置y为0，让其自动下落
            else {
                this.velocity.y = 0;
            }
            // 如果开始跳了就不再给离地跳跃的机会
            groundDebounce = 0;
        }
        // 碰到敌人头顶弹起来
        else if (enemyBounce > 0) {
            enemyBounce -= deltaTime;
            this.velocity.y = enemyBounce * 27.0f * jumpBoost;
        } else if (!onGround) {
            // 如果提前放开空格键，在空中
            if (this.jumpTime > 0) {
                // 衰减上升速度
                this.velocity.y *= 0.35;
                // 提前放开空格不给第二次按的机会
                this.jumpTime = 0;
            }
            // 已经离开了地面，离地跳跃时间要衰减
            groundDebounce -= deltaTime;
            // 离地之后增加重力加速度
            this.acceleration.y = Window.getPhysics().getGravity().y * 0.7f;
        }
        // 如果在地面上
        else {
            this.velocity.y = 0;
            this.acceleration.y = 0;
            groundDebounce = groundDebounceTime;
        }

        // 如果正在升级就在Y方向加一点速度，防止卡地面
        if (isPowerUp) {
            this.velocity.y += 1.0f;
            isPowerUp = false;
        }

        // 更新速度
        this.velocity.x += this.acceleration.x * deltaTime;
        this.velocity.y += this.acceleration.y * deltaTime;

        // 最大速度限制
        this.velocity.x = Math.max(Math.min(this.velocity.x, this.terminalVelocity.x), -this.terminalVelocity.x);
        this.velocity.y = Math.max(Math.min(this.velocity.y, this.terminalVelocity.y), -this.terminalVelocity.y);

        this.rigidBody.setVelocity(this.velocity);
        this.rigidBody.setAngularVelocity(0);
    }

    // 检查是否在地面上
    public void checkOnGround() {
        float innerPlayerWidth = this.playerWidth * 0.6f;
        // 中心距离地面的偏移
        float yValue = playerState == PlayerState.SMALL ? -0.14f : -0.24f;

        this.onGround = Physics2D.checkOnGround(this.gameObject, innerPlayerWidth, yValue);
    }

    // 马里奥升级
    public void powerUp() {
        AssetPool.getSound("assets/sounds/powerup.ogg").play();
        if (playerState == PlayerState.SMALL) {
            playerState = PlayerState.BIG;
            this.isPowerUp = true;
            gameObject.transform.scale.y = 0.42f;

            PillboxCollider pillboxCollider = gameObject.getComponent(PillboxCollider.class);
            if (pillboxCollider != null) {
                jumpBoost *= bigJumpBoostFactor;
                walkSpeed *= bigJumpBoostFactor;

                pillboxCollider.setHeight(0.42f);
            }
        } else if (playerState == PlayerState.BIG) {
            playerState = PlayerState.FIRE;
        }

        stateMachine.trigger("powerup");
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        // 如果玩家已经死亡就不处理碰撞
        if (isDead) {
            return;
        }

        // 如果玩家触碰到了地面物体就将速度降为0
        if (collidingGameObject.getComponent(Ground.class) != null) {
            // 水平撞击
            if (Math.abs(contactNormal.x) > 0.8f) {
                this.velocity.x = 0;
            }
            // 这个游戏对象的头部撞到了其他游戏对象
            else if (contactNormal.y > 0.8f) {
                this.velocity.y = 0;
                this.acceleration.y = 0;
                this.jumpTime = 0;
            }
        }
    }

    // 角色死亡
    public void die() {
        this.stateMachine.trigger("die");
        if (this.playerState == PlayerState.SMALL) {
            // 清除所有状态
            this.velocity.set(0, 0);
            this.acceleration.set(0, 0);
            this.rigidBody.setVelocity(new Vector2f());
            this.isDead = true;
            this.rigidBody.setIsSensor();
            this.deadGoingUp = true;

            // 播放声音
            AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").stop();
            AssetPool.getSound("assets/sounds/mario_die.ogg").play();

            // 死亡上升的最大高度
            deadMaxHeight = this.gameObject.transform.position.y + 0.3f;
            this.rigidBody.setBodyType(BodyType.STATIC);

            // 如果死亡时在水平线上方就要确保下降到水平线以下
            if (this.gameObject.transform.position.y > 0) {
                deadMinHeight = -Settings.GRID_HEIGHT;
            }

        } else if (this.playerState == PlayerState.BIG) {
            this.playerState = PlayerState.SMALL;
            // 恢复原有高度
            gameObject.transform.scale.y = Settings.GRID_HEIGHT;

            // 恢复碰撞盒子
            PillboxCollider pillboxCollider = gameObject.getComponent(PillboxCollider.class);
            if (pillboxCollider != null) {
                // 恢复跳跃提升和行走提升
                jumpBoost /= bigJumpBoostFactor;
                walkSpeed /= bigJumpBoostFactor;
                pillboxCollider.setHeight(0.25f);
            }

            // 受伤了，开启无敌模式
            hurtInvincibleTimeLeft = 1.4f;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        } else if (this.playerState == PlayerState.FIRE) {
            this.playerState = PlayerState.BIG;
            // 受伤了，开启无敌模式
            hurtInvincibleTimeLeft = 1.4f;
            AssetPool.getSound("assets/sounds/pipe.ogg").play();
        }
    }

    public void playWinAnimation(GameObject flagPole) {
        if (!playWinAnimation) {
            playWinAnimation = true;
            AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").stop();
            // 清空马里奥的速度
            this.velocity.zero();
            this.rigidBody.setVelocity(this.velocity);
            // 设置马里奥为sensor
            this.rigidBody.setIsSensor();
            // 设置为static类型，这样我们可以控制马里奥的位置
            this.rigidBody.setBodyType(BodyType.STATIC);
            // 传输马里奥到旗帜的x位置
            this.gameObject.transform.position.x = flagPole.transform.position.x;

            AssetPool.getSound("assets/sounds/flagpole.ogg").play();
        }
    }

    // 设置玩家的位置
    public void setPosition(Vector2f position) {
        this.gameObject.transform.position.set(position);
        this.rigidBody.setPosition(position);
    }

    public void enemyBounce() {
        this.enemyBounce = 0.14f;
    }

    public boolean isSmall() {
        return this.playerState == PlayerState.SMALL;
    }

    // 是否受伤无敌状态
    public boolean isHurtInvincible() {
        return this.hurtInvincibleTimeLeft > 0 || playWinAnimation;
    }

    // 是否无敌状态
    public boolean isInvincible() {
        return this.playerState == PlayerState.INVINCIBLE || isHurtInvincible();
    }

    public boolean isDead() {
        return isDead;
    }

    public boolean hasWon() {
        return false;
    }
}
