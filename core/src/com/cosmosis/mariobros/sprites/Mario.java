package com.cosmosis.mariobros.sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.screens.PlayScreen;
import com.cosmosis.mariobros.sprites.enemies.Enemy;
import com.cosmosis.mariobros.sprites.enemies.Koopa;

/**
 * Created by Cal on 2/9/2017.
 */

public class Mario extends Sprite {
    public enum State {FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD}

    public State curState, prevState;
    private float stateTimer;

    public World world;
    public Body body;
    private TextureRegion marioDead;
    private TextureRegion marioStand;
    private Animation<TextureRegion> marioRun, marioJump;
    private TextureRegion bigMarioStand;
    private Animation<TextureRegion> bigMarioRun, bigMarioJump, growMario;
    private boolean runningRight;
    public boolean marioIsBig;
    private boolean runGrowAnimation;
    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;
    private boolean marioIsDead;

    public Mario(PlayScreen screen) {
        //super(screen.getAtlas().findRegion("little_mario"));
        TextureRegion littleMario = screen.getAtlas().findRegion("little_mario");
        TextureRegion bigMario = screen.getAtlas().findRegion("big_mario");

        world = screen.getWorld();
        defineMario();
        curState = State.STANDING;
        prevState = State.STANDING;
        stateTimer = 0;
        runningRight = true;
        marioIsDead = false;

        Array<TextureRegion> frames = new Array<TextureRegion>();
        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(littleMario, i * 16, 0, 16, 16));
        }
        marioRun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(bigMario, i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(littleMario, i * 16, 0, 16, 16));
        }
        marioJump = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        for (int i = 4; i < 6; i++) {
            frames.add(new TextureRegion(bigMario, i * 16, 0, 16, 32));
        }
        bigMarioJump = new Animation<TextureRegion>(0.1f, frames);
        frames.clear();

        frames.add(new TextureRegion(bigMario, 240, 0, 16, 32));
        frames.add(new TextureRegion(bigMario, 0, 0, 16, 32));
        frames.add(new TextureRegion(bigMario, 240, 0, 16, 32));
        frames.add(new TextureRegion(bigMario, 0, 0, 16, 32));
        growMario = new Animation<TextureRegion>(0.2f, frames);

        marioStand = new TextureRegion(littleMario, 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(bigMario, 0, 0, 16, 32);
        marioDead = new TextureRegion(littleMario, 96, 0, 16, 16);

        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);
    }

    public void update(float dt) {
        if (marioIsBig) {
            setPosition(body.getPosition().x - getWidth() / 2,
                    body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        } else {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        }
        setRegion(getFrame(dt));
        if (timeToDefineBigMario) defineBigMario();
        if (timeToRedefineMario) redefineMario();
    }

    private TextureRegion getFrame(float dt) {
        prevState = curState;
        curState = getState();
        stateTimer = curState == prevState ? stateTimer + dt : 0;

        TextureRegion region;
        switch (curState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if (growMario.isAnimationFinished(stateTimer)) runGrowAnimation = false;
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump.getKeyFrame(stateTimer) : marioJump.getKeyFrame(stateTimer);
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }
        if ((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if ((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        return region;
    }

    private State getState() {
        if (marioIsDead) return State.DEAD;
        else if (runGrowAnimation) return State.GROWING;
        else if (body.getLinearVelocity().y > 0 ||
                (body.getLinearVelocity().y < 0 && prevState == State.JUMPING))
            return State.JUMPING;
        else if (body.getLinearVelocity().y < 0) return State.FALLING;
        else if (body.getLinearVelocity().x != 0) return State.RUNNING;
        else return State.STANDING;
    }

    public void defineMario() {
        BodyDef bDef = new BodyDef();
        bDef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fDef.shape = shape;
        body.createFixture(fDef).setUserData(this);

        EdgeShape feet = new EdgeShape();
        feet.set(new Vector2(-2 / MarioBros.PPM, -6 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, -6 / MarioBros.PPM));
        fDef.shape = feet;
        fDef.isSensor = false;
        body.createFixture(fDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fDef.shape = head;
        fDef.isSensor = true;

        body.createFixture(fDef).setUserData("head");
    }

    public void defineBigMario() {
        Vector2 currentPosition = body.getPosition();
        world.destroyBody(body);

        BodyDef bDef = new BodyDef();
        bDef.position.set(currentPosition.add(0, 10 / MarioBros.PPM));
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fDef.shape = shape;
        body.createFixture(fDef).setUserData(this);

        shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
        body.createFixture(fDef).setUserData(this);

        EdgeShape feet = new EdgeShape();
        feet.set(new Vector2(-2 / MarioBros.PPM, -20 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, -20 / MarioBros.PPM));
        fDef.shape = feet;
        fDef.isSensor = false;
        body.createFixture(fDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fDef.shape = head;
        fDef.isSensor = true;

        body.createFixture(fDef).setUserData("head");

        timeToDefineBigMario = false;
    }

    public void redefineMario() {
        Vector2 position = body.getPosition();
        world.destroyBody(body);

        BodyDef bDef = new BodyDef();
        bDef.position.set(position);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;
        fDef.shape = shape;
        body.createFixture(fDef).setUserData(this);

        EdgeShape feet = new EdgeShape();
        feet.set(new Vector2(-2 / MarioBros.PPM, -6 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, -6 / MarioBros.PPM));
        fDef.shape = feet;
        fDef.isSensor = false;
        body.createFixture(fDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM),
                new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fDef.shape = head;
        fDef.isSensor = true;

        body.createFixture(fDef).setUserData("head");
        timeToRedefineMario = false;
    }

    public void grow() {
        runGrowAnimation = true;
        marioIsBig = true;
        timeToDefineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
    }

    public void hit(Enemy enemy) {
        if (enemy instanceof Koopa && ((Koopa) enemy).getCurState() == Koopa.State.STILL_SHELL) {
            ((Koopa) enemy).kick(this.getX() <= enemy.getX() ? Koopa.KICK_RIGHT_SPEED : Koopa.KICK_LEFT_SPEED);
        }

        else if (marioIsBig) {
            marioIsBig = false;
            timeToRedefineMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            MarioBros.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
        }
        else {
            MarioBros.manager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioBros.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
            marioIsDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for (Fixture fix : body.getFixtureList()) fix.setFilterData(filter);
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
        }
    }

    public boolean isDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }
}
