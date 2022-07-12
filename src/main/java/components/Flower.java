package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import util.AssetPool;

public class Flower extends Component {
    private transient RigidBody2D rigidBody;

    @Override
    public void start() {
        this.rigidBody = this.gameObject.getComponent(RigidBody2D.class);
        AssetPool.getSound("assets/sounds/powerup_appears.ogg");
        this.rigidBody.setIsSensor();
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            playerController.powerUp();
            this.gameObject.destroy();
        }
    }
}
