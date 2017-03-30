package com.cosmosis.mariobros.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.sprites.Mario;
import com.cosmosis.mariobros.sprites.enemies.Enemy;
import com.cosmosis.mariobros.sprites.items.Item;
import com.cosmosis.mariobros.sprites.ito.InteractiveTileObject;

/**
 * Created by Cal on 2/10/2017.
 */
public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        //Gdx.app.log("Tag", "Begin Contact");

        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cDef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if ("head".equals(fixA.getUserData()) || "head".equals(fixB.getUserData())) {
            Fixture head = "head".equals(fixA.getUserData()) ? fixA : fixB;
            Fixture object = head == fixA ? fixB : fixA;

            if (object.getUserData() != null &&
                    InteractiveTileObject.class.isAssignableFrom(object.getUserData().getClass())) {
                ((InteractiveTileObject) object.getUserData()).onHeadHit();
            }
        }

        switch (cDef) {
            case MarioBros.ENEMY_HEAD_BIT | MarioBros.MARIO_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT) {
                    ((Enemy) fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                } else {
                    ((Enemy) fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                }
                break;

            case MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.ENEMY_BIT) {
                    ((Enemy) fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Enemy) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    ((Item) fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Item) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            case MarioBros.ITEM_BIT | MarioBros.MARIO_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    if (fixB.getUserData() == "head") break;
                    ((Item) fixA.getUserData()).use((Mario) fixB.getUserData());
                } else {
                    if (fixA.getUserData() == "head") break;
                    ((Item) fixB.getUserData()).use((Mario) fixA.getUserData());
                }
                break;
            case MarioBros.MARIO_BIT | MarioBros.ENEMY_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.MARIO_BIT) {
                    ((Mario) fixA.getUserData()).hit((Enemy) fixB.getUserData());
                }
                else {
                    ((Mario) fixB.getUserData()).hit((Enemy) fixA.getUserData());
                }
                break;
            case MarioBros.ENEMY_BIT | MarioBros.ENEMY_BIT:
                ((Enemy) fixA.getUserData()).onEnemyHit((Enemy) fixB.getUserData());
                ((Enemy) fixB.getUserData()).onEnemyHit((Enemy) fixA.getUserData());
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {
        //Gdx.app.log("Tag", "End Contact");
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
