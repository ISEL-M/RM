import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class Trajectories {
    private final double d_bw,v,vmin, vmax;

    public Trajectories(double v, double vmin, double vmax){
        this.d_bw = 9.5;
        this.v=v;
        this.vmin=vmin;
        this.vmax=vmax;
    }

    public Map<String, Double> selectTraj(Point p) {
        Map<String, Double> result = Map.of();

        Point p2 = p;
        if(p2.getYf()<0) p2 = p.abs();

        List<Double> cT2 = pointsTraj2(p2);
        List<Double> cT3 = pointsTraj3(p2);
        double dxif = dxif(p2);

        if (p.getXf() < p.getYf())
            return result;

        if ( (45 > p2.getF()) && (p2.getF() > -90)) {
            System.out.println("Trajetoria 2 ou 3");
            if(cT2.get(1) < cT2.get(3)) {
                System.out.println("Trajetoria 2");
                result = traj_2(cT2.get(0), cT2.get(1), cT2.get(2), cT2.get(3), cT2.get(4), p2);
            }
            else if(cT3.get(1) < cT3.get(3)){
                System.out.println("Trajetoria 3");
                result = traj_3(cT3.get(0), cT3.get(1), cT3.get(2), cT3.get(3), cT3.get(4), cT3.get(5));
            }
        } else if(dxif <= p2.getXf()) {
            System.out.println("Trajetoria 1");
            result = traj_1(p2);
        }
        return result;
    }

    public Map<String, Double> traj_1(Point p){
        double F = toRadians(p.getF()),
                a = 2 + 2 * cos(F),
                b = 2 * p.getYf() * (1 - cos(F)) + 2 * p.getXf() * sin(F),
                c = -(pow(p.getXf(),2) + pow(p.getYf(),2));
        double r = sqrt(pow(b,2) - 4 * a * c);
        double r1 = (-b - r) / (2 * a);
        double r2 = (-b + r) / (2 * a);
        r = max(r1,r2);
        r = (double) Math.round(r * 100)/100;
        //System.out.println(r);

        double f = (r + d_bw/2) / (r - d_bw/2);
        f = (double) Math.round(f * 1000)/1000;
        double vLeft = 2/(1+f) * v;
        int vRight = (int)round(vLeft * f);
        //f = vRight/vLeft;todo(test)
        double radius = (d_bw/2) * (f+1) / (f-1);

        double xc1 = 0;
        double yc1 = radius;

        double xc2 = p.getXf() - radius * sin(F);
        xc2 = (double) Math.round(xc2 * 100)/100;

        double yc2 = p.getYf() + radius * cos(F);
        yc2 = (double) Math.round(yc2 * 100)/100;

        double d12 = sqrt(pow(xc1-xc2,2) + pow(yc1-yc2,2));
        d12 = (double) Math.round(d12 * 100)/100;

        double a1 = Math.toDegrees(acos(xc2/d12));
        a1 = (double) Math.round(a1 * 100)/100;
        double a2 = p.getF()-a1;

        return Map.of("Name",1D,"r", radius,"ds", d12, "a1", a1, "a2", a2);
    }
    public Map<String, Double> traj_2(double xc1, double yc1, double xc2, double yc2, double radius, Point p){

        double d12 = sqrt(pow(xc1-xc2,2) + pow(yc1-yc2,2));
        d12 = (double) Math.round(d12 * 100)/100;
        //System.out.println(d12);

        double delta = Math.toDegrees(acos(radius/(d12/2)));

        double a1 = Math.toDegrees(asin(xc2/d12)) - delta;
        a1 = (double) Math.round(a1 * 100)/100;

        double a2 = a1-p.getF();

        double d_straight = d12 * sin(Math.toRadians(delta));
        d_straight = (double) Math.round(d_straight * 100)/100;

        return Map.of("Name",2D,"r", radius, "ds", d_straight, "a1", a1, "a2", a2);
    }
    public Map<String, Double> traj_3(double xc1, double yc1, double xc2, double yc2, double radius, double f){

        double d12 = sqrt(pow(xc1-xc2,2) + pow(yc1-yc2,2));
        d12 = (double) Math.round(d12 * 100)/100;
        //System.out.println(d12);

        double delta = Math.toDegrees(acos(radius/(d12/2))),
                beta = Math.toDegrees(asin(xc2/d12)),
                a1 = 180 - delta - beta;
        a1 = (double) Math.round(a1 * 100)/100;

        double a2 = a1 - f;
        a2 = (double) Math.round(a2 * 100)/100;

        double d_straight = d12 * sin(Math.toRadians(delta));
        d_straight = (double) Math.round(d_straight * 100)/100;

        return Map.of("Name",3D,"r", radius, "ds", d_straight, "a1", a1, "a2", a2);
    }

    public List<Double> pointsTraj2(Point p){
        double F = toRadians(p.getF()),
                a = 2 - 2 * cos(F),
                b = 2 * p.getYf() * (1 + cos(F)) - 2 * p.getXf() * sin(F),
                c = -(pow(p.getXf(),2) + pow(p.getYf(),2));
        /*System.out.println("2a: "+a);
        System.out.println("2b: "+b);
        System.out.println("2c: "+c);*/

        double r;
        if (a<.2 && a>-.2){
            r = (double) Math.round(-c/b * 100) / 100;
        } else {
            r = sqrt(pow(b, 2) - 4 * a * c);
            double r1 = (-b - r) / (2 * a);
            double r2 = (-b + r) / (2 * a);
            r = max(r1, r2);
            r = (double) Math.round(r * 100) / 100;
            //System.out.println(r);
            r = max(r1, r2);
            r = (double) Math.round(r * 100) / 100;
        }
        /*System.out.println("---------------------");
        System.out.println("3r: "+r);*/

        double f = (r + d_bw/2) / (r - d_bw/2);
        f = (double) Math.round(f * 1000)/1000;
        double vLeft = 2/(1+f) * v;
        int vRight = (int)ceil(vLeft * f);
        vLeft= floor(vLeft);
        f = vRight/vLeft;
        double radius = (d_bw/2) * (f+1) / (f-1);
        //radius = pow(p.getXf()+radius*sin(F),2) + pow(radius*(cos(F)+1)-p.getYf(),2);
        //radius = sqrt(radius)/2;
        radius = Math.ceil(radius * 100) /100;

        double xc1 = 0;
        double yc1 = radius;

        double xc2 = p.getXf() + radius * sin(F);
        xc2 = Math.ceil(xc2 * 100) /100;
        //System.out.println(xc2);

        double yc2 = p.getYf() - radius * cos(F);
        yc2 = Math.ceil(yc2 * 100)/100;
        //System.out.println(yc2);

        return List.of(xc1, yc1, xc2, yc2, radius);
    }
    public List<Double> pointsTraj3(Point p){
        double F = Math.abs(toRadians(p.getF())),
                a = 2 - 2 * cos(F),
                b = 2 * p.getYf() * (1 + cos(F)) + 2 * p.getXf() * sin(F),
                c = -(pow(p.getXf(),2) + pow(p.getYf(),2));
        double r;
        if (a<.2 && a>-.2){
            r = (double) Math.round(-c/b * 100) / 100;
        } else {
            r = sqrt(pow(b, 2) - 4 * a * c);
            double r1 = (-b - r) / (2 * a);
            double r2 = (-b + r) / (2 * a);
            r = max(r1, r2);
            r = (double) Math.round(r * 100) / 100;
            //System.out.println(r);
            r = max(r1, r2);
            r = (double) Math.round(r * 100) / 100;
        }
        /*System.out.println("---------------------");
        System.out.println("3r: "+r);*/

        double f = (r + d_bw/2) / (r - d_bw/2);
        f = (double) Math.round(f * 1000)/1000;
        double vLeft = 2/(1+f) * v;
        int vRight = (int)ceil(2 * v -vLeft);
        vLeft= floor(vLeft);
        f = vRight/vLeft;
        double radius = (d_bw/2) * (f+1) / (f-1);
        radius = Math.ceil(radius * 100) /100;


        double xc1 = 0;
        double yc1 = radius;

        double xc2 = p.getXf() - radius * sin(F);
        xc2 = Math.ceil(xc2 * 100) /100;
        //System.out.println(xc2);

        double yc2 = p.getYf() - radius * cos(F);
        yc2 = Math.ceil(yc2 * 100)/100;
        //System.out.println(yc2);

        return List.of(xc1, yc1, xc2, yc2, radius, p.getF());
    }

    public double dxif(Point p){
        double b = p.getYf() - tan(-p.getF()) * p.getXf();
        return abs(b-p.getXf());
    }
}
