package components;

public class FontRenderer extends Component {
    @Override
    public void start() {
        if (gameObject.getComponent(SpriteRenderer.class) != null) {
            System.out.println("找到字体渲染");
        }
    }

    @Override
    public void update(float deltaTime) {

    }
}
