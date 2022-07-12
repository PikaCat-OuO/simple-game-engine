package physics2d;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;
import org.joml.Vector2f;
import pikacat.GameObject;

public class RayCastInfo implements RayCastCallback {
    public Fixture fixture;
    public Vector2f point ;
    public Vector2f normal;
    public float fraction;
    public boolean hit;
    public GameObject hitObject;

    private GameObject requestingObject;

    public RayCastInfo(GameObject gameObject) {
        this.fixture = null;
        this.point = new Vector2f();
        this.normal = new Vector2f();
        this.fraction = 0.0f;
        this.hit = false;
        this.hitObject = null;
        this.requestingObject = gameObject;
    }

    @Override
    public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction) {
        if (fixture.getUserData() == requestingObject) {
            // 碰到自己，不关心，继续找
            return 1;
        }

        this.fixture = fixture;
        this.point = new Vector2f(point.x ,point.y);
        this.normal = new Vector2f(normal.x, normal.y);
        this.fraction = fraction;
        this.hit = fraction != 0;
        this.hitObject = (GameObject) fixture.getUserData();

        return fraction;
    }
}
