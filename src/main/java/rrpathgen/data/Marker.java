package rrpathgen.data;

public class Marker extends Node {
    public double displacement;
    public String code;

    public Marker(double displacement){
        this(displacement, "", Type.addTemporalMarker);
        super.isMidpoint = true;
    }

    public Marker(double displacement, String code, Type type){
        super();
        super.isMidpoint = true;
        this.setType(type);
        this.displacement = displacement;
        this.code = code;
    }
}
