// PelotaLogics.java
package com.mygdx.pong.logics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.pong.entities.Marcadores;
import com.mygdx.pong.entities.Pala;
import com.mygdx.pong.entities.Pelota;

public class PelotaLogics {
    private final Pelota pelota;
    private final Pala bottomPaddle, topPaddle;
    private final Camera camera;
    private final Marcadores marcadores;
    private final ScoreListener scoreListener;

    private boolean dropping = true;
    private float dropElapsed = 0f;
    private static final float DROP_DURATION = 1f;

    private final float finalSize;
    private final float startSize;

    private final Vector2 velocity = new Vector2();
    private static final float BASE_SPEED = 999f;
    private static final float MAX_SPEED = 2000f;
    private static final float HORIZONTAL_SPEED_BOOST = 400f;
    private static final float MIN_BOUNCE_ANGLE_DEG = 20f;
    private static final float MAX_BOUNCE_ANGLE_DEG = 75f;
    private static final float PADDLE_VEL_INFLUENCE = 0.5f;
    private static final float PADDLE_RESTITUTION = 1f;
    private static final float WALL_RESTITUTION = 1f;
    private static final float DRAG = 0.0005f;

    private float bottomPrevX, topPrevX;

    public PelotaLogics(Pelota pelota, Pala bottomPaddle, Pala topPaddle, Camera camera, Marcadores marcadores, ScoreListener listener) {
        this.pelota = pelota;
        this.bottomPaddle = bottomPaddle;
        this.topPaddle = topPaddle;
        this.camera = camera;
        this.marcadores = marcadores;
        this.scoreListener = listener;

        Rectangle b = pelota.getBounds();
        finalSize = b.width;
        startSize = finalSize + 60f;

        bottomPrevX = bottomPaddle.getBounds().x;
        topPrevX    = topPaddle.getBounds().x;
        resetDrop();
    }

    public void update(float delta) {
        float worldW = camera.viewportWidth;
        float worldH = camera.viewportHeight;

        Rectangle b = pelota.getBounds();
        float w = b.width, h = b.height;
        float x = b.x, y = b.y;

        if (dropping) {
            dropElapsed += delta;
            float t = Math.min(dropElapsed / DROP_DURATION, 1f);
            float newSize = startSize + (finalSize - startSize) * t;
            float centerX = worldW / 2f;
            float targetY = worldH / 2f - finalSize / 2f;
            float newX = centerX - newSize / 2f;
            float newY = worldH + (targetY - worldH) * t;
            pelota.setBounds(newX, newY, newSize, newSize);

            if (t >= 1f) {
                dropping = false;
                boolean left = MathUtils.randomBoolean();
                boolean down = MathUtils.randomBoolean();
                velocity.set((left ? -1f : 1f) * BASE_SPEED, (down ? -1f : 1f) * BASE_SPEED);
            }
            return;
        }

        float bottomVel = (bottomPaddle.getBounds().x - bottomPrevX) / delta;
        float topVel    = (topPaddle.getBounds().x - topPrevX) / delta;
        bottomPrevX = bottomPaddle.getBounds().x;
        topPrevX    = topPaddle.getBounds().x;

        x += velocity.x * delta;
        y += velocity.y * delta;
        velocity.scl(1f - DRAG * delta);

        if (x <= 0f) {
            x = 0f; reflect(WALL_RESTITUTION, 1, 0);
        } else if (x + w >= worldW) {
            x = worldW - w; reflect(WALL_RESTITUTION, -1, 0);
        }

        y = handlePaddleCollision(bottomPaddle.getBounds(), false, x, y, w, h, bottomVel);
        y = handlePaddleCollision(topPaddle.getBounds(),    true,  x, y, w, h, topVel);

        float currentSpeed = velocity.len();
        velocity.setLength(MathUtils.clamp(currentSpeed, BASE_SPEED, MAX_SPEED));
        pelota.setBounds(x, y, w, h);

        // si sale del campo, cuenta el punto y resetea
        if (y + h < 0f) {
            // salió por abajo → punto arriba
            marcadores.addPointTop();
            if (scoreListener != null) scoreListener.onPointScored(false);
            resetDrop();
        } else if (y > worldH) {
            // salió por arriba → punto abajo
            marcadores.addPointBottom();
            if (scoreListener != null) scoreListener.onPointScored(true);
            resetDrop();
        }
    }

    private void reflect(float restitution, float nx, float ny) {
        float dot = velocity.x * nx + velocity.y * ny;
        velocity.x = velocity.x - 2 * dot * nx;
        velocity.y = velocity.y - 2 * dot * ny;
        velocity.scl(restitution);
    }

    private float handlePaddleCollision(Rectangle paddle, boolean isTop,
                                        float x, float y, float w, float h,
                                        float paddleVel) {
        boolean verticalHit = (!isTop && velocity.y < 0 && y <= paddle.y + paddle.height)
            || (isTop  && velocity.y > 0 && y + h >= paddle.y);
        if (!verticalHit || x + w < paddle.x || x > paddle.x + paddle.width) {
            return y;
        }

        reflect(PADDLE_RESTITUTION, 0, isTop ? -1 : 1);

        float rel = MathUtils.clamp(
            ((x + w/2f) - (paddle.x + paddle.width/2f)) / (paddle.width/2f),
            -1f, 1f
        );
        float angleRad = rel * MAX_BOUNCE_ANGLE_DEG * MathUtils.degreesToRadians;
        float minRad   = MIN_BOUNCE_ANGLE_DEG * MathUtils.degreesToRadians;
        angleRad = angleRad > 0
            ? MathUtils.clamp(angleRad, minRad, MAX_BOUNCE_ANGLE_DEG * MathUtils.degreesToRadians)
            : MathUtils.clamp(angleRad, -MAX_BOUNCE_ANGLE_DEG * MathUtils.degreesToRadians, -minRad);

        float speed = MathUtils.clamp(velocity.len(), BASE_SPEED, MAX_SPEED);
        velocity.x = speed * MathUtils.sin(angleRad) + paddleVel * PADDLE_VEL_INFLUENCE;
        velocity.y = speed * MathUtils.cos(angleRad) * (isTop ? -1 : 1);

        float bonus = Math.abs(rel) * HORIZONTAL_SPEED_BOOST;
        velocity.setLength(MathUtils.clamp(velocity.len() + bonus, BASE_SPEED, MAX_SPEED));

        return isTop ? paddle.y - h : paddle.y + paddle.height;
    }

    public void resetDrop() {
        dropping = true;
        dropElapsed = 0f;
        velocity.setZero();
        float worldW = camera.viewportWidth;
        float centerX = worldW / 2f;
        pelota.setBounds(centerX - startSize/2f, camera.viewportHeight, startSize, startSize);
    }
    public interface ScoreListener {
        /**
         * @param bottomScored true si anota la pala inferior, false si anota la superior
         */
        void onPointScored(boolean bottomScored);
    }

}
