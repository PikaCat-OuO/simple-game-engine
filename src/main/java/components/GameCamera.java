package components;

import org.joml.Vector4f;
import pikacat.Camera;
import pikacat.GameObject;
import pikacat.Window;
import util.Settings;

public class GameCamera extends Component {
    private transient GameObject player;
    private transient Camera gameCamera;

    // 水平线
    private transient float undergroundYLevel = 0.0f;
    // 地下摄像机的偏移位置
    private transient float cameraBuffer = 1.0f;

    // 进入地下的判定
    private transient float playerBuffer = Settings.GRID_HEIGHT;

    // 蓝天的颜色
    private Vector4f skyColor = new Vector4f(92.0f / 255.0f, 148.0f / 255.0f, 252.0f/ 255.0f, 1);

    // 地下的颜色
    private Vector4f undergroundColor = new Vector4f(0, 0, 0, 1);

    public GameCamera(Camera gameCamera) {
        this.gameCamera = gameCamera;
    }

    @Override
    public void start() {
        this.player = Window.getCurrentScene().getGameObjectWith(PlayerController.class);
        // 默认设置为天空的蓝色
        this.gameCamera.clearColor.set(skyColor);
        // 调整地下摄像机的位置
        this.undergroundYLevel = this.gameCamera.position.y - this.gameCamera.getProjectionSize().y - this.cameraBuffer;
    }

    @Override
    public void update(float deltaTime) {
        // 玩家赢了播放动画时不要移动摄像头
        if (player != null && !player.getComponent(PlayerController.class).hasWon()) {
            // 调整摄像机到马里奥的身后2.5f的位置
            this.gameCamera.position.x = Math.max(gameCamera.position.x, player.transform.position.x - 2.5f);

            if (player.transform.position.y < -playerBuffer) {
                // 玩家在地下，变成地下的位置
                this.gameCamera.position.y = undergroundYLevel;
                // 设置地下的颜色
                this.gameCamera.clearColor.set(undergroundColor);
            } else {
                // 玩家在地上，变成地上的位置
                this.gameCamera.position.y = 0.0f;
                // 设置地下的颜色
                this.gameCamera.clearColor.set(skyColor);
            }
        }
    }
}
