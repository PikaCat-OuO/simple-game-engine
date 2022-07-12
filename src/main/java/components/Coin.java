package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import pikacat.GameObject;
import pikacat.Transform;
import util.AssetPool;
import util.Settings;

public class Coin extends Component {
    private Vector2f topY;
    private float coinSpeed = 1.4f;
    private transient boolean playAnim = false;

    @Override
    public void start() {
        topY = new Vector2f(this.gameObject.transform.position.y).add(0, Settings.GRID_HEIGHT);
    }

    @Override
    public void update(float deltaTime) {
        if (playAnim) {
            Transform transform = this.gameObject.transform;
            if (transform.position.y < topY.y) {
                transform.position.y += coinSpeed * deltaTime;
                transform.scale.x -= (0.5f * deltaTime) % -1.0f;
            } else {
                gameObject.destroy();
            }
        }
    }

    @Override
    public void preSolve(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        if (collidingGameObject.getComponent(PlayerController.class) != null) {
            AssetPool.getSound("assets/sounds/coin.ogg").play();
            playAnim = true;
            contact.setEnabled(false);
        }
    }
}
