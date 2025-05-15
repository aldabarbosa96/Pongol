package com.mygdx.pong;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mygdx.pong.entities.Pala;
import com.mygdx.pong.entities.Marcadores;
import com.mygdx.pong.entities.Pelota;
import com.mygdx.pong.entities.upgrades.UpgradeManager;
import com.mygdx.pong.logics.PalaLogics;
import com.mygdx.pong.logics.PelotaLogics;

public class Pong extends ApplicationAdapter {
    public static Pong instance;
    private final Array<PelotaLogics> balls = new Array<>();
    private static final float WORLD_WIDTH = 800f;
    private static final float WORLD_HEIGHT = 480f;
    private static final float FIXED_STEP = 1f / 60f;
    private static final float MARGIN = 100f;
    private static final float MARGIN_RECT = 12.5f;
    private static final float PADDLE_W = 200f;
    private static final float PADDLE_H = 15f;
    private static final float BALL_D = 20f;
    private static final float FLASH_DUR = 1f;
    private OrthographicCamera camera;
    private Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture paddleTex, ballTex;
    private Music music;
    private Pala bottomPaddle, topPaddle;
    private PalaLogics bottomLogic, topLogic;
    private Marcadores marcadores;
    private UpgradeManager upgradeManager;
    private boolean gamePaused = false;

    private boolean countdown = true;
    private int countdownNumber = 3;
    private float countdownTimer = 1f;
    private BitmapFont countdownFont;
    private GlyphLayout countdownLayout;

    // Game state
    private float accumulator = 0f;
    private boolean flash = false;
    private boolean lastBottomPoint = false;
    private float flashTimer = 0f;
    private float crazyHue = 0f;

