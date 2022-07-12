package renderer;

import org.joml.Vector2f;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.*;

// 捡起物品
public class PickingTexture {
    private int pickingTextureID;
    private int fbo;
    private int depthTexture;

    public PickingTexture(int width, int height) {
        if (!init(width, height)) {
            assert false :"错误： 无法初始化物品拾取模块";
        }
    }

    public boolean init(int width, int height) {
        // 生成帧缓存并绑定、配置
        fbo = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fbo);

        // 将创建好的材质空间绑定到帧缓存中，绑定到最顶层
        this.pickingTextureID = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, pickingTextureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, width, height, 0, GL_RGB, GL_FLOAT, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                this.pickingTextureID, 0);

        // 生成深度缓存
        glEnable(GL_TEXTURE_2D);
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, width, height,
                0, GL_DEPTH_COMPONENT, GL_FLOAT, 0);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);

        // 禁止读
        glReadBuffer(GL_NONE);
        glDrawBuffer(GL_COLOR_ATTACHMENT0);

        // 确保帧缓存已经准备好了，解绑
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : "错误： 帧缓存还未完成";
        glBindTexture(GL_TEXTURE_2D, 0);
        glBindBuffer(GL_FRAMEBUFFER, 0);
        return true;
    }

    public void enableWriting() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, fbo);
    }

    public void disableWriting() {
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
    }

    // 从帧缓冲中读一个Pixel
    public int readPixel(int x, int y) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
        glReadBuffer(GL_COLOR_ATTACHMENT0);

        float[] pixels = new float[3];
        glReadPixels(x, y, 1, 1, GL_RGB, GL_FLOAT, pixels);

        // 当没有任何游戏物品时就返回-1，因为之前游戏物品的UID已经+1了，所以就不用担心UID出错
        return (int) pixels[0] - 1;
    }

    // 从帧缓存中读取多个pixel
    public float[] readPixels(Vector2f from, Vector2f to) {
        glBindFramebuffer(GL_READ_FRAMEBUFFER, fbo);
        glReadBuffer(GL_COLOR_ATTACHMENT0);

        Vector2f size = new Vector2f(to).sub(from).absolute();
        Vector2f start = new Vector2f(from).add(to).div(2).sub(size.x / 2.0f, size.y / 2.0f);
        float[] pixels = new float[3 * (int) size.x * (int) size.y];
        glReadPixels((int) start.x, (int) start.y, (int) size.x, (int) size.y, GL_RGB, GL_FLOAT, pixels);

        for (int i = 0; i < pixels.length; ++i) {
            pixels[i] -= 1;
        }

        // 当没有任何游戏物品时就返回-1，因为之前游戏物品的UID已经+1了，所以就不用担心UID出错
        return pixels;
    }
}
