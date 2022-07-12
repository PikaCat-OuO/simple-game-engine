package scenes;

import components.*;
import imgui.ImGui;
import imgui.ImVec2;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.RigidBody2D;
import pikacat.*;
import util.AssetPool;
import util.Settings;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class LevelEditorSceneInitializer extends SceneInitializer {
    private GameObject levelEditorStuff;

    private int uid = 0;

    public LevelEditorSceneInitializer(String levelPath) {
        super(levelPath);
    }

    // 装载资源
    @Override
    public void loadResources(Scene scene) {
        AssetPool.addSpritesheet("assets/images/decorationsAndBlocks.png",
                new Spritesheet(AssetPool.getTexture("assets/images/decorationsAndBlocks.png"),
                        16, 16, 81, 0));
        AssetPool.addSpritesheet("assets/images/spritesheet.png",
                new Spritesheet(AssetPool.getTexture("assets/images/spritesheet.png"),
                        16, 16, 26, 0));
        AssetPool.addSpritesheet("assets/images/turtle.png",
                new Spritesheet(AssetPool.getTexture("assets/images/turtle.png"),
                        16, 24, 4, 0));
        AssetPool.addSpritesheet("assets/images/bigSpritesheet.png",
                new Spritesheet(AssetPool.getTexture("assets/images/bigSpritesheet.png"),
                        16, 32, 42, 0));
        AssetPool.addSpritesheet("assets/images/pipes.png",
                new Spritesheet(AssetPool.getTexture("assets/images/pipes.png"),
                        32, 32, 4, 0));
        AssetPool.addSpritesheet("assets/images/items.png",
                new Spritesheet(AssetPool.getTexture("assets/images/items.png"),
                        16, 16, 43, 0));
        AssetPool.addSpritesheet("assets/images/gizmos.png",
                new Spritesheet(AssetPool.getTexture("assets/images/gizmos.png"),
                        24, 48, 3, 0));

        AssetPool.addSound("assets/sounds/main-theme-overworld.ogg", true);
        AssetPool.addSound("assets/sounds/flagpole.ogg", false);
        AssetPool.addSound("assets/sounds/break_block.ogg", false);
        AssetPool.addSound("assets/sounds/bump.ogg", false);
        AssetPool.addSound("assets/sounds/coin.ogg", false);
        AssetPool.addSound("assets/sounds/gameover.ogg", false);
        AssetPool.addSound("assets/sounds/jump-small.ogg", false);
        AssetPool.addSound("assets/sounds/mario_die.ogg", false);
        AssetPool.addSound("assets/sounds/pipe.ogg", false);
        AssetPool.addSound("assets/sounds/powerup.ogg", false);
        AssetPool.addSound("assets/sounds/powerup_appears.ogg", false);
        AssetPool.addSound("assets/sounds/stage_clear.ogg", false);
        AssetPool.addSound("assets/sounds/stomp.ogg", false);
        AssetPool.addSound("assets/sounds/kick.ogg", false);
        AssetPool.addSound("assets/sounds/invincible.ogg", false);


        super.loadResources(scene);
    }

    // 场景初始化
    @Override
    public void init(Scene scene) {
        this.levelEditorStuff = scene.createGameObject("levelEditor");
        this.levelEditorStuff.setNoSerialize();
        this.levelEditorStuff.addComponent(new MouseControls());
        this.levelEditorStuff.addComponent(new KeyControls());
        this.levelEditorStuff.addComponent(new GridLines());
        this.levelEditorStuff.addComponent(new EditorCamera(scene.getCamera()));
        this.levelEditorStuff.addComponent(new GizmoSystem(
                AssetPool.getSpritesheet("assets/images/gizmos.png")));
        scene.addGameObjectToScene(this.levelEditorStuff);
    }

    @Override
    public void imgui() {
        ImGui.begin("Objects");

        if (ImGui.beginTabBar("Window Tab Bar")) {
            if (ImGui.beginTabItem("Solid Blocks")) {
                ImVec2 windowPos = new ImVec2();
                ImGui.getWindowPos(windowPos);
                ImVec2 windowSize = new ImVec2();
                ImGui.getWindowSize(windowSize);
                ImVec2 itemSpacing = new ImVec2();
                ImGui.getStyle().getItemSpacing(itemSpacing);

                float windowX2 = windowPos.x + windowSize.x;

                Spritesheet spritesheet = AssetPool.getSpritesheet(
                        "assets/images/decorationsAndBlocks.png");
                for (int i = 0; i < spritesheet.size(); ++i) {
                    if (i == 34) continue;
                    if (i >= 38 && i < 61) continue;

                    final int currentI = i;
                    addGameObjectToItemBar(spritesheet.getSprite(i), Settings.GRID_WIDTH, Settings.GRID_HEIGHT,
                            gameObject -> {
                                RigidBody2D rigidBody = new RigidBody2D();
                                rigidBody.setBodyType(BodyType.STATIC);
                                gameObject.addComponent(rigidBody);

                                Box2DCollider box2DCollider = new Box2DCollider();
                                box2DCollider.setHalfSize(new Vector2f(0.125f, 0.125f));

                                gameObject.addComponent(box2DCollider);
                                gameObject.addComponent(new Ground());

                                if (currentI == 12) {
                                    gameObject.addComponent(new BreakableBrick());
                                }
                            });
                    ImVec2 lastButtonPos = new ImVec2();
                    ImGui.getItemRectMax(lastButtonPos);
                    float lastButtonX2 = lastButtonPos.x;
                    float nextButtonX2 = lastButtonX2 + spritesheet.getSprite(i).getWidth() * 2.0f + itemSpacing.x;
                    if (i + 1 < spritesheet.size() && nextButtonX2 < windowX2) {
                        ImGui.sameLine();
                    }
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Decoration Blocks")) {
                ImVec2 windowPos = new ImVec2();
                ImGui.getWindowPos(windowPos);
                ImVec2 windowSize = new ImVec2();
                ImGui.getWindowSize(windowSize);
                ImVec2 itemSpacing = new ImVec2();
                ImGui.getStyle().getItemSpacing(itemSpacing);

                float windowX2 = windowPos.x + windowSize.x;

                Spritesheet spritesheet = AssetPool.getSpritesheet(
                        "assets/images/decorationsAndBlocks.png");
                for (int i = 31; i < 61; ++i) {
                    if ((i >= 35 && i < 38) || (i >= 42 && i < 45)) continue;

                    addGameObjectToItemBar(spritesheet.getSprite(i), Settings.GRID_WIDTH, Settings.GRID_HEIGHT,
                            gameObject -> {
                            });

                    ImVec2 lastButtonPos = new ImVec2();
                    ImGui.getItemRectMax(lastButtonPos);
                    float lastButtonX2 = lastButtonPos.x;
                    float nextButtonX2 = lastButtonX2 + spritesheet.getSprite(i).getWidth() * 2.0f + itemSpacing.x;
                    if (i + 1 < spritesheet.size() && nextButtonX2 < windowX2) {
                        ImGui.sameLine();
                    }
                }
                ImGui.endTabItem();
            }

            if (ImGui.beginTabItem("Prefabs")) {
                Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/spritesheet.png");
                addGameObjectToItemBar(playerSprites.getSprite(0), Prefabs::generateMario);
                ImGui.sameLine();

                addGameObjectToItemBar(playerSprites.getSprite(14), Prefabs::generateGoomba);
                ImGui.sameLine();


                Spritesheet turtleSpritesheet = AssetPool.getSpritesheet("assets/images/turtle.png");
                addGameObjectToItemBar(turtleSpritesheet.getSprite(0), Prefabs::generateTurtle);
                ImGui.sameLine();

                addGameObjectToItemBar(turtleSpritesheet.getSprite(0), Prefabs::generateDialogTurtle);
                ImGui.sameLine();

                Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
                addGameObjectToItemBar(itemSprites.getSprite(0), Prefabs::generateQuestionBlock);
                ImGui.sameLine();

                addGameObjectToItemBar(itemSprites.getSprite(6), Prefabs::generateFlagTop);
                ImGui.sameLine();

                addGameObjectToItemBar(itemSprites.getSprite(33), Prefabs::generateFlagPole);
                ImGui.sameLine();

                addGameObjectToItemBar(itemSprites.getSprite(7), Prefabs::generateCoin);
                ImGui.sameLine();


                Spritesheet pipeSprites = AssetPool.getSpritesheet("assets/images/pipes.png");
                addGameObjectToItemBar(pipeSprites.getSprite(0), Prefabs::generatePipe, Direction.DOWN);
                ImGui.sameLine();

                addGameObjectToItemBar(pipeSprites.getSprite(1), Prefabs::generatePipe, Direction.UP);
                ImGui.sameLine();

                addGameObjectToItemBar(pipeSprites.getSprite(2), Prefabs::generatePipe, Direction.RIGHT);
                ImGui.sameLine();

                addGameObjectToItemBar(pipeSprites.getSprite(3), Prefabs::generatePipe, Direction.LEFT);
                ImGui.sameLine();

                ImGui.endTabItem();
            }

            // 声音
            if (ImGui.beginTabItem("Sounds")) {
                Collection<Sound> sounds = AssetPool.getAllSounds();
                for (Sound sound : sounds) {
                    File tmp = new File(sound.getFilePath());
                    if (ImGui.button(tmp.getName())) {
                        if (!sound.isPlaying()) {
                            sound.play();
                        } else {
                            sound.stop();
                        }
                    }
                }

                ImGui.endTabItem();
            }

            ImGui.endTabBar();
        }

        // 重置uid
        this.uid = 0;

        ImGui.end();
    }

    public void addGameObjectToItemBar(Sprite sprite, Supplier<GameObject> supplier) {
        Vector2f[] texCoords = sprite.getTexCoords();
        ImGui.pushID(uid++);
        if (ImGui.imageButton(sprite.getTexID(), sprite.getWidth() * 2, sprite.getHeight() * 2,
                texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
            // 如果用户点击了就新生成一个游戏对象并加到场景中，然后鼠标捡起这个对象
            levelEditorStuff.getComponent(MouseControls.class).pickupObject(supplier.get());
        }
        ImGui.popID();
    }

    public <T> void addGameObjectToItemBar(Sprite sprite, Function<T, GameObject> function, T arg) {
        Vector2f[] texCoords = sprite.getTexCoords();
        ImGui.pushID(uid++);
        if (ImGui.imageButton(sprite.getTexID(), sprite.getWidth() * 2, sprite.getHeight() * 2,
                texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
            // 如果用户点击了就新生成一个游戏对象并加到场景中，然后鼠标捡起这个对象
            levelEditorStuff.getComponent(MouseControls.class).pickupObject(function.apply(arg));
        }
        ImGui.popID();
    }

    public void addGameObjectToItemBar(Sprite sprite, float width, float height,
                                       Consumer<GameObject> consumer) {
        Vector2f[] texCoords = sprite.getTexCoords();
        ImGui.pushID(uid++);
        if (ImGui.imageButton(sprite.getTexID(), sprite.getWidth() * 2, sprite.getHeight() * 2,
                texCoords[2].x, texCoords[0].y, texCoords[0].x, texCoords[2].y)) {
            GameObject gameObject = Prefabs.generateSpriteObject(sprite, width, height);
            // 如果用户点击了就新生成一个游戏对象并加到场景中，然后鼠标捡起这个对象
            consumer.accept(gameObject);
            levelEditorStuff.getComponent(MouseControls.class).pickupObject(gameObject);
        }
        ImGui.popID();
    }
}
