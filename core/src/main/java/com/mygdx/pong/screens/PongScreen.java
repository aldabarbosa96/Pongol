package com.mygdx.pong.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.mygdx.pong.Pong;
import com.mygdx.pong.screens.VictoryScreen;

public class PongScreen implements Screen {
    private final Game game;
    private final Pong pong;

    public PongScreen(Game game) {
        this.game = game;
        this.pong = new Pong();
    }

    @Override
    public void show() {
        pong.create();
    }

    @Override
    public void render(float delta) {
        pong.render();

        int bs = pong.getMarcadores().getBottomScore();
        int ts = pong.getMarcadores().getTopScore();
        if ((bs >= 11 || ts >= 11) && Math.abs(bs - ts) >= 2) {
            // al cumplirse la condición de victoria, pasamos ambas puntuaciones
            game.setScreen(new VictoryScreen(game, bs, ts));
            dispose();
            return;
        }
    }

    @Override
    public void resize(int width, int height) {
        pong.resize(width, height);
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
    }
}
