package physics2d;

import components.Component;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.WorldManifold;
import org.jbox2d.dynamics.contacts.Contact;
import org.joml.Vector2f;
import pikacat.GameObject;

public class ContactListener implements org.jbox2d.callbacks.ContactListener {

    // 碰撞之前
    @Override
    public void preSolve(Contact contact, Manifold manifold) {
        // 获得碰撞的双方
        GameObject gameObjectA = (GameObject) contact.getFixtureA().getUserData();
        GameObject gameObjectB = (GameObject) contact.getFixtureB().getUserData();

        // 获得物理世界当时的情况
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);

        // 从当时的情况中获取两个物体的碰撞方向
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        Vector2f bNormal = new Vector2f(aNormal).negate();

        // 对游戏对象的组件进行碰撞，这里调用用户自定义的碰撞流程
        for (Component component : gameObjectA.getComponents()) {
            component.preSolve(gameObjectB, contact, aNormal);
        }

        for (Component component : gameObjectB.getComponents()) {
            component.preSolve(gameObjectA, contact, bNormal);
        }
    }

    // 开始碰撞
    @Override
    public void beginContact(Contact contact) {
        // 获得碰撞的双方
        GameObject gameObjectA = (GameObject) contact.getFixtureA().getUserData();
        GameObject gameObjectB = (GameObject) contact.getFixtureB().getUserData();

        // 获得物理世界当时的情况
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);

        // 从当时的情况中获取两个物体的碰撞方向
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        Vector2f bNormal = new Vector2f(aNormal).negate();

        // 对游戏对象的组件进行碰撞，这里调用用户自定义的碰撞流程
        for (Component component : gameObjectA.getComponents()) {
            component.beginCollision(gameObjectB, contact, aNormal);
        }

        for (Component component : gameObjectB.getComponents()) {
            component.beginCollision(gameObjectA, contact, bNormal);
        }
    }

    // 碰撞之中
    @Override
    public void endContact(Contact contact) {
        // 获得碰撞的双方
        GameObject gameObjectA = (GameObject) contact.getFixtureA().getUserData();
        GameObject gameObjectB = (GameObject) contact.getFixtureB().getUserData();

        // 获得物理世界当时的情况
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);

        // 从当时的情况中获取两个物体的碰撞方向
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        Vector2f bNormal = new Vector2f(aNormal).negate();

        // 对游戏对象的组件进行碰撞，这里调用用户自定义的碰撞流程
        for (Component component : gameObjectA.getComponents()) {
            component.endCollision(gameObjectB, contact, aNormal);
        }

        for (Component component : gameObjectB.getComponents()) {
            component.endCollision(gameObjectA, contact, bNormal);
        }
    }

    // 碰撞之后
    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {
        // 获得碰撞的双方
        GameObject gameObjectA = (GameObject) contact.getFixtureA().getUserData();
        GameObject gameObjectB = (GameObject) contact.getFixtureB().getUserData();

        // 获得物理世界当时的情况
        WorldManifold worldManifold = new WorldManifold();
        contact.getWorldManifold(worldManifold);

        // 从当时的情况中获取两个物体的碰撞方向
        Vector2f aNormal = new Vector2f(worldManifold.normal.x, worldManifold.normal.y);
        Vector2f bNormal = new Vector2f(aNormal).negate();

        // 对游戏对象的组件进行碰撞，这里调用用户自定义的碰撞流程
        for (Component component : gameObjectA.getComponents()) {
            component.postSolve(gameObjectB, contact, aNormal);
        }

        for (Component component : gameObjectB.getComponents()) {
            component.postSolve(gameObjectA, contact, bNormal);
        }
    }
}
