package com.cosmosis.mariobros.sprites.ito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.scenes.Hud;
import com.cosmosis.mariobros.screens.PlayScreen;

/**
 * Created by Cal on 2/9/2017.
 */

public class Brick extends InteractiveTileObject {
    public Brick(PlayScreen screen, MapObject obj) {
        super(screen, obj);
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.BRICK_BIT);
    }

    @Override
    public void onHeadHit() {
        Gdx.app.log("Brick", "Collision");
        setCategoryFilter(MarioBros.DESTROYED_BIT);
        getCell().setTile(null);
        Hud.addScore(200);
        MarioBros.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
    }
}
