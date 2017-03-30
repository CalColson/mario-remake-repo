package com.cosmosis.mariobros.sprites.ito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.scenes.Hud;
import com.cosmosis.mariobros.screens.PlayScreen;
import com.cosmosis.mariobros.sprites.items.ItemDef;
import com.cosmosis.mariobros.sprites.items.Mushroom;

/**
 * Created by Cal on 2/9/2017.
 */

public class Coin extends InteractiveTileObject {
    private static TiledMapTileSet set;
    private final int BLANK_COIN = 28;

    public Coin(PlayScreen screen, MapObject obj) {
        super(screen, obj);
        set = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit() {
        Gdx.app.log("Coin", "Collision");
        if (getCell().getTile().getId() == BLANK_COIN) {
            MarioBros.manager.get("audio/sounds/bump.wav", Sound.class).play();
        } else {
            getCell().setTile(set.getTile(BLANK_COIN));
            Hud.addScore(500);
            if (obj.getProperties().containsKey("mushroom")) {
                screen.spawnItem(new ItemDef(
                        new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
                MarioBros.manager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            } else MarioBros.manager.get("audio/sounds/coin.wav", Sound.class).play();

        }
    }
}
