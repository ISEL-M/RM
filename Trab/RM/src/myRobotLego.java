import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Math.*;

public class myRobotLego{
    /**
     * Weel radious => wr
     * Right Weel => rw
     * Left weel => lw
     * Velocidade => v
     * Perimetro => p
     * Weel Distance => wd
     */
    double wr = 2.73D; //cm
    double p = 2 * 3.14 * wr, wd = 9.4;
    int v = 30, wallDistance = 20;
    boolean on_wall = false;


    private final Trajectories t = new Trajectories(v, 15, 80);
    private final Timer executor = new Timer();
    private final ArrayList<Map<String, Double>> executionList = new ArrayList<>();
    private AtomicBoolean debug = new AtomicBoolean(false),
            step = new AtomicBoolean(false),
            stepStop = new AtomicBoolean(false);
    private AtomicInteger working = new AtomicInteger(0),
            hitWall = new AtomicInteger(0),
            alignWall = new AtomicInteger(0);

    Timer timer_wall;

    InterpretadorEV3 interpretadorEV3 = new InterpretadorEV3();

    public myRobotLego(int speed){
        v = speed;
        startExecutor();
    }
    private void startExecutor(){
        TimerTask toExecute = new TimerTask() {
            @Override
            public void run() {
                Map<String, Double> c;
                if ((!debug.get() || step.get() || stepStop.get()) && working.get() == 0 && executionList.size() > 0 ) {
                    if (stepStop.get()) stepStop.set(false);
                    if (step.get()){
                        if(executionList.size()>1 && executionList.get(1).get("Name")!=4D){
                            step.set(false);
                            stepStop.set(true);
                            executionList.add(1, Map.of("Name", 4D));
                        }
                    }

                    if (hitWall.get() > 0) hitWall.getAndDecrement();
                    if (alignWall.get() > 0) alignWall.getAndDecrement();

                    working.set(1);
                    interpretadorEV3.ResetAll();

                    if (executionList.size()==0) return;
                    c = executionList.remove(0);
                    Double name = c.get("Name");
                    if (name == 0D) {
                        Reta(c.get("r"));
                    } else if (name == 1D) {
                        Back(c.get("r"));
                    } else if (name == 2D) {
                        CurvarDireita(c.get("r"), c.get("a1"));
                    } else if (name == 3D) {
                        CurvarEsquerda(c.get("r"), c.get("a1"));
                    } else if (name == 4D) {
                        Parar(false);
                    }
                    //working.set(0);
                }
            }
        };
        executor.scheduleAtFixedRate(toExecute, 1, 1);
    }

    public void Parar(boolean b) {
        if (b) {
            working.set(-1);
            executionList.clear();
            alignWall.set(0);
            hitWall.set(0);
            if (on_wall)
                timer_wall.cancel();
            on_wall = false;
            interpretadorEV3.Off(InterpretadorEV3.OUT_BC);
            working.set(0);
        } else {
            interpretadorEV3.Off(InterpretadorEV3.OUT_BC);
            working.set(0);
        }
    }
    public void Reta(double distanciaInt) {
        // Variaveis
        double radius = (Math.toDegrees( distanciaInt / wr) );

        //Iniciar Motor
        interpretadorEV3.OnFwd(
                InterpretadorEV3.OUT_C, v,
                InterpretadorEV3.OUT_B, v
        );

        getRotation(radius, radius);
    }
    public void Back(double distanciaInt){
        // Variaveis
        double radius = (Math.toDegrees(distanciaInt/ wr) );

        //Iniciar Motor
        interpretadorEV3.OnRev(
                InterpretadorEV3.OUT_C, v,
                InterpretadorEV3.OUT_B, v
        );

        System.out.println("back " + radius);
        getRotation(radius, radius);
    }
    public void CurvarEsquerda(double raio, double angulo) {
        double lradius = raio - wd/2;
        double rradius = raio + wd/2;
        double ratio = rradius/lradius;

        double lv = (2/(1+ratio)) * v;
        double rv = lv * ratio;

        double lDisctance = Math.toRadians(angulo) * lradius;
        double rDisctance = Math.toRadians(angulo) * rradius;


        double rgraus = (Math.toDegrees(rDisctance / wr));
        double lgraus = (Math.toDegrees(lDisctance / wr));

        //Iniciar Motor
        interpretadorEV3.OnFwd(
                InterpretadorEV3.OUT_C, (int) Math.round(lv),
                InterpretadorEV3.OUT_B, (int) Math.round(rv)
        );

        getRotation(lgraus, rgraus);
    }
    public void CurvarDireita(double raio, double angulo) {
        double lradius = raio + wd/2;
        double rradius = raio - wd/2;

        double ratio = lradius/rradius;
        double rv = (2/(1+ratio)) * v;
        double lv = rv * ratio;

        //double radius = (Math.toDegrees((double) distanciaInt / wr) );

        double rDisctance = Math.toRadians(angulo) * rradius;
        double lDisctance = Math.toRadians(angulo) * lradius;


        double rgraus = (Math.toDegrees(rDisctance / wr) + 0.5D);
        double lgraus = (Math.toDegrees(lDisctance / wr) + 0.5D);

        //Iniciar Motor
        interpretadorEV3.OnFwd(
                InterpretadorEV3.OUT_C, (int) Math.round(lv),
                InterpretadorEV3.OUT_B, (int) Math.round(rv)
        );

        getRotation(lgraus, rgraus);
    }

