package com.cosmosis.mariobros.screens;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.cosmosis.mariobros.MarioBros;
import com.cosmosis.mariobros.scenes.Hud;
import com.cosmosis.mariobros.sprites.enemies.Enemy;
import com.cosmosis.mariobros.sprites.Mario;
import com.cosmosis.mariobros.sprites.items.Item;
import com.cosmosis.mariobros.sprites.items.ItemDef;
import com.cosmosis.mariobros.sprites.items.Mushroom;
import com.cosmosis.mariobros.tools.Controller;
import com.cosmosis.mariobros.tools.WorldContactListener;
import com.cosmosis.mariobros.tools.WorldCreator;

import java.util.PriorityQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Cal on 2/5/2017.
 */

public class PlayScreen implements Screen {

    private MarioBros game;
    private TextureAtlas atlas;

    private OrthographicCamera gameCam;
    private Viewport gamePort;
    private Hud hud;
    private Controller controller;

    //Tiled map variables
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private String mapLocation = "level1.tmx";
    private OrthogonalTiledMapRenderer renderer;

    //Box2d variables
    private World world;
    private Box2DDebugRenderer b2dr;
    private WorldCreator creator;

    private Mario mario;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    public PlayScreen(MarioBros game) {
        this.game = game;
        atlas = new TextureAtlas("Mario_and_Enemies.pack");
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM,
                MarioBros.V_HEIGHT / MarioBros.PPM, gameCam);
        hud = new Hud(game.batch);
        controller = new Controller(game);

        mapLoader = new TmxMapLoader();
        map = mapLoader.load(mapLocation);
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioBros.PPM);

        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new WorldCreator(this);

        mario = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioBros.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(.1f);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();
    }

    public void spawnItem(ItemDef iDef) {
        itemsToSpawn.add(iDef);
    }

    public void handleSpawningItems() {
        if (!itemsToSpawn.isEmpty()) {
            ItemDef iDef = itemsToSpawn.poll();
            if (iDef.type == Mushroom.class) {
                items.add(new Mushroom(this, iDef.position.x, iDef.position.y));
            }
        }
    }

    public boolean gameOver() {
        if (mario.isDead() && mario.getStateTimer() > 3) {
            return true;
        }
        return false;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    @Override
    public void show() {

    }

    public void handleInput(float dt) {
        if (mario.isDead()) {
            float yVel = mario.body.getLinearVelocity().y;
            mario.body.setLinearVelocity(0, yVel);
            return;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) ||
                controller.isUpPressed()) {
            if (Gdx.app.getType() == Application.ApplicationType.Android) {
                mario.body.applyLinearImpulse(new Vector2(0, 4f), mario.body.getWorldCenter(), true);
                controller.setUpPressed(false);
            }
            else {
                mario.body.applyLinearImpulse(new Vector2(0, 4f), mario.body.getWorldCenter(), true);
            }
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
                controller.isRightPressed()) && mario.body.getLinearVelocity().x <= 2) {
            mario.body.applyLinearImpulse(new Vector2(0.1f, 0), mario.body.getWorldCenter(), true);
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
                controller.isLeftPressed()) && mario.body.getLinearVelocity().x >= -2) {
            mario.body.applyLinearImpulse(new Vector2(-0.1f, 0), mario.body.getWorldCenter(), true);
        }
    }

    public void update(float dt) {
        handleInput(dt);
        handleSpawningItems();

        world.step(1 / 60f, 6, 2);

        mario.update(dt);

        for (Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if (enemy.getX() < mario.getX() + 224 / MarioBros.PPM) enemy.body.setActive(true);
        }

        for (Item item : items) {
            item.update(dt);
        }

        hud.update(dt);

        gameCam.position.x = mario.body.getPosition().x;
        //gameCam.position.y = mario.body.getPosition().y;

        gameCam.update();
        renderer.setView(gameCam);
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderer.render();
        b2dr.render(world, gameCam.combined);

        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        mario.draw(game.batch);
        for (Enemy enemy : creator.getEnemies()) {
            enemy.draw(game.batch);
        }
        for (Item item : items) {
            item.draw(game.batch);
        }
        game.batch.end();

        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            controller.draw();
        }


        if (gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
        controller.resize(width, height);

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
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public World getWorld() {
        return world;
    }

    public TiledMap getMap() {
        return map;
    }
}
