import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

// ------------------------------------------------------------------
// MyFirstBot
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion, and spins the gun around at each end.
// ------------------------------------------------------------------
public class Myaetank extends Bot {
    
    //boolean movingForward;
    // The main method starts our bot
    public static void main(String[] args) {
        new Myaetank().start();
    }

    // Constructor, which loads the bot config file
    Myaetank() {
        super(BotInfo.fromFile("Myaetank.json"));
    }

    // 새 라운드가 시작되면 호출됩니다 -> 초기화 및 약간의 이동을 수행합니다
    @Override
    public void run() {
        // Repeat while the bot is running
        while (isRunning()) {
            forward(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
        }
    }


    // We scanned another bot -> fire!
    @Override
	public void onScannedBot(ScannedBotEvent e) {
		fire(1);
	}

    // We were hit by a bullet -> set turn rate
    @Override
	public void onHitByBullet(HitByBulletEvent e) {
		// Turn to confuse the other bots
		setTurnRate(5);
	}
	
    // We hit a wall -> move in the opposite direction
    @Override
	public void onHitWall(HitWallEvent e) {
		// Move away from the wall by reversing the target speed.
		// Note that current speed is 0 as the bot just hit the wall.
		setTargetSpeed(-1 * getTargetSpeed());
	}
}
