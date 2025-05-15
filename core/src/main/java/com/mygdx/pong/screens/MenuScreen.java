package com.mygdx.pong.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MenuScreen implements Screen {
    private final Game game;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private Rectangle playButtonBounds;
    private ShapeRenderer shapeRenderer;
    private String buttonText = "JUGAR";
    private float padding = 20f;

    public MenuScreen(Game game) {
        this.game = game;

        batch = new SpriteBatch();

        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 100;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        layout = new GlyphLayout();
        playButtonBounds = new Rectangle();
        shapeRenderer = new ShapeRenderer();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        // 1) limpia pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) dibujamos contorno del botón
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        shapeRenderer.end();

        // 3) dibuja el texto centrado dentro del botón
        batch.begin();
        font.draw(batch, layout, playButtonBounds.x + padding, playButtonBounds.y + padding + layout.height);
        batch.end();

        // 4) manejo de inputTouch
        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touch.y = Gdx.graphics.getHeight() - touch.y;
            if (playButtonBounds.contains(touch.x, touch.y)) {
                game.setScreen(new PongScreen(game));
                dispose();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        layout.setText(font, buttonText);
        float btnW = layout.width + padding * 2;
        float btnH = layout.height + padding * 2;
        playButtonBounds.set(width / 2f - btnW / 2f, height / 2f - btnH / 2f, btnW, btnH);
    }

    @Override
    public void show() {
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
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