    ArrayList<Integer> rotC0 = new ArrayList<>();
    ArrayList<Integer> rotC1 = new ArrayList<>();
    Timer timer_rotation = new Timer();
    private void getRotation(double graus0, double graus1)
    {
        TimerTask predict = new TimerTask() {
            @Override
            public void run() {
                rotC0.clear();
                rotC1.clear();
                working.set(0);
            }
        };
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (working.get()<0)
                    return;

                int[] rotations = interpretadorEV3.RotationCount(InterpretadorEV3.OUT_C, InterpretadorEV3.OUT_B);
                rotations[0] = abs(rotations[0]);
                rotations[1] = abs(rotations[1]);
                rotC0.add(rotations[0]);
                rotC1.add(rotations[1]);
                /*System.out.println(rotations[0]);
                System.out.println(rotations[1]);
                System.out.println("---------------------------------");*/



                if (graus0>rotations[0] || graus1>rotations[1])
                    if (rotC0.size()<2)
                        getRotation( graus0, graus1);
                    else{
                        int diff_0 = rotC0.get(rotC0.size()-1) - rotC0.get(rotC0.size()-2);
                        int diff_1 = rotC1.get(rotC1.size()-1) - rotC1.get(rotC1.size()-2);

                        if (graus0<rotations[0]+diff_0 || graus1<rotations[1]+diff_1){
                            timer_rotation.schedule(predict, 50);
                        } else {
                            getRotation( graus0, graus1);
                        }
                    }
                else {
                    rotC0.clear();
                    rotC1.clear();
                    working.set(0);
                }
            }
        };
        timer_rotation.schedule(timerTask, 50);
    }

    public void setWork(String fun, double... par) {
        HashMap<String, Double> map = new HashMap<>();
        switch (fun) {
            case "Reta" -> {
                map.put("Name", 0D);
                map.put("r", par[0]);
            }
            case "Back" -> {
                map.put("Name", 1D);
                map.put("r", par[0]);
            }
            case "Right" -> {
                map.put("Name", 2D);
                map.put("r", par[0]);
                map.put("a1", par[1]);
            }
            case "Left" -> {
                map.put("Name", 3D);
                map.put("r", par[0]);
                map.put("a1", par[1]);
            }
            case "Stop" -> {
                map.put("Name", 4D);
            }
            default -> throw new IllegalStateException("Unexpected value: " + fun);
        }
        executionList.add(map);
    }

    public void trajectories(Point p){
        Map<String, Double> r =  t.selectTraj(p);
        if (r.size()==0) return;
        Double name = r.get("Name");

        if (name == 1D) {
            // esquerda - reta - esquerda
            double curva = 3D;
            if (p.getYf()<0) curva = 2D;

            executionList.add(Map.of("Name", curva,"r", r.get("r"), "a1", r.get("a1")));
            executionList.add(Map.of("Name", 0D,"r", r.get("ds")));
            executionList.add(Map.of("Name", curva,"r", r.get("r"), "a1", r.get("a2")));
            executionList.add(Map.of("Name", 4D));
        } else if (name == 2D || name == 3D) {

            // esquerda - reta - direita
            double curva1 = 3D, curva2 = 2D;
            if (p.getYf()<0) {curva2 = 3D; curva1 = 2D;}

            executionList.add(Map.of("Name", curva1,"r", r.get("r"), "a1", r.get("a1")));
            executionList.add(Map.of("Name", 0D,"r", r.get("ds")));
            executionList.add(Map.of("Name", curva2,"r", r.get("r"), "a1", r.get("a2")));
            executionList.add(Map.of("Name", 4D));
        }
    }

    public void Debug() {
        debug.set(!debug.get());
    }

    public void Step() {
        step.set(true);
    }

    public boolean OpenEV3(String s) {
        return interpretadorEV3.OpenEV3(s);
    }

    public void CloseEV3() {
        interpretadorEV3.CloseEV3();
    }

    public void Wall() {
        if (on_wall) timer_wall.cancel();
        else {
            timer_wall = new Timer();
            startWall();
        }

        on_wall=!on_wall;
    }

    private void startWall(){
        ArrayList<Integer> sensorUS = new ArrayList<>();

        int[] rotations = interpretadorEV3.RotationCount(InterpretadorEV3.OUT_C, InterpretadorEV3.OUT_B);
        rotations[0] = abs(rotations[0]);
        rotations[1] = abs(rotations[1]);
        final double[] last_distance = {Math.toRadians((rotations[0] + rotations[1]) / 2D) * wr};
        TimerTask toExecute = new TimerTask() {
            @Override
            public void run() {

                //Hit wall
                if (hitWall.get() > 0) return;
                int sensorTouch = interpretadorEV3.SensorTouch(InterpretadorEV3.S_1);
                if (sensorTouch==1){
                    hitWallFun();
                    return;
                }

                //Align trajectory
                if (alignWall.get() > 0) return;
                int distance = interpretadorEV3.SensorUS(InterpretadorEV3.S_2);
                sensorUS.add(0,distance);

                if (sensorUS.size()>1){
                    followWallFun(last_distance, sensorUS);
                }

                //keep wallking
                if (executionList.size()<3) {
                    System.out.println("wall front");
                    executionList.add(Map.of("Name", 0D, "r", 20D));
                }
            }
        };
        timer_wall.scheduleAtFixedRate(toExecute, 50,50);
    }

    public void hitWallFun(){
        HashMap<String, Double> map;

        hitWall.set(2);
        executionList.clear();
        System.out.println("hit wall");

        map = new HashMap<>();
        map.put("Name", 1D);
        map.put("r", 2 * wallDistance - 12D);
        executionList.add(0,map);

        map = new HashMap<>();
        map.put("Name", 3D);
        map.put("r", (double) wallDistance);
        map.put("a1", 90D);
        executionList.add(1, map);
    }

    public void followWallFun(double[] last_distance, ArrayList<Integer> sensorUS) {
        int[] rotations = interpretadorEV3.RotationCount(InterpretadorEV3.OUT_C, InterpretadorEV3.OUT_B);
        rotations[0] = abs(rotations[0]);
        rotations[1] = abs(rotations[1]);
        double curr_distance = Math.toRadians((rotations[0] + rotations[1]) / 2D) * wr;
        double d = last_distance[0] - curr_distance;
        last_distance[0] = curr_distance;


        double d_sensor = sensorUS.get(1) - sensorUS.get(0);
        if (d_sensor==0 && sensorUS.get(0) == wallDistance){
            if (sensorUS.get(0) == wallDistance) return;

            alignWall.set(3);
            executionList.clear();
            System.out.println("wall parallel");
            double new_y = -(sensorUS.get(0) - wallDistance);
            double new_x = abs(new_y) > 10 ? abs(new_y) + 1 : 10;
            trajectories(new Point(new_x, new_y, 0));
        } else {
           /*System.out.println("wall not parallel");
            double a = atan(abs(d_sensor)/d);
            double dp1 = sensorUS.get(0) * cos(a), dort = abs(dp1-wallDistance);

            Point p = getWallPoint(dort, a);
            if (p.getXf()!=0 && p.getYf()!=0){
                alignWall.set(3);
                executionList.clear();
                trajectories(p);
            }*/
        }
    }

    public Point getWallPoint(double dort, double a){
        double x2 = dort * sin(a), y2 = dort * cos(a),
                x1 = 0, y1 = dort/sin(90-a),
                Vx = x2 - x1, Vy = y2 - y1,
                m = Vy/Vx, b = y2 - m * x2,
                x = 10, y=0,
                alpha = atan(m);

        while (true){
            y = m * x + b;
            System.out.println(x);
            System.out.println(y);
            System.out.println("----------------");

            if (x > y) break;
            else x++;
            if (x==1000) return new Point(10,5,alpha);
        }

        return new Point(x,y,alpha);
    }
}
