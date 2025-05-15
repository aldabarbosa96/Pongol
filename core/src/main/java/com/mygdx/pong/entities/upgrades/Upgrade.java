package com.mygdx.pong.entities.upgrades;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Upgrade {
    public static final float SIZE  = 150f;
    private static final float SPEED = 35f;

    public final Upgrades  type;
    private final Sprite   sprite;
    public  final Rectangle bounds;
    public  boolean active;

    public Upgrade(Upgrades type, Texture tex, float x, float y) {
        this.type   = type;
        this.sprite = new Sprite(tex);
        sprite.setSize(SIZE, SIZE);
        sprite.setOriginCenter();
        sprite.setPosition(x - SIZE / 2f, y - SIZE / 2f);

        this.bounds = new Rectangle(sprite.getX(), sprite.getY(), SIZE, SIZE);
        this.active = true;
    }

    /** Desciende desde el lateral hasta el centro y actualiza bounds */
    public void update(float delta) {
        if (!active) return;
        final float centerX = 400f - SIZE / 2f;
        float dir = Math.signum(centerX - sprite.getX());

        if (dir != 0) sprite.translateX(dir * SPEED * delta);
        bounds.setPosition(sprite.getX(), sprite.getY());
    }

    public void render(SpriteBatch batch) { if (active) sprite.draw(batch); }
}
