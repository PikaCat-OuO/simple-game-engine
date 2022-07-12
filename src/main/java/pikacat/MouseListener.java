package pikacat;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import util.Settings;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

public class MouseListener {
    // 单例模式，饿汉式
    private static final MouseListener instance = new MouseListener();

    // 鼠标滚动位置
    private double scrollX, scrollY;

    // 鼠标的现在的位置和原来的位置
    private double xPos, yPos, lastX, lastY, worldX, worldY, lastWorldX, lastWorldY;

    // 当前鼠标的哪个键被按下了
    private boolean[] mouseButtonPressed = new boolean[9];

    // 有多少个按键被按下了
    private int mouseButtonDown = 0;

    // 现在鼠标是否被拖拽
    private boolean isDragging;

    // 视图的位置和宽高
    private Vector2f gameViewPortPos = new Vector2f();
    private Vector2f gameViewPortSize = new Vector2f();

    // 单例模式
    private MouseListener() {
        // 初始化鼠标的值
        this.scrollX = 0.0;
        this.scrollY = 0.0;
        this.xPos = 0.0;
        this.yPos = 0.0;
        this.lastX = 0.0;
        this.lastY = 0.0;
        this.worldX = 0.0;
        this.worldY = 0.0;
        this.lastWorldX = 0.0;
        this.lastWorldY = 0.0;
    }

    // 获取鼠标实例
    public static MouseListener getInstance() {
        return MouseListener.instance;
    }

    // 鼠标移动的回调函数
    public static void mousePosCallback(long window, double xPos, double yPos) {
        // 获取实例
        MouseListener mouseListener = getInstance();

        // 如果在鼠标移动的过程中有至少一个按钮被按下了就是拖动
        if (mouseListener.mouseButtonDown > 0) {
            mouseListener.isDragging = true;
        }

        // 记录原来的位置
        mouseListener.lastX = mouseListener.xPos;
        mouseListener.lastY = mouseListener.yPos;
        mouseListener.lastWorldX = mouseListener.worldX;
        mouseListener.lastWorldY = mouseListener.worldY;

        // 设置新的鼠标位置
        mouseListener.xPos = xPos;
        mouseListener.yPos = yPos;
        calcWorldPos();
    }

    // 鼠标按钮的回调函数
    public static void mouseButtonCallback(long window, int button, int action, int mods) {
        // 获取实例
        MouseListener mouseListener = getInstance();

        // 限制按钮个数
        if (button < mouseListener.mouseButtonPressed.length) {
            if (GLFW_PRESS == action) {
                // 点击事件
                mouseListener.mouseButtonPressed[button] = true;
                ++mouseListener.mouseButtonDown;
            } else if (GLFW_RELEASE == action) {
                // 鼠标释放了
                mouseListener.mouseButtonPressed[button] = false;
                --mouseListener.mouseButtonDown;
                // 同时根据情况解除鼠标移动标志
                mouseListener.isDragging = mouseListener.mouseButtonDown != 0;
            }
        }
    }

    // 鼠标滚动事件
    public static void mouseScrollCallback(long window, double xOffset, double yOffset) {
        // 获取实例
        MouseListener mouseListener = getInstance();

        // 设置相应的鼠标滚动坐标
        mouseListener.scrollX = xOffset;
        mouseListener.scrollY = yOffset;
    }

    // 最后一帧
    public static void endFrame() {
        // 获取实例
        MouseListener mouseListener = getInstance();

        // 恢复所有的鼠标坐标
        mouseListener.scrollX = 0;
        mouseListener.scrollY = 0;
        mouseListener.lastX = mouseListener.xPos;
        mouseListener.lastY = mouseListener.yPos;
        mouseListener.lastWorldX = mouseListener.worldX;
        mouseListener.lastWorldY = mouseListener.worldY;
    }

    // 计算鼠标在世界的坐标
    public static void calcWorldPos() {
        // 获得鼠标在窗口的坐标
        float currentX = getX() - getInstance().gameViewPortPos.x;
        float currentY = getInstance().gameViewPortPos.y + getInstance().gameViewPortSize.y - getY();
        Vector2f worldCoords = screenToWorld(new Vector2f(currentX, currentY));
        getInstance().worldX = worldCoords.x;
        getInstance().worldY = worldCoords.y;
    }

