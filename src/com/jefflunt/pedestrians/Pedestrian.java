package com.jefflunt.pedestrians;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.LinkedList;

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
  public static final float WALKING_SPEED = 20;
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
  /** The history of this Pedestrian's movement, a collection of MovementRecord objects. */
  private LinkedList<MovementRecord> movementHistory;
  
  /** The Path that this Pedestrian is following. */
  private Path targetPath;
  /** The index of the current point in the Path this this Pedestrian is following. */
  private int targetPathIndex;
  /** This Pedestrian's array of turning sensors. */
  private ObstacleSensor[] turningSensors;
  
  /** A record of the tile map block this Pedestrian was in, the last time they moved. NOTE: It's possible, if a Pedestrian only moves a little, or not at all,
   * or if ConfigValues.TILE_SIZE is defined to have blocks much larger than Pedestrians, that this value won't change very often.
   */ 
  private Point lastTileMapBlock;
  /** This Pedestrian's unique ID. */
  private int uniqueID;
  /** This Pedestrian's name (does not have to be unique. */
  private String name;
  /** The color that this Pedestrian will use to be rendered. */
  private Color renderColor;
  
  /** Creates a new Pedestrian */
  public Pedestrian(float x, float y, GameContainer container) {
    super(x, y, ConfigValues.PEDESTRIAN_RADIUS);
    
    movementVector = new Vector(0, STOPPED);
    targetX = x;
    targetY = y;
    movementHistory = new LinkedList<MovementRecord>();
    targetPathIndex = 0;
    targetPath = null;
    uniqueID = claimNextUniqueID();
    name = ConfigValues.randomNames[(int) (Math.random()*ConfigValues.randomNames.length)];
    renderColor = new Color((int) (Math.random()*150)+100, (int) (Math.random()*150)+100, (int) (Math.random()*150)+100);
    lastTileMapBlock = getCoordinatesOfCurrentBlock();
    
    turningSensors = new ObstacleSensor[] {
        new ObstacleSensor(this,   ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.pedestrianTurnRate,   0.1f),
        new ObstacleSensor(this,   ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.pedestrianTurnRate,   0.1f),
        new ObstacleSensor(this, 2*ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.PEDESTRIAN_RADIUS*1.5f,  ConfigValues.pedestrianTurnRate/2, 0.5f),
        new ObstacleSensor(this, 2*ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.PEDESTRIAN_RADIUS*1.5f, -ConfigValues.pedestrianTurnRate/2, 0.5f),
        new ObstacleSensor(this, 3*ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.PEDESTRIAN_RADIUS*2.5f,  ConfigValues.pedestrianTurnRate/3, 1),
        new ObstacleSensor(this, 3*ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.PEDESTRIAN_RADIUS*2.5f, -ConfigValues.pedestrianTurnRate/3, 1),
        new ObstacleSensor(this, 4*ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.pedestrianTurnRate/3, 1),
        new ObstacleSensor(this, 4*ConfigValues.PEDESTRIAN_RADIUS,       ConfigValues.PEDESTRIAN_RADIUS,      -ConfigValues.pedestrianTurnRate/3, 1),
    };
    
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
  
  /** Gets a {@link Point2D} relative to this Pedestrian, rotated in the direction of travel, and assuming the center of the Pedestrian is the origin
   * of the coordinate system. For example, if a Pedestrian's direction of travel is 'A', and their center point is (Px, Py), then this method
   * will return (rx, rx) rotated by 'A', and then translated by (Px, Py).
   * 
   * @see http://en.wikipedia.org/wiki/Rotation_(mathematics)
   * 
   * @param rx the relative x-coordinate
   * @param ry the relative y-coordinate
   * @return a {@link Point2D.Float} representing the specified coordinates, relative to the Pedestrians center, and rotation (direction).
   */
  public Point2D.Float getRelativePointFromCenter(float rx, float ry) {
    float resultX = (float) ((rx*(Math.cos(movementVector.getDirection()))) - (ry*(Math.sin(movementVector.getDirection()))));
    float resultY = (float) ((rx*(Math.sin(movementVector.getDirection()))) + (ry*(Math.cos(movementVector.getDirection()))));
    
    resultX += getCenterX();
    resultY += getCenterY();
    
    return (new Point2D.Float(resultX, resultY));
  }
  
  /** Causes the Pedestrian to move an appropriate amount, toward their target location, based on how much time has passed.
   * 
   * @param timeSlice The amount of time that has elapsed, in milliseconds.
   */
  public void move(long timeSlice) {
    while (movementHistory.size() >= ConfigValues.pedestrianMovementHistoryDepth) {
      movementHistory.removeLast();
    }
    
    TILE_MAP.getTileStateAt(lastTileMapBlock.x, lastTileMapBlock.y).unregisterPedestrian(this);
    
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
      boolean collisionSteeringUsed = false;
      float speedMultiplier = 1.0f;
      
      Pedestrian thePedestrianSensed = null;
      boolean theRelativeTileIsBlocked = false;
      
      // Basic delta and direction establishment
      Vector deltaVector = Vector.getVectorFromComponents(
          (float) (movementVector.getMagnitude()*Math.cos(getDirection())) * (timeSlice/1000.0f),
          (float) (movementVector.getMagnitude()*Math.sin(getDirection())) * (timeSlice/1000.0f));
  
      for (ObstacleSensor s : turningSensors) {
        thePedestrianSensed = s.relativePointSensesPedestrian(TILE_MAP);
        theRelativeTileIsBlocked = s.relativeTileIsBlocked(TILE_MAP);
        if (theRelativeTileIsBlocked || (thePedestrianSensed != null)) {
          movementVector.setDirection(movementVector.getDirection()+(s.turnRate*(timeSlice/1000.0f)));
          speedMultiplier = s.speedMultiplier;
          collisionSteeringUsed = true;
          break;
        }
      }
      
      // Target steering - only done if collision steering was not used
      if (!collisionSteeringUsed) {
        float targetDirectionDelta = getDirectionToTarget() - getDirection();
        if (targetDirectionDelta < 0) {
          targetDirectionDelta += 2*(Math.PI);
        }
        if (targetDirectionDelta > 2*Math.PI) {
          targetDirectionDelta -= 2*Math.PI;
        }
        
        if (targetDirectionDelta > Math.PI/4) {
          if (targetDirectionDelta < Math.PI) {
            movementVector.setDirection(movementVector.getDirection()+(ConfigValues.pedestrianTurnRate*(timeSlice/1000.0f)));
          } else {
            movementVector.setDirection(movementVector.getDirection()-(ConfigValues.pedestrianTurnRate*(timeSlice/1000.0f)));
          }
        }
      }
      
      setCenterX(getCenterX() + (deltaVector.getXComponent()*speedMultiplier));
      setCenterY(getCenterY() + (deltaVector.getYComponent()*speedMultiplier));
      
      lastTileMapBlock = getCoordinatesOfCurrentBlock();
    }
    TILE_MAP.getTileStateAt(lastTileMapBlock.x, lastTileMapBlock.y).registerPedestrian(this);
    movementHistory.add(new MovementRecord(getCenterX(), getCenterY(), getDirection()));
  }
  
  /** Gets this Pedestrian's movement history. */
  public LinkedList<MovementRecord> getMovementHistory() {
    return movementHistory;
  }
  
  /** Gets the (x, y) coordinate of the block that this Pedestrian currently occupies.
   * 
   * @return a Point, containing the (x, y) coordinates of the tile in which this Pedestrian currently resides.
   */
  public Point getCoordinatesOfCurrentBlock() {
    return getCoordinatesForBlockAt(getCenterX(), getCenterY());
  }
  
  /** Gets the tile map coordinates of the block at absolute coordinates (x, y). For example, if your ConfigValues.TILE_SIZE is 10,
   * and you call getCoordinatesForBlockAt(15, 25), this method will return a new Point(1, 2).
   * 
   * @param x the x-coordinate being evaluated
   * @param y the y-coordinate being evaluated
   * @return a Point representing the tile map coordinates of the tile map block at (x, y).
   */
  public Point getCoordinatesForBlockAt(float x, float y) {
    return (new Point((int) (x/ConfigValues.TILE_SIZE), (int) (y/ConfigValues.TILE_SIZE)));
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
  }

  @Override
  public void draw(float x, float y) {
    Graphics g = container.getGraphics();
    
    if (ConfigValues.renderPaths) {
      if (isOnAPathSomewhere()) {
        g.setColor(Color.blue);
        g.drawLine(getCenterX(), getCenterY(), getTargetX(), getTargetY());
        
        for (int i = targetPathIndex+1; i < targetPath.getLength(); i++) {
          if (i % 2 == 0)
            g.setColor(Color.cyan);
          else
            g.setColor(Color.orange);
          g.drawLine(targetPath.getX(i), targetPath.getY(i), targetPath.getX(i-1), targetPath.getY(i-1));
        }
        
        g.setColor(Color.red);
        g.fillOval(getTargetX(), getTargetY(), 2, 2);
      }
    }
    
    if (ConfigValues.renderTurnSensors) { 
      for (ObstacleSensor s : turningSensors) {
        Point2D.Float sensorLocation = getRelativePointFromCenter(s.rx, s.ry);
        if (s.relativeTileIsBlocked(TILE_MAP) || (s.relativePointSensesPedestrian(TILE_MAP) != null)) { 
          g.setColor(Color.white);
          g.fillOval(sensorLocation.x, sensorLocation.y, 4, 4);
        } else {
          g.setColor(Color.green);
          g.fillOval(sensorLocation.x, sensorLocation.y, 2, 2);
        }
        
      }
    }
    
    if (ConfigValues.renderXRay) {
      g.setColor(renderColor);
      g.drawOval(x-radius, y-radius, 2*radius, 2*radius);
      g.drawLine(getCenterX(), getCenterY(), (float) (getCenterX()+(5*(Math.cos(getDirection())))), (float) (getCenterY()+(5*(Math.sin(getDirection())))));
    } else {
      g.drawImage(PedestrianSim.getImageResource(2), getCenterX()-10, getCenterY()-16);
    }
    
    if (ConfigValues.renderPedNames) {
      g.setColor(Color.white);
      g.drawString(name, getCenterX() + 8, getCenterY() - 10);
    }
  }
  
  /** This class implements a simple obstacle sensor, used to tell the Pedestrian when an obstacle is encountered. */
  private class ObstacleSensor {
    
    /** The Pedestrian associated with this ObstacleSensor. */
    private Pedestrian pedestrian;
    private float rx, ry;
    private float speedMultiplier;
    private float turnRate;
    
    /** Builds a new obstacle sensor.
     * 
     * @param ped the Pedestrian who owns this sensor.
     * @param relativeX the x-coordinate of the position relative to the Pedestrian's center point and direction of travel
     * @param relativeY the y-coordinate of the position relative to the Pedestrian's center point, and direction of travel
     * @param turnRate the rate at which the Pedestrian should turn when this sensor indicates an obstacle
     * @param speedMultiplier the speed multiplier applied to the Pedestrian's speed, when this sensor indicates an obstacle
     */
    public ObstacleSensor(Pedestrian ped, float relativeX, float relativeY, float turnRate, float speedMultiplier) {
      this.pedestrian = ped;
      this.rx = relativeX;
      this.ry = relativeY;
      this.turnRate = turnRate;
      this.speedMultiplier = speedMultiplier;
    }
    
    /** Gets whether or not the PedestrianTileBasedMap tile is blocked, or open.
     * 
     * @param tileMap the PedestrianTileBasedMap to use
     * @return true, if the corresponding tile is blocked, false if it is open.
     */
    public boolean relativeTileIsBlocked(PedestrianTileBasedMap tileMap) {
      Point2D.Float relativePoint = pedestrian.getRelativePointFromCenter(rx, ry);
      
      return (tileMap.blocked(null, (int) (relativePoint.x/ConfigValues.TILE_SIZE), (int) (relativePoint.y/ConfigValues.TILE_SIZE)));
    }
    
    /** Gets whether or not this sensor senses a Pedestrian.
     * 
     * @param tileMap the PedestrianTileBasedMap to use, to get the TileState, and the resulting list of Pedestrians in that tile
     * @return true if a Pedestrian is detected, false otherwise.
     */
    public Pedestrian relativePointSensesPedestrian(PedestrianTileBasedMap tileMap) {
      Pedestrian thePedestrianSensed = null;
      Point2D.Float relativePoint = pedestrian.getRelativePointFromCenter(rx, ry);
      LinkedList<Pedestrian> peds = tileMap.getTileStateAt((int) (relativePoint.x/ConfigValues.TILE_SIZE), (int) (relativePoint.y/ConfigValues.TILE_SIZE)).getRegisteredPedestrians();
      
      for (Pedestrian p : peds) {
        if (Math.hypot(relativePoint.x - p.getCenterX(), relativePoint.y - p.getCenterY()) <= ConfigValues.PEDESTRIAN_RADIUS) {
          thePedestrianSensed = p;
          break;
        }
      }
      
      return thePedestrianSensed;
    }
    
  }

}
