package com.mygdx.pong.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;

public class Pelota {
    private final Sprite sprite;
    private final Circle hitbox;

    public Pelota(Texture texture, float x, float y, float width, float height) {
        sprite = new Sprite(texture);
        sprite.setSize(width, height);
        sprite.setOrigin(width / 2f, height / 2f);
        sprite.setPosition(x, y);
        hitbox = new Circle(sprite.getX() + sprite.getOriginX(), sprite.getY() + sprite.getOriginY(), Math.min(width, height) / 2f);
    }

    // Actualizamos posición, tamaño y origen
    public void setBounds(float x, float y, float width, float height) {
        sprite.setSize(width, height);
        sprite.setOrigin(width / 2f, height / 2f);
        sprite.setPosition(x, y);
        updateHitbox();
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    public void rotate(float degrees) { // todo --> valorar si hacemos que rote en un futuro
        sprite.rotate(degrees);
        updateHitbox();
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    private void updateHitbox() {
        float cx = sprite.getX() + sprite.getOriginX();
        float cy = sprite.getY() + sprite.getOriginY();
        hitbox.setPosition(cx, cy);
        hitbox.setRadius(Math.min(sprite.getWidth(), sprite.getHeight()) / 2f);
    }

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public void setColor(Color c) {
        sprite.setColor(c);
    }

    public Color getColor() {
        return sprite.getColor();
    }

    public Sprite getSprite() {
        return sprite;
    }
}
