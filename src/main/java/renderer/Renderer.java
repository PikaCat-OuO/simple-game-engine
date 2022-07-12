package renderer;

import components.SpriteRenderer;
import pikacat.GameObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Renderer {
    private final int MAX_BATCH_SIZE = 1000;
    private List<RenderBatch> batches = new ArrayList<>();

    // 着色器
    private static Shader currentShader;

    public Renderer() {

    }

    // 添加一个游戏对象到当前的渲染器中
    public void add(GameObject gameObject) {
        SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null) {
            add(spriteRenderer);
        }
    }

    // 添加一个sprite到当前的渲染组中
    public void add(SpriteRenderer spriteRenderer) {
        boolean added = false;
        int spriteZIndex = spriteRenderer.gameObject.transform.zIndex;
        for (RenderBatch renderBatch : batches) {
            if (renderBatch.hasRoom() && spriteZIndex == renderBatch.getzIndex()) {
                Texture texture = spriteRenderer.getTexture();
                if (texture == null ||
                        (renderBatch.hasTexture(texture) || renderBatch.hasTextureRoom())) {
                    renderBatch.addSprite(spriteRenderer);
                    added = true;
                    break;
                }
            }
        }

        // 如果所有渲染组都已经满了就新造一个
        if (!added) {
            RenderBatch renderBatch = new RenderBatch(MAX_BATCH_SIZE, spriteZIndex, this);
            renderBatch.start();
            renderBatch.addSprite(spriteRenderer);
            batches.add(renderBatch);
            // 如果新添加了一个渲染组，就需要重排序
            Collections.sort(this.batches);
        }
    }

    // 从渲染组中移除一个游戏对象
    public void destoryGameObject(GameObject gameObject) {
        if (gameObject.getComponent(SpriteRenderer.class) == null) {
            return;
        }

        for (RenderBatch renderBatch : batches) {
            if (renderBatch.destroyIfExist(gameObject)) {
                return;
            }
        }
    }

    // 渲染所有的渲染组
    public void render() {
        currentShader.use();
        for (int i = 0; i < batches.size(); ++i) {
            batches.get(i).render();
        }
    }

    public static void bindShader(Shader shader) {
        currentShader = shader;
    }

    public static Shader getBoundShader() {
        return currentShader;
    }
}
