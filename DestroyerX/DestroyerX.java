import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class DestroyerX extends Bot {

    boolean movingForward;
    double moveAmount;
    // boolean peek;

    public static void main(String[] args) {
        new DestroyerX().start();
    }

    DestroyerX() {
        super(BotInfo.fromFile("DestroyerX.json"));
    }

    @Override
    public void run() {
        // 탱크 색깔 지정
        Color white = Color.fromString("#f8f6f6");
        Color red = Color.fromString("#db0404");
        Color blue = Color.fromString("#1e73be");
        Color yellow = Color.fromString("#fbe02c");
        setBodyColor(white);
        setTurretColor(red); 
        setRadarColor(blue); 
        setScanColor(red);
        setBulletColor(yellow);
	    // // 싸우는 곳 너비 높이
	    // moveAmount = Math.max(getArenaWidth(), getArenaHeight());
	    // // Initialize peek to false
        // peek = false;

        // // turn to face a wall.
        // // `getDirection() % 90` means the remainder of getDirection() divided by 90.
        // turnRight(getDirection() % 90);
        // forward(moveAmount);

        // // Turn the gun to turn right 90 degrees.
        // peek = true;
        // turnGunRight(90);
        // turnRight(90);

        // Movement loop
        while (isRunning()) {
            setTurnGunLeft(360);  // Continually scan with gun
            // Move forward and rotate gun to scan
            setForward(40000);
            movingForward = true;
            setTurnRight(45);  // 90도 회전
		    // 회전이 끝날때까지 대기
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
            setTurnLeft(180);  // 180도 회전
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
            setTurnRight(180); // Rotate right
            waitFor(new TurnCompleteCondition(this));  // Wait until turn is done
        }
    }

    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // double distance = Math.hypot(e.getX() - getX(), e.getY() - getY());
        // Calculate bearing and adjust gun position
        var bearingFromGun = gunBearingTo(e.getX(), e.getY());
        turnGunLeft(bearingFromGun);

        // double firePower;
        // if (distance < 50) {
        //     firePower = 3;  // 아주 가까운 거리: 최대 데미지
        // } else if (distance < 150) {
        //     firePower = 2.5;  // 가까운 거리: 높은 데미지
        // } else if (distance < 300) {
        //     firePower = 2;  // 중간 거리: 중간 데미지
        // } else if (distance < 500) {
        //     firePower = 1.5;  // 조금 먼 거리: 약간 낮은 데미지
        // } else {
        //     firePower = 1;  // 매우 먼 거리: 최소 데미지
        // }

        // Fire if aligned and gun is ready
        if (Math.abs(bearingFromGun) <= 3 && getGunHeat() == 0) {
           fire(Math.min(3 - Math.abs(bearingFromGun), getEnergy() - .1));
            // fire(firePower);
        }
	    // fire(1);

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
