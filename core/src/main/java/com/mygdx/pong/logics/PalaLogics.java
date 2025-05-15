package com.mygdx.pong.logics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.pong.entities.Pala;

public class PalaLogics {

    private final Pala pala;
    private final boolean isBottom;
    private final Camera camera;

    private final float defaultWidth;
    private float widthTimer = 0f;
    private float mirrorTimer = 0f;

    private final Color defaultColor;
    private float colorTimer = 0f;


    public PalaLogics(Pala pala, boolean isBottom, Camera camera) {
        this.pala        = pala;
        this.isBottom    = isBottom;
        this.camera      = camera;
        this.defaultWidth = pala.getBounds().width;
        this.defaultColor = new Color(pala.getColor());
    }

    public void update(float delta) {
        float wW = camera.viewportWidth;
        float wH = camera.viewportHeight;

        // Movimiento táctil
        for (int p = 0; p < 5; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            Vector3 pos = new Vector3(Gdx.input.getX(p), Gdx.input.getY(p), 0);
            camera.unproject(pos);

            boolean okHalf = isBottom ? pos.y < wH / 2f : pos.y >= wH / 2f;
            if (okHalf) {
                float inputX = pos.x;
                if (mirrorTimer > 0f) {
                    inputX = wW - inputX;
                }
                float half = pala.getBounds().width / 2f;
                float x = Math.max(0f, Math.min(inputX - half, wW - pala.getBounds().width));
                pala.setPosition(x, pala.getBounds().y);
            }
        }

        // Temporizador de enlarge
        if (widthTimer > 0f) {
            widthTimer -= delta;
            if (widthTimer <= 0f) {
                pala.getSprite().setSize(defaultWidth, pala.getBounds().height);
            }
        }

        // Temporizador de espejo
        if (mirrorTimer > 0f) {
            mirrorTimer -= delta;
            if (mirrorTimer <= 0f) {
                mirrorTimer = 0f;
            }
        }
        if (colorTimer > 0f) {
            colorTimer -= delta;
            if (colorTimer <= 0f) pala.setColor(defaultColor);
        }
    }

    /** Agranda la pala durante <duration> segundos */
    public void enlargePaddle(float factor, float duration, Color color) {
        pala.getSprite().setSize(defaultWidth * factor, pala.getBounds().height);
        widthTimer  = duration;
        setTemporaryColor(color, duration);
    }


    public void mirrorControls(float duration, Color color) {
        mirrorTimer = duration;
        setTemporaryColor(color, duration);
    }


    private void setTemporaryColor(Color c, float duration) {
        pala.setColor(c);
        colorTimer = duration;
    }

}
