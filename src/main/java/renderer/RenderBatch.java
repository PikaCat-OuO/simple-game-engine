package renderer;

import components.SpriteRenderer;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import pikacat.Camera;
import pikacat.GameObject;
import pikacat.Transform;
import pikacat.Window;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderBatch implements Comparable<RenderBatch> {
    // 节点数据
    // 节点位置     颜色      材质坐标        材质ID
    private final int POS_SIZE = 2;
    private final int COLOR_SIZE = 4;
    private final int TEX_COORDS_SIZE = 2;
    private final int TEX_ID_SIZE = 1;
    private final int ENTITY_ID_SIZE = 1;

    private final int POS_OFFSET = 0;
    private final int COLOR_OFFSET = POS_OFFSET + POS_SIZE * Float.BYTES;
    private final int TEX_COORDS_OFFSET = COLOR_OFFSET + COLOR_SIZE * Float.BYTES;
    private final int TEX_ID_OFFSET = TEX_COORDS_OFFSET + TEX_COORDS_SIZE * Float.BYTES;
    private final int ENTITY_ID_OFFSET = TEX_ID_OFFSET + TEX_ID_SIZE * Float.BYTES;

    private final int VERTEX_SIZE = 10;
    private final int VERTEX_SIZE_BYTES = VERTEX_SIZE * Float.BYTES;

    // 精灵相关
    private SpriteRenderer[] sprites;
    private int numSprites;
    private boolean hasRoom;
    private float[] vertices;
    private int[] texSlots;

    private List<Texture> textures;

    // 最大的材质槽数目
    private final int MAX_TEXTURE_SIZE = glGetInteger(GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);

    private int vaoID, vboID;
    private int maxBatchSize;

    // 这个渲染组渲染的游戏对象属于哪一层
    private int zIndex;

    // 渲染器
    private Renderer renderer;

    public RenderBatch(int maxBatchSize, int zIndex, Renderer renderer) {
        // 初始化精灵数据
        sprites = new SpriteRenderer[maxBatchSize];
        this.maxBatchSize = maxBatchSize;
        // 三角形渲染
        vertices = new float[maxBatchSize * 4 * VERTEX_SIZE];

        this.numSprites = 0;
        this.hasRoom = true;

        this.textures = new ArrayList<>();

        this.zIndex = zIndex;

        this.renderer = renderer;

        this.texSlots = new int[MAX_TEXTURE_SIZE];

        // 填充槽编号
        for (int i = 0; i < MAX_TEXTURE_SIZE; ++i) {
            this.texSlots[i] = i;
        }
    }

    public void start() {
        // 生成 VAO VBO EBO 缓冲区对象， 并发送给GPU
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // 在显存中安排空间给节点
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertices.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // 在显存中安排节点空间
        int eboID = glGenBuffers();
        int[] indices = generateIndices();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        // 启用这些缓存
        glVertexAttribPointer(0, POS_SIZE, GL_FLOAT, false,
                VERTEX_SIZE_BYTES, POS_OFFSET);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, COLOR_SIZE, GL_FLOAT, false,
                VERTEX_SIZE_BYTES, COLOR_OFFSET);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, TEX_COORDS_SIZE, GL_FLOAT, false,
                VERTEX_SIZE_BYTES, TEX_COORDS_OFFSET);
        glEnableVertexAttribArray(2);

        glVertexAttribPointer(3, TEX_ID_SIZE, GL_FLOAT, false,
                VERTEX_SIZE_BYTES, TEX_ID_OFFSET);
        glEnableVertexAttribArray(3);

        glVertexAttribPointer(4, ENTITY_ID_SIZE, GL_FLOAT, false,
                VERTEX_SIZE_BYTES, ENTITY_ID_OFFSET);
        glEnableVertexAttribArray(4);
    }

    public int[] generateIndices() {
        // 每个sprite需要6个下标，两个三角形，每个三个
        int[] elements = new int[maxBatchSize * 6];
        for (int i = 0; i < maxBatchSize; ++i) {
            loadElementIndices(elements, i);
        }
        return elements;
    }

    // 加载一个sprite的节点下标
    public void loadElementIndices(int[] elements, int index) {
        // 6个一组，4个4个递增
        int offsetArrayIndex = 6 * index;
        int offset = 4 * index;

        // 3 2 0 0 2 1    7 6 4 4 6 5
        // 第一个三角形
        elements[offsetArrayIndex] = offset + 3;
        elements[offsetArrayIndex + 1] = offset + 2;
        elements[offsetArrayIndex + 2] = offset;

        // 第二个三角形
        elements[offsetArrayIndex + 3] = offset;
        elements[offsetArrayIndex + 4] = offset + 2;
        elements[offsetArrayIndex + 5] = offset + 1;
    }

    // 添加一个sprite的节点信息
    public void loadVertexProperties(int index) {
        SpriteRenderer spriteRenderer = this.sprites[index];
        Transform transform = spriteRenderer.gameObject.transform;

        int offset = index * 4 * VERTEX_SIZE;

        // 添加到vertices中
        Vector2f position = transform.position;
        Vector2f scale = transform.scale;
        Vector4f color = spriteRenderer.getColor();
        Vector2f[] texCoords = spriteRenderer.getTexCoords();

        int texID = 0;
        Texture texture = spriteRenderer.getTexture();
        if (texture != null) {
            // 留出0号槽，不使用材质
            texID = textures.indexOf(texture) + 1;
        }

        // 如果旋转了就要另外处理
        boolean isRotated = transform.rotation != 0.0f;
        Matrix4f transformMatrix = new Matrix4f().identity();
        if (isRotated) {
            transformMatrix.translate(transform.position.x, transform.position.y, 0);
            transformMatrix.rotate((float) Math.toRadians(transform.rotation), 0, 0, 1);
            transformMatrix.scale(transform.scale.x, transform.scale.y, 1);
        }

        float xAdd = 0.5f;
        float yAdd = 0.5f;
        for (int i = 0; i < 4; ++i) {
            switch (i) {
                case 1 -> yAdd = -0.5f;
                case 2 -> xAdd = -0.5f;
                case 3 -> yAdd = 0.5f;
            }

            Vector4f currentPos = new Vector4f(position.x + xAdd * scale.x, position.y + yAdd * scale.y,
                    0, 1);
            if (isRotated) {
                currentPos = new Vector4f(xAdd, yAdd, 0, 1).mul(transformMatrix);
            }

            // 加载位置(x, y)
            vertices[offset] = currentPos.x;
            vertices[offset + 1] = currentPos.y;

            // 加载颜色
            vertices[offset + 2] = color.x;
            vertices[offset + 3] = color.y;
            vertices[offset + 4] = color.z;
            vertices[offset + 5] = color.w;

            // 加载材质坐标
            vertices[offset + 6] = texCoords[i].x;
            vertices[offset + 7] = texCoords[i].y;

            // 加载材质位置
            vertices[offset + 8] = texID;

            // 加载游戏物品的UID，以用来拾取物品，+1是为了使得没有拾取任何物品时，返回-1，所以这里要先行+1
            vertices[offset + 9] = spriteRenderer.gameObject.getUID() + 1;

            offset += VERTEX_SIZE;
        }
    }

    // 添加sprite
    public void addSprite(SpriteRenderer spriteRenderer) {
        int index = this.numSprites;
        this.sprites[this.numSprites++] = spriteRenderer;

        // 如果有材质就把材质添加到渲染组的材质包里
        Texture texture = spriteRenderer.getTexture();
        if (texture != null) {
            if (!textures.contains(texture)) {
                this.textures.add(texture);
            }
        }

        // 将这个sprite的节点信息添加到vertices数组里
        loadVertexProperties(index);

        if (this.numSprites >= this.maxBatchSize) {
            this.hasRoom = false;
        }
    }

    // 移除游戏对象
    public boolean destroyIfExist(GameObject gameObject) {
        SpriteRenderer spriteRenderer = gameObject.getComponent(SpriteRenderer.class);
        return destroyIfExist(spriteRenderer);
    }

    // 移除游戏对象
    public boolean destroyIfExist(SpriteRenderer spriteRenderer) {
        for (int i = 0; i < numSprites; ++i) {
            if (sprites[i] == spriteRenderer) {
                // 向前移动填补空位
                for (int j = i; j < numSprites - 1; ++j) {
                    sprites[j] = sprites[j + 1];
                    sprites[j].setDirty();
                }
                --numSprites;
                return true;
            }
        }
        return false;
    }

    public void render() {
        // 检查渲染组里是不是有东西改变了
        boolean needToReBuffer = false;
        for (int i = 0; i < numSprites; ++i) {
            SpriteRenderer spriteRenderer = sprites[i];
            if (spriteRenderer.gameObject.transform.zIndex != this.zIndex) {
                // 如果这个物品的zIndex改变了就从这个组中移除并重新加入其他组中
                destroyIfExist(spriteRenderer);
                this.renderer.add(spriteRenderer);
                --i;
            } else if (spriteRenderer.isDirty()) {
                if (!hasTexture(spriteRenderer.getTexture())) {
                    // 刷新材质ID
                    destroyIfExist(spriteRenderer);
                    this.renderer.add(spriteRenderer);
                    --i;
                } else {
                    needToReBuffer = true;
                    spriteRenderer.setClean();
                    loadVertexProperties(i);
                }
            }
        }

        if (needToReBuffer) {
            // 重新缓冲所有的sprite
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        }

        // 使用着色器
        Shader shader = Renderer.getBoundShader();
        Camera camera = Window.getCurrentScene().getCamera();
        shader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        shader.uploadMat4f("uView", camera.getViewMatrix());
        for (int i = 0; i < textures.size(); ++i) {
            // 留出0号槽，不使用材质
            glActiveTexture(GL_TEXTURE0 + i + 1);
            textures.get(i).bind();
        }
        shader.uploadIntArray("uTextures", texSlots);

        // 绑定vao准备画图
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // 画图
        glDrawElements(GL_TRIANGLES, this.numSprites * 6, GL_UNSIGNED_INT, 0);

        // 解绑
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        // 解绑材质
        for (Texture texture : textures) {
            texture.bind();
        }

        shader.detach();
    }

    public boolean hasRoom() {
        return this.hasRoom;
    }

    public boolean hasTextureRoom() {
        return this.textures.size() < MAX_TEXTURE_SIZE - 1;
    }

    public boolean hasTexture(Texture texture) {
        return this.textures.contains(texture);
    }

    public int getzIndex() {
        return this.zIndex;
    }

    @Override
    public int compareTo(RenderBatch renderBatch) {
        return Integer.compare(this.zIndex, renderBatch.zIndex);
    }
}
