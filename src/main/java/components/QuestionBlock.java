package components;

import pikacat.GameObject;
import pikacat.Prefabs;
import pikacat.Window;
import util.AssetPool;
import util.Settings;

public class QuestionBlock extends Block {
    private enum BlockType {
        COIN,
        POWER_UP,
        INVINCIBILITY
    }

    public BlockType blockType = BlockType.COIN;

    @Override
    public void playerHit(PlayerController playerController) {
        if (isActive()) {
            switch (blockType) {
                case COIN -> doCoin(playerController);
                case POWER_UP -> doPowerUp(playerController);
                case INVINCIBILITY -> doInvincibility(playerController);
            }

            StateMachine stateMachine = this.gameObject.getComponent(StateMachine.class);
            if (stateMachine != null) {
                stateMachine.trigger("setInactive");
                this.setInactive();
            }
        } else {
            AssetPool.getSound("assets/sounds/bump.ogg");
        }
    }

    private void doInvincibility(PlayerController playerController) {
    }

    private void doPowerUp(PlayerController playerController) {
        if (playerController.isSmall()) {
            spawnMushroom();
        } else {
            spawnFlower();
        }
    }

    private void doCoin(PlayerController playerController) {
        GameObject coin = Prefabs.generateBlockCoin();
        coin.transform.position.set(this.gameObject.transform.position);
        coin.transform.position.y += Settings.GRID_HEIGHT;
        Window.getCurrentScene().addGameObjectToScene(coin);
    }

    private void spawnMushroom() {
        GameObject mushroom = Prefabs.generateMushroom();
        mushroom.transform.position.set(this.gameObject.transform.position);
        mushroom.transform.position.y += Settings.GRID_HEIGHT;
        Window.getCurrentScene().addGameObjectToScene(mushroom);
    }

    private void spawnFlower() {
        GameObject flower = Prefabs.generateFlower();
        flower.transform.position.set(this.gameObject.transform.position);
        flower.transform.position.y += Settings.GRID_HEIGHT;
        Window.getCurrentScene().addGameObjectToScene(flower);
    }
}
