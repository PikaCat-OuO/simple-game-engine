package components;

import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import pikacat.Direction;
import pikacat.GameObject;
import pikacat.KeyListener;
import pikacat.Window;
import util.AssetPool;

import static org.lwjgl.glfw.GLFW.*;

public class Pipe extends Component {
    // 管道的方向
    private Direction direction;

    // 连接的管道的名字
    private String connectingPipeName = "";

    // 是不是入口
    private boolean isEntrance = false;

    // 与之相连接的管道
    private transient GameObject connectingPipe = null;

    // 进入管道需要的位置
    private transient float entranceVectorTolerance = 0.6f;

    // 玩家
    private transient PlayerController collidingPlayer = null;

    public Pipe(Direction direction) {
        this.direction = direction;
    }

    @Override
    public void start() {
        this.connectingPipe = Window.getCurrentScene().getGameObjectByName(connectingPipeName);
    }

    @Override
    public void update(float deltaTime) {
        // 如果没有连接到任何管道或者不是入口就不处理
        if (!isEntrance || connectingPipeName == null) {
            return;
        }

        // 如果没有接触到游戏对象或者玩家不在入口范围内就不处理
        if (collidingPlayer != null && playerAtEntrance()) {
            boolean playerEntering = false;

            switch (direction) {
                case DOWN -> {
                    if (KeyListener.isKeyPressed(GLFW_KEY_UP) || KeyListener.isKeyPressed(GLFW_KEY_W)) {
                        playerEntering = true;
                    }
                }
                case UP -> {
                    if (KeyListener.isKeyPressed(GLFW_KEY_DOWN) || KeyListener.isKeyPressed(GLFW_KEY_S)) {
                        playerEntering = true;
                    }
                }
                case RIGHT -> {
                    if (KeyListener.isKeyPressed(GLFW_KEY_LEFT) || KeyListener.isKeyPressed(GLFW_KEY_A)) {
                        playerEntering = true;
                    }
                }
                case LEFT -> {
                    if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT) || KeyListener.isKeyPressed(GLFW_KEY_D)) {
                        playerEntering = true;
                    }
                }
            }

            if (playerEntering) {
                collidingPlayer.setPosition(getPlayerPosition(connectingPipe));
                AssetPool.getSound("assets/sounds/pipe.ogg").play();
            }
        }
    }

    public boolean playerAtEntrance() {
        // 管道的左下角
        Vector2f min = new Vector2f(this.gameObject.transform.position)
                .sub(new Vector2f(this.gameObject.transform.scale).mul(0.5f));
        // 管道的右上角
        Vector2f max = new Vector2f(this.gameObject.transform.position)
                .add(new Vector2f(this.gameObject.transform.scale).mul(0.5f));
        // 玩家的左下角
        Vector2f playerMin = new Vector2f(collidingPlayer.gameObject.transform.position)
                .sub(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.45f));
        // 玩家的右上角
        Vector2f playerMax = new Vector2f(collidingPlayer.gameObject.transform.position)
                .add(new Vector2f(collidingPlayer.gameObject.transform.scale).mul(0.45f));

        return switch (direction) {
            case DOWN -> playerMax.y <= min.y && playerMin.x >= min.x && playerMin.x <= max.x;
            case UP -> playerMin.y >= max.y && playerMin.x >= min.x && playerMin.x <= max.x;
            case RIGHT -> playerMin.x >= max.x && playerMin.y >= min.y && playerMax.y <= max.y;
            case LEFT -> playerMax.x <= min.x && playerMin.y >= min.y && playerMax.y <= max.y;
        };
    }

    // 返回连接到某个管道后，玩家传送过去的地方
    private Vector2f getPlayerPosition(GameObject gameObject) {
        return switch (gameObject.getComponent(Pipe.class).direction) {
            case DOWN -> new Vector2f(gameObject.transform.position).sub(0.0f, 0.5f);
            case UP -> new Vector2f(gameObject.transform.position).add(0.0f, 0.5f);
            case RIGHT -> new Vector2f(gameObject.transform.position).add(0.5f, 0.0f);
            case LEFT -> new Vector2f(gameObject.transform.position).sub(0.5f, 0.0f);
        };
    }

    @Override
    public void beginCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            collidingPlayer = playerController;
        }
    }

    // 碰撞结束后清除，玩家只是想走过去，并不想通过管道
    @Override
    public void endCollision(GameObject collidingGameObject, Contact contact, Vector2f contactNormal) {
        PlayerController playerController = collidingGameObject.getComponent(PlayerController.class);
        if (playerController != null) {
            this.collidingPlayer = null;
        }
    }
}
