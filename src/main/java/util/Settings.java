package util;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;

public class Settings {
    // 屏幕的显示分辨率，默认与屏幕分辨率匹配
    public static final int RESOLUTION_WIDTH;
    public static final int RESOLUTION_HEIGHT;

    // 关卡编辑器的格子的宽高
    public static final float GRID_WIDTH = 0.25f;
    public static final float GRID_HEIGHT = 0.25f;

    // 鼠标移动灵敏度
    public static final float MOUSE_MOVE_SENSITIVITY = 30.0f;
    // 鼠标滚轮灵敏度
    public static final float MOUSE_SCROLL_SENSITIVITY = 0.1f;

    static {
        // 设置OpenGL出现错误时错误输出的位置
        GLFWErrorCallback.createPrint(System.err).set();

        // 初始化GLFW框架
        if (!glfwInit()) {
            assert false :"不能初始化GLFW框架";
        }

        // 获取屏幕分辨率
        GLFWVidMode glfwVidMode = glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        assert glfwVidMode != null : "错误： 无法获取屏幕分辨率";
        RESOLUTION_WIDTH = glfwVidMode.width();
        RESOLUTION_HEIGHT = glfwVidMode.height() - 63;
    }

    // 获得分辨率的宽高比
    public static float getResolutionRatio() {
        return (float) RESOLUTION_WIDTH / RESOLUTION_HEIGHT;
    }
}
