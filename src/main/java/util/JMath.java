package util;

import org.joml.Vector2f;

public class JMath {
    // 一个点绕着origin旋转angle°之后的新坐标位置
    public static void rotatePoint(Vector2f point, Vector2f origin, float angleDegree) {
        // 先将点平移到坐标原点
        point.sub(origin);

        // 带入公式计算
        float sin = (float) Math.sin(Math.toRadians(angleDegree));
        float cos = (float) Math.cos(Math.toRadians(angleDegree));
        float x = point.x;
        float y = point.y;

        point.x = x * cos - y * sin;
        point.y = y * cos + x * sin;

        // 平移回来
        point.add(origin);
    }

    // 旋转多个点，传入多个顶点的坐标，原点坐标和旋转的角度
    public static void rotatePoints(Vector2f[] points, Vector2f origin, float angleDegree) {
        for (Vector2f point : points) {
            rotatePoint(point, origin, angleDegree);
        }
    }
}
