import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class Point{
    double xf, yf, f;
    public Point(double xf,double yf,double f){
        this.xf=xf;
        this.yf=yf;
        this.f=f;

    }

    @Override
    public String toString() {
        return "Point{" +
                "xf=" + xf +
                ", yf=" + yf +
                ", f=" + f +
                '}';
    }

    public Point abs(){
        return new Point(xf,Math.abs(yf),f);
    }
}

@Getter
@Setter
public class Variables {
    private String robotName;
    private boolean isOnOff, isDebug;
    private int radius, angle, distance, speed;
    private myRobotLego robot;
    private RobotLegoEV3 robot2;
    private Point trajectory;


    public Variables() {
        robotName = "EV7";
        isOnOff = false;
        isDebug = false;
        angle = 90;
        distance = 20;
        radius = 20;
        speed = 50;
        robot = new myRobotLego(speed);
        trajectory = new Point(70,40,70);
    }

    public void setSpeed(int speed) {
        this.speed = speed;
        robot.v = speed;
    }
}
