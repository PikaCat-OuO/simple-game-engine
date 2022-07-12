package pikacat;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class KeyListener {
    // 单例模式，饿汉式
    private static final KeyListener instance = new KeyListener();

    // 当前键盘的哪个键被按下了
    private boolean[] keyPressed = new boolean[350];
    private boolean[] keyBeginPress = new boolean[350];

    // 单例模式
    private KeyListener() {
    }

    // 获取键盘实例
    public static KeyListener getInstance() {
        return KeyListener.instance;
    }

    // 键盘按键回调函数
    public static void keyCallback(long window, int key, int scanCode, int action, int mods) {
        // 获取实例
        KeyListener keyListener = getInstance();

        // 限制按钮个数
        if (key >=0 && key < keyListener.keyPressed.length) {
            if (GLFW_PRESS == action) {
                // 设置键盘按下
                keyListener.keyPressed[key] = true;
                keyListener.keyBeginPress[key] = true;
            } else if (GLFW_RELEASE == action) {
                // 设置键盘弹起
                keyListener.keyPressed[key] = false;
                keyListener.keyBeginPress[key] = false;
            }
        }
    }

    // 键盘的某个键是否被按下
    public static boolean isKeyPressed(int keyCode) {
        // 获取实例
        KeyListener keyListener = getInstance();

        // 检查是否满足长度限制
        if (keyCode < keyListener.keyPressed.length) {
            // 返回按钮状态
            return keyListener.keyPressed[keyCode];
        }

        // 不满足就返回假
        return false;
    }

    // 键盘的某个键是否被按下
    public static boolean keyBeginPress(int keyCode) {
        // 获取实例
        KeyListener keyListener = getInstance();

        // 检查是否满足长度限制
        if (keyCode < keyListener.keyPressed.length) {
            // 返回按钮状态
            return keyListener.keyBeginPress[keyCode];
        }

        return false;
    }

    // 清除按钮第一次被按下的状态
    public static void endFrame() {
        Arrays.fill(getInstance().keyBeginPress, false);
    }
}
