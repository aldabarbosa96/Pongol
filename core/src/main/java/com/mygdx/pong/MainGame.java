package com.mygdx.pong;

import com.badlogic.gdx.Game;
import com.mygdx.pong.screens.MenuScreen;

public class MainGame extends Game {
    @Override
    public void create() {
        setScreen(new MenuScreen(this));
    }
}
