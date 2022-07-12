package util;

import components.Spritesheet;
import pikacat.Sound;
import renderer.Shader;
import renderer.Texture;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// 资源池，文件的绝对路径与资源的对应，依赖注入的思想
public class AssetPool {
    // 着色器
    private static Map<String, Shader> shaders = new HashMap<>();

    // 材质
    private static Map<String, Texture> textures = new HashMap<>();

    // 材质表
    private static Map<String, Spritesheet> spritesheets = new HashMap<>();

    // 声音
    private static Map<String, Sound> sounds = new HashMap<>();

    // 获得着色器，如果没有就新创建一个并加进去，有了就直接返回
    public static Shader getShader(String resourceName) {
        File file = new File(resourceName);
        if (shaders.containsKey(file.getAbsolutePath())) {
            return shaders.get(file.getAbsolutePath());
        } else {
            Shader shader = new Shader(resourceName);
            shader.compile();
            AssetPool.shaders.put(file.getAbsolutePath(), shader);
            return shader;
        }
    }

    // 获得材质，如果没有就新创建一个并加进去，有了就直接返回
    public static Texture getTexture(String resourceName) {
        File file = new File(resourceName);
        if (textures.containsKey(file.getAbsolutePath())) {
            return textures.get(file.getAbsolutePath());
        } else {
            Texture texture = new Texture();
            texture.init(resourceName);
            AssetPool.textures.put(file.getAbsolutePath(), texture);
            return texture;
        }
    }

    // 添加材质
    public static void addSpritesheet(String resourceName, Spritesheet spritesheet) {
        File file = new File(resourceName);
        if (!spritesheets.containsKey(file.getAbsolutePath())) {
            spritesheets.put(file.getAbsolutePath(), spritesheet);
        }
    }

    // 获取材质
    public static Spritesheet getSpritesheet(String resourceName) {
        File file = new File(resourceName);
        assert spritesheets.containsKey(file.getAbsolutePath()) : "找不到材质表'" + resourceName + "'";
        return spritesheets.getOrDefault(file.getAbsolutePath(), null);
    }

    // 添加声音
    public static Sound addSound(String resourceName, boolean loops) {
        File file = new File(resourceName);
        if (sounds.containsKey(file.getAbsolutePath())) {
            return sounds.get(file.getAbsolutePath());
        } else {
            Sound sound = new Sound(file.getAbsolutePath(), loops);
            sounds.put(file.getAbsolutePath(), sound);
            return sound;
        }
    }

    // 获取声音
    public static Sound getSound(String resourceName) {
        File file = new File(resourceName);
        assert sounds.containsKey(file.getAbsolutePath()) : "错误：找不到音频：'" + resourceName + "'";
        return sounds.getOrDefault(file.getAbsolutePath(), null);
    }

    // 获取所有声音
    public static Collection<Sound> getAllSounds() {
        return sounds.values();
    }
}
