package com.mygdx.pong.entities.upgrades;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.mygdx.pong.logics.PalaLogics;
import com.mygdx.pong.logics.PelotaLogics;
import com.mygdx.pong.Pong;

import java.util.ArrayList;

public class UpgradeManager {
    private final PalaLogics bottomLogic, topLogic;
    private final PelotaLogics pelotaLogics;
    private final ArrayList<Upgrade> upgrades = new ArrayList<>();
    private int hitsCount = 0;
    private final int hitsForSpawn = 5;
    private final float spawnProb = 0.5f;
    private boolean lastBottomTouched = true;

    private final Texture texWide, texFast, texMulti, texEspejo, texRebote;

    /* -------------- rebote loco -------------- */
    private float crazyTimer = 0f;          // >0 mientras esté activo

    public boolean isCrazyBounceActive() {  // consultado desde PelotaLogics
        return crazyTimer > 0f;
    }
    /* ----------------------------------------- */

    public UpgradeManager(PalaLogics bottomLogic, PalaLogics topLogic, PelotaLogics pelotaLogics) {
        this.bottomLogic = bottomLogic;
        this.topLogic = topLogic;
        this.pelotaLogics = pelotaLogics;

        texWide = new Texture("wide.png");
        texFast = new Texture("ballSpeed.png");
        texMulti = new Texture("ballMult.png");
        texEspejo = new Texture("espejo.png");
        texRebote = new Texture("rebote.png");
    }

    public void onBallHit(boolean bottomTouched) {
        lastBottomTouched = bottomTouched;

        hitsCount++;
        if (hitsCount >= hitsForSpawn && MathUtils.random() < spawnProb) {
            spawnRandomUpgrade();
            hitsCount = 0;
        }
    }

    public void update(float delta, Rectangle ballBounds) {
        for (Upgrade up : upgrades) {
            if (!up.active) continue;

            up.update(delta);

            if (up.bounds.overlaps(ballBounds)) {
                applyUpgrade(up);
                up.active = false;
            }
        }

        /* -- temporizador rebote loco -- */
        if (crazyTimer > 0f) {
            crazyTimer -= delta;
            if (crazyTimer < 0f) crazyTimer = 0f;
        }
    }

    public void render(SpriteBatch batch) {
        for (Upgrade up : upgrades) if (up.active) up.render(batch);
    }

    private void spawnRandomUpgrade() {
        Upgrades[] types = Upgrades.values();
        Upgrades type = types[MathUtils.random(types.length - 1)];

        Texture tex;
        switch (type) {
            case PADDLE_WIDE:
                tex = texWide;
                break;
            case BALL_FAST:
                tex = texFast;
                break;
            case MULTI_BALL:
                tex = texMulti;
                break;
            case ESPEJO:
                tex = texEspejo;
                break;
            case REBOTE_LOCO:
                tex = texRebote;
                break;
            default:
                tex = texWide;
                break;
        }

        boolean fromLeft = MathUtils.randomBoolean();
        float x = fromLeft ? -Upgrade.SIZE : 800f + Upgrade.SIZE;
        float y = pelotaLogics.getFieldCenterY();

        upgrades.add(new Upgrade(type, tex, x, y));
    }

    private void applyUpgrade(Upgrade up) {
        switch (up.type) {
            case PADDLE_WIDE:
                (lastBottomTouched ? bottomLogic : topLogic)
                    .enlargePaddle(1.75f, 10f, Color.GREEN);
                break;

            case BALL_FAST:
                for (PelotaLogics pl : Pong.instance.getBalls())
                    pl.changeSpeed(1.15f, 5f, Color.GOLD);
                break;

            case MULTI_BALL:
                pelotaLogics.spawnExtraBall();
                break;

            case ESPEJO:
                PalaLogics objetivo = lastBottomTouched ? topLogic : bottomLogic;
                objetivo.mirrorControls(10f, Color.RED);
                break;

            case REBOTE_LOCO:
                crazyTimer = 10f;
                break;
        }
    }
}
