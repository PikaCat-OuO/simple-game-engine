package pikacat;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Camera {

    // 投影矩阵，视野矩阵
    private Matrix4f projectionMatrix, viewMatrix, inverseProjection, inverseView;

    // 相机的位置
    public Vector2f position;

    // 背景的颜色
    public Vector4f clearColor = new Vector4f(1, 1, 1, 1);

    private Vector2f projectionSize = new Vector2f(6, 3);

    // 放大级别
    private float zoom = 1.0f;

    public Camera(Vector2f position) {
        this.position = position;
        this.projectionMatrix = new Matrix4f();
        this.viewMatrix = new Matrix4f();
        this.inverseProjection = new Matrix4f();
        this.inverseView = new Matrix4f();
        adjustProjection();
    }

    // 调整相机的投影
    public void adjustProjection() {
        this.projectionMatrix.identity();
        this.projectionMatrix.ortho(0.0f, this.projectionSize.x * this.zoom,
                0.0f, this.projectionSize.y * this.zoom,
                0.0f, 100.0f);
        this.projectionMatrix.invert(inverseProjection);
    }

    // 计算视图矩阵并返回
    public Matrix4f getViewMatrix() {
        Vector3f cameraFront = new Vector3f(0.0f, 0.0f, -1.0f);
        Vector3f cameraUp = new Vector3f(0.0f, 1.0f ,0.0f);
        this.viewMatrix.identity();
        this.viewMatrix.lookAt(new Vector3f(position.x, position.y, 20.0f),
                cameraFront.add(position.x, position.y, 0.0f), cameraUp);
        this.viewMatrix.invert(inverseView);
        return this.viewMatrix;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getInverseProjection() {
        return inverseProjection;
    }

    public Matrix4f getInverseView() {
        return inverseView;
    }

    public Vector2f getProjectionSize() {
        return projectionSize;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void addZoom(float value) {
        this.zoom += value;
    }
}
