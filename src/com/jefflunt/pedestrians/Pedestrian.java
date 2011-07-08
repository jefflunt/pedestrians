package com.jefflunt.pedestrians;

import java.awt.Point;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Renderable;
import org.newdawn.slick.geom.Circle;
import org.newdawn.slick.util.pathfinding.Mover;
import org.newdawn.slick.util.pathfinding.Path;

import com.jefflunt.pedestrians.pathfinding.PedestrianTileBasedMap;
import com.jefflunt.pedestrians.physics.Vector;

/** A class describing a Pedestrian that moves around the world. */
public class Pedestrian extends Circle implements Renderable, Mover {
  
  /** The next unique ID in the queue. */
  private static int nextUniqueID = 1;
  /** The PedestrianTileBasedMap that all Pedestrians are moving on. */
  private static PedestrianTileBasedMap TILE_MAP;
  
  private static final long serialVersionUID = 1202551036619728216L;
  /** The maximum distance from a target location at which a Pedestrian is considered to have arrived. */
  public static final float STOP_DISTNACE = ConfigValues.TILE_SIZE;
  /** Speed for when you're stopped. */
  public static final float STOPPED = 0;
  /** Speed for when you're walking. */
  public static final float WALKING_SPEED = 30;
  /** Speed for when you're running. */
  public static final float RUNNING_SPEED = 120;
  
  /** The GameContainer of which this Pedestrian is a part. */
  private GameContainer container;
  /** The Vector representing this Pedestrian's current movement. */
  private Vector movementVector;
  
  /** The x-coordinate of the current target point. */
  private float targetX;
  /** The y-coordinate of the current target point. */
  private float targetY;
  
  /** The Path that this Pedestrian is following. */
  private Path targetPath;
  /** The index of the current point in the Path this this Pedestrian is following. */
  private int targetPathIndex;
  
  /** This Pedestrian's unique ID. */
  private int uniqueID;
  
  /** Creates a new Pedestrian */
  public Pedestrian(float x, float y, GameContainer container) {
    super(x, y, ConfigValues.PEDESTRIAN_RADIUS);
    
    movementVector = new Vector(0, STOPPED);
    targetX = x;
    targetY = y;
    targetPathIndex = 0;
    targetPath = null;
    uniqueID = claimNextUniqueID();
    this.container = container;
  }
  
  /** Gets the PedestrianTileBasedMap that all Pedestrians will use.
   * 
   * @param pedMap the Pedestrian TileBasedMap that will become the new, global tile map.
   */
  public static void setGlobalTileMap(PedestrianTileBasedMap pedMap) {
    TILE_MAP = pedMap;
  }
  
  /** Gets this Pedestrian's unique ID.
   * 
   * @return the unique ID assigned to this Pedestrian.
   */
  public int getUniqueID() {
    return uniqueID;
  }
  
  /** Gets the next unique Pedestrian ID, as increments. */
  private static int claimNextUniqueID() {
    return nextUniqueID++;
  }
  
  /** Gets the Path the this Pedestrian is following.
   * 
   * @return the pPath that this Pedestrian is following.
   */
  public Path getTargetPath() {
    return targetPath;
  }
  
  /** Gets the index of the next point in the Path that this Pedestrian if following. That is, when a Pedestrian is following a Path,
   * that Path is made up of many waypoints from the beginning to the end. As the Pedestrian traverses that that it moves from waypoint
   * to waypoint, until it reaches the end of the Path. The index is zero-based, so if the Pedestrian is currently heading toward 
   * the 17th waypoint in its Path, then this method will return 16.
   * 
   * @return the index of the next point along this Pedestrian's path.
   */
  public int getTargetPathIndex() {
    return targetPathIndex;
  }
  
  /** Gets the total number of points in the Path that this Pedestrian is following.
   * 
   * @return the total number of points in this Pedestrian's path.
   */
  public int getNumberOfPointsInPath() {
    return targetPath.getLength();
  }
  