    @Override
    public void create() {
        instance = this;

        // Música
        music = Gdx.audio.newMusic(Gdx.files.internal("music1.mp3"));
        music.setLooping(true);
        music.setVolume(0.5f);
        music.play();

        // Cámara y viewport
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Texturas
        paddleTex = new Texture("pala.png");
        ballTex = new Texture("pelota.png");

        // Entidades base
        bottomPaddle = new Pala(paddleTex, 0, 0, PADDLE_W, PADDLE_H);
        topPaddle = new Pala(paddleTex, 0, 0, PADDLE_W, PADDLE_H);
        Pelota ball = new Pelota(ballTex, 0, 0, BALL_D * 2, BALL_D * 2);

        // Marcadores y lógica de palas
        marcadores = new Marcadores(3f, 50f);
        bottomLogic = new PalaLogics(bottomPaddle, true, camera);
        topLogic = new PalaLogics(topPaddle, false, camera);

        PelotaLogics ballLogic = new PelotaLogics(ball, bottomPaddle, topPaddle, camera, marcadores, this::onPointScored, null);
        balls.add(ballLogic);
        upgradeManager = new UpgradeManager(bottomLogic, topLogic, ballLogic);
        ballLogic.setUpgradeManager(upgradeManager);

        // Fuente para la cuenta atrás
        FreeTypeFontGenerator gen = new FreeTypeFontGenerator(Gdx.files.internal("font1.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 120;
        param.color = Color.WHITE;
        countdownFont = gen.generateFont(param);
        gen.dispose();
        countdownLayout = new GlyphLayout();

        // Preparamos posición inicial
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();

        bottomPaddle.setPosition(W / 2f - PADDLE_W / 2f, MARGIN);
        topPaddle.setPosition(W / 2f - PADDLE_W / 2f, H - PADDLE_H - MARGIN);

        for (PelotaLogics pl : balls) pl.resetDrop();
    }

    @Override
    public void render() {
        float delta = gamePaused ? 0f : Gdx.graphics.getDeltaTime();

        // Cuenta atrás inicial
        if (countdown) {
            countdownTimer -= delta;
            if (countdownTimer <= 0f) {
                countdownNumber--;
                countdownTimer = 1f;
                if (countdownNumber <= 0) {
                    countdown = false;
                    for (PelotaLogics pl : balls) pl.resetDrop();
                }
            }

            // Dibujar número en cada mitad
            Gdx.gl.glClearColor(0, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            float W = viewport.getWorldWidth();
            float H = viewport.getWorldHeight();
            String txt = Integer.toString(Math.max(1, countdownNumber));
            countdownLayout.setText(countdownFont, txt);
            float tw = countdownLayout.width;
            float th = countdownLayout.height;

            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            // Mitad inferior
            countdownFont.draw(batch, txt, W / 2f - tw / 2f, H / 4f + th / 2f);
            // Mitad superior (invertida)
            countdownFont.getData().setScale(1f, -1f);
            countdownFont.draw(batch, txt, W / 2f - tw / 2f, H * 3 / 4f - th / 2f);
            countdownFont.getData().setScale(1f, 1f);
            batch.end();
            return;
        }

        // Actualización con paso fijo
        accumulator += delta;
        while (accumulator >= FIXED_STEP) {
            bottomLogic.update(FIXED_STEP);
            topLogic.update(FIXED_STEP);
            for (int i = balls.size - 1; i >= 0; i--) {
                PelotaLogics pl = balls.get(i);
                pl.update(FIXED_STEP);
                upgradeManager.update(FIXED_STEP, pl.getPelota().getBounds());
                if (pl.isMarkedForRemoval()) {
                    pl.dispose();
                    balls.removeIndex(i);
                }
            }
            accumulator -= FIXED_STEP;
        }

        // Dibujado de escena completa
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float W = viewport.getWorldWidth();
        float H = viewport.getWorldHeight();
        float innerW = W - 2 * MARGIN_RECT;
        float innerH = H - 2 * MARGIN_RECT;

        // Marco y color loco
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        float lineWidth = flash ? 4f : 2f;
        Gdx.gl.glLineWidth(lineWidth);
        shapeRenderer.setProjectionMatrix(camera.combined);
        Color marcoColor = Color.WHITE;
        if (upgradeManager.isCrazyBounceActive()) {
            crazyHue = (crazyHue + delta * 60f) % 360f;
            marcoColor = new Color(Color.WHITE);
            marcoColor.fromHsv(crazyHue, 1f, 1f);
        }
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(marcoColor);
        shapeRenderer.rect(MARGIN_RECT, MARGIN_RECT, innerW, innerH);
        shapeRenderer.end();

        // Líneas interiores y círculo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        float areaW = innerW * 0.55f;
        float areaH = innerH * 0.175f;
        float areaX = MARGIN_RECT + (innerW - areaW) / 2f;
        shapeRenderer.rect(areaX, MARGIN_RECT, areaW, areaH);
        shapeRenderer.rect(areaX, MARGIN_RECT + innerH - areaH, areaW, areaH);
        shapeRenderer.circle(W / 2f, H / 2f, 75f, 64);
        shapeRenderer.end();

        // Flash al anotar
        if (flash) {
            flashTimer -= delta;
            if (flashTimer <= 0f) flash = false;
            else {
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (lastBottomPoint) {
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.rect(areaX, MARGIN_RECT, areaW, areaH);
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(areaX, MARGIN_RECT + innerH - areaH, areaW, areaH);
                } else {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.rect(areaX, MARGIN_RECT, areaW, areaH);
                    shapeRenderer.setColor(Color.GREEN);
                    shapeRenderer.rect(areaX, MARGIN_RECT + innerH - areaH, areaW, areaH);
                }
                shapeRenderer.end();
            }
        }

        // Línea y punto centrales
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect(MARGIN_RECT, H / 2f - 2f, innerW, 4f);
        shapeRenderer.circle(W / 2f, H / 2f, 7.5f, 16);
        shapeRenderer.end();

        shapeRenderer.setProjectionMatrix(camera.combined);
        for (PelotaLogics pl : balls) {
            pl.renderTrail(shapeRenderer);
        }

        // Sprites, upgrades y marcadores
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        bottomPaddle.draw(batch);
        topPaddle.draw(batch);
        for (PelotaLogics pl : balls) pl.getPelota().draw(batch);
        upgradeManager.render(batch);
        marcadores.draw(batch, W, H);
        batch.end();
    }

    public void onPointScored(boolean bottomScored) {
        flash = true;
        lastBottomPoint = bottomScored;
        flashTimer = FLASH_DUR;
    }

    public void addBallLogics(PelotaLogics logic) {
        logic.setUpgradeManager(upgradeManager);
        balls.add(logic);
    }

    public Array<PelotaLogics> getBalls() {
        return balls;
    }

    public Marcadores getMarcadores() {
        return marcadores;
    }

    public void setPaused(boolean paused) {
        this.gamePaused = paused;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        paddleTex.dispose();
        ballTex.dispose();
        music.dispose();
        countdownFont.dispose();
        marcadores.dispose();
        for (PelotaLogics pl : balls) pl.dispose();
    }
}
