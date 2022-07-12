package pikacat;

import imgui.ImGui;
import org.joml.Vector2f;
import util.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;

// 对话系统
public class DialogSystem {
    // 对话相关
    private static transient int count = 0;
    private static transient GameObject objA, objB;
    private static transient String[] dialogs;

    public static void reset() {
        DialogSystem.objA = null;
        DialogSystem.objB = null;
        DialogSystem.dialogs = null;
        DialogSystem.count = 0;
    }

    public static void openDialog(GameObject objA, GameObject objB, String[] dialogs) {
        DialogSystem.dialogs = dialogs;
        DialogSystem.objA = objA;
        DialogSystem.objB = objB;
        DialogSystem.count = 0;
    }

    public static boolean isDialogEnd() {
        return objA == null || objB == null || DialogSystem.count >= DialogSystem.dialogs.length;
    }

    public static void imgui() {
        if (objA != null && objB != null && count < dialogs.length) {
            ImGui.setNextWindowSize(300, 150);
            if (count % 2 == 0) {
                Vector2f realScreenPos = MouseListener.getRealScreenPos(objA.transform.position);
                ImGui.setNextWindowPos(realScreenPos.x, Settings.RESOLUTION_HEIGHT - realScreenPos.y - 150);
                ImGui.begin(objA.name);
            } else {
                Vector2f realScreenPos = MouseListener.getRealScreenPos(objB.transform.position);
                ImGui.setNextWindowPos(realScreenPos.x, Settings.RESOLUTION_HEIGHT - realScreenPos.y - 150);
                ImGui.begin(objB.name);
            }
            ImGui.textWrapped(dialogs[count]);
            ImGui.end();

            // 键盘N键继续对话
            if (KeyListener.keyBeginPress(GLFW_KEY_N)) {
                ++DialogSystem.count;
            }
        }
    }
}
