package components;

import org.joml.Vector2f;
import renderer.Texture;

public class Sprite {

    // 精灵的宽度和高度
    private int width, height;

    // 精灵的材质
    private Texture texture = null;
    // 精灵的材质坐标
    private Vector2f[] texCoords = {
            new Vector2f(1, 1),
            new Vector2f(1, 0),
            new Vector2f(0, 0),
            new Vector2f(0, 1)
    };

    public Texture getTexture() {
        return this.texture;
    }

    public Vector2f[] getTexCoords() {
        return this.texCoords;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public void setTexCoords(Vector2f[] texCoords) {
        this.texCoords = texCoords;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getTexID() {
        if (texture == null) {
            return -1;
        } else {
            return texture.getTexID();
        }
    }
}
