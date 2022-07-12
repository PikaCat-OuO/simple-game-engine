package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import util.AssetPool;

public class MushroomAI extends Component {
    private transient boolean goingRight = true;
    private transient RigidBody2D rigidBody;
    private transient float force = 1.0f;
    private transient float maxSpeed = 0.8f;

    private transient boolean hitPlayer = false;

    @Override
    public void start() {
        this.rigidBody = gameObject.getComponent(RigidBody2D.class);
        AssetPool.getSound("assets/sounds/powerup_appears.ogg").play();
    }

    @Override
    public void update(float deltaTime) {
        if (goingRight && Math.abs(rigidBody.getVelocity().x) < maxSpeed) {
            rigidBody.addVelocity(new Vector2f(force, 0.0f));
        } else if (!goingRight && Math.abs(rigidBody.getVelocity().x) < maxSpeed){
            rigidBody.addVelocity(new Vector2f(-force, 0.0f));
        }
    }

    @Override
    public void preSolve(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            // 不往下走，不处理这次碰撞
            contact.setEnabled(false);
            if (!hitPlayer) {
                if (playerController.isSmall()) {
                    playerController.powerUp();
                } else {
                    AssetPool.getSound("assets/sounds/coin.ogg").play();
                }
                this.gameObject.destroy();
                hitPlayer = true;
            }
        }
        // 蘑菇不与非地面碰撞
        else if (collidingGameObject.getComponent(Ground.class) == null) {
            // 不往下走，不处理这次碰撞
            contact.setEnabled(false);
            return;
        }

        if (Math.abs(contactNormal.y) < 0.1f) {
            // 水平撞击
            goingRight = contactNormal.x < 0;
        }
    }
}
