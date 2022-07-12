package renderer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    // 材质的位置
    private String filepath;

    // 材质的ID
    private transient int texID;

    // 宽度和高度
    private int width, height;

    public Texture() {
        this.texID = -1;
        this.width = -1;
        this.height = -1;
    }

    public Texture(int width, int height) {
        this.filepath = "Generated";

        // 在GPU上生成材质，先生成位置，然后绑定现在使用的材质
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // 设置宽高不一致时的伸缩策略，不然无法显示图片
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        // 生成占位材质图片，为帧缓存做准备
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, width, height,
                0, GL_RGB, GL_UNSIGNED_BYTE, 0);
    }

    public void init(String filepath) {
        this.filepath = filepath;

        // 在GPU上生成材质，先生成位置，然后绑定现在使用的材质
        texID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texID);

        // 设置刚绑定的材质的属性，左右重复，上下重复
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);

        // 伸缩材质时使用像素精确的方式，不要模糊
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // 使用stb加载材质
        IntBuffer width = BufferUtils.createIntBuffer(1);
        IntBuffer height = BufferUtils.createIntBuffer(1);
        IntBuffer channels = BufferUtils.createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true);
        ByteBuffer image = stbi_load(filepath, width, height, channels, 0);

        assert image != null : "错误： （材质） 不能加载材质'" + filepath + "'";
        this.width = width.get(0);
        this.height = height.get(0);

        // 将材质传送到GPU
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                0, GL_RGBA, GL_UNSIGNED_BYTE, image);

        // 材质已经上传到GPU了，释放材质
        stbi_image_free(image);
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texID);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getTexID() {
        return texID;
    }

    public String getFilepath() {
        return filepath;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Texture texture)) return false;
        return texture.width == this.width &&  texture.height == this.height &&  texture.texID == this.texID &&
                texture.filepath.equals(this.filepath);
    }

}
