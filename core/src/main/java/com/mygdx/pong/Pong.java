package com.mygdx.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.pong.entities.Marcadores;
import com.mygdx.pong.entities.Pala;
import com.mygdx.pong.entities.Pelota;
import com.mygdx.pong.logics.PalaLogics;
import com.mygdx.pong.logics.PelotaLogics;

public class Pong extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;
    private static final float FIXED_STEP = 1 / 60f;
    private static final float MARGIN = 50f;
    private static final float MARGINRECT = 12.5f;

    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private Texture paddleTex, ballTex;
    private ShapeRenderer shapeRenderer;

    private Pala bottomPaddle, topPaddle;
    private PalaLogics bottomLogic, topLogic;
    private Pelota ball;
    private PelotaLogics ballLogic;
    private Marcadores marcadores;
    private float accumulator = 0f;

    private static final float FLASH_DURATION = 1f;
    private boolean flashActive = false;
    private float flashTimer = 0f;
    private boolean lastScoreWasBottom = false;

    // tamaños fijos de las palas y la pelota
    private final float paddleW = 200f, paddleH = 15f;
    private final float ballD = 20f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        paddleTex = new Texture("pala.png");
        ballTex = new Texture("pelota.png");

        // entidades
        bottomPaddle = new Pala(paddleTex, 0, 0, paddleW, paddleH);
        topPaddle = new Pala(paddleTex, 0, 0, paddleW, paddleH);
        ball = new Pelota(ballTex, 0, 0, ballD * 2, ballD * 2);

        // marcador
        marcadores = new Marcadores(3f, 50f);

        // lógicas
        bottomLogic = new PalaLogics(bottomPaddle, true, camera);
        topLogic = new PalaLogics(topPaddle, false, camera);
        ballLogic = new PelotaLogics(ball, bottomPaddle, topPaddle, camera, marcadores, this::onPointScored);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        // posicionar palas
        bottomPaddle.setPosition(w / 2f - paddleW / 2f, MARGIN);
        topPaddle.setPosition(w / 2f - paddleW / 2f, h - paddleH - MARGIN);

        // reiniciar caída de la bola
        ballLogic.resetDrop();
    }

    @Override
    public void render() {
        // 0) Limpiar pantalla (fondo negro)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 1) Lógica fija a 60 fps
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;
        while (accumulator >= FIXED_STEP) {
            bottomLogic.update(FIXED_STEP);
            topLogic.update(FIXED_STEP);
            ballLogic.update(FIXED_STEP);
            accumulator -= FIXED_STEP;
        }

        // 2) Dimensiones del mundo
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();
        float innerW = w - 2 * MARGINRECT;
        float innerH = h - 2 * MARGINRECT;
        float halfH = innerH / 2f;

        // 3) Dibujar líneas de campo o efecto flash (solo contornos)
        float lineWidth = flashActive ? 4 : 2;
        Gdx.gl.glLineWidth(lineWidth);
        shapeRenderer.setProjectionMatrix(camera.combined);

        // Actualizar temporizador de flash
        if (flashActive) {
            flashTimer -= delta;
            if (flashTimer <= 0f) {
                flashActive = false;
            }
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        if (flashActive) {
            if (lastScoreWasBottom) {
                // Mitad inferior (anotador) verde
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.rect(MARGINRECT, MARGINRECT, innerW, halfH);
                // Mitad superior (receptor) rojo
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(MARGINRECT, MARGINRECT + halfH, innerW, halfH);
            } else {
                // Mitad inferior (receptor) rojo
                shapeRenderer.setColor(Color.RED);
                shapeRenderer.rect(MARGINRECT, MARGINRECT, innerW, halfH);
                // Mitad superior (anotador) verde
                shapeRenderer.setColor(Color.GREEN);
                shapeRenderer.rect(MARGINRECT, MARGINRECT + halfH, innerW, halfH);
            }
        } else {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.rect(MARGINRECT, MARGINRECT, innerW, innerH);
        }

        float areaWidth = innerW * 0.55f;      // ancho del área (60% del ancho interior)
        float areaHeight = innerH * 0.135f;      // profundidad del área (20% de la altura interior)
        float areaX = MARGINRECT + (innerW - areaWidth) / 2f;

        // Área inferior (apoyada en la línea de fondo)
        float bottomY = MARGINRECT;
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(areaX, bottomY, areaWidth, areaHeight);

        //Área superior (apoyada en la línea de fondo superior)
        float topY = MARGINRECT + innerH - areaHeight;
        shapeRenderer.rect(areaX, topY, areaWidth, areaHeight);
        shapeRenderer.end();

        // 4) Línea central
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(MARGINRECT, h / 2f - 2f, innerW, 4f);
        shapeRenderer.end();

        // 5) Círculo central como anillo (solo contorno)
        float circleR = 75f;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(w / 2f, h / 2f, circleR, 64);
        shapeRenderer.end();

        // 6) Punto central (relleno)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(w / 2f, h / 2f, 7.5f, 16);
        shapeRenderer.end();

        // 7) Dibujar sprites y marcadores
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bottomPaddle.draw(batch);
        topPaddle.draw(batch);
        ball.draw(batch);
        marcadores.draw(batch, w, h);
        batch.end();
    }

    public void onPointScored(boolean bottomScored) {
        flashActive = true;
        flashTimer = FLASH_DURATION;
        lastScoreWasBottom = bottomScored;
    }

    @Override
    public void dispose() {
        batch.dispose();
        paddleTex.dispose();
        ballTex.dispose();
        shapeRenderer.dispose();
        marcadores.dispose();
    }
}
