package components;

import util.AssetPool;

public class BreakableBrick extends Block {
    @Override
    public void playerHit(PlayerController playerController) {
        if (!playerController.isSmall()) {
            AssetPool.getSound("assets/sounds/break_block.ogg").play();
            gameObject.destroy();
        } else {
            AssetPool.getSound("assets/sounds/bump.ogg").play();
        }
    }
}
