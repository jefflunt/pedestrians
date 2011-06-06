package com.jefflunt.pedestrians;

import org.newdawn.slick.BasicGame;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

/** The Pedestrian Simulation that handles logic, rendering, etc. */
public class PedestrianSim extends BasicGame {
  
  private Pedestrian simon;
  
  public PedestrianSim(String title) {
    super(title);
  }
  
  @Override
  public void init(GameContainer container) throws SlickException {
    simon = new Pedestrian(400, 300, container);
  }

  @Override
  public void update(GameContainer gc, int delta) throws SlickException {
    Input input  = gc.getInput();
    if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
      simon.addStepToPath(input.getMouseX(), input.getMouseY());
      if (simon.getSpeed() == Pedestrian.STOPPED)
        simon.changeSpeedTo(Pedestrian.WALKING_SPEED);
    } else if (input.isKeyDown(Input.KEY_P)) {
      simon.pause();
    } else if (input.isKeyDown(Input.KEY_S)) {
      simon.stop();
    } else if (input.isKeyDown(Input.KEY_SPACE)) {
      simon.resume(Pedestrian.WALKING_SPEED);
    } else if (input.isKeyDown(Input.KEY_LSHIFT) || input.isKeyDown(Input.KEY_RSHIFT)) {
      simon.changeSpeedTo(Pedestrian.RUNNING_SPEED);
    }
    
    simon.move(delta);
  }
  
  @Override
  public void render(GameContainer container, Graphics g) throws SlickException {
    simon.draw(simon.getCenterX(), simon.getCenterY());
    
    
    g.drawString("p: " + (int)simon.getCenterX() + ", " + (int)simon.getCenterY() + 
                 " -> " + (int)simon.getTargetX() + ", " + (int)simon.getTargetY(), 10, 30); 
    g.drawString("@ " + simon.getSpeed() + " px/sec", 10, 45);
    g.drawString("direction: " + simon.getDirection(), 10, 60);
    
    if (simon.getTargetPath() == null)
      g.drawString("Path segment/length: 0/0", 10, 75);
    else
      g.drawString("Path segment/length: " + simon.getTargetPathIndex() + "/" + simon.getTargetPath().getLength(), 10, 75);
    
    g.drawString("MEM total(used): " + (Runtime.getRuntime().totalMemory()/1000000) + "(" + ((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/1000000) + ") MB", 10, 90);
  }

}
