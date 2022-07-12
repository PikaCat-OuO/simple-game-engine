package components;

import org.joml.Vector2f;
import physics2d.RayCastInfo;
import pikacat.DialogSystem;
import pikacat.Window;

public class Dialog extends Component {
    // 弹出对话的偏移位置
    private Vector2f offset;

    // 对话的内容
    private String[] dialogs;

    @Override
    public void update(float deltaTime) {
        if (DialogSystem.isDialogEnd()) {
            // 开启对话的逻辑判断
            Vector2f rayCastBegin = new Vector2f(gameObject.transform.position);
            Vector2f rayCastEnd = new Vector2f(gameObject.transform.position).add(offset);
            RayCastInfo rayCastInfo = Window.getPhysics().rayCast(gameObject, rayCastBegin, rayCastEnd);

            if (rayCastInfo.hit && rayCastInfo.hitObject != null &&
                    rayCastInfo.hitObject.getComponent(PlayerController.class) != null) {
                // 开始对话
                DialogSystem.openDialog(rayCastInfo.hitObject, this.gameObject, this.dialogs);
            }
        }
    }
}
