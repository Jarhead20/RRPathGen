package jarhead;

public class Marker extends Node {
    public double displacement;
    public String code;

    public Marker(double displacement){
        this(displacement, "");
    }

    public Marker(double displacement, String code){
        super();
        this.displacement = displacement;
        this.code = code;
    }


}
