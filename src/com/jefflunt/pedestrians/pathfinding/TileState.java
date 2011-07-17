package com.jefflunt.pedestrians.pathfinding;

import java.util.LinkedList;

import com.jefflunt.pedestrians.ConfigValues;
import com.jefflunt.pedestrians.Pedestrian;

/** A class that describes the current state of a tile in a PedestrianTileBasedMap. */
public class TileState {

  /** Whether or not this tile is blocked. */
  private boolean blocked;
  /** The list of all Pedestrians who claim to be inside this tile. */
  private LinkedList<Pedestrian> pedestriansInThisTile;
  
  /** The previous congestion level. */
  private float congestion;
  /** The last time congestion was calculated on this Tile. */
  private long nextCongestionCalculationTime;
  
  /** Creates a new TileState. Default values are 'false' for the blocked state, and no Pedestrians registered. */
  public TileState() {
    blocked = false;
    pedestriansInThisTile = new LinkedList<Pedestrian>();
    
    congestion = 0;
    nextCongestionCalculationTime = System.currentTimeMillis() + ConfigValues.MILLIS_BETWEEN_CONGESTION_CALCULATIONS;
  }
  
  /** Gets whether or not this tile is blocked. */
  public boolean isBlocked() {
    return blocked;
  }
  
  /** Sets whether or not this tile is blocked. */
  public void setIsBlocked(boolean blocked) {
    this.blocked = blocked;
  }
  
  /** Recalculates the congestion level. */
  private void recalculateCongestion() {
    float momentaryCongestion = (pedestriansInThisTile.size()*2)+1;
    
    congestion = (congestion + momentaryCongestion) / 2;
  }
  
  /** The relative congestion of the given tile.
   * 
   * @return The more Pedestrians that are in this tile, the more congested it is.
   */
  public float getCongestion() {
    if (System.currentTimeMillis() > nextCongestionCalculationTime) {
      recalculateCongestion();
      nextCongestionCalculationTime = System.currentTimeMillis() + ConfigValues.MILLIS_BETWEEN_CONGESTION_CALCULATIONS;
    }
    
    return congestion;
  }
  
  /** Sets the congestion on this tile to zero. */
  public void resetCongestion() {
    congestion = 0;
  }
  
  /** Gets a shallow copy of the Pedestrians registered with this tile. The LinkedList returned can be safely modified without breaking this tile's state.
   * 
   * @return A LinkedList containing all the currently registered Pedestrians for this tile.
   */
  @SuppressWarnings("unchecked")
  public LinkedList<Pedestrian> getRegisteredPedestrians() {
    return ((LinkedList<Pedestrian>)pedestriansInThisTile.clone());
  }
  
  /** Registers a Pedestrian, as claiming that they are currently inside this tile.
   * 
   * @param ped the Pedestrian claiming to be here.
   */
  public void registerPedestrian(Pedestrian ped) {
    if (!pedestriansInThisTile.contains(ped))
      pedestriansInThisTile.add(ped);
  }
  
  /** If the specified Pedestrian is registered in this tile, they will be removed. If they are not currently registered in this tile, there is no effect.
   * 
   * @param ped the Pedestrian requesting to be unregistered.
   */
  public void unregisterPedestrian(Pedestrian ped) {
    pedestriansInThisTile.remove(ped);
  }
  
  /** Removes all Pedestrians from the list of registered Pedestrians. */
  public void clearRegisteredPedestrians() {
    pedestriansInThisTile.clear();
  }
  
}
