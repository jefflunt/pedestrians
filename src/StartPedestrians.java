import org.newdawn.slick.*;

import com.jefflunt.pedestrians.PedestrianSim;

/** Starts the Movers applications.
 */
public class StartPedestrians {
  
  public static void main(String[] args) {
    try {
      PedestrianSim pedSim = new PedestrianSim("Pedestrians");
      AppGameContainer container = new AppGameContainer(pedSim);
      container.setDisplayMode(1280, 800, true);
      
      container.setClearEachFrame(true);
      
      pedSim.init(container);
      container.start();
    } catch (SlickException e) {
      e.printStackTrace();
    }
  }
  
}
