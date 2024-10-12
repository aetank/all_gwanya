import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class TrackFire extends Bot {

    boolean movingForward;
    double moveAmount;
    boolean peek;

    public static void main(String[] args) {
        new TrackFire().start();
    }

    TrackFire() {
        super(BotInfo.fromFile("TrackFire.json"));
    }

    @Override
    public void run() {
        // Set colors
        Color pink = Color.fromString("#FF69B4");
        setBodyColor(pink);
        setTurretColor(pink);
        setRadarColor(pink);
        setScanColor(pink);
        setBulletColor(pink);
	// 싸우는 곳 너비 높이
	moveAmount = Math.max(getArenaWidth(), getArenaHeight());
	// Initialize peek to false
        peek = false;

        // turn to face a wall.
        // `getDirection() % 90` means the remainder of getDirection() divided by 90.
        turnRight(getDirection() % 90);
        forward(moveAmount);

        // Turn the gun to turn right 90 degrees.
        peek = true;
        turnGunRight(90);
        turnRight(90);

        // Movement loop
        while (isRunning()) {
	    turnGunLeft(360);  // Continually scan with gun
            // Move forward and rotate gun to scan
            setForward(40000);
            movingForward = true;
            setTurnRight(45);  // 90도 회전
		// 회전이 끝날때까지 대기
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
	    setBack(40000);
            setTurnLeft(90);  // 180도 회전
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
            setTurnRight(180); // Rotate right
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Calculate bearing and adjust gun position
        var bearingFromGun = gunBearingTo(e.getX(), e.getY());
        turnGunLeft(bearingFromGun);

        // Fire if aligned and gun is ready
        //if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
        //    fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
        //}
	fire(1);

        // Rescan if gun is aligned with target
        if (bearingFromGun == 0) {
            rescan();
        }
    }
	// 벽에 부딫힌 경우
    @Override
    public void onHitWall(HitWallEvent e) {
        // Reverse direction on hitting a wall
        reverseDirection();
    }
	// 어딘가에 박았을때 거꾸로 가도록
    public void reverseDirection() {
        if (movingForward) {
            setBack(40000);
            movingForward = false;
        } else {
            setForward(40000);
            movingForward = true;
        }
    }
	// 적과 부딫힌 경우
    @Override
    public void onHitBot(HitBotEvent e) {
        // Reverse direction if rammed into another bot
        if (e.isRammed()) {
            reverseDirection();
        }
    }
    // 봇의 행동 상태를 파악하는?
    public static class TurnCompleteCondition extends Condition {
        private final IBot bot;

        public TurnCompleteCondition(IBot bot) {
            this.bot = bot;
        }

        @Override
        public boolean test() {
            return bot.getTurnRemaining() == 0;
        }
    }
}

