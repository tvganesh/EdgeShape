package com.tvganesh.edge;
/*
 * Created by Tinniam V Ganesh, 7 Jun 2013
 * Uses AndEngine Game Engine 
 * Uses Box2D physics engine
 */


import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import android.hardware.SensorManager;
import android.util.Log;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;


public class MainActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {
	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	public static final float PIXEL_TO_METER_RATIO_DEFAULT = 32.0f;
	
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mCircleFaceTextureRegion;
    private Scene mScene;
    
    private PhysicsWorld mPhysicsWorld;
    private int mBallCount = 0;
    
	private TextureRegion mBallTextureRegion;
	private TextureRegion mBlackBallTextureRegion,mBlueBallTextureRegion;
	private TextureRegion mPurpleBallTextureRegion,mGreenBallTextureRegion;

   
  
    
	public EngineOptions onCreateEngineOptions() {
		
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
	}
	
	public void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 74, 42, TextureOptions.BILINEAR);		
		
		this.mBallTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball.png", 0, 0);
		
		this.mCircleFaceTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "face_circle_tiled.png", 10, 10, 2, 1); // 64x32
		
		this.mBitmapTextureAtlas.load();	
		
		this.enableAccelerationSensor(this);
	

	}
	
	@Override
	public Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0.09804f, 0.6274f, 0.8784f));
		this.mScene.setOnSceneTouchListener(this);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_JUPITER), false);
		
		
		
		// Create a Maze scene
		this.initEdge(mScene);
		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		

		return mScene;		
		
	}
	
	public void initEdge(Scene mScene){
		
		final float PI=3.1415f;
		final int nBodies = 900;		
		final Sprite circles[] = new Sprite[nBodies];
		final Body circlesBody[] = new Body[nBodies];
		
		//Create the floor,ceiling and walls
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.0f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		
		final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(200f, 0.0f, 0.1f);
		
		float x1 = 50.0f;
		for (int i = 0; i < nBodies; ++i)
		{
			
			
			float angle = (float) ((10.0 * PI * i)/180.0);
			float y1 = 300 + (float)Math.cos(angle/20) * 120;
			//Log.d("Test","Test" + "x:" + x1 +"y:"+ y1);
			circles[i] = new Sprite(x1, y1, this.mBallTextureRegion, this.getVertexBufferObjectManager());
			circlesBody[i] = PhysicsFactory.createCircleBody(this.mPhysicsWorld, circles[i], BodyType.StaticBody, FIXTURE_DEF);
		  
			this.mScene.attachChild(circles[i]);
			x1 = (float) (x1 + 0.5);
			
	
		}
					
			this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		
	}

	@Override
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				this.addBall(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				Log.d("Touch","touch" + "x:" + pSceneTouchEvent.getX() + "y:" + pSceneTouchEvent.getY());
				return true;
			}
		}
		return false;
	}

	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
		
	}


	@Override
	public void onResumeGame() {
		super.onResumeGame();

		this.enableAccelerationSensor(this);

	}

	@Override
	public void onPauseGame() {
		super.onPauseGame();

		this.disableAccelerationSensor();
	}
	
	// ===========================================================
		// Methods
		// ===========================================================

		private void addBall(final float pX, final float pY) {
			Sprite blackBall, blueBall, purpleBall, greenBall;
			Body blackBallBody,blueBallBody,greenBallBody,purpleBallBody;
			final AnimatedSprite face;
			final Body body;
			this.mBallCount++;
			Debug.d("Faces: " + this.mBallCount);
			// Create the balls in the maze, with no restitution and a small coefficient of friction
			final FixtureDef gameFixtureDef = PhysicsFactory.createFixtureDef(0.5f, 0.0f, 0.1f);
			face = new AnimatedSprite(pX, pY, this.mCircleFaceTextureRegion, this.getVertexBufferObjectManager());
			body = PhysicsFactory.createCircleBody(this.mPhysicsWorld, face, BodyType.DynamicBody, gameFixtureDef);
			face.animate(200);
		
			body.applyLinearImpulse(-200,200,pX,pY);
			this.mScene.attachChild(face);
			this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(face, body, true, true));			

	
		}
}