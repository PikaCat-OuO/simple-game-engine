package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import pikacat.GameObject;

public class FlagPole extends Component {
    private boolean isTop = false;

    // 如果传入的是真，代表是旗帜的顶部，如果传入的是假，代表是旗帜的底部
    public FlagPole(boolean isTop) {
        this.isTop = isTop;
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            playerController.playWinAnimation(this.gameObject);
        }
    }
}