  /** Causes the Pedestrian to move an appropriate amount, toward their target location, based on how much time has passed.
   * 
   * @param timeSlice The amount of time that has elapsed, in milliseconds.
   */
  public void move(long timeSlice) {
    if (hasReachedDestination()) {
      if (isOnAPathSomewhere()) {
        targetPathIndex++;
        if (targetPathIndex >= targetPath.getLength()) {
          stop();
        } else {
          headToward(targetPath.getX(targetPathIndex), targetPath.getY(targetPathIndex), getSpeed());
        }
      }
    } else {
      // Basic direction establishment
      float baseDeltaX = (float) (movementVector.getMagnitude()*Math.cos(getDirection())) * (timeSlice/1000.0f);
      float baseDeltaY = (float) (movementVector.getMagnitude()*Math.sin(getDirection())) * (timeSlice/1000.0f);
      
      // Tile vectors (which include both the obstacle, and Pedestrian push vectors
      Vector baseVector = Vector.getVectorFromComponents(baseDeltaX, baseDeltaY);
      Point blockCoordinates = getCoordinatesOfCurrentBlock();
      
      for (int x = -2; x <= 2; x++) {
        for (int y = -2; y <= 2; y++) {
          Vector tileVector = TILE_MAP.pushVectorFromTile(this, blockCoordinates.x+x, blockCoordinates.y+y);
          tileVector.scaleMagnitudeByPercentage((timeSlice/1000.0f));
          baseVector.add(tileVector);
        }
      }
      
      // Tile steering
      float directionDelta = baseVector.getDirection() - movementVector.getDirection();
      //float percentageTurn = baseVector.getMagnitude() / movementVector.getMagnitude();
      movementVector.setDirection(movementVector.getDirection()+(directionDelta));
      
      // Target steering
      float targetDirectionDelta = getDirectionToTarget() - getDirection();
      if (targetDirectionDelta < 0) {
        targetDirectionDelta += 2*(Math.PI);
      }
      if (targetDirectionDelta > 2*Math.PI) {
        targetDirectionDelta -= 2*Math.PI;
      }
      
      if (targetDirectionDelta > Math.PI/4) {
        if (targetDirectionDelta < Math.PI) {
          movementVector.setDirection(movementVector.getDirection()+(targetDirectionDelta*0.06f));
        } else {
          movementVector.setDirection(movementVector.getDirection()-(targetDirectionDelta*0.06f));
        }
      }
      
      // Final adjustment of movement vector
      float proposedX = getCenterX() + baseVector.getXComponent();
      float proposedY = getCenterY() + baseVector.getYComponent();
      if (!TILE_MAP.blocked(null, (int) (proposedX/ConfigValues.TILE_SIZE), (int) (proposedY/ConfigValues.TILE_SIZE))) {
        setCenterX(getCenterX() + baseVector.getXComponent());
        setCenterY(getCenterY() + baseVector.getYComponent());
        TILE_MAP.getTileStateAt((int) (getCenterX()/ConfigValues.TILE_SIZE), (int) (getCenterY()/ConfigValues.TILE_SIZE)).registerPedestrian(this);
      }
    }
  }
  
  /** Gets the (x, y) coordinate of the block that this Pedestrian currently occupies.
   * 
   * @return a Point, containing the (x, y) coordinates of the tile in which this Pedestrian currently resides.
   */
  public Point getCoordinatesOfCurrentBlock() {
    return (new Point((int) (getCenterX()/ConfigValues.TILE_SIZE), (int) (getCenterY()/ConfigValues.TILE_SIZE)));
  }
  
  /** The distance, from the Pedestrian's current location, along the Path they are following, to the end
   * of the Path. It should be noted that this is an approximation. Due to various Pedestrian behaviors
   * (such as collision avoidance), it's very possible that the actual Path the Pedestrian takes may be
   * slightly modified from it's planned Path. If the Pedestrian doesn't run into anything they need to avoid
   * along their Path, then the value returned from this method is going to be extremely accurate, to within
   * a fraction of a single pixel. In a heavily populated area where the Pedestrian must avoid collisions,
   * and other movement modifiers may apply, this method can only provide an approximation at best.
   * 
   * Finally, if a Pedestrian is not currently following a Path, then this method will return 0.0.
   * 
   * @return The distance from the Pedestrian's current location, along their Path, to the end of the Path.
   */
  public float distanceToEndOfPath() {
    float totalDistance = distanceToTarget();
    
    if (targetPath != null) {
      for (int i = targetPathIndex+1; i < targetPath.getLength(); i++) {
        totalDistance += Math.hypot(targetPath.getX(i)-targetPath.getX(i-1),
                                    targetPath.getY(i)-targetPath.getY(i-1));
      }
    }
    
    return totalDistance;
  }
  
