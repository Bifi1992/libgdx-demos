package com.netthreads.gdx.app.layer.main;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.netthreads.gdx.app.definition.AppEvents;
import com.netthreads.gdx.app.layer.ManagedLayer;
import com.netthreads.gdx.app.layer.element.WorldElementContactListener;
import com.netthreads.gdx.app.sprite.LabeledSprite;
import com.netthreads.libgdx.director.AppInjector;
import com.netthreads.libgdx.director.Director;
import com.netthreads.libgdx.event.ActorEvent;
import com.netthreads.libgdx.event.ActorEventObserver;
import com.netthreads.libgdx.scene.Layer;

/**
 * Main Application layer is composed of Box2D layers to partition operations.
 * 
 */
public class MainLayer extends Layer implements ManagedLayer, ActorEventObserver
{
	// Director of the action.
	private Director director;

	// Composing layers
	private WorldLayer worldLayer;
	private TouchLayer touchLayer;
	private Layer elementLayer;
	private Layer pointerLayer;

	/**
	 * Create main layer which composes all the main application layers.
	 * 
	 * @param width
	 * @param height
	 */
	public MainLayer(float width, float height)
	{
		setWidth(width);
		setHeight(height);

		director = AppInjector.getInjector().getInstance(Director.class);

		createView();
	}

	/**
	 * Enter scene handler.
	 * 
	 */
	@Override
	public void enter()
	{
		super.enter();

		// Add this as an event observer.
		director.registerEventHandler(this);
	}

	/**
	 * Enter scene handler.
	 * 
	 */
	@Override
	public void exit()
	{
		super.exit();

		// Remove this as an event observer.
		director.deregisterEventHandler(this);
	}

	/**
	 * Compose view.
	 * 
	 */
	public void createView()
	{
		// ---------------------------------------------------------------
		// Create Box2D world layer.
		// ---------------------------------------------------------------
		worldLayer = new WorldLayer(getWidth(), getHeight());

		addActor(worldLayer);

		// ---------------------------------------------------------------
		// Create Box2D touch layer
		// ---------------------------------------------------------------
		World world = worldLayer.getWorld();
		float pixelsPerMetre = worldLayer.getPixelsPerMetre();

		touchLayer = new TouchLayer(getWidth(), getHeight(), world, pixelsPerMetre);

		// Activate broadcast of touch events.
		touchLayer.setSendTouchDown(true);
		touchLayer.setSendTouchDragged(true);
		touchLayer.setSendTouchUp(true);

		addActor(touchLayer);

		// ---------------------------------------------------------------
		// Create Box2D element layer
		// ---------------------------------------------------------------
		WorldElementContactListener contactListener = worldLayer.getContactListener();
		elementLayer = new ElementLayer(getWidth(), getHeight(), world, pixelsPerMetre, contactListener);

		addActor(elementLayer);

		// ---------------------------------------------------------------
		// Pointer and 'flare'.
		// ---------------------------------------------------------------
		pointerLayer = new PointerLayer(getWidth(), getHeight());

		addActor(pointerLayer);
	}

	/**
	 * Cleanup work elements.
	 * 
	 * @param all
	 */
	@Override
	public void cleanupView()
	{
		int size = getChildren().size;
		while (size > 0)
		{
			Actor actor = getChildren().get(--size);
			if (actor instanceof ManagedLayer)
			{
				ManagedLayer managedLayer = (ManagedLayer) actor;
				managedLayer.cleanupView();
			}
		}

		// We have to do this here after the layers have been cleaned up.
		worldLayer.getWorld().dispose();
	}

	/**
	 * Handle events.
	 * 
	 */
	@Override
	public boolean handleEvent(ActorEvent event)
	{
		boolean handled = false;

		Actor actor = event.getActor();

		switch (event.getId())
		{
		case AppEvents.EVENT_BODY_TOUCH_UP:
			handleTouchUp(actor);
			handled = true;
			break;
		default:
			break;
		}

		return handled;
	}

	/**
	 * Handle touch up event from the TouchLayer.
	 * 
	 * @param actor
	 */
	private void handleTouchUp(Actor actor)
	{
		if (actor instanceof LabeledSprite)
		{
			director.sendEvent(AppEvents.EVENT_ITEM_CLEAR, actor);
			director.sendEvent(AppEvents.EVENT_POINTER_FLARE, actor);
		}
	}
}