    // 屏幕坐标到世界坐标
    public static Vector2f screenToWorld(Vector2f screenCoords) {
        // 转化为-1到1的标准化坐标
        float currentX = (screenCoords.x / getInstance().gameViewPortSize.x) * 2.0f - 1.0f;
        float currentY = (screenCoords.y / getInstance().gameViewPortSize.y) * 2.0f - 1.0f;
        // 作映射逆变换
        Vector4f tmp = new Vector4f(currentX, currentY, 0, 1);
        Camera camera = Window.getCurrentScene().getCamera();
        Matrix4f inverseProjection = new Matrix4f(camera.getInverseProjection());
        Matrix4f inverseView = new Matrix4f(camera.getInverseView());
        tmp.mul(inverseView.mul(inverseProjection));
        return new Vector2f(tmp.x, tmp.y);
    }

    // 世界坐标到视图的屏幕坐标
    public static Vector2f worldToScreen(Vector2f worldCoords) {
        MouseListener mouseListener = getInstance();

        Camera camera = Window.getCurrentScene().getCamera();
        Vector4f ndcSpacePos = new Vector4f(worldCoords.x, worldCoords.y, 0, 1);
        Matrix4f view = new Matrix4f(camera.getViewMatrix());
        Matrix4f projection = new Matrix4f(camera.getProjectionMatrix());
        ndcSpacePos.mul(projection.mul(view));
        Vector2f windowSpace = new Vector2f(ndcSpacePos.x, ndcSpacePos.y).mul(1.0f / ndcSpacePos.w);
        // 转换为[0到1]的标准区间
        windowSpace.add(new Vector2f(1.0f, 1.0f)).div(2);

        // 匹配buffer大小
        return windowSpace.mul(Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT);
    }

    // 获取X坐标
    public static float getX() {
        return (float) getInstance().xPos;
    }

    // 获取Y坐标
    public static float getY() {
        return (float) getInstance().yPos;
    }

    // 获取Dx坐标
    public static float getDx() {
        return (float) (getInstance().xPos - getInstance().lastX);
    }

    // 获取Dy坐标
    public static float getDy() {
        return (float) (getInstance().yPos - getInstance().lastY);
    }

    // 获得鼠标在游戏视图的X坐标
    public static float getScreenX() {
        // 获得鼠标在窗口的坐标
        float currentX = getX() - getInstance().gameViewPortPos.x;
        // 转化为分辨率大小以匹配帧缓冲
        return currentX / getInstance().gameViewPortSize.x * Settings.RESOLUTION_WIDTH;
    }

    // 获得鼠标在游戏视图的Y坐标
    public static float getScreenY() {
        // 获得鼠标在窗口的坐标
        float currentY = getInstance().gameViewPortPos.y + getInstance().gameViewPortSize.y - getY();
        // 转化为分辨率大小以匹配帧缓冲
        return currentY / getInstance().gameViewPortSize.y * Settings.RESOLUTION_HEIGHT;
    }

    // 获得鼠标在游戏视图的坐标
    public static Vector2f getScreen() {
        return new Vector2f(getScreenX(), getScreenY());
    }

    // 获得世界坐标X
    public static float getWorldX() {
        return (float) getInstance().worldX;
    }

    // 获得世界坐标Y
    public static float getWorldY() {
        return (float) getInstance().worldY;
    }

    // 获得世界坐标
    public static Vector2f getWorld() {
        return new Vector2f(getWorldX(), getWorldY());
    }

    // 获得世界坐标Dx
    public static float getWorldDx() {
        return (float) (getInstance().worldX - getInstance().lastWorldX);
    }

    // 获得世界坐标Dy
    public static float getWorldDy() {
        return (float) (getInstance().worldY - getInstance().lastWorldY);
    }

    // 获取ScrollX坐标
    public static float getScrollX() {
        return (float) getInstance().scrollX;
    }

    // 获取ScrollY坐标
    public static float getScrollY() {
        return (float) getInstance().scrollY;
    }

    // 鼠标是否正在拖拽
    public static boolean isDragging() {
        return getInstance().isDragging;
    }

    // 鼠标某个按钮是否被按下
    public static boolean mouseButtonDown(int button) {
        // 获取实例
        MouseListener mouseListener = getInstance();

        // 检查是否满足长度限制
        if (button < mouseListener.mouseButtonPressed.length) {
            // 返回按钮状态
            return mouseListener.mouseButtonPressed[button];
        }

        // 不满足就返回假
        return false;
    }

    // 世界坐标转换屏幕真实坐标
    public static Vector2f getRealScreenPos(Vector2f worldPos) {
        Vector2f screenPos = worldToScreen(worldPos);
        return screenPos.div(Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT)
                .mul(getInstance().gameViewPortSize).add(getInstance().gameViewPortPos);
    }

    public static void setGameViewPortPos(Vector2f gameViewPortPos) {
        getInstance().gameViewPortPos.set(gameViewPortPos);
    }

    public static void setGameViewPortSize(Vector2f gameViewPortSize) {
        getInstance().gameViewPortSize.set(gameViewPortSize);
    }
}