  /** Whether or not the Pedestrian has reached the end of their Path.
   * 
   * @return true if the Pedestrian is currently on a Path somewhere (even if their current speed is zero). False otherwise.
   */
  public boolean isOnAPathSomewhere() {
    return (targetPath != null);
  }
  
  /** Sets a new target (x, y) for this Pedestrian.
   * 
   * @param x the x-coordinate of the new target location.
   * @param y the y-coordinate of the new target location.
   * @param speed the speed at which you'd like the Pedestrian to travel there.
   */
  public void setNewTargetPoint(float x, float y, float speed) {
    setTargetLocation(x, y);
    movementVector.setMagnitude(speed);
  }
  
  /** Tells the Pedestrian to head from their current location, along the specified Path.
   * 
   * @param p the Path to follow.
   * @param speed the speed at which you want them to travel along this path.
   */
  public void headAlongPath(Path p, float speed) throws IllegalArgumentException {
    if (p != null) {
      targetPath = p;
      targetPathIndex = 0;
      if (p.getLength() == 0)
        throw new IllegalArgumentException("The path you send a Pedestrian on, must have at least one step.");
      else
        setNewTargetPoint(targetPath.getX(targetPathIndex), targetPath.getY(targetPathIndex), speed);
    }
  }
  
  /** Tells the Pedestrian to head from their current location, along the specified Path.
   * 
   * @param p the Path to follow
   * @param speed the speed at which you want them to travel along this path.
   * @param expandPath if true, the path passed in is expanded by PedestrianSim.TILE_SIZE. This method is provided to handle paths generated by the PathFinder.
   * @throws IllegalArgumentException
   */
  public void headAlongPath(Path p, float speed, boolean expandPath) throws IllegalArgumentException {
    if (p != null) {
      if (expandPath) {
        Path expandedPath = new Path();
        for (int i = 0; i < p.getLength(); i++) {
          expandedPath.appendStep((p.getStep(i).getX()*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2),
                                  (p.getStep(i).getY()*ConfigValues.TILE_SIZE) + (ConfigValues.TILE_SIZE/2));
        }
        
        headAlongPath(expandedPath, speed);
      } else {
        headAlongPath(p, speed);
      }
    }
  }
  
  /** Adds an additional step to the already defined path, or sets this Pedestrian along a path if they are not currently on one.
   * 
   * @param x the x-coordinate of the new step
   * @param y the y-coordinate of the new step
   */
  public void addStepToPath(int x, int y) {
    boolean wasNotOnAPath = (targetPath == null);
    if (wasNotOnAPath) {
      targetPath = new Path();
      targetPathIndex = 0;
    }
    
    if ((targetPath.getLength() == 0) || ((targetPath.getX(targetPath.getLength()-1) != x) || (targetPath.getY(targetPath.getLength()-1) != y))) 
      targetPath.appendStep(x, y);
    
    if (wasNotOnAPath) {
      headToward(targetPath.getX(targetPathIndex), targetPath.getY(targetPathIndex), getSpeed());
    }
  }
  
  /** The distance, from the Pedestrian's current location, to their current target location. If this Pedestrian
   * is following a Path, then this method returns how far it is to the next point in the path only. If you want
   * to find out how far the Pedestrian has yet to go along their Path, see distanceToEndOfPath.
   * 
   * @see distanceToEndOfPath
   * 
   * @return The distance between the Pedestrian's current location, and their target point.
   */
  public float distanceToTarget() {
    return distanceToPoint(targetX, targetY);
  }
  
  /** The distance, directly from the Pedestrian's current location, to the (x, y) point specified.
   * 
   * @param x the x-coordinate of the point
   * @param y the y-coordinate of the point
   * @return the distance from the Pedestrian's current location to the specified point.
   */
  public float distanceToPoint(float x, float y) {
    return (float) Math.hypot(getCenterX()-x, getCenterY()-y);
  }
  
