package components;

import editor.PropertiesWindow;
import pikacat.GameObject;
import pikacat.KeyListener;
import pikacat.Window;
import util.Settings;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class KeyControls extends Component {
    private final PropertiesWindow propertiesWindow = Window.getImGuiLayer().getPropertiesWindow();

    private float debounce = 0.0f;
    private float debounceTime = 0.1f;

    @Override
    public void editorUpdate(float deltaTime) {
        debounce -= deltaTime;

        GameObject activeGameObject = this.propertiesWindow.getActiveGameObject();
        List<GameObject> activeGameObjects = this.propertiesWindow.getActiveGameObjects();

        // ctrl+d复制物品
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW_KEY_D) &&
                activeGameObject != null) {
            GameObject newObj = activeGameObject.copy();
            newObj.transform.position.x += Settings.GRID_WIDTH;
            // 刷新材质ID
            StateMachine stateMachine = newObj.getComponent(StateMachine.class);
            if (stateMachine != null) {
                stateMachine.refreshTextureID();
            }
            Window.getCurrentScene().addGameObjectToScene(newObj);
            this.propertiesWindow.setActiveGameObject(newObj);
        }

        // ctrl+d复制多个物品
        else if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW_KEY_D) &&
                activeGameObjects.size() > 1) {
            List<GameObject> pendingObject = new ArrayList<>(this.propertiesWindow.getActiveGameObjects());
            propertiesWindow.clearSelected();
            for (GameObject gameObject : pendingObject) {
                GameObject newObj = gameObject.copy();
                // 刷新材质ID
                StateMachine stateMachine = newObj.getComponent(StateMachine.class);
                if (stateMachine != null) {
                    stateMachine.refreshTextureID();
                }
                Window.getCurrentScene().addGameObjectToScene(newObj);
                propertiesWindow.addActiveGameObject(newObj);
            }
        }

        // del删除物品
        else if (KeyListener.keyBeginPress(GLFW_KEY_DELETE)) {
            for (GameObject gameObject : activeGameObjects) {
                gameObject.destroy();
            }
            this.propertiesWindow.clearSelected();
        }

        else if (debounce < 0) {
            float multiplier = KeyListener.isKeyPressed(GLFW_KEY_LEFT_SHIFT) ? 0.1f : 1.0f;
            debounce = debounceTime;
            // 上下翻页按键移动z坐标
            if (KeyListener.isKeyPressed(GLFW_KEY_PAGE_DOWN)) {
                for (GameObject gameObject : activeGameObjects) {
                    --gameObject.transform.zIndex;
                }
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_PAGE_UP)) {
                for (GameObject gameObject : activeGameObjects) {
                    ++gameObject.transform.zIndex;
                }
            }

            // 上下左右移动物品
            if (KeyListener.isKeyPressed(GLFW_KEY_DOWN)) {
                for (GameObject gameObject : activeGameObjects) {
                    gameObject.transform.position.y -= Settings.GRID_HEIGHT * multiplier;
                }
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_UP)) {
                for (GameObject gameObject : activeGameObjects) {
                    gameObject.transform.position.y += Settings.GRID_HEIGHT * multiplier;
                }
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_LEFT)) {
                for (GameObject gameObject : activeGameObjects) {
                    gameObject.transform.position.x -= Settings.GRID_WIDTH * multiplier;
                }
            }
            if (KeyListener.isKeyPressed(GLFW_KEY_RIGHT)) {
                for (GameObject gameObject : activeGameObjects) {
                    gameObject.transform.position.x += Settings.GRID_WIDTH * multiplier;
                }
            }
        }
    }
}
