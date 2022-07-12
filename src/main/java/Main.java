import pikacat.Window;
import scenes.LevelEditorSceneInitializer;

public class Main {
    public static void main(String[] args) {
        Window window = Window.getInstance();
        window.init();
        Window.changeScene(new LevelEditorSceneInitializer(null));
        window.run();
    }
}
