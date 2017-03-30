package com.cosmosis.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.screens.PlayScreen;
import com.cosmosis.mariobros.sprites.Mario;

/**
 * Created by Cal on 2/15/2017.
 */

public class Koopa extends Enemy {
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;

    public enum State {WALKING, STILL_SHELL, MOVING_SHELL, DEAD}

    public State curState, prevState;
    private float stateTime;
    private TextureRegion shell;
    private Animation<TextureRegion> walkAnimation;
    private Array<TextureRegion> frames;
    private boolean isInShell;
    private boolean isDead;
    private float deadRotationDegree;
    private boolean setToDestroy;
    private boolean destroyed;

    public Koopa(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        TextureRegion koopaRegion = screen.getAtlas().findRegion("turtle");
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(koopaRegion, 0, 0, 16, 24));
        frames.add(new TextureRegion(koopaRegion, 16, 0, 16, 24));
        shell = new TextureRegion(koopaRegion, 64, 0, 16, 24);
        walkAnimation = new Animation<TextureRegion>(0.2f, frames);
        curState = prevState = State.WALKING;
        isInShell = false;
        isDead = false;
        deadRotationDegree = 0;

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
    }

    @Override
    protected void defineEnemy() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(getX(), getY());
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fDef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fDef.filter.maskBits = MarioBros.GROUND_BIT | MarioBros.MARIO_BIT | MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT | MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT;
        fDef.shape = shape;
        body.createFixture(fDef).setUserData(this);

        //Create the head here:
        PolygonShape head = new PolygonShape();
        Vector2[] vertices = new Vector2[4];
        vertices[0] = new Vector2(-5, 8).scl(1 / MarioBros.PPM);
        vertices[1] = new Vector2(5, 8).scl(1 / MarioBros.PPM);
        vertices[2] = new Vector2(-3, 3).scl(1 / MarioBros.PPM);
        vertices[3] = new Vector2(3, 3).scl(1 / MarioBros.PPM);
        head.set(vertices);

        fDef.shape = head;
        fDef.restitution = 1.5f;
        fDef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        body.createFixture(fDef).setUserData(this);
    }

    @Override
    public void update(float dt) {
        if (curState == State.STILL_SHELL && stateTime > 5) {
            isInShell = false;
            velocity.x = 1;
        }
        setRegion(getFrame(dt));
        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - 8 / MarioBros.PPM);

        if (curState == State.DEAD) {
            deadRotationDegree += 3;
            rotate(deadRotationDegree);
            if (stateTime > 5 && !destroyed) {
                world.destroyBody(body);
                destroyed = true;
            }
        } else body.setLinearVelocity(velocity);
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;

        prevState = curState;
        curState = getState();
        stateTime = curState == prevState ? stateTime + dt : 0;

        switch (curState) {
            case STILL_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if (velocity.x > 0 && !region.isFlipX()) region.flip(true, false);
        else if (velocity.x < 0 && region.isFlipX()) region.flip(true, false);

        return region;
    }

    private State getState() {
        if (isDead) return State.DEAD;
        if (isInShell) {
            if (velocity.x == 0) return State.STILL_SHELL;
            else return State.MOVING_SHELL;

        }
        return State.WALKING;
    }

    @Override
    public void draw(Batch batch) {
        if (!destroyed) super.draw(batch);
    }

    @Override
    public void hitOnHead(Mario mario) {
        if (curState != State.STILL_SHELL) {
            isInShell = true;
            velocity.x = 0;
        } else {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if (curState == State.MOVING_SHELL) {
            if (enemy instanceof Koopa && ((Koopa) enemy).getCurState() == State.MOVING_SHELL) {
                reverseVelocity(true, false);
            }
        }
        else {
            if (enemy instanceof Koopa && ((Koopa) enemy).getCurState() == State.MOVING_SHELL) {
                kill();
            }
            else reverseVelocity(true, false);
        }
    }

    public void kick(int speed) {
        velocity.x = speed;
    }

    public void kill() {
        isDead = true;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;

        for (Fixture fix : body.getFixtureList()) fix.setFilterData(filter);

        body.applyLinearImpulse(new Vector2(0, 5f), body.getWorldCenter(), true);
    }

    public State getCurState() {
        return curState;
    }
}
