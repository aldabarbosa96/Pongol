package com.mygdx.pong.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

public class VictoryScreen implements Screen {
    private final Game game;
    private final int bottomScore, topScore;
    private final String bottomMessage, topMessage;
    private final String scoreText;

    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private ShapeRenderer shapeRenderer;
    private Rectangle buttonBounds;
    private String buttonText = "MENU";
    private float padding = 20f;

    public VictoryScreen(Game game, int bottomScore, int topScore) {
        this.game = game;
        this.bottomScore = bottomScore;
        this.topScore = topScore;

        // Determinamos los mensajes de cada jugador
        if (bottomScore > topScore) {
            bottomMessage = "¡GANASTE!";
            topMessage = "PERDISTE";
        } else if (topScore > bottomScore) {
            bottomMessage = "PERDISTE";
            topMessage = "¡GANASTE!";
        } else {
            bottomMessage = "EMPATE";
            topMessage = "EMPATE";
        }
        scoreText = bottomScore + "  :  " + topScore;

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Cargamos la fuente
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 64;
        param.color = Color.WHITE;
        font = gen.generateFont(param);
        gen.dispose();

        layout = new GlyphLayout();
        buttonBounds = new Rectangle();

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        // limpia pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // línea divisoria horizontal en el centro
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.line(0, h / 2f, w, h / 2f);
        shapeRenderer.end();

        // ——— DIBUJAR MITAD INFERIOR ———
        batch.begin();
        // Mensaje
        layout.setText(font, bottomMessage);
        float bx = (w - layout.width) / 2f;
        float by = h / 3f + layout.height / 2f;
        font.draw(batch, layout, bx, by);

        // Puntuación
        layout.setText(font, scoreText);
        float sx = (w - layout.width) / 2f;
        float sy = h / 3f - layout.height * 5 / 2f;
        font.draw(batch, layout, sx, sy);

        batch.end();

        // ——— DIBUJAR MITAD SUPERIOR ROTADA ———
        // Guardamos la matriz original
        Matrix4 original = batch.getTransformMatrix().cpy();
        // Creamos rotación de 180° alrededor del centro de pantalla
        Matrix4 rot = new Matrix4();
        rot.translate(w / 2f, h / 2f, 0);
        rot.rotate(0, 0, 1, 180);
        rot.translate(-w / 2f, -h / 2f, 0);
        batch.setTransformMatrix(rot);

        batch.begin();
        // Mensaje
        layout.setText(font, topMessage);
        float tx = (w - layout.width) / 2f;
        float ty = h / 3f + layout.height / 2f;  // coincide con by antes de rotar
        font.draw(batch, layout, tx, ty);

        // Puntuación
        layout.setText(font, scoreText);
        float ux = (w - layout.width) / 2f;
        float uy = h / 3f - layout.height * 5 / 2f;  // coincide con sy antes de rotar
        font.draw(batch, layout, ux, uy);
        batch.end();

        // Restauramos la matriz
        batch.setTransformMatrix(original);

        // ——— BOTÓN MENU ———
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.rect(buttonBounds.x, buttonBounds.y, buttonBounds.width, buttonBounds.height);
        shapeRenderer.end();

        batch.begin();
        layout.setText(font, buttonText);
        font.draw(batch, layout, buttonBounds.x + padding, buttonBounds.y + padding + layout.height);
        batch.end();

        // Detectar toque en el botón
        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            touch.y = Gdx.graphics.getHeight() - touch.y;
            if (buttonBounds.contains(touch.x, touch.y)) {
                game.setScreen(new MenuScreen(game));
                dispose();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        // Recalculamos el botón
        layout.setText(font, buttonText);
        float bw = layout.width + padding * 2;
        float bh = layout.height + padding * 2;
        // Colocamos el botón centrado horizontal y a 10% desde abajo
        buttonBounds.set(width / 2f - bw / 2f, height * 0.10f, bw, bh);
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
