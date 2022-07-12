package pikacat;

import components.*;
import org.jbox2d.dynamics.BodyType;
import org.joml.Vector2f;
import physics2d.components.Box2DCollider;
import physics2d.components.CircleCollider;
import physics2d.components.PillboxCollider;
import physics2d.components.RigidBody2D;
import util.AssetPool;

public class Prefabs {
    public static GameObject generateSpriteObject(Sprite sprite, float sizeX, float sizeY) {
        GameObject block = Window.getCurrentScene().createGameObject("Sprite_Object_Gen");
        block.transform.scale.x = sizeX;
        block.transform.scale.y = sizeY;
        SpriteRenderer spriteRenderer = new SpriteRenderer();
        spriteRenderer.setSprite(sprite);
        block.addComponent(spriteRenderer);
        return block;
    }

    public static GameObject generateMario() {
        Spritesheet playerSprites = AssetPool.getSpritesheet("assets/images/spritesheet.png");
        Spritesheet bigPlayerSprites = AssetPool.getSpritesheet("assets/images/bigSpritesheet.png");
        GameObject mario = generateSpriteObject(playerSprites.getSprite(0), 0.25f, 0.25f);

        // 小马里奥
        AnimationState run = new AnimationState();
        run.title = "Run";
        final float DEFAULT_FRAME_TIME = 0.2f;
        run.addFrame(playerSprites.getSprite(0), DEFAULT_FRAME_TIME);
        run.addFrame(playerSprites.getSprite(2), DEFAULT_FRAME_TIME);
        run.addFrame(playerSprites.getSprite(3), DEFAULT_FRAME_TIME);
        run.addFrame(playerSprites.getSprite(2), DEFAULT_FRAME_TIME);
        run.setLoop(true);

        AnimationState switchDirection = new AnimationState();
        switchDirection.title = "Switch Direction";
        switchDirection.addFrame(playerSprites.getSprite(4), 0.1f);
        switchDirection.setLoop(false);

        AnimationState idle = new AnimationState();
        idle.title = "Idle";
        idle.addFrame(playerSprites.getSprite(0), 0.1f);
        idle.setLoop(false);

        AnimationState jump = new AnimationState();
        jump.title = "Jump";
        jump.addFrame(playerSprites.getSprite(5), 0.1f);
        jump.setLoop(false);

        // 大马里奥
        AnimationState bigRun = new AnimationState();
        bigRun.title = "BigRun";
        bigRun.addFrame(bigPlayerSprites.getSprite(0), DEFAULT_FRAME_TIME);
        bigRun.addFrame(bigPlayerSprites.getSprite(1), DEFAULT_FRAME_TIME);
        bigRun.addFrame(bigPlayerSprites.getSprite(2), DEFAULT_FRAME_TIME);
        bigRun.addFrame(bigPlayerSprites.getSprite(3), DEFAULT_FRAME_TIME);
        bigRun.addFrame(bigPlayerSprites.getSprite(2), DEFAULT_FRAME_TIME);
        bigRun.addFrame(bigPlayerSprites.getSprite(1), DEFAULT_FRAME_TIME);
        bigRun.setLoop(true);

        AnimationState bigSwitchDirection = new AnimationState();
        bigSwitchDirection.title = "Big Switch Direction";
        bigSwitchDirection.addFrame(bigPlayerSprites.getSprite(4), 0.1f);
        bigSwitchDirection.setLoop(false);

        AnimationState bigIdle = new AnimationState();
        bigIdle.title = "BigIdle";
        bigIdle.addFrame(bigPlayerSprites.getSprite(0), 0.1f);
        bigIdle.setLoop(false);

        AnimationState bigJump = new AnimationState();
        bigJump.title = "BigJump";
        bigJump.addFrame(bigPlayerSprites.getSprite(5), 0.1f);
        bigJump.setLoop(false);

        // 火球马里奥
        int fireOffset = 21;
        AnimationState fireRun = new AnimationState();
        fireRun.title = "FireRun";
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset), DEFAULT_FRAME_TIME);
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset + 1), DEFAULT_FRAME_TIME);
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset + 2), DEFAULT_FRAME_TIME);
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset + 3), DEFAULT_FRAME_TIME);
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset + 2), DEFAULT_FRAME_TIME);
        fireRun.addFrame(bigPlayerSprites.getSprite(fireOffset + 1), DEFAULT_FRAME_TIME);
        fireRun.setLoop(true);

        AnimationState fireSwitchDirection = new AnimationState();
        fireSwitchDirection.title = "Fire Switch Direction";
        fireSwitchDirection.addFrame(bigPlayerSprites.getSprite(fireOffset + 4), 0.1f);
        fireSwitchDirection.setLoop(false);

        AnimationState fireIdle = new AnimationState();
        fireIdle.title = "FireIdle";
        fireIdle.addFrame(bigPlayerSprites.getSprite(fireOffset), 0.1f);
        fireIdle.setLoop(false);

        AnimationState fireJump = new AnimationState();
        fireJump.title = "FireJump";
        fireJump.addFrame(bigPlayerSprites.getSprite(fireOffset + 5), 0.1f);
        fireJump.setLoop(false);

        AnimationState die = new AnimationState();
        die.title = "Die";
        die.addFrame(playerSprites.getSprite(6), 0.1f);
        die.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(run);
        stateMachine.addState(idle);
        stateMachine.addState(switchDirection);
        stateMachine.addState(jump);
        stateMachine.addState(die);

        stateMachine.addState(bigRun);
        stateMachine.addState(bigIdle);
        stateMachine.addState(bigSwitchDirection);
        stateMachine.addState(bigJump);

        stateMachine.addState(fireRun);
        stateMachine.addState(fireIdle);
        stateMachine.addState(fireSwitchDirection);
        stateMachine.addState(fireJump);

        stateMachine.setDefaultStateTitle(idle.title);
        stateMachine.addStateTrigger(run.title, switchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(run.title, idle.title, "stopRunning");
        stateMachine.addStateTrigger(run.title, jump.title, "jump");
        stateMachine.addStateTrigger(switchDirection.title, idle.title, "stopRunning");
        stateMachine.addStateTrigger(switchDirection.title, run.title, "startRunning");
        stateMachine.addStateTrigger(switchDirection.title, jump.title, "jump");
        stateMachine.addStateTrigger(idle.title, run.title, "startRunning");
        stateMachine.addStateTrigger(idle.title, jump.title, "jump");
        stateMachine.addStateTrigger(jump.title, idle.title, "stopJumping");

        stateMachine.addStateTrigger(bigRun.title, bigSwitchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(bigRun.title, bigIdle.title, "stopRunning");
        stateMachine.addStateTrigger(bigRun.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigIdle.title, "stopRunning");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigRun.title, "startRunning");
        stateMachine.addStateTrigger(bigSwitchDirection.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigIdle.title, bigRun.title, "startRunning");
        stateMachine.addStateTrigger(bigIdle.title, bigJump.title, "jump");
        stateMachine.addStateTrigger(bigJump.title, bigIdle.title, "stopJumping");

        stateMachine.addStateTrigger(fireRun.title, fireSwitchDirection.title, "switchDirection");
        stateMachine.addStateTrigger(fireRun.title, fireIdle.title, "stopRunning");
        stateMachine.addStateTrigger(fireRun.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireIdle.title, "stopRunning");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireRun.title, "startRunning");
        stateMachine.addStateTrigger(fireSwitchDirection.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireIdle.title, fireRun.title, "startRunning");
        stateMachine.addStateTrigger(fireIdle.title, fireJump.title, "jump");
        stateMachine.addStateTrigger(fireJump.title, fireIdle.title, "stopJumping");

        stateMachine.addStateTrigger(run.title, bigRun.title, "powerup");
        stateMachine.addStateTrigger(idle.title, bigIdle.title, "powerup");
        stateMachine.addStateTrigger(switchDirection.title, bigSwitchDirection.title, "powerup");
        stateMachine.addStateTrigger(jump.title, bigJump.title, "powerup");
        stateMachine.addStateTrigger(bigRun.title, fireRun.title, "powerup");
        stateMachine.addStateTrigger(bigIdle.title, fireIdle.title, "powerup");
        stateMachine.addStateTrigger(bigSwitchDirection.title, fireSwitchDirection.title, "powerup");
        stateMachine.addStateTrigger(bigJump.title, fireJump.title, "powerup");

        stateMachine.addStateTrigger(bigRun.title, run.title, "damage");
        stateMachine.addStateTrigger(bigIdle.title, idle.title, "damage");
        stateMachine.addStateTrigger(bigSwitchDirection.title, switchDirection.title, "damage");
        stateMachine.addStateTrigger(bigJump.title, jump.title, "damage");
        stateMachine.addStateTrigger(fireRun.title, bigRun.title, "damage");
        stateMachine.addStateTrigger(fireIdle.title, bigIdle.title, "damage");
        stateMachine.addStateTrigger(fireSwitchDirection.title, bigSwitchDirection.title, "damage");
        stateMachine.addStateTrigger(fireJump.title, bigJump.title, "damage");

        stateMachine.addStateTrigger(run.title, die.title, "die");
        stateMachine.addStateTrigger(switchDirection.title, die.title, "die");
        stateMachine.addStateTrigger(idle.title, die.title, "die");
        stateMachine.addStateTrigger(jump.title, die.title, "die");
        stateMachine.addStateTrigger(bigRun.title, run.title, "die");
        stateMachine.addStateTrigger(bigSwitchDirection.title, switchDirection.title, "die");
        stateMachine.addStateTrigger(bigIdle.title, idle.title, "die");
        stateMachine.addStateTrigger(bigJump.title, jump.title, "die");
        stateMachine.addStateTrigger(fireRun.title, bigRun.title, "die");
        stateMachine.addStateTrigger(fireSwitchDirection.title, bigSwitchDirection.title, "die");
        stateMachine.addStateTrigger(fireIdle.title, bigIdle.title, "die");
        stateMachine.addStateTrigger(fireJump.title, bigJump.title, "die");
        mario.addComponent(stateMachine);

        // 给马里奥设置碰撞
        PillboxCollider pillboxCollider = new PillboxCollider();
        pillboxCollider.setWidth(0.21f);
        pillboxCollider.setHeight(0.25f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.DYNAMIC);
        rigidBody.setContinuesCollision(false);
        rigidBody.setFixedRotation(true);
        rigidBody.setMass(25.0f);

        mario.addComponent(rigidBody);
        mario.addComponent(pillboxCollider);
        mario.addComponent(new PlayerController());

        mario.transform.zIndex = 10;

        return mario;
    }

    public static GameObject generateQuestionBlock() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject questionBlock = generateSpriteObject(itemSprites.getSprite(0), 0.25f, 0.25f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState flicker = new AnimationState();
        flicker.title = "Question";
        flicker.addFrame(itemSprites.getSprite(0), 0.57f);
        flicker.addFrame(itemSprites.getSprite(1), DEFAULT_FRAME_TIME);
        flicker.addFrame(itemSprites.getSprite(2), DEFAULT_FRAME_TIME);
        flicker.setLoop(true);

        AnimationState inactive = new AnimationState();
        inactive.title = "Inactive";
        inactive.addFrame(itemSprites.getSprite(3), 0.1f);
        inactive.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(flicker);
        stateMachine.addState(inactive);
        stateMachine.setDefaultStateTitle(flicker.title);
        stateMachine.addStateTrigger(flicker.title, inactive.title, "setInactive");

        questionBlock.addComponent(stateMachine);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        questionBlock.addComponent(rigidBody);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.125f, 0.125f));
        questionBlock.addComponent(box2DCollider);

        questionBlock.addComponent(new Ground());

        questionBlock.addComponent(new QuestionBlock());

        return questionBlock;
    }

    public static GameObject generateBlockCoin() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject coin = generateSpriteObject(itemSprites.getSprite(7), 0.25f, 0.25f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState coinFlip = new AnimationState();
        coinFlip.title = "CoinFlip";
        coinFlip.addFrame(itemSprites.getSprite(7), 0.57f);
        coinFlip.addFrame(itemSprites.getSprite(8), DEFAULT_FRAME_TIME);
        coinFlip.addFrame(itemSprites.getSprite(9), DEFAULT_FRAME_TIME);
        coinFlip.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(coinFlip);
        stateMachine.setDefaultStateTitle(coinFlip.title);

        coin.addComponent(stateMachine);

        coin.addComponent(new BlockCoin());

        return coin;
    }

    public static GameObject generateCoin() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject coin = generateSpriteObject(itemSprites.getSprite(7), 0.25f, 0.25f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState coinFlip = new AnimationState();
        coinFlip.title = "CoinFlip";
        coinFlip.addFrame(itemSprites.getSprite(7), 0.57f);
        coinFlip.addFrame(itemSprites.getSprite(8), DEFAULT_FRAME_TIME);
        coinFlip.addFrame(itemSprites.getSprite(9), DEFAULT_FRAME_TIME);
        coinFlip.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(coinFlip);
        stateMachine.setDefaultStateTitle(coinFlip.title);

        coin.addComponent(stateMachine);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.12f);
        coin.addComponent(circleCollider);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        coin.addComponent(rigidBody);

        coin.addComponent(new Coin());

        return coin;
    }

    public static GameObject generateMushroom() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject mushroom = generateSpriteObject(itemSprites.getSprite(10), 0.25f, 0.25f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.DYNAMIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        mushroom.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.14f);
        mushroom.addComponent(circleCollider);

        mushroom.addComponent(new MushroomAI());

        return mushroom;
    }

    public static GameObject generateFlower() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject flower = generateSpriteObject(itemSprites.getSprite(20), 0.25f, 0.25f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        flower.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.14f);
        flower.addComponent(circleCollider);

        flower.addComponent(new Flower());

        return flower;
    }

    public static GameObject generateGoomba() {
        Spritesheet enemySprites = AssetPool.getSpritesheet("assets/images/spritesheet.png");
        GameObject goomba = generateSpriteObject(enemySprites.getSprite(14), 0.25f, 0.25f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState walk = new AnimationState();
        walk.title = "Walk";
        walk.addFrame(enemySprites.getSprite(14), DEFAULT_FRAME_TIME);
        walk.addFrame(enemySprites.getSprite(15), DEFAULT_FRAME_TIME);
        walk.setLoop(true);

        AnimationState squashed = new AnimationState();
        squashed.title = "Squashed";
        squashed.addFrame(enemySprites.getSprite(16), 0.1f);
        squashed.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(walk);
        stateMachine.addState(squashed);
        stateMachine.setDefaultStateTitle(walk.title);

        stateMachine.addStateTrigger(walk.title, squashed.title, "squashMe");

        goomba.addComponent(stateMachine);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.DYNAMIC);
        rigidBody.setMass(0.1f);
        rigidBody.setFixedRotation(true);
        goomba.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.12f);
        goomba.addComponent(circleCollider);

        goomba.addComponent(new GoombaAI());

        return goomba;
    }

    public static GameObject generateTurtle() {
        Spritesheet turtleSprites = AssetPool.getSpritesheet("assets/images/turtle.png");
        GameObject turtle = generateSpriteObject(turtleSprites.getSprite(0), 0.25f, 0.35f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState walk = new AnimationState();
        walk.title = "Walk";
        walk.addFrame(turtleSprites.getSprite(0), DEFAULT_FRAME_TIME);
        walk.addFrame(turtleSprites.getSprite(1), DEFAULT_FRAME_TIME);
        walk.setLoop(true);

        AnimationState turtleShellSpin = new AnimationState();
        turtleShellSpin.title = "TurtleShellSpin";
        turtleShellSpin.addFrame(turtleSprites.getSprite(2), 0.1f);
        turtleShellSpin.setLoop(false);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(walk);
        stateMachine.addState(turtleShellSpin);
        stateMachine.setDefaultStateTitle(walk.title);

        stateMachine.addStateTrigger(walk.title, turtleShellSpin.title, "squashMe");

        turtle.addComponent(stateMachine);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.DYNAMIC);
        rigidBody.setMass(0.1f);
        rigidBody.setFixedRotation(true);
        turtle.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.13f);
        circleCollider.setOffset(new Vector2f(0, -0.05f));
        turtle.addComponent(circleCollider);

        turtle.addComponent(new TurtleAI());

        return turtle;
    }

    public static GameObject generateDialogTurtle() {
        Spritesheet turtleSprites = AssetPool.getSpritesheet("assets/images/turtle.png");
        GameObject turtle = generateSpriteObject(turtleSprites.getSprite(0), 0.25f, 0.35f);

        final float DEFAULT_FRAME_TIME = 0.23f;

        AnimationState walk = new AnimationState();
        walk.title = "Walk";
        walk.addFrame(turtleSprites.getSprite(0), DEFAULT_FRAME_TIME);
        walk.addFrame(turtleSprites.getSprite(1), DEFAULT_FRAME_TIME);
        walk.setLoop(true);

        StateMachine stateMachine = new StateMachine();
        stateMachine.addState(walk);
        stateMachine.setDefaultStateTitle(walk.title);

        turtle.addComponent(stateMachine);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        rigidBody.setMass(0.1f);
        rigidBody.setFixedRotation(true);
        turtle.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.13f);
        circleCollider.setOffset(new Vector2f(0, -0.05f));
        turtle.addComponent(circleCollider);

        turtle.addComponent(new Dialog());

        return turtle;
    }

    public static GameObject generatePipe(Direction direction) {
        Spritesheet pipeSprites = AssetPool.getSpritesheet("assets/images/pipes.png");
        int index = switch (direction) {
            case DOWN -> 0;
            case UP -> 1;
            case RIGHT -> 2;
            case LEFT -> 3;
        };

        GameObject pipe = generateSpriteObject(pipeSprites.getSprite(index), 0.5f, 0.5f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        pipe.addComponent(rigidBody);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.25f, 0.25f));
        pipe.addComponent(box2DCollider);

        pipe.addComponent(new Pipe(direction));

        pipe.addComponent(new Ground());

        return pipe;
    }

    public static GameObject generateFlagTop() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject flagTop = generateSpriteObject(itemSprites.getSprite(6), 0.25f, 0.25f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        flagTop.addComponent(rigidBody);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.05f, 0.125f));
        box2DCollider.setOffset(new Vector2f(-0.075f, 0.0f));
        flagTop.addComponent(box2DCollider);

        flagTop.addComponent(new FlagPole(true));

        return flagTop;
    }

    public static GameObject generateFlagPole() {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject flagTop = generateSpriteObject(itemSprites.getSprite(33), 0.25f, 0.25f);

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.STATIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        flagTop.addComponent(rigidBody);

        Box2DCollider box2DCollider = new Box2DCollider();
        box2DCollider.setHalfSize(new Vector2f(0.05f, 0.125f));
        box2DCollider.setOffset(new Vector2f(-0.075f, 0.0f));
        flagTop.addComponent(box2DCollider);

        flagTop.addComponent(new FlagPole(false));

        return flagTop;
    }

    public static GameObject generateFireball(Vector2f position) {
        Spritesheet itemSprites = AssetPool.getSpritesheet("assets/images/items.png");
        GameObject fireball = generateSpriteObject(itemSprites.getSprite(32), 0.18f, 0.18f);
        fireball.transform.position = position;

        RigidBody2D rigidBody = new RigidBody2D();
        rigidBody.setBodyType(BodyType.DYNAMIC);
        rigidBody.setFixedRotation(true);
        rigidBody.setContinuesCollision(false);
        fireball.addComponent(rigidBody);

        CircleCollider circleCollider = new CircleCollider();
        circleCollider.setRadius(0.08f);
        fireball.addComponent(circleCollider);

        fireball.addComponent(new Fireball());

        return fireball;
    }
}