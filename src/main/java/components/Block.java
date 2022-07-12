package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import pikacat.GameObject;

public abstract class Block extends Component {
    private transient boolean bopGoingUp = true;
    private transient Vector2f bopStart;
    private transient Vector2f topBopLocation;
    private transient boolean active = true;

    private float bopSpeed = 0.4f;

    @Override
    public void start() {
        this.bopStart = new Vector2f(this.gameObject.transform.position);
        this.topBopLocation = new Vector2f(this.bopStart).add(0.0f, 0.02f);
    }

    @Override
    public void update(float deltaTime) {
        Vector2f position = this.gameObject.transform.position;
        // 要做向上弹一下的动画
        if (bopGoingUp) {
            if (position.y < topBopLocation.y) {
                // 没弹到顶就接着弹
                position.y += bopSpeed * deltaTime;
            } else {
                // 弹到顶了，不弹了
                bopGoingUp = false;
            }
        } else {
            // 向下回落
            if (position.y > bopStart.y) {
                position.y -= bopSpeed * deltaTime;
            } else {
                position.y = bopStart.y;
            }
        }
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        // 玩家从下方撞击方块
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (active && playerController != null && contactNormal.y < -0.8f) {
            bopGoingUp = true;
            playerHit(playerController);
        }
    }

    public abstract void playerHit(PlayerController playerController);

    public void setInactive() {
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }
}
