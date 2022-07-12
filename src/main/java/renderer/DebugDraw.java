package renderer;

import org.joml.Vector2f;
import org.joml.Vector3f;
import pikacat.Camera;
import pikacat.Window;
import util.AssetPool;
import util.JMath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

// 画出关卡编辑器上面的格子
public class DebugDraw {
    private static final int MAX_LINES = 3000;

    private static List<Line2D> lines = new ArrayList<>();

    // 每一条线有两个点，每个点需要6个float,3个位置，3个颜色
    private static float[] vertexArray = new float[MAX_LINES * 6 * 2];

    // 使用画线的着色器
    private static Shader shader = AssetPool.getShader("assets/shaders/debugLine2D.glsl");

    private static int vaoID;
    private static int vboID;

    // 是否已经将数据缓存到GPU了
    private static boolean started = false;

    public static void start() {
        // 生成vao
        vaoID = glGenVertexArrays();
        glBindVertexArray(vaoID);

        // 生成vbo
        vboID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, (long) vertexArray.length * Float.BYTES, GL_DYNAMIC_DRAW);

        // 启用指定指针位置并启用vao vbo
        // 位置
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // 颜色
        glVertexAttribPointer(1, 3, GL_FLOAT, false,
                6 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // 设置线条粗细
        glLineWidth(2.0f);
    }

    public static void beginFrame() {
        // 如果还没有开启就开启
        if (!started) {
            start();
            started = true;
        }

        // 移除超过生命周期的线
        for (int i = 0; i < lines.size(); ++i) {
            // 超过了生命周期
            if (lines.get(i).beginFrame() < 0) {
                lines.remove(i);
                --i;
            }
        }
    }

    public static void draw() {
        if (lines.size() <= 0) {
            return;
        }

        int index = 0;
        // 遍历每一条线，加到vertexArray里面
        for (Line2D line2D : lines) {
            // 每条线有两个点
            for (int i = 0; i < 2; ++i) {
                Vector2f position = i == 0 ? line2D.getFrom() : line2D.getTo();
                Vector3f color = line2D.getColor();

                // 装载位置
                vertexArray[index] = position.x;
                vertexArray[index + 1] = position.y;
                vertexArray[index + 2] = -10.0f;

                // 装载颜色
                vertexArray[index + 3] = color.x;
                vertexArray[index + 4] = color.y;
                vertexArray[index + 5] = color.z;

                index += 6;
            }
        }

        // 将准备好的节点数据上传到GPU
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferSubData(GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertexArray, 0, lines.size() * 6 * 2));

        // 使用着色器
        shader.use();

        // 上传映射矩阵和视图矩阵
        Camera camera = Window.getCurrentScene().getCamera();
        shader.uploadMat4f("uProjection", camera.getProjectionMatrix());
        shader.uploadMat4f("uView", camera.getViewMatrix());

        // 绑定vao
        glBindVertexArray(vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);

        // 画像
        glDrawArrays(GL_LINES, 0, lines.size());

        // 解除绑定
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);

        glBindVertexArray(0);

        shader.detach();
    }

    // 画直线
    public static void addLine2D(Vector2f from, Vector2f to) {
        addLine2D(from, to, new Vector3f(0, 1, 0));
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color) {
        addLine2D(from, to, color, 1);
    }

    public static void addLine2D(Vector2f from, Vector2f to, Vector3f color, int lifeTime) {
        // 超出范围的线不画
        Camera camera = Window.getCurrentScene().getCamera();
        // 相机左边，添加一点偏移
        Vector2f cameraLeft = new Vector2f(camera.position).sub(2.0f, 2.0f);
        // 相机右边，添加一点偏移
        Vector2f cameraRight = new Vector2f(camera.position)
                .add(new Vector2f(camera.getProjectionSize()).mul(camera.getZoom())).add(4.0f, 4.0f);
        if (lines.size() >= MAX_LINES ||
                !((from.x > cameraLeft.x && from.x < cameraRight.x &&
                        from.y > cameraLeft.y && from.y < cameraRight.y) ||
                        (to.x > cameraLeft.x && to.x < cameraRight.x &&
                                to.y > cameraLeft.y && to.y < cameraRight.y)
                        )) {
            return;
        }
        DebugDraw.lines.add(new Line2D(from, to, color, lifeTime));
    }

    // 画形状
    public static void addShape2D(Vector2f[] vertices) {
        addShape2D(vertices, new Vector3f(0, 1, 0));
    }

    public static void addShape2D(Vector2f[] vertices, Vector3f color) {
        addShape2D(vertices, color, 1);
    }

    public static void addShape2D(Vector2f[] vertices, Vector3f color, int lifeTime) {
        for (int i = 0; i < vertices.length; ++i) {
            addLine2D(vertices[i], vertices[(i + 1) % vertices.length], color, lifeTime);
        }
    }

    // 长方形 重心，长宽，旋转，生存时间
    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation) {
        addBox2D(center, dimensions, rotation, new Vector3f(0, 1, 0));
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color) {
        addBox2D(center, dimensions, rotation, new Vector3f(0, 1, 0), 1);
    }

    public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color, int lifeTime) {
        // 左下角和右上角
        Vector2f min = new Vector2f(center).sub(dimensions);
        Vector2f max = new Vector2f(center).add(dimensions);

        // 构造四个顶点
        Vector2f[] vertices = {
                new Vector2f(min.x, max.y), new Vector2f(max.x, max.y),
                new Vector2f(max.x, min.y), new Vector2f(min.x, min.y)
        };

        // 如果有旋转就带上旋转
        if (rotation != 0.0f) {
            JMath.rotatePoints(vertices, center, rotation);
        }

        // 画长方形
        addShape2D(vertices, color, lifeTime);
    }

    // 画圆
    public static void addCircle2D(Vector2f center, float radius) {
        addCircle2D(center, radius, new Vector3f(0, 1, 0));
    }

    public static void addCircle2D(Vector2f center, float radius, Vector3f color) {
        addCircle2D(center, radius, color, 1);
    }

    public static void addCircle2D(Vector2f center, float radius, Vector3f color, int lifeTime) {
        // 采样率为360
        final int sampleRate = 20;
        // 构造顶点数组
        Vector2f[] vertices = new Vector2f[sampleRate];
        for (int i = 0, angle = 0; i < sampleRate; ++i, angle += 360 / sampleRate) {
            vertices[i] = new Vector2f(center.x + radius, center.y);
            JMath.rotatePoint(vertices[i], center, angle);
        }

        // 画图
        addShape2D(vertices, color, lifeTime);
    }
}
