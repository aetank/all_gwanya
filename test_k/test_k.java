// import dev.robocode.tankroyale.botapi.*;
// import dev.robocode.tankroyale.botapi.events.*;
// import java.awt.Color;

// public class test_k extends Bot {

//     boolean movingForward = false; // 기본 방향 설정
//     double moveAmount = 20; // 기본 이동량 설정
//     double desiredDistance = 50; // 원하는 거리 설정

//     public static void main(String[] args) {
//         new test_k().start();
//     }

//     test_k() {
//         super(BotInfo.fromFile("test_k.json"));
//     }

//     @Override
//     public void run() {
//         setColors();
//         while (isRunning()) {
//             // 스캔 계속 진행
//             turnGunLeft(360); // 360도로 스캔을 해
//         }
//     }

//     @Override // 상대방이 스캔이 된다면 아래의 이벤트 동작 시작
//     public void onScannedBot(ScannedBotEvent e) {
//         double enemyX = e.getX(); // 적의 X 좌표
//         double enemyY = e.getY(); // 적의 Y 좌표
//         double distanceToEnemy = calcDistanceTo(enemyX, enemyY); // 적까지의 거리

//         // 원하는 거리보다 멀면 다가감
//         if (distanceToEnemy > desiredDistance) {
//             // 적에게 다가가는 각도 계산
//             turnToFaceTarget(enemyX, enemyY);
//             // 회전하며 다가가기
//             moveWithZigzag(enemyX, enemyY);
//         } else {
//             // 적정 거리에 도달하면 총 발사
//             fireAtOptimalDistance(distanceToEnemy);
//         }
//     }

//     // 각도를 계산하는 함수
//     private double calcAngleTo(double x, double y) {
//         double dx = x - getX();
//         double dy = y - getY();
//         return Math.toDegrees(Math.atan2(dy, dx)); // 라디안에서 각도로 변환
//     }

//     // 거리를 계산하는 함수
//     private double calcDistanceTo(double x, double y) {
//         double dx = x - getX();
//         double dy = y - getY();
//         return Math.sqrt(dx * dx + dy * dy); // 유클리디안 거리
//     }

//     private void moveWithZigzag(double x, double y) {
//         double dx = x - getX();
//         double dy = y - getY();
//         double angleToTarget = Math.toDegrees(Math.atan2(dy, dx));
//         double targetAngle = Utils.normalRelativeAngleDegrees(angleToTarget - getDirection());
//         turnRight(targetAngle);
//         for (int i = 0; i < 5; i++) {
//             forward(Math.hypot(dx, dy) / 5);
//             turnRight(20); // 무빙을 위한 각도 조정
//             forward(Math.hypot(dx, dy) / 5);
//             turnLeft(20); // 무빙을 위한 각도 조정
//         }
//     }

//     private void fireAtOptimalDistance(double distance) {
//         if (distance < 200) {
//             fire(3); // 최대 데미지
//         } else if (distance < 400) {
//             fire(2);
//         } else {
//             fire(1);
//         }
//     }

//     @Override
//     public void onHitBot(HitBotEvent e) {
//         // 적과 충돌 시 방향 전환
//         reverseDirection();
//     }

//     @Override
//     public void onHitWall(HitWallEvent e) {
//         // 벽에 부딪히면 방향 전환
//         reverseDirection();
//     }

//     private void reverseDirection() {
//         if (movingForward) {
//             setBack(100);
//             movingForward = false;
//         } else {
//             setForward(100);
//             movingForward = true;
//         }
//     }

//     private void turnToFaceTarget(double x, double y) {
//         double bearing = calcAngleTo(x, y) - getDirection(); // 현재 방향과의 차이 계산
//         turnRight(bearing); // 적에게 바라보도록 회전
//     }

//     private void setColors() {
//         setBodyColor(Color.BLUE);
//         setTurretColor(Color.YELLOW);
//         setRadarColor(Color.GREEN);
//         setBulletColor(Color.RED);
//         setScanColor(Color.CYAN);
//     }
// }

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
//import java.awt.Color;
import java.util.Hashtable;
import java.util.Enumeration;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;

// ------------------------------------------------------------------
// Fury
// ------------------------------------------------------------------
// A sample bot original made for Robocode by Mathew Nelson.
// Ported to Robocode Tank Royale by Flemming N. Larsen.
//
// Probably the first bot you will learn about.
// Moves in a seesaw motion, and spins the gun around at each end.
// ------------------------------------------------------------------
public class test_k extends Bot {
    static Hashtable enemies = new Hashtable();
    static microEnemy target;
    static Point2D.Double nextDestination;
    static Point2D.Double lastPosition;
    static Point2D.Double myPos;
    static double myEnergy;

    public static void main(String[] args) {
        new test_k().start();
    }

