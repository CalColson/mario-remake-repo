package com.cosmosis.mariobros.sprites.items;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.scenes.Hud;
import com.cosmosis.mariobros.screens.PlayScreen;
import com.cosmosis.mariobros.sprites.Mario;
import com.cosmosis.mariobros.sprites.enemies.Enemy;

/**
 * Created by Cal on 2/14/2017.
 */

public class Mushroom extends Item {
    public Mushroom(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        setRegion(screen.getAtlas().findRegion("mushroom"), 0, 0, 16, 16);
        velocity = new Vector2(0.7f, 0);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        if (!destroyed) {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
            velocity.y = body.getLinearVelocity().y;
            body.setLinearVelocity(velocity);
        }
    }

    @Override
    public void defineItem() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(getX(), getY());
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fDef.filter.categoryBits = MarioBros.ITEM_BIT;
        fDef.filter.maskBits = MarioBros.MARIO_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.GROUND_BIT;
        fDef.shape = shape;
        body.createFixture(fDef).setUserData(this);
    }

    @Override
    public void use(Mario mario) {
        Hud.addScore(1000);
        destroy();
        if (!mario.marioIsBig) mario.grow();
        MarioBros.manager.get("audio/sounds/powerup.wav", Sound.class).play();
    }
}
