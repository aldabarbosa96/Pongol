package com.mygdx.pong.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class Marcadores {
    private final BitmapFont font;
    private final BitmapFont fontInverted;
    private final GlyphLayout layout = new GlyphLayout();
    private int bottomScore;
    private int topScore;
    private final float margin;

    public Marcadores(float scale, float margin) {
        this.margin = margin;

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = (int) (25 * scale);
        param.color = Color.WHITE;

        font = gen.generateFont(param);
        fontInverted = gen.generateFont(param);

        // Fuente invertida
        fontInverted.getData().setScale(-1f, -1f);

        gen.dispose();

        bottomScore = 0;
        topScore = 0;
    }

    public void addPointBottom() {
        bottomScore++;
    }

    public void addPointTop() {
        topScore++;
    }

    public void draw(SpriteBatch batch, float worldW, float worldH) {
        // Dibujo puntuación inferior
        String bScore = Integer.toString(bottomScore);
        layout.setText(font, bScore);
        float textWb = layout.width;
        float textHb = layout.height;
        float xRight = worldW - margin - textWb;
        float centerY = worldH / 2f;
        float offsetY = worldH * 0.05f;
        float yBottom = (centerY - offsetY) + textHb / 2f;
        font.draw(batch, bScore, xRight, yBottom);

        // Dibujo puntuación superior (invertida)
        String tScore = Integer.toString(topScore);
        layout.setText(fontInverted, tScore);
        float textWt = Math.abs(layout.width);
        float textHt = Math.abs(layout.height);
        float xInv = worldW - margin - textWt;
        float yTopBase = (centerY + offsetY) - textHt / 2f;
        fontInverted.draw(batch, tScore, xInv, yTopBase);
    }

    public void dispose() {
        font.dispose();
        fontInverted.dispose();
    }

    public int getBottomScore() {
        return bottomScore;
    }

    public int getTopScore() {
        return topScore;
    }
}
