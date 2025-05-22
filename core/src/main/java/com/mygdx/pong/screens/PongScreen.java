package com.mygdx.pong.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.mygdx.pong.Pong;
import com.mygdx.pong.entities.Marcadores;
import com.mygdx.pong.screens.MenuScreen;
import com.mygdx.pong.screens.VictoryScreen;

public class PongScreen implements Screen {
    private final Game game;
    private final Pong pong;
    private boolean paused;
    private boolean pauseFromTop; 

    private final Rectangle pauseButtonBR = new Rectangle();
    private final Rectangle pauseButtonTL = new Rectangle();
    private final Rectangle resumeButton = new Rectangle();
    private final Rectangle menuButton = new Rectangle();
    private final Rectangle exitButton = new Rectangle();

    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;
    private final BitmapFont iconFont;
    private final GlyphLayout layout = new GlyphLayout();
    private Matrix4 uiMatrix;

    public PongScreen(Game game) {
        this.game = game;
        this.pong = new Pong();

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = 32;
        p.color = Color.WHITE;
        font = gen.generateFont(p);

        FreeTypeFontGenerator.FreeTypeFontParameter ip = new FreeTypeFontGenerator.FreeTypeFontParameter();
        ip.size = 48;
        ip.color = Color.WHITE;
        iconFont = gen.generateFont(ip);

        gen.dispose();
    }

    @Override
    public void show() {
        pong.create();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        // 1) Compruebo victoria (11 con diff ≥2)
        Marcadores m = pong.getMarcadores();
        int b = m.getBottomScore(), t = m.getTopScore();
        if ((b >= 11 || t >= 11) && Math.abs(b - t) >= 2) {
            game.setScreen(new VictoryScreen(game, b, t));
            dispose();
            return;
        }

        // 2) Pausa vs normal
        if (!paused) {
            pong.setPaused(false);
            pong.render();
            drawPauseButtons();

            if (Gdx.input.justTouched()) {
                Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                touch.y = Gdx.graphics.getHeight() - touch.y;
                if (pauseButtonBR.contains(touch.x, touch.y)) {
                    pauseFromTop = false;
                    paused = true;
                } else if (pauseButtonTL.contains(touch.x, touch.y)) {
                    pauseFromTop = true;
                    paused = true;
                }
            }
        } else {
            // muestro escena congelada
            pong.setPaused(true);
            pong.render();
            drawPauseOverlay();

            if (Gdx.input.justTouched()) {
                // capturo y transformo según orientación
                Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                touch.y = Gdx.graphics.getHeight() - touch.y;
                float tx = touch.x, ty = touch.y;
                if (pauseFromTop) {
                    // al estar girado 180°, invierto punto
                    tx = Gdx.graphics.getWidth() - tx;
                    ty = Gdx.graphics.getHeight() - ty;
                }
                if (resumeButton.contains(tx, ty)) {
                    paused = false;
                } else if (menuButton.contains(tx, ty)) {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                } else if (exitButton.contains(tx, ty)) {
                    Gdx.app.exit();
                }
            }
        }
    }

    private void drawPauseButtons() {
        shapeRenderer.setProjectionMatrix(uiMatrix);
        batch.setProjectionMatrix(uiMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(pauseButtonBR.x, pauseButtonBR.y, pauseButtonBR.width, pauseButtonBR.height);
        shapeRenderer.rect(pauseButtonTL.x, pauseButtonTL.y, pauseButtonTL.width, pauseButtonTL.height);
        shapeRenderer.end();

        String icon = "||";
        batch.begin();
        layout.setText(iconFont, icon);

        float x = pauseButtonBR.x + (pauseButtonBR.width - layout.width) / 2f;
        float y = pauseButtonBR.y + (pauseButtonBR.height + layout.height) / 2f;
        iconFont.draw(batch, icon, x, y);

        x = pauseButtonTL.x + (pauseButtonTL.width - layout.width) / 2f;
        y = pauseButtonTL.y + (pauseButtonTL.height + layout.height) / 2f;
        iconFont.draw(batch, icon, x, y);
        batch.end();
    }

    private void drawPauseOverlay() {
        int w = Gdx.graphics.getWidth(), h = Gdx.graphics.getHeight();

        // UI matrix girada si pausó el jugadord e arriba
        Matrix4 mtx = new Matrix4(uiMatrix);
        if (pauseFromTop) {
            mtx.translate(w / 2f, h / 2f, 0);
            mtx.rotate(0, 0, 1, 180);
            mtx.translate(-w / 2f, -h / 2f, 0);
        }
        shapeRenderer.setProjectionMatrix(mtx);
        batch.setProjectionMatrix(mtx);

        // fondo semitransp.
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // botones overlay
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(resumeButton.x, resumeButton.y, resumeButton.width, resumeButton.height);
        shapeRenderer.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        shapeRenderer.end();

        // etiquetas
        float pad = 20f;
        batch.begin();
        layout.setText(font, "Reanudar");
        font.draw(batch, "Reanudar", resumeButton.x + (resumeButton.width - layout.width) / 2f, resumeButton.y + resumeButton.height - pad);

        layout.setText(font, "Menú");
        font.draw(batch, "Menú", menuButton.x + (menuButton.width - layout.width) / 2f, menuButton.y + menuButton.height - pad);

        layout.setText(font, "Salir");
        font.draw(batch, "Salir", exitButton.x + (exitButton.width - layout.width) / 2f, exitButton.y + exitButton.height - pad);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        uiMatrix = new Matrix4().setToOrtho2D(0, 0, width, height);
        try {
            pong.resize(width, height);
        } catch (Exception ignored) {
        }

        float sz = 80f, mg = 20f;
        pauseButtonBR.set(width - sz - mg, mg, sz, sz);
        pauseButtonTL.set(mg, height - sz - mg, sz, sz);

        float bw = 250f, bh = 80f, sp = 30f;
        float cx = width / 2f - bw / 2f, cy = height / 2f;
        resumeButton.set(cx, cy + bh + sp, bw, bh);
        menuButton.set(cx, cy, bw, bh);
        exitButton.set(cx, cy - bh - sp, bw, bh);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        pong.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        iconFont.dispose();
    }
}