    test_k() {
        super(BotInfo.fromFile("test_k.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        setRadarColor(Color.ORANGE);
        setGunColor(Color.YELLOW);
        setBodyColor(Color.RED);
        setAdjustGunForBodyTurn(true);
        setAdjustRadarForGunTurn(true);

        setTurnRadarRight(Double.POSITIVE_INFINITY);

        nextDestination = lastPosition = myPos = new Point2D.Double(getX(), getY());
        target = new microEnemy();

        while (isRunning()) {
            myPos = new Point2D.Double(getX(),getY());
            myEnergy = getEnergy();
            if (target.live && getTurnNumber() > 9 ) {
                doMovementAndGun();
            }
            go();
        }
    }

    //- stuff -----------------------------------------------------------------------------------------------------------------------------------
    public void doMovementAndGun() {

        double distanceToTarget = myPos.distance(target.pos);

        //**** gun ******************//
        // HeadOnTargeting there's nothing I can say about this
        if(getGunTurnRemaining() == 0 && myEnergy > 3) {
            setFire( Math.min(Math.min(myEnergy/6d, 1300d/distanceToTarget), target.energy/3d) );
            System.out.println("target.id: " + target.pos);
        }
        setTurnGunLeft(normalizeRelativeAngle(calcAngle(target.pos, myPos) - getGunDirection()));

        //**** move *****************//
        double distanceToNextDestination = myPos.distance(nextDestination);

        //search a new destination if I reached this one
        if(distanceToNextDestination < 15) {

            // there should be better formulas then this one but it is basically here to increase OneOnOne performance. with more bots
            // addLast will mostly be 1
            double addLast = 1 - Math.rint(Math.pow(Math.random(), getEnemyCount()));

            Rectangle2D.Double battleField = new Rectangle2D.Double(30, 30, getArenaWidth() - 60, getArenaHeight() - 60);
            Point2D.Double testPoint;
            int i=0;

            do {
                //	calculate the testPoint somewhere around the current position. 100 + 200*Math.random() proved to be good if there are
                //	around 10 bots in a 1000x1000 field. but this needs to be limited this to distanceToTarget*0.8. this way the bot wont
                //	run into the target (should mostly be the closest bot)
                testPoint = calcPoint(myPos, Math.min(distanceToTarget*0.8, 100 + 200*Math.random()), 2*Math.PI*Math.random());
                if(battleField.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint;
                }
            } while(i++ < 200);
            System.out.println("nextDestination: " + nextDestination);
            lastPosition = myPos;

        } else {


            double angle = calcAngle(nextDestination, myPos) - getDirection();
            double direction = 1;
            angle = normalizeRelativeAngle(angle);
            setForward(distanceToNextDestination * direction);
            setTurnLeft(angle);
            setMaxSpeed(6d);
        }
    }

    //- eval position ---------------------------------------------------------------------------------------------------------------------------
    public static double evaluate(Point2D.Double p, double addLast) {
        // this is basically here that the bot uses more space on the battlefield. In melee it is dangerous to stay somewhere too long.
        double eval = addLast*0.08/p.distanceSq(lastPosition);

        Enumeration _enum = enemies.elements();
        while (_enum.hasMoreElements()) {
            microEnemy en = (microEnemy)_enum.nextElement();
            // this is the heart of HawkOnFire. So I try to explain what I wanted to do:
            // -	Math.min(en.energy/myEnergy,2) is multiplied because en.energy/myEnergy is an indicator how dangerous an enemy is
            // -	Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p))) is bigger if the moving direction isn't good in relation
            //		to a certain bot. it would be more natural to use Math.abs(Math.cos(calcAngle(p, myPos) - calcAngle(en.pos, myPos)))
            //		but this wasn't going to give me good results
            // -	1 / p.distanceSq(en.pos) is just the normal anti gravity thing
            if(en.live) {
                eval += Math.min(en.energy/myEnergy,2) *
                        (1 + Math.abs(Math.cos(Math.toRadians(calcAngle(myPos, p) - calcAngle(en.pos, p))))) / p.distanceSq(en.pos);
            }
        }
        return eval;
    }

    @Override
    public void onScannedBot(ScannedBotEvent e)
    {
        microEnemy en = (microEnemy)enemies.get(e.getScannedBotId());

        if(en == null){
            en = new microEnemy();
            enemies.put(e.getScannedBotId(), en);
        }

        en.energy = e.getEnergy();
        en.live = true;
        en.pos = new Point2D.Double(e.getX(), e.getY());

        // normal target selection: the one closer to you is the most dangerous so attack him
        if(!target.live || myPos.distance(en.pos) < myPos.distance(target.pos)) {
            target = en;
        }

        // locks the radar if there is only one opponent left
        if(getEnemyCount()==1)	setTurnRadarLeft(getRadarTurnRemaining());
    }

    //- math ------------------------------------------------------------------------------------------------------------------------------------
    private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return new Point2D.Double(p.x + dist*Math.sin(ang), p.y + dist*Math.cos(ang));
    }

    private static double calcAngle(Point2D.Double p2,Point2D.Double p1){
        return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
    }

    //- microEnemy ------------------------------------------------------------------------------------------------------------------------------
    public class microEnemy {
        public Point2D.Double pos;
        public double energy;
        public boolean live;
    }
}