  /** Whether or not the Pedestrian has reached the (x, y) location they are currently heading for.
   * 
   * @return true if they've reached that destination, false otherwise. If you ask a Pedestrian to follow
   * a Path, this method is used internally to know when the Pedestrian arrives at each of the points
   * along that Path, and serves as a way to know when the Pedestrian should change direction and head
   * for the next point on that Path.
   */
  public boolean hasReachedDestination() {
    return (distanceToPoint(targetX, targetY) <= Pedestrian.STOP_DISTNACE);
  }
  
  /** Causes the Pedestrian to stop, and forget where they were headed.
   * @see pause
   * @see resume
   */
  public void stop() {
    movementVector = new Vector(0, 0);
    targetX = getCenterX();
    targetY = getCenterY();
    targetPath = null;
    targetPathIndex = 0;
  }
  
  /** Changes the Pedestrian's speed, but does not change their destination. If the Pedestrian has already arrived
   * at their destination, then this method will have no effect.
   * 
   * @param speed the speed at which you desire them to get there (e.g. "Post haste!").
   */
  public void changeSpeedTo(float speed) {
    if (!hasReachedDestination())
      movementVector.setMagnitude(speed);
  }
  
  /** Sends this Pedestrian from their current location, directly toward the specified point in space.
   * 
   * @param x the x-coordinate of the destination
   * @param y the y-coordinate of the destination
   * @param speed the speed at which to travel there.
   */
  public void headToward(float x, float y, float speed) {
    setTargetLocation(x, y);
    movementVector.setMagnitude(speed);
  }
  
  /** Sets the Pedestrian in motion either up, down, left, or right. The Pedestrian's target
   * location is actually set to Float.MAX_VALUE or -Float.MAX_VALUE, depending on which way
   * you tell them to go. They will continue in that direction unless blocked by something,
   * until they literally reach the edge of the world, where they will stop.
   * 
   * It should also be noted that an assumption about direction has been made. It is assumed
   * that "UP" is going to move you up along a computer screen, since it's assumed that you
   * will be displaying Pedestrians on a screen of some sort.
   * 
   * Coordinate systems on a computer screen usually have their y-axis increasing DOWNWARD,
   * whereas the coordinate systems you learned about in school have their y-axis increasing UPWARD. 
   * <pre>
   * Your typical coordinate system:          Your computer screen's coordinate system:
   * 
   *                  +y  (up)                               ((-Y))  (up)
   *                 / \                                      / \
   *                  |                                        |
   *                  |                                        |
   * (left) -x  <-----+-----> +x (right)      (left) -x  <-----+-----> +x (right)
   *                  |                                        |
   *                  |                                        |
   *                  |                                        |
   *                 \ /                                      \ /
   *                  -y  (down)                             ((+Y)) (down)
   * 
   * </pre>
   * 
   * That's it. No big deal, but it might save you some trouble one day.
   * 
   * @see ConfigValues.UP, DOWN, LEFT, and RIGHT.
   * 
   * @param upDownLeftOrRight the direction you want this Pedestrian to go.
   * @param speed the speed at which you desire the Pedestrian to travel.
   */
  public void headDirection(int upDownLeftOrRight, float speed) {
    switch (upDownLeftOrRight) {
      case ConfigValues.UP:
        headToward(getCenterX(), -Float.MAX_VALUE, speed);
        break;
      case ConfigValues.DOWN:
        headToward(getCenterX(), Float.MAX_VALUE, speed);
        break;
      case ConfigValues.LEFT:
        headToward(-Float.MAX_VALUE, getCenterY(), speed);
        break;
      case ConfigValues.RIGHT:
        headToward(Float.MAX_VALUE, getCenterY(), speed);
        break;
      case ConfigValues.UP_LEFT:
        headToward(-Float.MAX_VALUE, -Float.MAX_VALUE, speed);
        break;
      case ConfigValues.UP_RIGHT:
        headToward(Float.MAX_VALUE, -Float.MAX_VALUE, speed);
        break;
      case ConfigValues.DOWN_LEFT:
        headToward(-Float.MAX_VALUE, Float.MAX_VALUE, speed);
        break;
      case ConfigValues.DOWN_RIGHT:
        headToward(Float.MAX_VALUE, Float.MAX_VALUE, speed);
        break;
    }
  }
  
