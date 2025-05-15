// core/src/main/java/com/mygdx/pong/logics/PelotaLogics.java
package com.mygdx.pong.logics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.pong.entities.Marcadores;
import com.mygdx.pong.entities.Pala;
import com.mygdx.pong.entities.Pelota;
import com.mygdx.pong.entities.upgrades.UpgradeManager;
import com.mygdx.pong.Pong;

public class PelotaLogics {
    public interface ScoreListener {
        void onPointScored(boolean bottomScored);
    }

    private final Pelota pelota;
    private final Pala bottomPaddle, topPaddle;
    private final Camera camera;
    private final Marcadores marcadores;
    private final ScoreListener scoreListener;
    private UpgradeManager upgradeManager;
    private final Vector2 velocity = new Vector2();
    private boolean extraBall = false;
    private boolean removeMe = false;

    // —— Trail data ——
    private static final int MAX_TRAIL = 12;
    private final Array<Vector2> trailPositions = new Array<>(MAX_TRAIL);

    private static final float BASE_SPEED = 999f, MAX_SPEED = 3500f;
    private static final float HORIZONTAL_SPEED_BOOST = 400f;
    private static final float MIN_BOUNCE = 20f, MAX_BOUNCE = 75f;
    private static final float PADDLE_INFLUENCE = 0.5f;
    private static final float PADDLE_REST = 1f, WALL_REST = 1f;
    private static final float DRAG = 0.0005f;
    private boolean dropping = true;
    private float dropElapsed = 0f;
    private static final float DROP_DUR = 1f;
    private final float startSize, finalSize;
    private float speedFactor = 1f, speedTimer = 0f;
    private float bottomPrevX, topPrevX;
    private final Sound ping;
    private final Sound scored;
    private final Color defaultColor;

    public PelotaLogics(Pelota pelota, Pala bottomPaddle, Pala topPaddle, Camera camera, Marcadores marcadores, ScoreListener listener, UpgradeManager mgr) {
        this.pelota = pelota;
        this.bottomPaddle = bottomPaddle;
        this.topPaddle = topPaddle;
        this.camera = camera;
        this.marcadores = marcadores;
        this.scoreListener = listener;
        this.upgradeManager = mgr;
        this.defaultColor = new Color(pelota.getSprite().getColor());

        Rectangle b = pelota.getBounds();
        finalSize = b.width;
        startSize = finalSize + 60f;

        bottomPrevX = bottomPaddle.getBounds().x;
        topPrevX = topPaddle.getBounds().x;

        ping = Gdx.audio.newSound(Gdx.files.internal("pingpong.wav"));
        scored = Gdx.audio.newSound(Gdx.files.internal("error_sound.mp3"));
        resetDrop();
    }

    public Pelota getPelota() {
        return pelota;
    }

    public boolean isMarkedForRemoval() {
        return removeMe;
    }

    public void setUpgradeManager(UpgradeManager mgr) {
        this.upgradeManager = mgr;
    }

    public float getFieldCenterY() {
        return camera.viewportHeight / 2f;
    }

    public void setExtraBall(boolean b) {
        extraBall = b;
    }

    public void update(float delta) {
        float wW = camera.viewportWidth;
        float wH = camera.viewportHeight;
        Rectangle b = pelota.getBounds();

        if (dropping) {
            dropElapsed += delta;
            float t = Math.min(dropElapsed / DROP_DUR, 1f);
            float sz = startSize + (finalSize - startSize) * t;
            float cx = wW / 2f;
            float ty = wH / 2f - finalSize / 2f;
            pelota.setBounds(cx - sz / 2f, wH + (ty - wH) * t, sz, sz);
            if (t >= 1f) {
                dropping = false;
                velocity.set(MathUtils.randomSign() * BASE_SPEED, MathUtils.randomSign() * BASE_SPEED);
            }
            return;
        }

        float bVel = (bottomPaddle.getBounds().x - bottomPrevX) / delta;
        float tVel = (topPaddle.getBounds().x - topPrevX) / delta;
        bottomPrevX = bottomPaddle.getBounds().x;
        topPrevX = topPaddle.getBounds().x;

        float x = b.x + velocity.x * delta;
        float y = b.y + velocity.y * delta;
        velocity.scl(1f - DRAG * delta);

        if (x <= 0f) {
            x = 0f;
            reflect(WALL_REST, 1, 0);
            if (upgradeManager != null && upgradeManager.isCrazyBounceActive())
                randomizeWallBounce(true);
            ping.play();
        }
        if (x + b.width >= wW) {
            x = wW - b.width;
            reflect(WALL_REST, -1, 0);
            if (upgradeManager != null && upgradeManager.isCrazyBounceActive())
                randomizeWallBounce(false);
            ping.play();
        }

        y = collidePaddle(bottomPaddle.getBounds(), false, x, y, b.width, b.height, bVel);
        y = collidePaddle(topPaddle.getBounds(), true, x, y, b.width, b.height, tVel);

        float curSpeed = velocity.len();
        float effectiveMax = MAX_SPEED * speedFactor;
        float target = MathUtils.clamp(curSpeed * speedFactor, BASE_SPEED, effectiveMax);
        velocity.setLength(target);

        pelota.setBounds(x, y, b.width, b.height);

        if (y + b.height < 0f || y > wH) {
            boolean bottomScored = (y > wH);
            if (bottomScored) {
                marcadores.addPointBottom();
                scored.play();
            } else {
                marcadores.addPointTop();
                scored.play();
            }
            scoreListener.onPointScored(bottomScored);
            if (extraBall) {
                removeMe = true;
            } else {
                resetDrop();
            }
        }

        if (speedTimer > 0f) {
            speedTimer -= delta;
            if (speedTimer <= 0f) speedFactor = 1f;
        }
        if (speedTimer <= 0f && !pelota.getSprite().getColor().equals(defaultColor)) {
            pelota.getSprite().setColor(defaultColor);
        }

        // —— Añadir posición al trail ——
        float cx = b.x + b.width / 2f;
        float cy = b.y + b.height / 2f;
        trailPositions.add(new Vector2(cx, cy));
        if (trailPositions.size > MAX_TRAIL) {
            trailPositions.removeIndex(0);
        }
    }

