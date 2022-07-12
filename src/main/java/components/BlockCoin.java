package components;

import pikacat.Transform;
import util.AssetPool;
import util.Settings;

public class BlockCoin extends Component {
    private float topY;
    private float coinSpeed = 1.4f;

    @Override
    public void start() {
        topY = this.gameObject.transform.position.y + Settings.GRID_HEIGHT * 2.0f;
        AssetPool.getSound("assets/sounds/coin.ogg").play();
    }

    @Override
    public void update(float deltaTime) {
        Transform transform = this.gameObject.transform;
        if (transform.position.y < topY) {
            transform.position.y += coinSpeed * deltaTime;
            transform.scale.x -= (0.5f * deltaTime) % -1.0f;
        } else {
            gameObject.destroy();
        }
    }
}
