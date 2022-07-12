package components;

import org.joml.Vector2f;
import pikacat.Camera;
import pikacat.KeyListener;
import pikacat.MouseListener;
import util.Settings;

import static org.lwjgl.glfw.GLFW.*;

public class EditorCamera extends Component {
    private boolean isDragging = false;

    private Camera levelEditorCamera;
    private Vector2f clickOrigin;

    private boolean reset = false;
    private float lerpTime = 0.0f;

    public EditorCamera(Camera levelEditorCamera) {
        this.levelEditorCamera = levelEditorCamera;
        this.clickOrigin = new Vector2f();
    }

    @Override
    public void editorUpdate(float deltaTime) {
        // 鼠标中间拖动画面
        if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE) && !isDragging) {
            // 记录鼠标的原始位置
            this.clickOrigin = MouseListener.getWorld();
            isDragging = true;
        } else if (MouseListener.mouseButtonDown(GLFW_MOUSE_BUTTON_MIDDLE)) {
            // 获得鼠标现在的位置
            Vector2f mousePos = MouseListener.getWorld();
            // 获得偏移
            Vector2f delta = new Vector2f(mousePos).sub(this.clickOrigin);
            // 慢慢移动摄像机
            levelEditorCamera.position.sub(delta.mul(deltaTime).mul(Settings.MOUSE_MOVE_SENSITIVITY));

            this.clickOrigin.lerp(mousePos, deltaTime);
        } else if (isDragging) {
            isDragging = false;
        }

        // 鼠标滚轮放大缩小
        if (MouseListener.getScrollY() != 0.0f) {
            float addValue = (float) Math.pow(Math.abs(MouseListener.getScrollY() * Settings.MOUSE_SCROLL_SENSITIVITY),
                    1 / levelEditorCamera.getZoom());
            addValue *= -Math.signum(MouseListener.getScrollY());
            this.levelEditorCamera.addZoom(addValue);
        }

        // 按ctrl+r恢复原来的位置
        if (KeyListener.isKeyPressed(GLFW_KEY_LEFT_CONTROL) && KeyListener.keyBeginPress(GLFW_KEY_R)) {
            reset = true;
        }

        // 是否恢复原来的位置
        if (reset) {
            levelEditorCamera.position.lerp(new Vector2f(), lerpTime);
            levelEditorCamera.setZoom(this.levelEditorCamera.getZoom() +
                    (1.0f - this.levelEditorCamera.getZoom()) * lerpTime);

            // 慢慢恢复到原来的位置
            this.lerpTime += 0.1f * deltaTime;

            // 如果接近目标范围，直接snap进去
            if (Math.abs(levelEditorCamera.position.x) <= 5.0f && Math.abs(levelEditorCamera.position.y) <= 5.0f) {
                this.lerpTime = 0.0f;
                levelEditorCamera.position.set(0, 0);
                this.levelEditorCamera.setZoom(1.0f);
                reset = false;
            }
        }
    }
}
