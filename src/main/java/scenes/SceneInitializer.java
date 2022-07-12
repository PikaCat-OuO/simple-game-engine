package scenes;

import components.SpriteRenderer;
import components.StateMachine;
import pikacat.GameObject;
import util.AssetPool;

// 场景初始化器
public abstract class SceneInitializer {
    // 关卡的位置
    private String levelPath;

    public SceneInitializer(String levelPath) {
        this.levelPath = levelPath;
    }

    public void loadResources(Scene scene) {
        // 重置资源这样就不会导致多次加载材质了
        for (GameObject gameObject : scene.getGameObjects()) {
            SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null && spriteRenderer.getTexture() != null) {
                spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilepath()));
            }

            StateMachine stateMachine = gameObject.getComponent(StateMachine.class);
            if (stateMachine != null) {
                stateMachine.refreshTextureID();
            }
        }
    }
    public abstract void init(Scene scene);
    public abstract void imgui();

    public void setLevelPath(String levelPath) {
        this.levelPath = levelPath;
    }

    public String getLevelPath() {
        return this.levelPath;
    }
}
