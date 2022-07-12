package renderer;

import static org.lwjgl.opengl.GL30.*;

// 生成帧缓存，用于预渲染到某一个地方，然后再输出到屏幕上，这样就不会因为屏幕分辨率的改变而导致重新渲染
public class FrameBuffer {
    private int fboID = 0;
    private Texture texture = null;

    public FrameBuffer(int width, int height) {
        // 生成帧缓存并绑定、配置
        fboID = glGenFramebuffers();
        bind();

        // 将创建好的材质空间绑定到帧缓存中，绑定到最顶层
        this.texture = new Texture(width, height);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D,
                this.texture.getTexID(), 0);

        // 生成渲染缓冲
        int rboID = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, rboID);
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, width, height);

        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboID);

        // 确保帧缓存已经准备好了，解绑
        assert glCheckFramebufferStatus(GL_FRAMEBUFFER) == GL_FRAMEBUFFER_COMPLETE : "错误： 帧缓存还未完成";
        unbind();
    }

    // 将内容渲染到帧缓存中
    public void bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, fboID);
    }

    // 停止将内容渲染到帧缓存中
    public void unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER,0);
    }

    public int getFboID() {
        return fboID;
    }

    public int getTextureID() {
        return texture.getTexID();
    }

}
