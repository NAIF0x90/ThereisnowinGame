package com.naif.thereisnowin.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class BounceGame extends InputAdapter implements ApplicationListener {

	SpriteBatch batch;
	ShapeRenderer ballShape, boxShape;

	private World world;
	private OrthographicCamera camera;
	Body wallsBody, ballBody, guardBody;

	private MouseJointDef jointDef;
	private MouseJoint joint;
	final float PIXELS_TO_METERS = 25.3f;
	final float PIXELS_TO_METERS2 = 30.8f;
	float radius = 15f / 5;

	@Override
	public void create() {
		world = new World(new Vector2(0, -9.81f), true);
		camera = new OrthographicCamera();
		camera.viewportHeight = Gdx.graphics.getHeight();
		batch = new SpriteBatch();
		ballShape = new ShapeRenderer();
		boxShape = new ShapeRenderer();

		InputMultiplexer inputMultiplexer = new InputMultiplexer();
		inputMultiplexer.addProcessor(this);
		Gdx.input.setInputProcessor(this);

		BodyDef bodyDef = new BodyDef();

		// box body
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.bullet = true;
		bodyDef.position.set(0,0);
		ballBody = world.createBody(bodyDef);

		PolygonShape shape = new PolygonShape();
		CircleShape circleShape = new CircleShape();
		circleShape.setRadius(radius);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;
		fixtureDef.density = 0;
		fixtureDef.restitution = 0.5f;
		ballBody.createFixture(fixtureDef);
		circleShape.dispose();

		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(0,0);
		wallsBody = world.createBody(bodyDef);

		// gauerd body
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(((Gdx.graphics.getWidth() / 3 / PIXELS_TO_METERS2)) ,0);
		guardBody = world.createBody(bodyDef);

		shape = new PolygonShape();
		shape.setAsBox(30 / PIXELS_TO_METERS, 30  / PIXELS_TO_METERS);
		fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = 0.3f;
		guardBody.createFixture(fixtureDef);
		shape.dispose();

		// boarders
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(0,0);
		wallsBody = world.createBody(bodyDef);

		ChainShape chainShape = new ChainShape();
		Vector2[] box = new Vector2[4];
		box[0] = new Vector2(-((Gdx.graphics.getWidth() / 2) / PIXELS_TO_METERS2), -((Gdx.graphics.getHeight() / 2) / PIXELS_TO_METERS));
		box[1] = new Vector2(-((Gdx.graphics.getWidth() / 2) / PIXELS_TO_METERS2), ((Gdx.graphics.getHeight() / 2) / PIXELS_TO_METERS));
		box[2] = new Vector2(((Gdx.graphics.getWidth() / 2) / PIXELS_TO_METERS2),((Gdx.graphics.getHeight() / 2) / PIXELS_TO_METERS));
		box[3] = new Vector2(((Gdx.graphics.getWidth() / 2) / PIXELS_TO_METERS2), -((Gdx.graphics.getHeight() / 2) / PIXELS_TO_METERS));

		chainShape.createLoop(box);
		fixtureDef = new FixtureDef();
		fixtureDef.shape = chainShape;
		fixtureDef.density = 0.0f;
		wallsBody.createFixture(fixtureDef);
		chainShape.dispose();
		// mouse joint

		jointDef = new MouseJointDef();
		jointDef.bodyA = wallsBody;
		jointDef.collideConnected = true;
		jointDef.maxForce = 10000;
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width / 30;
		camera.viewportHeight = height / 25;
		camera.update();
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(62/255f, 134/255f, 170/255f, 0);
		Gdx.gl.glClear( GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		world.step(1 / 60f, 8, 3);

		batch.setProjectionMatrix(camera.combined);

		// Scale down the sprite batches projection matrix to box2D size

		batch.begin();
		batch.end();

		ballShape.setProjectionMatrix(camera.combined);
		ballShape.begin(ShapeRenderer.ShapeType.Filled);
		ballShape.setColor(Color.DARK_GRAY);
		ballShape.circle(ballBody.getPosition().x, ballBody.getPosition().y, radius, 100);
		ballShape.end();

		boxShape.setProjectionMatrix(camera.combined);
		boxShape.begin(ShapeRenderer.ShapeType.Filled);
		boxShape.setColor(Color.GREEN);
		boxShape.box(guardBody.getPosition().x - 1.15f, guardBody.getPosition().y - 1.15f, 0, 2.3f, 2.3f, 0);
		boxShape.end();

		guardBody.setTransform(new Vector2(guardBody.getPosition().x, ballBody.getTransform().getPosition().y), guardBody.getAngle());

	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		world.dispose();
	}

	private Vector3 tmp = new Vector3();
	private Vector2 tmp2 = new Vector2();

	private QueryCallback queryCallback = new QueryCallback() {

		@Override
		public boolean reportFixture(Fixture fixture) {
			if(!fixture.testPoint(tmp.x, tmp.y))
				return true;

			System.out.println("true");
			jointDef.bodyB = fixture.getBody();
			jointDef.target.set(tmp.x, tmp.y);
			joint = (MouseJoint) world.createJoint(jointDef);
			return false;
		}

	};

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		camera.unproject(tmp.set(screenX, screenY, 0));
		world.QueryAABB(queryCallback, tmp.x, tmp.y, tmp.x, tmp.y);
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(joint == null)
			return false;

		camera.unproject(tmp.set(screenX, screenY, 0));
		joint.setTarget(tmp2.set(tmp.x, tmp.y));
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(joint == null)
			return false;

		world.destroyJoint(joint);
		joint = null;
		return true;
	}

	@Override
	public boolean keyDown(int keycode) {
		// On right or left arrow set the velocity at a fixed rate in that
		if(keycode == Input.Keys.RIGHT){
			ballBody.setLinearVelocity(new Vector2(3, ballBody.getLinearVelocity().y));
		}
		if(keycode == Input.Keys.LEFT)
			ballBody.setLinearVelocity(new Vector2(-3, ballBody.getLinearVelocity().y));
		if(keycode == Input.Keys.UP)
			ballBody.setLinearVelocity(new Vector2(ballBody.getLinearVelocity().x,3));
		if(keycode == Input.Keys.DOWN)
			ballBody.setLinearVelocity(new Vector2(ballBody.getLinearVelocity().x,-3));

		// If user hits spacebar, reset everything back to normal
		if(keycode == Input.Keys.SPACE) {
			wallsBody.setLinearVelocity(0f, 0f);
			wallsBody.setAngularVelocity(0f);
			wallsBody.setTransform(0f,0f,0f);
		}

		return true;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		camera.zoom *= amountY > 0 ? 1.05f : 0.95f;
		return true;
	}
}