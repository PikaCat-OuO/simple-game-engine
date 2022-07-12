package components;

import editor.JImGui;
import org.joml.Vector2f;
import org.joml.Vector4f;
import pikacat.Transform;
import pikacat.Window;
import renderer.Texture;

public class SpriteRenderer extends Component {

    // 精灵的颜色
    private Vector4f color = new Vector4f(1, 1, 1, 1);
    // 精灵
    private Sprite sprite = new Sprite();

    // 精灵的位置
    private transient Transform lastTransform;

    // 是否需要更新
    private transient boolean isDirty = true;

    @Override
    public void start() {
        this.lastTransform = gameObject.transform.copy();
    }

    @Override
    public void editorUpdate(float deltaTime) {
        // 检查位置是否变化，如果变化了就要更新
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            this.isDirty = true;
        }
    }

    @Override
    public void update(float deltaTime) {
        // 检查位置是否变化，如果变化了就要更新
        if (!this.lastTransform.equals(this.gameObject.transform)) {
            this.gameObject.transform.copy(this.lastTransform);
            this.isDirty = true;
        }
    }

    @Override
    public void imgui() {
        if (JImGui.colorPicker4("Color Picker",
                Window.getImGuiLayer().getPropertiesWindow().getActiveGameObjectsOriginalColor())) {
            this.isDirty = true;
        }
    }

    public Vector4f getColor() {
        return this.color;
    }

    public Texture getTexture() {
        return this.sprite.getTexture();
    }

    public Vector2f[] getTexCoords() {
        return this.sprite.getTexCoords();
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.isDirty = true;
    }

    public void setColor(Vector4f color) {
        if (!this.color.equals(color)) {
            this.isDirty = true;
            this.color.set(color);
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void setClean() {
        this.isDirty = false;
    }

    public void setDirty() {
        this.isDirty = true;
    }

    public void setTexture(Texture texture) {
        this.sprite.setTexture(texture);
    }
}
