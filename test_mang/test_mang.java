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
// Robocode용으로 Mathew Nelson이 처음 만든 샘플 봇.
// Flemming N. Larsen에 의해 Robocode Tank Royale로 포팅됨.
//
// 아마도 당신이 처음 배우게 될 봇일 것입니다.
// 시소 움직임을 하고, 각 끝에서 포탑을 회전시킵니다.
// ------------------------------------------------------------------
public class test_mang extends Bot {
    static Hashtable enemies = new Hashtable(); // 적을 저장하는 해시테이블
    static microEnemy target; // 현재 타겟이 되는 적
    static Point2D.Double nextDestination; // 다음 목적지
    static Point2D.Double lastPosition; // 마지막 위치
    static Point2D.Double myPos; // 현재 위치
    static double myEnergy; // 현재 에너지

    public static void main(String[] args) {
        new test_mang().start(); // 봇 실행
    }

    test_mang() {
        super(BotInfo.fromFile("test_mang.json")); // 봇 설정 파일 읽기
    }

    // 새로운 라운드가 시작되었을 때 호출됨 -> 초기화하고 움직임을 시작
    @Override
    public void run() {
        setRadarColor(Color.ORANGE); // 레이더 색상 설정 (주황색)
        setGunColor(Color.YELLOW); // 총 색상 설정 (노란색)
        setBodyColor(Color.RED); // 몸체 색상 설정 (빨간색)
        setAdjustGunForBodyTurn(true); // 몸체 회전 시 총이 따로 움직이도록 설정
        setAdjustRadarForGunTurn(true); // 총 회전 시 레이더가 따로 움직이도록 설정

        setTurnRadarRight(Double.POSITIVE_INFINITY); // 레이더를 무한히 오른쪽으로 회전시킴

        nextDestination = lastPosition = myPos = new Point2D.Double(getX(), getY()); // 위치 초기화
        target = new microEnemy(); // 타겟 초기화

        while (isRunning()) {
            myPos = new Point2D.Double(getX(), getY()); // 현재 위치 갱신
            myEnergy = getEnergy(); // 에너지 갱신
            if (target.live && getTurnNumber() > 9 ) { // 타겟이 살아있고 라운드가 9턴 이상 진행되었다면
                doMovementAndGun(); // 이동 및 공격 수행
            }
            go(); // 봇을 실행
        }
    }

    //- 동작 및 총 -----------------------------------------------------------------------------------------------------------------------------------
    public void doMovementAndGun() {

        double distanceToTarget = myPos.distance(target.pos); // 타겟까지의 거리 계산

        //**** 총 관련 ******************//
        // HeadOnTargeting(헤드온 타겟팅) - 설명할 필요가 없습니다.
        if (getGunTurnRemaining() == 0 && myEnergy > 3) { // 총이 다 회전했고 에너지가 3 이상일 때 발사
            setFire( Math.min(Math.min(myEnergy/6d, 1300d/distanceToTarget), target.energy/3d) ); // 발사 에너지 계산 후 발사
            System.out.println("target.id: " + target.pos); // 타겟 위치 출력
        }
        setTurnGunLeft(normalizeRelativeAngle(calcAngle(target.pos, myPos) - getGunDirection())); // 총을 타겟으로 회전

        //**** 이동 관련 *****************//
        double distanceToNextDestination = myPos.distance(nextDestination); // 다음 목적지까지의 거리 계산

        // 현재 목적지에 도달했으면 새로운 목적지를 검색
        if (distanceToNextDestination < 15) {

            // 더 나은 공식이 있을 수 있지만, 이는 기본적으로 일대일 성능을 높이기 위해 사용됩니다. 더 많은 봇이 있으면 addLast는 대개 1이 됩니다.
            double addLast = 1 - Math.rint(Math.pow(Math.random(), getEnemyCount())); // 적 수에 따른 값 설정

            Rectangle2D.Double battleField = new Rectangle2D.Double(30, 30, getArenaWidth() - 60, getArenaHeight() - 60); // 전장 설정
            Point2D.Double testPoint; // 테스트할 지점
            int i = 0;

            do {
                // 현재 위치 주변의 테스트 지점을 계산합니다. 100 + 200*Math.random()은 1000x1000 필드에서 약 10개의 봇이 있을 때 적합하다고 증명되었습니다.
                // 하지만 이것을 distanceToTarget*0.8로 제한해야 합니다. 이를 통해 봇이 타겟으로 달려들지 않게 됩니다 (대개 가장 가까운 봇).
                testPoint = calcPoint(myPos, Math.min(distanceToTarget * 0.8, 100 + 200 * Math.random()), 2 * Math.PI * Math.random()); 
                if (battleField.contains(testPoint) && evaluate(testPoint, addLast) < evaluate(nextDestination, addLast)) {
                    nextDestination = testPoint; // 더 나은 목적지를 찾으면 설정
                }
            } while (i++ < 200);
            System.out.println("nextDestination: " + nextDestination); // 새로운 목적지 출력
            lastPosition = myPos; // 마지막 위치 업데이트

        } else {

            double angle = calcAngle(nextDestination, myPos) - getDirection(); // 목적지로 향하는 각도 계산
            double direction = 1; // 이동 방향 설정
            angle = normalizeRelativeAngle(angle); // 각도를 정규화
            setForward(distanceToNextDestination * direction); // 앞으로 이동
            setTurnLeft(angle); // 좌회전하여 목적지로 향함
            setMaxSpeed(6d); // 최대 속도 설정
        }
    }

