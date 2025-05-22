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
    private Rectangle exitButtonBounds;
    private ShapeRenderer shapeRenderer;
    private String playButtonText = "JUGAR";
    private String exitButtonText = "SALIR";
    private float padding = 20f;
    private float spacing = 40f;

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
        exitButtonBounds = new Rectangle();
        shapeRenderer = new ShapeRenderer();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        // 1) Limpiamos pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // 2) Dibujamos botón JUGAR
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        shapeRenderer.end();

        batch.begin();
        layout.setText(font, playButtonText);
        float playTextX = playButtonBounds.x + (playButtonBounds.width - layout.width) / 2f;
        float playTextY = playButtonBounds.y + (playButtonBounds.height + layout.height) / 2f;
        font.draw(batch, layout, playTextX, playTextY);
        batch.end();

        // 3) Dibujamos botón SALIR debajo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(exitButtonBounds.x, exitButtonBounds.y, exitButtonBounds.width, exitButtonBounds.height);
        shapeRenderer.end();

        batch.begin();
        layout.setText(font, exitButtonText);
        // Centrar texto en el botón SALIR
        float exitTextX = exitButtonBounds.x + (exitButtonBounds.width - layout.width) / 2f;
        float exitTextY = exitButtonBounds.y + (exitButtonBounds.height + layout.height) / 2f;
        font.draw(batch, layout, exitTextX, exitTextY);
        batch.end();

        // 4)input táctil
        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touch.y = Gdx.graphics.getHeight() - touch.y;

            if (playButtonBounds.contains(touch.x, touch.y)) {
                // Iniciar juego
                game.setScreen(new PongScreen(game));
                dispose();
            } else if (exitButtonBounds.contains(touch.x, touch.y)) {
                // Salir de la app
                Gdx.app.exit();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Calculamos tamaño y posición de botones centrados
        layout.setText(font, playButtonText);
        float btnW = layout.width + padding * 2;
        float btnH = layout.height + padding * 2;
        float centerX = width / 2f - btnW / 2f;
        float playY = height / 2f - btnH / 2f;

        playButtonBounds.set(centerX, playY, btnW, btnH);
        // Posicionamos SALIR debajo de JUGAR con spacing adicional
        float exitY = playY - btnH - spacing;
        exitButtonBounds.set(centerX, exitY, btnW, btnH);
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
