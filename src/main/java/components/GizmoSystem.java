package components;

import pikacat.KeyListener;
import pikacat.Window;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;

public class GizmoSystem extends Component {
    // 箭头的材质
    private Spritesheet gizmoSprite;

    // 现在正在使用的箭头
    private Gizmo currentGizmo;

    public GizmoSystem(Spritesheet gizmoSprites) {
        this.gizmoSprite = gizmoSprites;
    }

    @Override
    public void start() {
        this.gameObject.addComponent(new TranslateGizmo(gizmoSprite.getSprite(1),
                Window.getImGuiLayer().getPropertiesWindow()));
        this.gameObject.addComponent(new ScaleGizmo(gizmoSprite.getSprite(2),
                Window.getImGuiLayer().getPropertiesWindow()));

        this.currentGizmo = this.gameObject.getComponent(TranslateGizmo.class);
        this.currentGizmo.setUsing();
        this.gameObject.getComponent(ScaleGizmo.class).setNotUsing();
    }

    @Override
    public void editorUpdate(float deltaTime) {
        // 按下ctrl + g键切换箭头
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW_KEY_G)) {
            this.currentGizmo.setNotUsing();
            if (this.currentGizmo instanceof TranslateGizmo) {
                this.currentGizmo = this.gameObject.getComponent(ScaleGizmo.class);
            } else {
                this.currentGizmo = this.gameObject.getComponent(TranslateGizmo.class);
            }
            this.currentGizmo.setUsing();
        }
    }
}
