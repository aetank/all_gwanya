import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
// import java.awt.Color;

public class Walls extends Bot {

    boolean peek; // Don't turn if there's a bot there
    double moveAmount; // How much to move

    // The main method starts our bot
    public static void main(String[] args) {
        new Walls().start();
    }

    // Constructor, which loads the bot config file
    Walls() {
        super(BotInfo.fromFile("Walls.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Set colors
        setBodyColor("black");
        setTurretColor("black");
        setRadarColor("orange");
        setBulletColor("cyan");
        setScanColor("cyan");

        // Initialize moveAmount to the maximum possible for the arena
        moveAmount = Math.max(getArenaWidth(), getArenaHeight());
        // Initialize peek to false
        peek = false;

        // Turn to face a wall.
        turnRight(getDirection() % 90);
        forward(moveAmount); // Move to the wall

        // Turn the gun to turn right 90 degrees.
        peek = true;
        turnGunRight(90);
        turnRight(90);

        // Main loop
        while (isRunning()) {
            peek = true; // Peek before we turn when forward() completes.
            forward(moveAmount); // Move up the wall
            peek = false; // Don't peek now
            turnRight(90); // Turn to the next wall
            turnGunRight(90);
        }
    }

    // We hit another bot -> move away a bit
    @Override
    public void onHitBot(HitBotEvent e) {
        var bearing = calcAngleTo(e.getX(), e.getY());
        if (bearing > -90 && bearing < 90) {
            back(100); // Back up if the enemy is in front
        } else {
            forward(100); // Move forward if the enemy is behind
        }
    }

    // We scanned another bot -> predict and fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        double enemyX = e.getX();
        double enemyY = e.getY();
        double enemyVelocity = e.getVelocity();
        double enemyHeading = e.getHeading();

        // Calculate the enemy's predicted position
        double timeToHit = calcTimeToHit(enemyX, enemyY);
        double predictedX = enemyX + Math.sin(Math.toRadians(enemyHeading)) * enemyVelocity * timeToHit;
        double predictedY = enemyY + Math.cos(Math.toRadians(enemyHeading)) * enemyVelocity * timeToHit;

        // Fire at the predicted position
        fireAt(predictedX, predictedY);
        
        // Optionally, continue scanning
        if (peek) {
            turnGunRight(360); // Continue scanning
        }
    }

    private double calcTimeToHit(double targetX, double targetY) {
        // Calculate the time it will take for the bullet to reach the target
        double distanceToTarget = calcDistanceTo(targetX, targetY);
        double bulletSpeed = 3; // Adjust bullet speed if necessary
        return distanceToTarget / bulletSpeed; // time = distance / speed
    }

    // Distance calculation
    private double calcDistanceTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.sqrt(dx * dx + dy * dy); // Euclidean distance
    }

    // Angle calculation
    private double calcAngleTo(double x, double y) {
        double dx = x - getX();
        double dy = y - getY();
        return Math.toDegrees(Math.atan2(dy, dx)); // Convert radians to degrees
    }

    // Fire at the predicted position
    private void fireAt(double x, double y) {
        double angleToTarget = calcAngleTo(x, y);
        double bearing = angleToTarget - getGunHeading(); // Get the angle difference
        // Normalize the bearing to be between -180 and 180
        while (bearing > 180) bearing -= 360;
        while (bearing < -180) bearing += 360;

        turnGunRight(bearing); // Adjust the gun's angle
        fire(3); // Fire at max power
    }

}
