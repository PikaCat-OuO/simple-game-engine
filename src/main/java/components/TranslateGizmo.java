package components;

import editor.PropertiesWindow;
import pikacat.MouseListener;

// 用于移动场景物品的箭头
public class TranslateGizmo extends Gizmo {

    public TranslateGizmo(Sprite arrowSprite, PropertiesWindow propertiesWindow) {
        super(arrowSprite, propertiesWindow);
    }

    @Override
    public void editorUpdate(float deltaTime) {
        if (this.activeGameObject != null) {
            if (xAxisActive) {
                this.activeGameObject.transform.position.x += MouseListener.getWorldDx();
            } else if (yAxisActive) {
                this.activeGameObject.transform.position.y += MouseListener.getWorldDy();
            }
        }

        // 确保更新了物体的位置之后再更新箭头的位置，保证箭头跟紧物体
        super.editorUpdate(deltaTime);
    }
}
