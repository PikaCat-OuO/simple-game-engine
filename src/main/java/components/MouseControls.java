package components;

import editor.PropertiesWindow;
import org.joml.Vector2f;
import org.joml.Vector4f;
import pikacat.GameObject;
import pikacat.KeyListener;
import pikacat.MouseListener;
import pikacat.Window;
import renderer.DebugDraw;
import renderer.PickingTexture;
import scenes.Scene;
import util.Settings;

import java.util.HashSet;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

// 主要解决关卡编辑器的物品拖拉问题
public class MouseControls extends Component {
    GameObject holdingGameObject = null;

    // 放置单次点击放置多个物品
    private float debounceTime = 0.0f;

    // 盒子框选功能
    private boolean boxSelectSet = false;
    private Vector2f boxSelectStart = new Vector2f();
    private Vector2f boxSelectEnd = new Vector2f();

    public void pickupObject(GameObject gameObject) {
        // 如果现在手中还拿着东西，先销毁掉
        if (this.holdingGameObject != null) {
            this.holdingGameObject.destroy();
        }
        this.holdingGameObject = gameObject;
        // 这个物品不可捡起来
        this.holdingGameObject.addComponent(new NoPicking());
        // 将这个物品透明一下
        this.holdingGameObject.getComponent(SpriteRenderer.class)
                .setColor(new Vector4f(0.8f, 0.8f, 0.8f, 0.5f));
        // 将这个物品添加到场景中
        Window.getCurrentScene().addGameObjectToScene(gameObject);
    }

    public void place() {
        GameObject gameObject = this.holdingGameObject.copy();
        // 先刷新材质ID，因为材质ID也复制了
        StateMachine stateMachine = gameObject.getComponent(StateMachine.class);
        if (stateMachine != null) {
            stateMachine.refreshTextureID();
        }
        // 恢复颜色
        gameObject.getComponent(SpriteRenderer.class).setColor(new Vector4f(1, 1, 1, 1));
        // 解除不可捡起属性
        gameObject.removeComponent(NoPicking.class);
        Window.getCurrentScene().addGameObjectToScene(gameObject);
    }

    @Override
    public void editorUpdate(float deltaTime) {
        if (!Window.getImGuiLayer().getGameViewWindow().getWantCaptureMouse()) {
            return;
        }

        PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
        PickingTexture pickingTexture = propertiesWindow.getPickingTexture();
        Scene currentScene = Window.getCurrentScene();

        if (holdingGameObject != null) {
            debounceTime -= deltaTime;

            // 物品跟着鼠标走
            Vector2f holdingObjectPosition = holdingGameObject.transform.position;
            holdingObjectPosition.x = (int) Math.floor(MouseListener.getWorldX() / Settings.GRID_WIDTH)
                    * Settings.GRID_WIDTH;
            holdingObjectPosition.y = (int) Math.floor(MouseListener.getWorldY() / Settings.GRID_HEIGHT)
                    * Settings.GRID_HEIGHT;
            holdingObjectPosition.add(Settings.GRID_WIDTH / 2.0f, Settings.GRID_HEIGHT / 2.0f);

            // 点击了鼠标左键就放下
            if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                // 左键拖拉放置，需要格子里面没有东西才能放置
                if (MouseListener.isDragging() && !blockInSquare(holdingObjectPosition)) {
                    place();
                } else if (!MouseListener.isDragging() && debounceTime <= 0){
                    place();
                    debounceTime = 0.2f;
                }
            }

            // 键盘esc取消选中
            if (KeyListener.keyBeginPress(GLFW_KEY_ESCAPE)) {
                this.holdingGameObject.destroy();
                this.holdingGameObject = null;
            }
        }
        // 如果鼠标点击了但是又不是拖拽状态就开启物品拾取器的功能
        else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT) && !MouseListener.isDragging()) {
            int x = (int) MouseListener.getScreenX();
            int y = (int) MouseListener.getScreenY();
            GameObject gameObject = currentScene.getGameObjectByUID(pickingTexture.readPixel(x, y));
            if (gameObject != null && gameObject.getComponent(NoPicking.class) == null) {
                propertiesWindow.setActiveGameObject(gameObject);
            }
            // 必须要保证鼠标不在拖拽状态才能设置为空，否则在移动时鼠标可能移动其他地方，游戏物品跟不上，这样就导致游戏物品失去焦点
            else if (gameObject == null && !MouseListener.isDragging()) {
                propertiesWindow.clearSelected();
            }
        }
        // 盒子选择功能
        else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_RIGHT) && MouseListener.isDragging()) {
            // 第一次盒子框选记录起点
            if (!this.boxSelectSet) {
                this.boxSelectSet = true;
                // 盒子框选需要清除原有选择的物品
                propertiesWindow.clearSelected();
                this.boxSelectStart = MouseListener.getWorld();
            }
            // 途中记录终点
            this.boxSelectEnd = MouseListener.getWorld();

            // 盒子的半边长
            Vector2f halfSize = new Vector2f(this.boxSelectEnd).sub(this.boxSelectStart).absolute().div(2);
            // 盒子的中心
            Vector2f center = new Vector2f(this.boxSelectEnd).add(this.boxSelectStart).div(2);

            // 画盒子
            DebugDraw.addBox2D(center, halfSize, 0.0f);
        }
        // 拖拽完成
        else if (boxSelectSet) {
            this.boxSelectSet = false;

            // 获取捡起来的物品
            float[] gameObjectIDs = pickingTexture.readPixels(
                    MouseListener.worldToScreen(this.boxSelectStart),
                    MouseListener.worldToScreen(this.boxSelectEnd));

            // 获取选择的所有的游戏对象
            Set<Integer> uniqueGameObjectIDs = new HashSet<>();
            for (float gameObjectID : gameObjectIDs) {
                uniqueGameObjectIDs.add((int) gameObjectID);
            }

            for (Integer gameObjectID : uniqueGameObjectIDs) {
                GameObject pickedGameObject = Window.getCurrentScene().getGameObjectByUID(gameObjectID);
                if (pickedGameObject != null && pickedGameObject.getComponent(NoPicking.class) == null) {
                    propertiesWindow.addActiveGameObject(pickedGameObject);
                }
            }
        }
    }

    // 某个格子下面是否有东西
    private boolean blockInSquare(Vector2f position) {
        PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();
        PickingTexture pickingTexture = propertiesWindow.getPickingTexture();

        Vector2f start = MouseListener.worldToScreen(new Vector2f(position)
                .sub(Settings.GRID_WIDTH / 2.0f, Settings.GRID_HEIGHT / 2.0f));
        Vector2f end = MouseListener.worldToScreen(new Vector2f(position)
                .add(Settings.GRID_WIDTH / 2.0f, Settings.GRID_HEIGHT / 2.0f));

        // 加上一定的偏移读取
        float[] gameObjectIDs = pickingTexture.readPixels(start.add(2, 2), end.sub(2, 2));

        for (float gameObjectID : gameObjectIDs) {
            if (gameObjectID >= 0) {
                GameObject gameObject = Window.getCurrentScene().getGameObjectByUID((int) gameObjectID);
                if (gameObject.getComponent(NoPicking.class) == null) {
                    return true;
                }
            }
        }

        return false;
    }

    // 清除现在手里拿着的东西
    public void clearHoldingObject() {
        if (this.holdingGameObject != null) {
            this.holdingGameObject.destroy();
            this.holdingGameObject = null;
        }
    }
}
