package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;

import java.awt.*;
import java.util.List;

public interface Trajectory {
    public void generateTrajectory(NodeManager manager, Node exclude);
    public List<Pose2d> starts();
    public List<Pose2d> ends();
    public double duration();

    public Pose2d get(int i, double j);

    public List<Pose2d> midPoints();

    public void renderSplines(Graphics g, double resolution, double scale);
    public void renderPoints(Graphics g, double scale, double ovalScale);
    public void renderMarkers(Graphics g, double scale, double ovalScale);
    public void renderRobot(Graphics g, double scale, double ovalScale, Pose2d robotPose);

}
