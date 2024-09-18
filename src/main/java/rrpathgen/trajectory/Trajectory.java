package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;

import java.awt.*;
import java.util.List;

public interface Trajectory {

    void generateTrajectory(NodeManager manager, Node exclude, ProgramProperties robot);
    public boolean isReady();

    public List<Pose2d> starts();
    public List<Pose2d> ends();
    public double totalDuration();
    public double duration(int i);
    public int size();
    public Pose2d get(int i, double j);

    public List<Pose2d> midPoints();

    void renderSplines(Graphics g, double resolution, double scale, Color color);

    void renderPoints(Graphics g, double scale, double ovalScale, Color color);
    void resetPath();
    public void renderMarkers(Graphics g, double scale, double ovalScale, Color color);
    public void renderArrows(Graphics2D g, NodeManager nodeM, Polygon poly, int ovalScale, Color color1, Color color2, Color color3);
    public void renderRobot(Graphics2D g, double scale, double ovalScale, Pose2d robotPose, ProgramProperties robot);

    public Node.Type[] getValidNodeTypes();
    public Node.Type[] getValidMarkerTypes();

    public String constructExportString();

}
