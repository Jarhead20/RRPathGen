import java.awt.*;

public class Marker {
    public double x;
    public double y;
    public double heading;

    private Type type = Type.SPLINE;

    public enum Type {
        SPLINE,
        MARKER

    }
    Marker(){

    }

    Marker(java.awt.Point p){
        this.x = (1.0/Main.getSCALE()*p.x)-72;
        this.y = (1.0/Main.getSCALE()*p.y)-72;

    }
    Marker(double x, double y){
        this.x = x;
        this.y = y;
    }
    Marker(java.awt.Point p, Type t){
        this.x = p.x;
        this.y = p.y;
        this.type = t;
    }
    Marker(double x, double y, Type t){
        this.x = x;
        this.y = y;
        this.type = t;
    }
    public double distance(Marker pt) {
        double px = pt.x - this.x;
        double py = pt.y - this.y;
        return Math.sqrt(px * px + py * py);
    }
    public Marker mid(Marker pt){
        return new Marker((this.x+pt.x)/2,(this.y + pt.y)/2);
    }

    public void setType(Type t){
        this.type = t;
    }
    public Type getType(){
        return this.type;
    }
    public Marker setLocation(Marker p){
        this.x = p.x;
        this.y = p.y;
        return this;
    }

}
