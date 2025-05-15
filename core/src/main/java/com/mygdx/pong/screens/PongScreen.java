package com.mygdx.pong.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.pong.Pong;
import com.mygdx.pong.screens.MenuScreen;

public class PongScreen implements Screen {
    private final Game game;
    private final Pong pong;
    private boolean paused;

    private Rectangle pauseButtonBR, pauseButtonTL;
    private Rectangle resumeButton, menuButton, exitButton;

    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    // UI camera for screen-space projection
    private Matrix4 uiMatrix;

    public PongScreen(Game game) {
        this.game = game;
        this.pong = new Pong();

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 32;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        layout = new GlyphLayout();

        pauseButtonBR = new Rectangle();
        pauseButtonTL = new Rectangle();
        resumeButton = new Rectangle();
        menuButton = new Rectangle();
        exitButton = new Rectangle();
    }

    @Override
    public void show() {
        pong.create();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        if (!paused) {
            pong.render();
            drawPauseButtons();
            if (Gdx.input.justTouched()) {
                Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                touch.y = Gdx.graphics.getHeight() - touch.y;
                if (pauseButtonBR.contains(touch.x, touch.y) || pauseButtonTL.contains(touch.x, touch.y)) {
                    paused = true;
                }
            }
        } else {
            drawPauseOverlay();
            if (Gdx.input.justTouched()) {
                Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                touch.y = Gdx.graphics.getHeight() - touch.y;
                if (resumeButton.contains(touch.x, touch.y)) {
                    paused = false;
                } else if (menuButton.contains(touch.x, touch.y)) {
                    game.setScreen(new MenuScreen(game));
                    dispose();
                } else if (exitButton.contains(touch.x, touch.y)) {
                    Gdx.app.exit();
                }
            }
        }
    }

    private void drawPauseButtons() {
        // Setup UI projection
        shapeRenderer.setProjectionMatrix(uiMatrix);
        batch.setProjectionMatrix(uiMatrix);

        // Borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(pauseButtonBR.x, pauseButtonBR.y, pauseButtonBR.width, pauseButtonBR.height);
        shapeRenderer.rect(pauseButtonTL.x, pauseButtonTL.y, pauseButtonTL.width, pauseButtonTL.height);
        shapeRenderer.end();

        // Icons
        float iconPadding = 15f;
        batch.begin();
        font.draw(batch, "||", pauseButtonBR.x + iconPadding, pauseButtonBR.y + pauseButtonBR.height - iconPadding);
        font.draw(batch, "||", pauseButtonTL.x + iconPadding, pauseButtonTL.y + pauseButtonTL.height - iconPadding);
        batch.end();
    }

    private void drawPauseOverlay() {
        // Setup UI projection
        shapeRenderer.setProjectionMatrix(uiMatrix);
        batch.setProjectionMatrix(uiMatrix);

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // Semi-transparent background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.75f);
        shapeRenderer.rect(0, 0, w, h);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Button borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(resumeButton.x, resumeButton.y, resumeButton.width, resumeButton.height);
        shapeRenderer.rect(menuButton.x, menuButton.y, menuButton.width, menuButton.height);
        shapeRenderer.rect(exitButton.x, exitButton.y, exitButton.width, exitButton.height);
        shapeRenderer.end();

        // Labels
        float labelPadding = 20f;
        batch.begin();
        layout.setText(font, "Reanudar");
        font.draw(batch, "Reanudar", resumeButton.x + (resumeButton.width - layout.width) / 2f, resumeButton.y + resumeButton.height - labelPadding);
        layout.setText(font, "Menú");
        font.draw(batch, "Menú", menuButton.x + (menuButton.width - layout.width) / 2f, menuButton.y + menuButton.height - labelPadding);
        layout.setText(font, "Salir");
        font.draw(batch, "Salir", exitButton.x + (exitButton.width - layout.width) / 2f, exitButton.y + exitButton.height - labelPadding);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        // Initialize UI matrix
        uiMatrix = new Matrix4().setToOrtho2D(0, 0, width, height);

        // Resize game viewport
        try {
            pong.resize(width, height);
        } catch (Exception ignored) {
        }

        // Larger buttons, offset from corners
        float btnSize = 80f;
        float margin = 20f;
        pauseButtonBR.set(width - btnSize - margin, margin, btnSize, btnSize);
        pauseButtonTL.set(margin, height - btnSize - margin, btnSize, btnSize);

        // Centered pause menu buttons
        float btnW = 250f, btnH = 80f, spacing = 30f;
        float centerX = width / 2f - btnW / 2f;
        float centerY = height / 2f;
        resumeButton.set(centerX, centerY + btnH + spacing, btnW, btnH);
        menuButton.set(centerX, centerY, btnW, btnH);
        exitButton.set(centerX, centerY - btnH - spacing, btnW, btnH);
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
    }
}
