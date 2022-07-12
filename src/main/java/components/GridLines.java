package components;

import org.joml.Vector2f;
import org.joml.Vector3f;
import pikacat.Camera;
import pikacat.Window;
import renderer.DebugDraw;
import util.Settings;

public class GridLines extends Component {

    @Override
    public void editorUpdate(float deltaTime) {
        Camera camera = Window.getCurrentScene().getCamera();

        Vector2f cameraPos = camera.position;
        Vector2f projectionSize = new Vector2f(camera.getProjectionSize()).mul(camera.getZoom());

        // 设计辅助线的起始坐标
        float firstX = (int) Math.floor(cameraPos.x / Settings.GRID_WIDTH) * Settings.GRID_WIDTH;
        float firstY = (int) Math.floor(cameraPos.y / Settings.GRID_HEIGHT) * Settings.GRID_HEIGHT;

        // 需要画多少条横线和竖线
        int numberOfVerticalLines = (int) (projectionSize.x / Settings.GRID_WIDTH) + 2;
        int numberOfHorizontalLines = (int) (projectionSize.y / Settings.GRID_HEIGHT) + 2;

        // 宽度和高度
        float width = (int) projectionSize.x + Settings.GRID_WIDTH * 5;
        float height = (int) projectionSize.y + Settings.GRID_HEIGHT * 5;

        // 取横线和竖线最大值作为循环的结束条件
        int maxLines = Math.max(numberOfHorizontalLines, numberOfVerticalLines);
        Vector3f color = new Vector3f(0.2f, 0.2f, 0.2f);

        for (int i = 0 ; i < maxLines; ++i) {
            // 线条的位置
            float x = firstX + Settings.GRID_WIDTH * i;
            float y = firstY + Settings.GRID_HEIGHT * i;

            // 画纵向线
            if (i < numberOfVerticalLines) {
                DebugDraw.addLine2D(new Vector2f(x, firstY), new Vector2f(x, firstY + height), color);
            }

            // 画横向线
            if (i < numberOfHorizontalLines) {
                DebugDraw.addLine2D(new Vector2f(firstX, y), new Vector2f(firstX + width, y), color);
            }
        }
    }

}