    public void renderTrail(ShapeRenderer shapeRenderer) {
        if (trailPositions.isEmpty()) return;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < trailPositions.size; i++) {
            float alpha = (float) (i + 1) / trailPositions.size;
            Color c = pelota.getSprite().getColor();
            shapeRenderer.setColor(c.r, c.g, c.b, alpha);
            Vector2 pos = trailPositions.get(i);
            float size = 8f;
            shapeRenderer.rect(pos.x - size / 2f, pos.y - size / 2f, size, size);
        }
        shapeRenderer.end();
    }

    private float collidePaddle(Rectangle pad, boolean isTop, float x, float y, float w, float h, float padVel) {
        boolean overlapX = x < pad.x + pad.width && x + w > pad.x;
        boolean overlapY = y < pad.y + pad.height && y + h > pad.y;
        boolean heading = (!isTop && velocity.y < 0) || (isTop && velocity.y > 0);
        if (!(overlapX && overlapY && heading)) return y;

        if (upgradeManager != null) upgradeManager.onBallHit(!isTop);
        reflect(PADDLE_REST, 0, isTop ? -1 : 1);
        ping.play();

        float rel = MathUtils.clamp(((x + w / 2f) - (pad.x + pad.width / 2f)) / (pad.width / 2f), -1f, 1f);
        float ang = rel * MAX_BOUNCE * MathUtils.degreesToRadians;
        float min = MIN_BOUNCE * MathUtils.degreesToRadians;
        ang = ang > 0 ? MathUtils.clamp(ang, min, MAX_BOUNCE * MathUtils.degreesToRadians) : MathUtils.clamp(ang, -MAX_BOUNCE * MathUtils.degreesToRadians, -min);

        float spd = velocity.len();
        velocity.x = spd * MathUtils.sin(ang) + padVel * PADDLE_INFLUENCE;
        velocity.y = spd * MathUtils.cos(ang) * (isTop ? -1 : 1);
        velocity.setLength(MathUtils.clamp(velocity.len() + Math.abs(rel) * HORIZONTAL_SPEED_BOOST, BASE_SPEED, MAX_SPEED * speedFactor));

        return isTop ? pad.y - h : pad.y + pad.height;
    }

    private void reflect(float rest, float nx, float ny) {
        float dot = velocity.x * nx + velocity.y * ny;
        velocity.x -= 2 * dot * nx;
        velocity.y -= 2 * dot * ny;
        velocity.scl(rest);
    }

    private void randomizeWallBounce(boolean leftWall) {
        float rot = MathUtils.random(-75f, 75f);
        velocity.rotateDeg(rot);
        if (leftWall && velocity.x < 0) velocity.x *= -1f;
        if (!leftWall && velocity.x > 0) velocity.x *= -1f;
    }

    public void changeSpeed(float factor, float duration, Color color) {
        speedFactor = factor;
        speedTimer = duration;
        pelota.getSprite().setColor(color);
    }

    public void spawnExtraBall() {
        float d = finalSize;
        Pelota clone = new Pelota(pelota.getTexture(), pelota.getBounds().x, pelota.getBounds().y, d, d);
        PelotaLogics extra = new PelotaLogics(clone, bottomPaddle, topPaddle, camera, marcadores, scoreListener, upgradeManager);
        extra.dropping = false;
        extra.setExtraBall(true);
        extra.getPelota().setBounds(pelota.getBounds().x, pelota.getBounds().y, d, d);
        extra.velocity.set(this.velocity);
        extra.velocity.rotateDeg(MathUtils.randomSign() * 15f);
        Pong.instance.addBallLogics(extra);
        clone.getSprite().setColor(Color.BLUE);
    }

    public void resetDrop() {
        dropping = true;
        dropElapsed = 0f;
        velocity.setZero();
        speedFactor = 1f;
        speedTimer = 0f;
        float cx = camera.viewportWidth / 2f;
        pelota.setBounds(cx - startSize / 2f, camera.viewportHeight, startSize, startSize);
        trailPositions.clear();
    }

    public void dispose() {
        ping.dispose();
        scored.dispose();
    }
}
