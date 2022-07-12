package editor;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import observers.EventSystem;
import observers.events.Event;
import observers.events.EventType;
import org.joml.Vector2f;
import pikacat.MouseListener;
import pikacat.Window;
import util.Settings;

public class GameViewWindow {
    private float leftX, rightX, topY, bottomY;
    // 是否在玩
    private boolean isPlaying = false;

    public void imgui() {
        ImGui.begin("Game ViewPort", ImGuiWindowFlags.NoScrollbar |
                ImGuiWindowFlags.NoScrollWithMouse | ImGuiWindowFlags.MenuBar);

        // 开始和停止游戏的按钮
        ImGui.beginMenuBar();
        if (ImGui.menuItem("Play", "", isPlaying, !isPlaying)) {
            Window.getCurrentScene().save();
            this.isPlaying = true;
            EventSystem.notify(null, new Event(EventType.GAME_ENGINE_START_PLAY));
        }

        if (ImGui.menuItem("Stop", "", !isPlaying, isPlaying)) {
            this.isPlaying = false;
            EventSystem.notify(null, new Event(EventType.GAME_ENGINE_STOP_PLAY));
        }
        ImGui.endMenuBar();

        ImGui.setCursorPos(ImGui.getCursorPosX(), ImGui.getCursorPosY());

        // 获得视野的最大大小，保证能放进去
        ImVec2 viewPortSize = getLargestSizeForViewPort();
        // 获得视野的左上角位置
        ImVec2 viewPortStart = getStartPositionForViewPort(viewPortSize);

        // 设置开始的位置，在这里放一张图片，就是帧缓冲中已经渲染好的帧
        ImGui.setCursorPos(viewPortStart.x, viewPortStart.y);

        // 告诉鼠标监听器视图的位置和大小
        leftX = viewPortStart.x + 8;
        rightX = viewPortStart.x + viewPortSize.x + 8;
        topY = viewPortStart.y;
        bottomY = viewPortStart.y + viewPortSize.y;

        MouseListener.setGameViewPortPos(new Vector2f(viewPortStart.x + 8, viewPortStart.y));
        MouseListener.setGameViewPortSize(new Vector2f(viewPortSize.x, viewPortSize.y));

        // 把渲染好的帧输出到视图中
        int textureID = Window.getFrameBuffer().getTextureID();
        ImGui.image(textureID, viewPortSize.x, viewPortSize.y, 0, 1, 1, 0);

        ImGui.end();
    }

    public ImVec2 getViewPortWindowSize() {
        // 获得视图窗口的大小
        ImVec2 viewPortWindowSize = new ImVec2();
        ImGui.getContentRegionAvail(viewPortWindowSize);
        return viewPortWindowSize;
    }

    public ImVec2 getLargestSizeForViewPort() {
        // 获取视图窗口大小
        ImVec2 viewPortWindowSize = getViewPortWindowSize();

        // 首先按满宽算，看高能不能放下
        float viewPortWidth = viewPortWindowSize.x;
        float viewPortHeight = viewPortWidth / Settings.getResolutionRatio();

        if (viewPortHeight > viewPortWindowSize.y) {
            // 高放不下，转为满高放，这样一定能放下
            viewPortHeight = viewPortWindowSize.y;
            viewPortWidth = viewPortHeight * Settings.getResolutionRatio();
        }

        return new ImVec2(viewPortWidth, viewPortHeight);
    }

    public ImVec2 getStartPositionForViewPort(ImVec2 viewPortSize) {
        // 获取视图窗口大小
        ImVec2 viewPortWindowSize = getViewPortWindowSize();

        float viewPortStartX = (viewPortWindowSize.x - viewPortSize.x) / 2.0f;
        float viewPortStartY = (viewPortWindowSize.y - viewPortSize.y) / 2.0f;

        return new ImVec2(viewPortStartX + ImGui.getCursorPosX(),
                viewPortStartY + ImGui.getCursorPosY());
    }

    public boolean getWantCaptureMouse() {
        return MouseListener.getX() >= leftX && MouseListener.getX() <= rightX &&
                MouseListener.getY() >= topY && MouseListener.getY() <= bottomY;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
