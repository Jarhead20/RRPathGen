package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;

import java.awt.*;
import java.util.List;

public interface Trajectory {

    void generateTrajectory(NodeManager manager, Node exclude, ProgramProperties robot);

    public List<Pose2d> starts();
    public List<Pose2d> ends();
    public double duration();
    public int size();
    public Pose2d get(int i, double j);

    public List<Pose2d> midPoints();

    void renderSplines(Graphics g, double resolution, double scale, Color color);

    void renderPoints(Graphics g, double scale, double ovalScale, Polygon poly, Color color);

    public void renderMarkers(Graphics g, double scale, double ovalScale);
    public void renderRobot(Graphics2D g, double scale, double ovalScale, Pose2d robotPose, ProgramProperties robot);

}