  /** Gets the direction of travel of this Pedestrian, measured in radians.
   * 
   * @return the direction of travel, in radians.
   */
  public float getDirection() {
    return movementVector.getDirection();
  }
  
  /** Gets the direction from this Pedestrian's current location, toward their target location.
   * 
   * @return the direction, measured in radians, from the Pedestrian to their target location.
   */
  public float getDirectionToTarget() {
    return Vector.getDirectionFromDeltas(getTargetX()-getCenterX(), getTargetY()-getCenterY());
  }
  
  /** Returns the primary direction that this pedestrian is heading in. It is not necessary that a Pedestrian be heading
   * precisely in that direction. This method is simply supposed to tell you something like, "They're basically heading to the right."
   * or "They're basically heading up, and to the right", etc.
   * 
   * @return one of UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
   */
  public int getPrimaryDirection() {
    int primaryDirection = 666;
    float dir = movementVector.getDirection();
    
    if ((dir > 0.392699082) && (dir <= 1.17809725))
      primaryDirection = ConfigValues.DOWN_RIGHT;
    else if ((dir > 1.17809725) && (dir <= 1.96349541))
      primaryDirection = ConfigValues.DOWN;
    else if ((dir > 1.96349541) && (dir <= 2.74889358))
      primaryDirection = ConfigValues.DOWN_LEFT;
    else if ((dir > 2.74889358) && (dir <= 3.53429174))
      primaryDirection = ConfigValues.LEFT;
    else if ((dir > 3.53429174) && (dir <= 4.3196899))
      primaryDirection = ConfigValues.UP_LEFT;
    else if ((dir > 4.3196899)  && (dir <= 5.10508807))
      primaryDirection = ConfigValues.UP;
    else if ((dir > 5.10508807) && (dir <= 5.89048623))
      primaryDirection = ConfigValues.UP_RIGHT;
    else
      primaryDirection = ConfigValues.RIGHT;
    
    return primaryDirection;
  }
  
  /** Gets this Pedestrian's current speed.
   * 
   * @return this Pedestrian's current speed.
   */
  public float getSpeed() {
    return movementVector.getMagnitude();
  }
  
  /** Gets the x-coordinate of this Pedestrian's current target location.
   * 
   * @return the x-coordinate of the location to which this Pedestrian is traveling.
   */
  public float getTargetX() {
    return targetX;
  }
  
  /** Gets the y-coordinate of this Pedestrian's current target location.
   * 
   * @return the y-coordinate of the location to which this Pedestrian is traveling.
   */
  public float getTargetY() {
    return targetY;
  }
  
  /** Sets this Pedestrian's new targetLocation.
   * 
   * @param x the x-coordinate of the new target location.
   * @param y the y-coordinate of the new target location.
   */
  private void setTargetLocation(float x, float y) {
    targetX = x;
    targetY = y;
    
    movementVector = Vector.getVectorFromComponents(targetX-getCenterX(), targetY-getCenterY());
  }

  @Override
  public void draw(float x, float y) {
    Graphics g = container.getGraphics();
    
//    if (isOnAPathSomewhere()) {
//      g.setColor(Color.blue);
//      g.drawLine(getCenterX(), getCenterY(), getTargetX(), getTargetY());
//      
//      for (int i = targetPathIndex+1; i < targetPath.getLength(); i++) {
//        if (i % 2 == 0)
//          g.setColor(Color.cyan);
//        else
//          g.setColor(Color.orange);
//        g.drawLine(targetPath.getX(i), targetPath.getY(i), targetPath.getX(i-1), targetPath.getY(i-1));
//      }
//      
//      g.setColor(Color.red);
//      g.fillOval(getTargetX(), getTargetY(), 2, 2);
//    }
    
    g.setColor(Color.white);
    g.drawOval(x-radius, y-radius, 2*radius, 2*radius);
    g.drawLine(getCenterX(), getCenterY(), (float) (getCenterX()+(5*(Math.cos(getDirection())))), (float) (getCenterY()+(5*(Math.sin(getDirection())))));
  }

}
