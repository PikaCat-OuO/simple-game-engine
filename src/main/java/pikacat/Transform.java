package pikacat;

import components.Component;
import editor.JImGui;
import org.joml.Vector2f;

public class Transform extends Component {

    public Vector2f position;
    public Vector2f scale;
    public float rotation = 0.0f;
    public int zIndex;

    public Transform() {
        init(new Vector2f(), new Vector2f());
    }

    public Transform(Vector2f position) {
        init(position, new Vector2f());
    }

    public Transform(Vector2f position, Vector2f scale) {
        init(position, scale);
    }

    public void init(Vector2f position, Vector2f scale) {
        this.position = position;
        this.scale = scale;
        this.zIndex = 0;
    }

    @Override
    public void imgui() {
        this.gameObject.name = JImGui.inputText("Name", this.gameObject.name);
        JImGui.drawVec2Control("Position", this.position);
        JImGui.drawVec2Control("Scale", this.scale, 32.0f);
        this.rotation = JImGui.dragFloat("Rotation", this.rotation);
        this.zIndex = JImGui.dragInt("zIndex", this.zIndex);
    }

    public Transform copy() {
        return new Transform(new Vector2f(this.position), new Vector2f(this.scale));
    }

    public void copy(Transform to) {
        to.position.set(this.position);
        to.scale.set(this.scale);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Transform transform)) return false;
        else return transform.position.equals(this.position) && transform.scale.equals(this.scale) &&
                transform.rotation == this.rotation && transform.zIndex == this.zIndex;
    }
}
