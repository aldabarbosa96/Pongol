package com.mygdx.pong.entities;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Marcadores {
    private final BitmapFont font;
    private final BitmapFont fontInverted;
    private final GlyphLayout layout = new GlyphLayout();
    private int bottomScore;
    private int topScore;
    private final float scale;
    private final float margin;

    public Marcadores(float scale, float margin) {
        this.scale = scale;
        this.margin = margin;
        font = new BitmapFont();
        font.getData().setScale(scale, scale);
        fontInverted = new BitmapFont();
        fontInverted.getData().setScale(-scale, -scale);
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
        // --- Texto y medidas inferiores ---
        String bScore = Integer.toString(bottomScore);
        layout.setText(font, bScore);
        float textWb = layout.width;
        float textHb = layout.height;
        float xRight = worldW - margin - textWb;

        float centerY = worldH / 2f;
        float offsetY = worldH * 0.05f;

        // Baseline para el marcador inferior
        float desiredCenterBottom = centerY - offsetY;
        float yBottom = desiredCenterBottom + textHb / 2f;
        font.draw(batch, bScore, xRight, yBottom);

        // --- Texto y medidas superiores ---
        String tScore = Integer.toString(topScore);
        layout.setText(fontInverted, tScore);
        float textWt = Math.abs(layout.width);
        float textHt = Math.abs(layout.height);

        // CORRECCIÓN EJE X: restamos, no sumamos
        float xInv = worldW - margin - textWt;

        float desiredCenterTop = centerY + offsetY;
        float yTopBase = desiredCenterTop - textHt / 2f;

        fontInverted.draw(batch, tScore, xInv, yTopBase);
    }

    public void dispose() {
        font.dispose();
        fontInverted.dispose();
    }
}
