package com.jefflunt.pedestrians;

/** This class stores a single moment in a pedestrian's life. */
public class MovementRecord {

  /** The x-coordinate of the pedestrian's position. */
  public float x;
  /** The y-coordinate of the pedestrian's position. */
  public float y;
  /** The direction of travel. */
  public float direction;
  
  /** Constructs a new MovementRecord with the specified values.
   * 
   * @param x the x-coordinate of the MovementRecord.
   * @param y the y-coordinate of the MovementRecord.
   * @param direction the direction of the MovementRecord.
   */
  public MovementRecord(float x, float y, float direction) {
    this.x = x;
    this.y = y;
    this.direction = direction;
  }
  
}
