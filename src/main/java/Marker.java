import java.awt.*;

public class Marker {
    public int x;
    public int y;

    private Type type = Type.SPLINE;

    public enum Type {
        SPLINE,
        MARKER

    }

    Marker(java.awt.Point p){
        this.x = p.x;
        this.y = p.y;
    }
    Marker(int x, int y){
        this.x = x;
        this.y = y;
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
    public Marker setLocation(Point p){
        this.x = p.x;
        this.y = p.y;
        return this;
    }

}
