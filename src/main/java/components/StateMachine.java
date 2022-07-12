package components;

import editor.JImGui;
import imgui.ImGui;
import imgui.type.ImBoolean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

// 动画的状态机
public class StateMachine extends Component{
    private class StateTrigger {
        // 现在在什么状态，遇到什么触发事件
        public String state;
        public String trigger;

        public StateTrigger() {}
        public StateTrigger(String state, String trigger) {
            this.state = state;
            this.trigger = trigger;
        }

        @Override
        public boolean equals(Object object) {
            if (!(object instanceof StateTrigger stateTrigger)) return false;
            return this.state.equals(stateTrigger.state) && this.trigger.equals(stateTrigger.trigger);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state, trigger);
        }
    }

    public HashMap<StateTrigger, String> stateTransfers = new HashMap<>();
    private List<AnimationState> animationStates = new ArrayList<>();
    private transient AnimationState currentState = null;
    private String defaultStateTitle = "";

    // 添加状态转移
    public void addStateTrigger(String from, String to, String trigger) {
        this.stateTransfers.put(new StateTrigger(from, trigger), to);
    }

    // 给当前的动画状态组添加一个状态
    public void addState(AnimationState animationState) {
        this.animationStates.add(animationState);
    }

    // 设置默认状态
    public void setDefaultStateTitle(String title) {
        for (AnimationState animationState : animationStates) {
            if (animationState.title.equals(title)) {
                defaultStateTitle = title;
                if (currentState != null) {
                    currentState = animationState;
                    return;
                }
            }
        }
    }

    // 状态转移
    public void trigger(String trigger) {
        String newStateTitle = stateTransfers.get(new StateTrigger(this.currentState.title, trigger));
        AnimationState newAnimationState = this.animationStates.stream()
                .filter(animationState -> animationState.title.equals(newStateTitle)).findFirst().orElse(null);
        if (newAnimationState != null) {
            this.currentState = newAnimationState;
        }
    }

    @Override
    public void start() {
        this.currentState = this.animationStates.stream()
                .filter(animationState -> animationState.title.equals(this.defaultStateTitle))
                .findFirst().orElse(null);
        assert this.currentState != null : "错误：找不到默认状态";
    }

    @Override
    public void update(float deltaTime) {
        if (currentState != null) {
            currentState.update(deltaTime);
            SpriteRenderer spriteRenderer = this.gameObject.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {
                spriteRenderer.setSprite(currentState.getCurrentSprite());
            }
        }
    }

    @Override
    public void editorUpdate(float deltaTime) {
        if (currentState != null) {
            currentState.update(deltaTime);
            SpriteRenderer spriteRenderer = this.gameObject.getComponent(SpriteRenderer.class);
            if (spriteRenderer != null) {
                spriteRenderer.setSprite(currentState.getCurrentSprite());
            }
        }
    }

    @Override
    public void imgui() {
        for (AnimationState animationState : animationStates) {
            animationState.title = JImGui.inputText("State", animationState.title);

            ImBoolean doesLoop = new ImBoolean(animationState.doesLoop);
            ImGui.checkbox("Loop", doesLoop);
            animationState.setLoop(doesLoop.get());

            int count = 0;
            for (Frame frame : animationState.animationFrames) {
                frame.frameTime = JImGui.dragFloat("Frame(" + ++count + ")", frame.frameTime);
            }
        }
    }

    public void refreshTextureID() {
        for (AnimationState animationState : animationStates) {
            animationState.refreshTextureID();
        }
    }
}
