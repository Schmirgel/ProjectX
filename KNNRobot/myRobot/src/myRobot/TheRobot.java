package myRobot;


import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.encog.neural.networks.BasicNetwork;

import robocode.*;
import robocode.util.Utils;

public class TheRobot extends AdvancedRobot {

	List<Long> LIST = new ArrayList<Long>();
	private static BasicNetwork NETWORK = null;
	double FIREPOWER = 1.0;
	double VELOCITY = 20 - (3 * FIREPOWER);

/*	public static void main(String[] args) {
	
		NETWORK = encog.Adjust.trainNetwork();
		
		double[] wert= {1};
		
		System.out.println(encog.Adjust.predict(NETWORK, wert));
		
	}
*/	
	public void run() {
	    // ...



	
		NETWORK = encog.Adjust.trainNetwork();
		setAdjustGunForRobotTurn(true);
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		
	    do {
	        // ...
	        // Turn the radar if we have no more turn, starts it if it stops and at the start of round
//	        if ( getRadarHeading() == 0.0 )
//	    	turnGunLeft(360);
	    	turnRadarLeft(360);
//	        	setTurnRadarRightRadians( 360 );
	
	    } while ( true );

	}
	 
	public void onScannedRobot(ScannedRobotEvent e) {
	    // ...
		//if(getRadarHeadingRadians() - getGunHeading() == 0)
//			fire(1);
	    // Absolute angle towards target
	    double angleToEnemy = getHeadingRadians() + e.getBearingRadians();
	 
	    // Subtract current radar heading to get the turn required to face the enemy, be sure it is normalized
	    double radarTurn = Utils.normalRelativeAngle( angleToEnemy - getRadarHeadingRadians() );
	 
	    // Distance we want to scan from middle of enemy to either side
	    // The 36.0 is how many units from the center of the enemy robot it scans.
	    double extraTurn = Math.min( Math.atan( 5.0 / e.getDistance() ), Rules.RADAR_TURN_RATE_RADIANS );
	 
	    // Adjust the radar turn so it goes that much further in the direction it is going to turn
	    // Basically if we were going to turn it left, turn it even more left, if right, turn more right.
	    // This allows us to overshoot our enemy so that we get a good sweep that will not slip.
	    radarTurn += (radarTurn < 0 ? -extraTurn : extraTurn);

	    //Turn the radar
	    setTurnRadarRightRadians(radarTurn);
	    
	    //is the enemy left or right of us
	    double distance = getRadarHeading() - getGunHeading();
	    double[] wert = {distance};

    	// setTurnGunLeft(encog.Adjust.predict(NETWORK, wert)); 
    	
	    if(distance < 0)
	    setTurnGunRight(distance);
    
	    if (distance >= 0)
	    	setTurnGunRight(distance);

	 	//System.out.println(distance);
		  
	    //if enemy is within fire range -> fire
//	    if (getGunHeading() > getRadarHeading()-2 && getGunHeading() < getRadarHeading()+2) 
    	
	    setFire(1);
	    
	    System.out.println("gunheat:" + getGunHeat());
	    if(getGunHeat() == 1.2){
		    LIST.add(getTime());
		    System.out.println("distance: " + LIST);
//	    	int shouldHit = (int) (getTime() + (e.getDistance() / 17));
//    		System.out.println("when should it hit: " + shouldHit);
	    }

	    if(LIST.size() != 0){
		    if(LIST.get(0) + (getTime() - LIST.get(0)) * VELOCITY > e.getDistance()){
		    	System.out.println("did you hit?: " + getTime());
		    	LIST.remove(0);
		    }
	    }
	    	
	    execute();
	    
	}
	
	public void onBulletHit(BulletHitEvent e){
	
		System.out.println("you hit *hurray*: " + getTime());
		
	}
	
	private void csv(){
		PrintStream w = null;
		try {
			w = new PrintStream(new RobocodeFileOutputStream(getDataFile("test.csv")));

			w.println(1);
			w.println(2);

			// PrintStreams don't throw IOExceptions during prints, they simply set a flag.... so check it here.
			if (w.checkError()) {
				out.println("I could not write!");
			}
		} catch (IOException e) {
			out.println("IOException trying to write: ");
			e.printStackTrace(out);
		} finally {
			if (w != null) {
				w.close();
			}
		}
	}
	
}