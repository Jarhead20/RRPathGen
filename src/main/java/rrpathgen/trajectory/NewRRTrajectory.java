package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;

import java.awt.*;
import java.util.List;

public class NewRRTrajectory implements Trajectory{
    @Override
    public void generateTrajectory(NodeManager manager, Node exclude, ProgramProperties robot) {

    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public List<Pose2d> starts() {
        return null;
    }

    @Override
    public List<Pose2d> ends() {
        return null;
    }

    @Override
    public double totalDuration() {
        return 0;
    }

    @Override
    public double duration(int i) {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public Pose2d get(int i, double j) {
        return null;
    }

    @Override
    public List<Pose2d> midPoints() {
        return null;
    }

    @Override
    public void renderSplines(Graphics g, double resolution, double scale, Color color) {

    }

    @Override
    public void renderPoints(Graphics g, double scale, double ovalScale, Color color) {

    }

    @Override
    public void resetPath() {

    }

    @Override
    public void renderMarkers(Graphics g, double scale, double ovalScale) {

    }

    @Override
    public void renderArrows(Graphics2D g, NodeManager nodeM, Polygon poly, int ovalScale, Color color1, Color color2, Color color3) {

    }

    @Override
    public void renderRobot(Graphics2D g, double scale, double ovalScale, Pose2d robotPose, ProgramProperties robot) {

    }

    @Override
    public Node.Type[] getValidTypes() {
        return new Node.Type[0];
    }
}