    //- 위치 평가 ---------------------------------------------------------------------------------------------------------------------------
    public static double evaluate(Point2D.Double p, double addLast) {
        // 기본적으로 봇이 전장에서 더 많은 공간을 사용하게 합니다. 전투에서 한 위치에 오래 머무는 것은 위험합니다.
        double eval = addLast * 0.08 / p.distanceSq(lastPosition); // 마지막 위치와의 거리에 따른 평가 점수 계산

        Enumeration _enum = enemies.elements(); // 적 목록 반복
        while (_enum.hasMoreElements()) {
            microEnemy en = (microEnemy)_enum.nextElement();
            // HawkOnFire의 핵심 부분입니다. 제가 하려던 것을 설명하겠습니다:
            // -	Math.min(en.energy/myEnergy, 2)는 적의 에너지가 내 에너지보다 높을수록 더 위험하다는 지표입니다.
            // -	Math.abs(Math.cos(calcAngle(myPos, p) - calcAngle(en.pos, p)))는 특정 적에 대해 이동 방향이 좋지 않으면 값이 커집니다.
            //		보다 자연스럽게 하려면 Math.abs(Math.cos(calcAngle(p, myPos) - calcAngle(en.pos, myPos)))를 사용하는 것이 좋지만
            //		좋은 결과를 주지 않았습니다.
            // -	1 / p.distanceSq(en.pos)는 기본적인 반중력 메커니즘입니다.
            if (en.live) {
                eval += Math.min(en.energy/myEnergy, 2) *
                        (1 + Math.abs(Math.cos(Math.toRadians(calcAngle(myPos, p) - calcAngle(en.pos, p))))) / p.distanceSq(en.pos);
            }
        }
        return eval; // 평가 값 반환
    }

    // 적이 스캔되었을 때 호출되는 이벤트 핸들러
    @Override
    public void onScannedBot(ScannedBotEvent e)
    {
        microEnemy en = (microEnemy)enemies.get(e.getScannedBotId()); // 스캔된 적 정보 가져옴

        if (en == null) {
            en = new microEnemy(); // 적이 없으면 새로 생성
            enemies.put(e.getScannedBotId(), en); // 적 정보를 해시테이블에 추가
        }

        en.energy = e.getEnergy(); // 적의 에너지 업데이트
        en.live = true; // 적이 살아있음을 표시
        en.pos = new Point2D.Double(e.getX(), e.getY()); // 적의 위치 업데이트

        // 타겟 선택: 가장 가까운 적을 타겟으로 설정
        if (!target.live || myPos.distance(en.pos) < myPos.distance(target.pos)) {
            target = en; // 타겟 변경
        }

        // 적이 1명만 남았다면 레이더 고정
        if (getEnemyCount() == 1) {
            setTurnRadarLeft(getRadarTurnRemaining());
        }
    }

    //- 수학 관련 함수 ------------------------------------------------------------------------------------------------------------------------------------
    private static Point2D.Double calcPoint(Point2D.Double p, double dist, double ang) {
        return new Point2D.Double(p.x + dist * Math.sin(ang), p.y + dist * Math.cos(ang)); // 주어진 각도와 거리로 새로운 좌표 계산
    }

    private static double calcAngle(Point2D.Double p2, Point2D.Double p1) {
        return Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x)); // 두 좌표 사이의 각도 계산
    }

    //- microEnemy 클래스 ------------------------------------------------------------------------------------------------------------------------------
    public class microEnemy {
        public Point2D.Double pos; // 적의 위치
        public double energy; // 적의 에너지
        public boolean live; // 적의 생존 여부
    }
}