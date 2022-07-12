package pikacat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentDeserializer;
import components.SpriteRenderer;
import imgui.ImGui;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

// 游戏对象
public class GameObject {
    // 用处同Component
    private static int ID_COUNTER = 0;
    private int uid = -1;

    public String name;
    private List<Component> components = new ArrayList<>();
    public transient Transform transform;

    // 游戏对象是否死亡
    private boolean isDead = false;

    // 是否需要序列化
    private boolean doSerialize = true;

    public GameObject(String name) {
        this.name = name;

        this.uid = ID_COUNTER++;
    }

    // 启动游戏对象时启动里面所有的组件
    public void start() {
        for (int i = 0; i < components.size(); ++i) {
            components.get(i).start();
        }
    }

    // 销毁游戏对象
    public void destroy() {
        this.isDead = true;
        for (int i = 0; i < components.size(); ++i) {
            components.get(i).destroy();
        }
    }

    // 在启动引擎时反序列化用的，这时可以在文件中原有的游戏组件的基础之上继续添加新组件而UID不冲突
    // 不然的话如果从文件读入就会导致UID覆盖
    public static void init (int maxUID) {
        ID_COUNTER = maxUID;
    }

    // 更新这个游戏对象时更新里面的所有组件
    public void editorUpdate(float deltaTime) {
        for (Component component : components) {
            component.editorUpdate(deltaTime);
        }
    }

    // 更新这个游戏对象时更新里面的所有组件
    public void update(float deltaTime) {
        for (Component component : components) {
            component.update(deltaTime);
        }
    }

    // 调用组件的ImGui
    public void imgui() {
        for (Component component : components) {
            if (ImGui.collapsingHeader(component.getClass().getSimpleName())) {
                component.imgui();
            }
        }
    }

    // 获取这个游戏对象中的某一个组件，根据类名获取
    public <T extends Component> T getComponent(Class<T> componentClass) {
        for (Component component : components) {
            if (componentClass.isAssignableFrom(component.getClass())) {
                try {
                    return componentClass.cast(component);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                    assert false : "不能转换组件";
                }
            }
        }

        return null;
    }

    // 移除组件
    public <T extends Component> void removeComponent(Class<T> componentClass) {
        for (int i = 0; i < components.size(); ++i) {
            Component component = components.get(i);
            if (componentClass.isAssignableFrom(component.getClass())) {
                components.remove(i);
                return;
            }
        }
    }

    // 添加组件
    public void addComponent(Component component) {
        // 添加组件的同时生成uid
        component.generateUID();
        this.components.add(component);
        component.gameObject = this;
    }

    // 拷贝一份自己
    public GameObject copy() {
        // 序列化和反序列化工具
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Component.class, new ComponentDeserializer())
                .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
                .enableComplexMapKeySerialization()
                .create();

        GameObject gameObject = gson.fromJson(gson.toJson(this), GameObject.class);
        gameObject.generateUID();
        for (Component component : gameObject.components) {
            component.generateUID();
        }

        SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        if (spriteRenderer != null && spriteRenderer.getTexture() != null) {
            spriteRenderer.setTexture(AssetPool.getTexture(spriteRenderer.getTexture().getFilepath()));
        }

        return gameObject;
    }

    public List<Component> getComponents() {
        return this.components;
    }

    public void generateUID() {
        this.uid = ID_COUNTER++;
    }

    public int getUID() {
        return this.uid;
    }

    public void setNoSerialize() {
        this.doSerialize = false;
    }

    public boolean doSerialize() {
        return this.doSerialize;
    }

    public boolean isDead() {
        return isDead;
    }
}
