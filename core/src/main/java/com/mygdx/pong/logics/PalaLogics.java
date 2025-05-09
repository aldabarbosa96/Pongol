package com.mygdx.pong.logics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.pong.entities.Pala;

public class PalaLogics {
    private final Pala pala;
    private final boolean isBottom;
    private final Camera camera;

    public PalaLogics(Pala pala, boolean isBottom, Camera camera) {
        this.pala = pala;
        this.isBottom = isBottom;
        this.camera = camera;
    }

    public void update(float delta) {
        // lecturas dinámicas cada frame
        float worldW = camera.viewportWidth;
        float worldH = camera.viewportHeight;

        for (int p = 0; p < 5; p++) {
            if (!Gdx.input.isTouched(p)) continue;
            Vector3 pos = new Vector3(Gdx.input.getX(p), Gdx.input.getY(p), 0);
            camera.unproject(pos);

            boolean correctHalf = isBottom ? pos.y < worldH / 2f : pos.y >= worldH / 2f;

            if (correctHalf) {
                float halfW = pala.getBounds().width / 2f;
                float newX = pos.x - halfW;
                // clamp horizontal
                newX = Math.max(0f, Math.min(newX, worldW - pala.getBounds().width));
                pala.setPosition(newX, pala.getBounds().y);
            }
        }
    }
}
