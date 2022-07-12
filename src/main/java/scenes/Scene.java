package scenes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import components.Component;
import components.ComponentDeserializer;
import components.MouseControls;
import org.joml.Vector2f;
import physics2d.Physics2D;
import pikacat.*;
import renderer.Renderer;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Scene {

    // 场景的渲染器
    private Renderer renderer;

    // 场景的相机
    private Camera camera;

    // 场景里面所有的游戏对象
    private List<GameObject> gameObjects;

    // 将要被加入到场景里面的游戏对象，因为不能在更新物理的过程中加入对象
    private List<GameObject> pendingGameObjects;

    // 场景的加载器
    private SceneInitializer sceneInitializer;

    // 物理2D引擎
    private Physics2D physics2D;

    // 当前的场景是否在跑
    private boolean isRunning;

    // 序列化和反序列化工具
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Component.class, new ComponentDeserializer())
            .registerTypeAdapter(GameObject.class, new GameObjectDeserializer())
            .enableComplexMapKeySerialization()
            .setPrettyPrinting().create();

    public Scene(SceneInitializer sceneInitializer) {
        this.sceneInitializer = sceneInitializer;
        this.physics2D = new Physics2D();
        this.renderer = new Renderer();
        this.gameObjects = new ArrayList<>();
        this.pendingGameObjects = new ArrayList<>();
        this.isRunning = false;
        this.camera = new Camera(new Vector2f(0, 0));
    }

    // 使用场景初始化器进行场景初始化
    public void init() {
        this.sceneInitializer.loadResources(this);
        this.sceneInitializer.init(this);
    }

    // 启动场景
    public void start() {
        for (int i = 0; i < gameObjects.size(); ++i) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.start();
            this.renderer.add(gameObject);
            this.physics2D.add(gameObject);
        }
        isRunning = true;
    }

    // 销毁场景
    public void destroy() {
        for (GameObject gameObject : gameObjects) {
            gameObject.destroy();
        }
    }

    public void imgui() {
        sceneInitializer.imgui();
    }

    public void save() {
        if (sceneInitializer.getLevelPath() == null) {
            return;
        }

        Window.getImGuiLayer().getPropertiesWindow().clearSelected();

        GameObject levelEditorStuff = Window.getCurrentScene().getGameObjectWith(MouseControls.class);
        if (levelEditorStuff != null) {
            levelEditorStuff.getComponent(MouseControls.class).clearHoldingObject();
        }

        this.editorUpdate(0);

        try (FileWriter fileWriter = new FileWriter(sceneInitializer.getLevelPath())) {
            fileWriter.write(gson.toJson(this.gameObjects.stream().filter(GameObject::doSerialize).toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        if (sceneInitializer.getLevelPath() == null) {
            return;
        }

        String inFile = "";

        try {
            inFile = new String(Files.readAllBytes(Paths.get(sceneInitializer.getLevelPath())));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!inFile.equals("")) {
            GameObject[] gameObjects = gson.fromJson(inFile, GameObject[].class);
            int maxGameObjectUID = -1;
            int maxComponentUID = -1;
            for(GameObject gameObject : gameObjects) {
                addGameObjectToScene(gameObject);
                for (Component component : gameObject.getComponents()) {
                    maxComponentUID = Math.max(maxComponentUID, component.getUID());
                }
                maxGameObjectUID = Math.max(maxGameObjectUID, gameObject.getUID());
            };

            GameObject.init(maxGameObjectUID + 1);
            Component.init(maxComponentUID + 1);
        }
    }

    // 创建一个游戏对象
    public GameObject createGameObject(String name) {
        GameObject gameObject = new GameObject(name);
        gameObject.transform = new Transform();
        gameObject.addComponent(gameObject.transform);
        return gameObject;
    }

    // 添加游戏对象到场景中
    public void addGameObjectToScene(GameObject gameObject) {
        if (isRunning) {
            this.pendingGameObjects.add(gameObject);
        } else {
            this.gameObjects.add(gameObject);
        }
    }

    // 根据UID查找游戏对象
    public GameObject getGameObjectByUID(int uid) {
        Optional<GameObject> first = this.gameObjects.stream()
                .filter(gameObject -> gameObject.getUID() == uid).findFirst();

        return first.orElse(null);
    }

    // 根据名称查找游戏对象
    public GameObject getGameObjectByName(String name) {
        Optional<GameObject> first = this.gameObjects.stream()
                .filter(gameObject -> gameObject.name.equals(name)).findFirst();

        return first.orElse(null);
    }

    // 根据组件查找游戏对象
    public <T extends Component> GameObject getGameObjectWith(Class<T> clazz) {
        for (GameObject gameObject : gameObjects) {
            if (gameObject.getComponent(clazz) != null) {
                return gameObject;
            }
        }
        return null;
    }

    // 更新编辑器场景
    public void editorUpdate(float deltaTime) {
        // 更新摄像机
        this.camera.adjustProjection();

        // 更新游戏对象
        for (int i = 0; i < gameObjects.size(); ++i) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.editorUpdate(deltaTime);

            if (gameObject.isDead()) {
                gameObjects.remove(i);
                this.renderer.destoryGameObject(gameObject);
                this.physics2D.destoryGameObject(gameObject);
                --i;
            }
        }

        // 将等待的游戏对象加入到场景中
        for (GameObject gameObject : pendingGameObjects) {
            gameObjects.add(gameObject);
            gameObject.start();
            this.renderer.add(gameObject);
            this.physics2D.add(gameObject);
        }
        pendingGameObjects.clear();
    }

    // 更新场景
    public void update(float deltaTime) {
        // 更新摄像机
        this.camera.adjustProjection();
        // 更新物理
        this.physics2D.update(deltaTime);

        // 更新游戏对象
        for (int i = 0; i < gameObjects.size(); ++i) {
            GameObject gameObject = gameObjects.get(i);
            gameObject.update(deltaTime);

            if (gameObject.isDead()) {
                gameObjects.remove(i);
                this.renderer.destoryGameObject(gameObject);
                this.physics2D.destoryGameObject(gameObject);
                --i;
            }
        }

        // 将等待的游戏对象加入到场景中
        for (GameObject gameObject : pendingGameObjects) {
            gameObjects.add(gameObject);
            gameObject.start();
            this.renderer.add(gameObject);
            this.physics2D.add(gameObject);
        }
        pendingGameObjects.clear();
    }

    // 渲染场景
    public void render() {
        this.renderer.render();
    }

    public Camera getCamera() {
        return this.camera;
    }

    public List<GameObject> getGameObjects() {
        return this.gameObjects;
    }

    public Physics2D getPhysics() {
        return this.physics2D;
    }

    public String getLevelPath() {
        return this.sceneInitializer.getLevelPath();
    }

    public void setLevelPath(String levelPath) {
        this.sceneInitializer.setLevelPath(levelPath);
    }

    public SceneInitializer getSceneInitializer() {
        return sceneInitializer;
    }
}
