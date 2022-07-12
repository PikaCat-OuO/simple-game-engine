package components;

import editor.PropertiesWindow;
import org.joml.Vector2f;
import org.joml.Vector4f;
import pikacat.GameObject;
import pikacat.MouseListener;
import pikacat.Prefabs;
import pikacat.Window;
import scenes.Scene;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

public class Gizmo extends Component{
    private final Vector4f xAxisColor = new Vector4f(1, 0.3f, 0.3f, 1);
    private final Vector4f xAxisColorHover = new Vector4f(1, 0, 0, 1);
    private final Vector4f yAxisColor = new Vector4f(0.3f, 1, 0.3f , 1);
    private final Vector4f yAxisColorHover = new Vector4f(0, 1, 0, 1);

    private GameObject xAxisObject;
    private GameObject yAxisObject;
    private SpriteRenderer xAxisSprite;
    private SpriteRenderer yAxisSprite;

    private final Vector2f xAxisOffset = new Vector2f(24f / 80f, -6f / 80f);
    private final Vector2f yAxisOffset = new Vector2f(-7f / 80f, 21f / 80f);

    private final float GIZMO_WIDTH = 16f / 80f;
    private final float GIZMO_HEIGHT = 48 / 80f;

    protected boolean xAxisActive = false;
    protected boolean yAxisActive = false;

    // 当前的箭头是否正在使用
    private boolean inUse = false;

    private PropertiesWindow propertiesWindow;

    // 要操作的那个游戏对象
    protected GameObject activeGameObject;

    public Gizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
        this.xAxisObject = Prefabs.generateSpriteObject(arrowSprite, GIZMO_WIDTH, GIZMO_HEIGHT);
        this.yAxisObject = Prefabs.generateSpriteObject(arrowSprite, GIZMO_WIDTH, GIZMO_HEIGHT);

        this.xAxisObject.addComponent(new NoPicking());
        this.yAxisObject.addComponent(new NoPicking());

        this.xAxisSprite = this.xAxisObject.getComponent(SpriteRenderer.class);
        this.yAxisSprite = this.yAxisObject.getComponent(SpriteRenderer.class);

        Scene currentScene = Window.getCurrentScene();
        currentScene.addGameObjectToScene(this.xAxisObject);
        currentScene.addGameObjectToScene(this.yAxisObject);

        this.propertiesWindow = propertiesWindow;
    }

    @Override
    public void start() {
        this.xAxisObject.transform.rotation = 90;
        this.yAxisObject.transform.rotation = 180;
        this.xAxisObject.transform.zIndex = 100;
        this.yAxisObject.transform.zIndex = 100;
        this.xAxisObject.setNoSerialize();
        this.yAxisObject.setNoSerialize();
    }

    @Override
    public void update(float deltaTime) {
        if (inUse) {
            this.setInactive();
        }
    }

    @Override
    public void editorUpdate(float deltaTime) {
        if (!inUse) return;

        this.activeGameObject = this.propertiesWindow.getActiveGameObject();

        if (this.activeGameObject != null) {
            // 箭头跟着物品走
            Vector2f activeObjectPosition = this.activeGameObject.transform.position;
            this.xAxisObject.transform.position.set(activeObjectPosition.x + xAxisOffset.x,
                    activeObjectPosition.y + xAxisOffset.y);
            this.yAxisObject.transform.position.set(activeObjectPosition.x + yAxisOffset.x,
                    activeObjectPosition.y + yAxisOffset.y);
            // 显示箭头
            this.setActive();
            // 更新箭头悬浮状态
            boolean xAxisHover  = checkXHoverState();
            boolean yAxisHover  = checkYHoverState();

            // 如果左键拖拽
            if (MouseListener.isDragging() && MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_LEFT)) {
                if (xAxisHover || xAxisActive) {
                    xAxisActive = true;
                    yAxisActive = false;
                } else if (yAxisHover || yAxisActive) {
                    yAxisActive = true;
                    xAxisActive = false;
                }
            } else {
                xAxisActive = false;
                yAxisActive = false;
            }
        } else {
            // 隐藏箭头
            this.setInactive();
        }
    }

    private void setActive() {
        this.xAxisSprite.setColor(this.xAxisColor);
        this.yAxisSprite.setColor(this.yAxisColor);
    }

    private void setInactive() {
        this.xAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
        this.yAxisSprite.setColor(new Vector4f(0, 0, 0, 0));
    }

    // 检查X方向上的箭头是否悬浮
    private boolean checkXHoverState() {
        Vector2f mousePos = MouseListener.getWorld().add(GIZMO_HEIGHT / 2.0f, GIZMO_WIDTH / 2.0f);
        Vector2f gizmoPosition = xAxisObject.transform.position;
        if (mousePos.x >= gizmoPosition.x && mousePos.x <= gizmoPosition.x + GIZMO_HEIGHT &&
                mousePos.y >= gizmoPosition.y && mousePos.y <= gizmoPosition.y + GIZMO_WIDTH) {
            xAxisSprite.setColor(xAxisColorHover);
            return true;
        } else {
            xAxisSprite.setColor(xAxisColor);
            return false;
        }
    }

    // 检查Y方向上的箭头是否悬浮
    private boolean checkYHoverState() {
        Vector2f mousePos = MouseListener.getWorld().add(GIZMO_WIDTH / 2.0f, GIZMO_HEIGHT / 2.0f);
        Vector2f gizmoPosition = yAxisObject.transform.position;
        if (mousePos.x >= gizmoPosition.x && mousePos.x <= gizmoPosition.x + GIZMO_WIDTH &&
                mousePos.y >= gizmoPosition.y && mousePos.y <= gizmoPosition.y + GIZMO_HEIGHT) {
            yAxisSprite.setColor(yAxisColorHover);
            return true;
        } else {
            yAxisSprite.setColor(yAxisColor);
            return false;
        }
    }

    public void setUsing() {
        this.inUse = true;
    }

    public void setNotUsing() {
        this.inUse = false;
        this.setInactive();
    }

}
