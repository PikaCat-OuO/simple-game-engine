package editor;

import components.SpriteRenderer;
import imgui.ImGui;
import org.joml.Vector4f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.RigidBody2D;
import pikacat.GameObject;
import renderer.PickingTexture;

import java.util.ArrayList;
import java.util.List;

public class PropertiesWindow {
    // 鼠标圈起来的多个游戏对象
    private List<GameObject> activeGameObjects;

    // 鼠标圈起来的多个游戏对象原来的颜色
    private List<Vector4f> activeGameObjectsOriginalColor;

    // 用于处理捡起来物品的类
    private PickingTexture pickingTexture;

    public PropertiesWindow(PickingTexture pickingTexture) {
        this.activeGameObjects = new ArrayList<>();
        this.pickingTexture = pickingTexture;
        this.activeGameObjectsOriginalColor = new ArrayList<>();
    }

    public void imgui() {
        if (this.activeGameObjects.size() == 1) {
            GameObject activeGameObject = this.activeGameObjects.get(0);
            ImGui.begin("Properties");

            // 右键菜单
            if (ImGui.beginPopupContextWindow("ComponentAdder")) {
                // 添加物理实体
                if (ImGui.menuItem("Add RigidBody")) {
                    if (activeGameObject.getComponent(RigidBody2D.class) == null) {
                        activeGameObject.addComponent(new RigidBody2D());
                    }
                }

                // 添加箱体碰撞
                if (ImGui.menuItem("Add Box Collider")) {
                    if (activeGameObject.getComponent(Box2DCollider.class) == null &&
                            activeGameObject.getComponent(CircleCollider.class) == null) {
                        activeGameObject.addComponent(new Box2DCollider());
                    }
                }

                // 添加圆心碰撞
                if (ImGui.menuItem("Add Circle Collider")) {
                    if (activeGameObject.getComponent(Box2DCollider.class) == null &&
                            activeGameObject.getComponent(CircleCollider.class) == null) {
                        activeGameObject.addComponent(new CircleCollider());
                    }
                }

                ImGui.endPopup();
            }

            activeGameObject.imgui();
            ImGui.end();
        }
    }

    public GameObject getActiveGameObject() {
        if (this.activeGameObjects.size() == 1) {
            return this.activeGameObjects.get(0);
        }
        return null;
    }

    public List<GameObject> getActiveGameObjects() {
        return activeGameObjects;
    }

    public Vector4f getActiveGameObjectsOriginalColor() {
        return activeGameObjectsOriginalColor.get(0);
    }

    public void clearSelected() {
        // 取消选中恢复颜色
        for (int i = 0; i < this.activeGameObjectsOriginalColor.size(); ++i) {
            SpriteRenderer spriteRenderer = this.activeGameObjects.get(i).getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {
                spriteRenderer.setColor(this.activeGameObjectsOriginalColor.get(i));
            }
        }
        this.activeGameObjectsOriginalColor.clear();
        this.activeGameObjects.clear();
    }

    public void setActiveGameObject(GameObject gameObject) {
        if (gameObject != null) {
            clearSelected();
            this.addActiveGameObject(gameObject);
        }
    }

    public void addActiveGameObject(GameObject gameObject) {
        // 选中的物体的黄色高亮
        SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null) {
            this.activeGameObjectsOriginalColor.add(new Vector4f(spriteRenderer.getColor()));
            spriteRenderer.setColor(new Vector4f(0.8f, 0.8f, 0.0f, 0.8f));
        } else {
            this.activeGameObjectsOriginalColor.add(new Vector4f());
        }
        this.activeGameObjects.add(gameObject);
    }

    public PickingTexture getPickingTexture() {
        return pickingTexture;
    }
}
