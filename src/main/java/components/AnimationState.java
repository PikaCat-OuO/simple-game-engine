package components;


import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

// 一个完整的动画所有状态
public class AnimationState {
    // 动画的名字
    public String title;
    public List<Frame> animationFrames = new ArrayList<>();

    // 默认的材质
    private static Sprite defaultSprite = new Sprite();
    // 现在动画进行到那里了
    private transient float timeTracker = 0.0f;
    // 现在用的是哪一个材质
    private transient int currentSprite = 0;
    // 这个动画是不是循环播放的
    public boolean doesLoop = false;

    // 给这个动画添加一个状态，状态的材质，持续的时间
    public void addFrame(Sprite sprite, float frameTime) {
        this.animationFrames.add(new Frame(sprite, frameTime));
    }

    public void setLoop(boolean doesLoop) {
        this.doesLoop = doesLoop;
    }

    public void update(float deltaTime) {
        // 确保没有加载动画时不会播放
        if (currentSprite < animationFrames.size()) {
            timeTracker -= deltaTime;
            // 如果还没有播放到最后一帧就一直播放，需要循环播放就循环播放
            if (timeTracker <= 0 && (currentSprite != animationFrames.size() - 1 || doesLoop)) {
                currentSprite = (currentSprite + 1) % animationFrames.size();
                timeTracker = animationFrames.get(currentSprite).frameTime;
            }
        }
    }

    public void refreshTextureID() {
        for (Frame frame : animationFrames) {
            frame.sprite.setTexture(AssetPool.getTexture(frame.sprite.getTexture().getFilepath()));
        }
    }

    public Sprite getCurrentSprite() {
        if (currentSprite < animationFrames.size()) {
            return animationFrames.get(currentSprite).sprite;
        }
        return defaultSprite;
    }
}
