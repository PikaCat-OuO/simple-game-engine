package components;

import org.joml.Vector2f;
import renderer.Texture;

import java.util.ArrayList;
import java.util.List;

// 材质表
public class Spritesheet {

    // 材质表的图片
    private Texture texture;

    // 材质表上的精灵
    private List<Sprite> sprites;

    public Spritesheet(Texture texture, int spriteWidth, int spriteHeight, int numSprites, int spacing) {
        this.sprites = new ArrayList<>();

        this.texture = texture;


        int currentX = 0;
        int currentY = texture.getHeight() - spriteHeight;

        // 逐个遍历材质，提取材质，将宽度和高度信息转化为坐标
        for (int i = 0; i < numSprites; ++i) {
            float leftX =  currentX / (float) texture.getWidth();
            float rightX =  (currentX + spriteWidth) / (float) texture.getWidth();
            float topY =  (currentY + spriteHeight) / (float) texture.getHeight();
            float bottomY =  currentY / (float) texture.getHeight();

            // 逆时针摆放材质
            Vector2f[] texCoords = {
                    new Vector2f(rightX, topY),
                    new Vector2f(rightX, bottomY),
                    new Vector2f(leftX, bottomY),
                    new Vector2f(leftX, topY)
            };

            // 提取了材质之后就放入材质组里面
            Sprite sprite = new Sprite();
            sprite.setTexture(texture);
            sprite.setTexCoords(texCoords);
            sprite.setWidth(spriteWidth);
            sprite.setHeight(spriteHeight);
            this.sprites.add(sprite);


            // 读取下一个材质
            currentX += spriteWidth + spacing;
            if (currentX >= texture.getWidth()) {
                currentX = 0;
                currentY -= spriteHeight + spacing;
            }
        }
    }

    public Sprite getSprite(int index) {
        return this.sprites.get(index);
    }

    public int size() {
        return this.sprites.size();
    }
}
