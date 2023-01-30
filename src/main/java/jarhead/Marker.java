package jarhead;

public class Marker extends Node {
    public double displacement;
    public String code = "";

    public Marker(double displacement){
        super(100,100);
        this.displacement = displacement;
    }

}
