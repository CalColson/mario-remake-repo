package com.cosmosis.mariobros.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.screens.PlayScreen;
import com.cosmosis.mariobros.sprites.enemies.Enemy;
import com.cosmosis.mariobros.sprites.enemies.Koopa;
import com.cosmosis.mariobros.sprites.ito.Brick;
import com.cosmosis.mariobros.sprites.ito.Coin;
import com.cosmosis.mariobros.sprites.enemies.Goomba;

/**
 * Created by Cal on 2/9/2017.
 */

public class WorldCreator {
    private Array<Koopa> koopas;
    private Array<Goomba> goombas;

    public WorldCreator(PlayScreen screen) {
        World world = screen.getWorld();
        TiledMap map = screen.getMap();
        BodyDef bDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fDef = new FixtureDef();
        Body body;

        //create ground bodies/fixtures
        for (MapObject obj : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();

            bDef.type = BodyDef.BodyType.StaticBody;
            bDef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM,
                    (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);
            body = world.createBody(bDef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM,
                    rect.getHeight() / 2 / MarioBros.PPM);
            fDef.shape = shape;
            body.createFixture(fDef);
        }

        //create pipe bodies/fixtures
        for (MapObject obj : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();

            bDef.type = BodyDef.BodyType.StaticBody;
            bDef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM,
                    (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);
            body = world.createBody(bDef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM,
                    rect.getHeight() / 2 / MarioBros.PPM);
            fDef.shape = shape;
            fDef.filter.categoryBits = MarioBros.OBJECT_BIT;
            body.createFixture(fDef);
        }

        //create coin bodies/fixtures
        for (MapObject obj : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            //Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            new Coin(screen, obj);
        }

        //create brick bodies/fixtures
        for (MapObject obj : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            //Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            new Brick(screen, obj);
        }

        //create all goombas
        goombas = new Array<Goomba>();
        for (MapObject obj : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            goombas.add(new Goomba(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));

        }//create all koopas
        koopas = new Array<Koopa>();
        for (MapObject obj : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            koopas.add(new Koopa(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }

    public Array<Koopa> getKoopas() {
        return koopas;
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(koopas);
        return enemies;
    }
}
