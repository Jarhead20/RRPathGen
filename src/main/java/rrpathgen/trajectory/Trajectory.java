package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;

import java.awt.*;
import java.util.List;

public interface Trajectory {

    void generateTrajectory(NodeManager manager, Node exclude, ProgramProperties robot);
    boolean isReady();

    List<Pose2d> starts();
    List<Pose2d> ends();
    double totalDuration();
    double duration(int i);
    int size();
    Pose2d get(int i, double j);

    List<Pose2d> midPoints();

    void renderSplines(Graphics g, double resolution, double scale, Color color);

    void renderPoints(Graphics g, double scale, double ovalScale, Color color);
    void resetPath();
    void renderMarkers(Graphics g, double scale, double ovalScale, Color color);
    void renderArrows(Graphics2D g, NodeManager nodeM, Polygon poly, int ovalScale, Color color1, Color color2, Color color3);
    void renderRobot(Graphics2D g, double scale, double ovalScale, Pose2d robotPose, ProgramProperties robot);

    Node.Type[] getValidNodeTypes();
    Node.Type[] getValidMarkerTypes();
    String constructExportString();

}
