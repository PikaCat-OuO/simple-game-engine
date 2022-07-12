package pikacat;

import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import org.joml.Vector4f;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import physics2d.Physics2D;
import renderer.Renderer;
import renderer.*;
import scenes.LevelEditorSceneInitializer;
import scenes.LevelSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import util.AssetPool;
import util.Settings;

import javax.swing.*;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements Observer {
    // 窗口的宽，高
    private int width;
    private int height;

    // 窗口的标题
    private String title;

    // 窗口句柄
    private long glfwWindow;

    // 单例模式的实例，采用饿汉式模式
    private static Window window = new Window();

    // 当前游戏的场景
    private static Scene currentScene;

    // 窗口层
    private ImGuiLayer imGuiLayer;

    // 帧缓存
    private FrameBuffer frameBuffer;

    // 拾取物品
    private PickingTexture pickingTexture;

    // 当前是否真的在游玩
    private boolean runtimePlaying;

    // 声音设备的句柄
    private long audioContext;
    private long audioDevice;

    // 单例模式，只有一个窗口
    private Window() {
        // 窗口模式初始化为屏幕分辨率大小
        this.width = Settings.RESOLUTION_WIDTH;
        this.height = Settings.RESOLUTION_HEIGHT;
        this.title = "PikaCat";
        this.runtimePlaying = false;

        // 在消息系统中注册自己
        EventSystem.addObserver(this);
    }

    // 改变游戏的场景
    public static void changeScene(SceneInitializer sceneInitializer) {
        // 销毁当前的场景
        if (currentScene != null) {
            currentScene.destroy();
        }

        // 销毁对话
        DialogSystem.reset();

        getImGuiLayer().getPropertiesWindow().clearSelected();

        // 初始化场景
        currentScene = new Scene(sceneInitializer);
        currentScene.load();
        currentScene.init();
        currentScene.start();
    }

    // 窗口运行的函数
    public void run() {
        // 窗口循环
        loop();

        // 释放声音
        alcDestroyContext(audioContext);
        alcCloseDevice(audioDevice);

        // 因为是用C++申请的内存，不归Java GC管理，所以要手动释放
        glfwFreeCallbacks(glfwWindow);
        glfwDestroyWindow(glfwWindow);
        glfwTerminate();
        Objects.requireNonNull(glfwSetErrorCallback(null)).free();
    }

    // 窗口初始化
    public void init() {
        // 配置窗口参数
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);

        // 创建窗口
        glfwWindow = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (NULL == glfwWindow) {
            throw new IllegalStateException("不能创建窗口");
        }

        // 挂接鼠标监听事件
        glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
        glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
        glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);

        // 挂接键盘监听事件
        glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);

        // 挂架窗口大小改变事件
        glfwSetWindowSizeCallback(glfwWindow, (window, newWidth, newHeight) -> {
            Window.setWidth(newWidth);
            Window.setHeight(newHeight);
        });

        // 设置OpenGL为默认的渲染引擎
        glfwMakeContextCurrent(glfwWindow);

        // 启动垂直同步，帧率匹配屏幕刷新率（默认不开启）
        // glfwSwapInterval(1);

        // 让窗口可见
        glfwShowWindow(glfwWindow);

        // 初始化声音设备
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);

        // 初始化声音环境
        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        alcMakeContextCurrent(audioContext);

        // 将内存中的OpenAL实例与播放器进行绑定
        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        assert alCapabilities.OpenAL10 : "错误：不支持的声音设备";

        // 将内存中的OpenGL实例与当前的窗口进行绑定
        GL.createCapabilities();

        // 开启颜色连接
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);

        // 初始化帧缓存
        this.frameBuffer = new FrameBuffer(Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT);
        // 初始化物品拾取器
        this.pickingTexture = new PickingTexture(Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT);
        glViewport(0, 0, Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT);

        // 初始化ImGui
        this.imGuiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
        this.imGuiLayer.initImGui();
    }

    // 消息循环
    public void loop() {
        // 一帧开始的时间
        float beginTime = (float) glfwGetTime();

        // 一帧开结束的时间
        float endTime;

        // 在一帧之间经过的时间
        float deltaTime = -1.0f;

        // 初始化着色器
        Shader defaultShader = AssetPool.getShader("assets/shaders/default.glsl");
        Shader pickingShader = AssetPool.getShader("assets/shaders/picking.glsl");

        while (!glfwWindowShouldClose(glfwWindow)) {
            // 获得消息
            glfwPollEvents();

            // 第一次渲染，渲染到物品拾取器
            if (!runtimePlaying) {
                glDisable(GL_BLEND);
                pickingTexture.enableWriting();

                glViewport(0, 0, Settings.RESOLUTION_WIDTH, Settings.RESOLUTION_HEIGHT);
                glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                Renderer.bindShader(pickingShader);
                currentScene.render();

                pickingTexture.disableWriting();
                glEnable(GL_BLEND);
            }

            // 第二次，渲染到屏幕
            DebugDraw.beginFrame();

            this.frameBuffer.bind();

            Vector4f clearColor = currentScene.getCamera().clearColor;
            glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
            glClear(GL_COLOR_BUFFER_BIT);

            // 在帧刷新过程中更新场景
            if (deltaTime >= 0) {
                Renderer.bindShader(defaultShader);
                if (runtimePlaying) {
                    currentScene.update(deltaTime);
                } else {
                    currentScene.editorUpdate(deltaTime);
                }
                currentScene.render();
                DebugDraw.draw();
            }

            this.frameBuffer.unbind();

            this.imGuiLayer.update(deltaTime, currentScene);

            // 交换显卡的渲染帧，开始渲染新帧
            glfwSwapBuffers(glfwWindow);

            // 恢复鼠标监听器的状态
            MouseListener.endFrame();
            // 恢复键盘监听器的状态
            KeyListener.endFrame();

            // 统计一帧经过的时间
            endTime = (float) glfwGetTime();
            deltaTime = endTime - beginTime;
            beginTime = endTime;
        }
    }

    // 通知处理
    @Override
    public void onNotify(GameObject gameObject, Event event) {
        switch (event.eventType) {
            case GAME_ENGINE_START_PLAY -> {
                this.runtimePlaying = true;
                Window.changeScene(new LevelSceneInitializer(currentScene.getLevelPath()));
                AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").play();
            }
            case GAME_ENGINE_STOP_PLAY -> {
                this.runtimePlaying = false;
                Window.changeScene(new LevelEditorSceneInitializer(currentScene.getLevelPath()));
                AssetPool.getSound("assets/sounds/main-theme-overworld.ogg").stop();
            }
            case LOAD_LEVEL -> Window.changeScene(new LevelEditorSceneInitializer(event.message));
            case SAVE_LEVEL -> {
                currentScene.setLevelPath(event.message);
                currentScene.save();
                JOptionPane.showMessageDialog(null, "保存成功");
            }
        }
    }

    // 获取窗口实例
    public static Window getInstance() {
        return Window.window;
    }

    public static Scene getCurrentScene() {
        return Window.currentScene;
    }

    public static int getWidth() {
        return getInstance().width;
    }

    public static int getHeight() {
        return getInstance().height;
    }

    public static void setWidth(int width) {
        getInstance().width = width;
    }

    public static void setHeight(int height) {
        getInstance().height = height;
    }

    public static FrameBuffer getFrameBuffer() {
        return getInstance().frameBuffer;
    }

    public static ImGuiLayer getImGuiLayer() {
        return getInstance().imGuiLayer;
    }

    public static Physics2D getPhysics() {
        return getCurrentScene().getPhysics();
    }
}
