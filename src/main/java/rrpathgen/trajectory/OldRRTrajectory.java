package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint;
import rrpathgen.Main;
import rrpathgen.data.Marker;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.data.ProgramProperties;
import rrpathgen.trajectory.trajectorysequence.TrajectorySequence;
import rrpathgen.trajectory.trajectorysequence.TrajectorySequenceBuilder;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.SequenceSegment;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.TrajectorySegment;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.TurnSegment;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.WaitSegment;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class OldRRTrajectory implements Trajectory{
    AffineTransform outLine = new AffineTransform();
    private TrajectorySequence sequence;
    AffineTransform tx = new AffineTransform();

    @Override
    public void generateTrajectory(NodeManager manager, Node exclude, ProgramProperties robot) {
        Node node = exclude.shrink(Main.scale);
        TrajectorySequenceBuilder builder = new TrajectorySequenceBuilder(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading - 90)), Math.toRadians(-node.splineHeading - 90), new MecanumVelocityConstraint(robot.maxVelo, robot.trackWidth), new ProfileAccelerationConstraint(robot.maxAccel), Math.toRadians(robot.maxAngVelo), Math.toRadians(robot.maxAngAccel));
        builder.setReversed(exclude.reversed);
        for (int i = 0; i < manager.size(); i++) {
            if(exclude.equals(manager.get(i))) continue; //stops empty path segment error

            node = manager.get(i).shrink(Main.scale);

            try{

                switch (node.getType()){
                    case splineTo:
                        builder.splineTo(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToSplineHeading:
                        builder.splineToSplineHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToLinearHeading:
                        builder.splineToLinearHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)), Math.toRadians(-node.splineHeading-90));
                        break;
                    case splineToConstantHeading:
                        builder.splineToConstantHeading(new Vector2d(node.x, node.y), Math.toRadians(-node.splineHeading-90));
                        break;
                    case lineTo:
                        builder.lineTo(new Vector2d(node.x, node.y));
                        break;
                    case lineToSplineHeading:
                        builder.lineToSplineHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)));
                        break;
                    case lineToLinearHeading:
                        builder.lineToLinearHeading(new Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)));
                        break;
                    case lineToConstantHeading:
                        builder.lineToConstantHeading(new Vector2d(node.x, node.y));
                        break;
                    case addTemporalMarker:
                        Marker marker = (Marker) manager.get(i);
                        builder.UNSTABLE_addTemporalMarkerOffset(marker.displacement, () -> {});
                }
                builder.setReversed(node.reversed);
            } catch (Exception e) {
                Main.undo(false);
                i--;
                e.printStackTrace();
            }
        }
        if(manager.size() > 1)
            sequence = builder.build();
        else sequence = null;
    }

    @Override
    public List<Pose2d> starts() {
        List<Pose2d> starts = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if (!(segment instanceof WaitSegment))
                starts.add(segment.getStartPose());
        }
        return starts;
    }

    @Override
    public List<Pose2d> ends() {
        List<Pose2d> ends = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if (!(segment instanceof WaitSegment))
                ends.add(segment.getEndPose());

        }
        return ends;
    }

    @Override
    public double duration() {
        return sequence.duration();
    }

    @Override
    public int size() {
        return sequence.size();
    }


    @Override
    public Pose2d get(int i, double j) {
        SequenceSegment segment = sequence.get(i);
        if(segment == null) return null;
        if(segment instanceof TrajectorySegment) {
            TrajectorySegment trajectorySegment = (TrajectorySegment) segment;
            return trajectorySegment.getTrajectory().get(j);
        }
        return null;
    }

    @Override
    public List<Pose2d> midPoints() {
        List<Pose2d> midPoints = new ArrayList<>();
        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if (segment instanceof TrajectorySegment) {
                TrajectorySegment trajectorySegment = (TrajectorySegment) segment;
                midPoints.add(trajectorySegment.getTrajectory().get(trajectorySegment.getDuration()/2));
            }
        }
        return midPoints;
    }

    @Override
    public void renderSplines(Graphics g, double resolution, double scale, Color color) {
        for (int i = 0; i < this.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if(segment == null) continue;

            g.setColor(color);
            g = segment.renderSplines(g, resolution, scale);
        }
    }

    @Override
    public void renderPoints(Graphics g, double scale, double ovalScale, Color color) {
        for (int i = 0; i < this.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if (segment == null) continue;
            g.setColor(color);
            g = segment.renderPoints(g, Main.scale, ovalScale);
        }
    }

    @Override
    public void renderMarkers(Graphics g, double scale, double ovalScale) {

    }

    @Override
    public void renderArrows(Graphics2D g, NodeManager nodeM, Polygon poly, int ovalScale, Color color1, Color color2, Color color3) {
        List<Node> nodes = nodeM.getNodes();
        for (Node node : nodes) {
            tx.setToIdentity();
            tx.translate(node.x, node.y);
            if (!node.reversed)
                tx.rotate(Math.toRadians(-node.robotHeading + 180));
            else
                tx.rotate(Math.toRadians(-node.robotHeading));
            tx.scale(Main.scale, Main.scale);

            g.setTransform(tx);

            g.setColor(color1);
            g.fillOval(-ovalScale, -ovalScale, 2 * ovalScale, 2 * ovalScale);
            switch (node.getType()) {
                case splineTo:
                    g.setColor(color2);
                    break;
                case splineToSplineHeading:
                    g.setColor(color2.brighter());
                    break;
                case splineToLinearHeading:
                    g.setColor(Color.magenta);
                    break;
                default:
                    g.setColor(color3.brighter());
//                    throw new IllegalStateException("Unexpected value: " + node.getType());
                    break;
            }
            g.fill(poly);
        }
    }

    @Override
    public void renderRobot(Graphics2D g, double scale, double ovalScale, Pose2d robotPose, ProgramProperties robot) {

        double rX = robot.robotLength * Main.scale;
        double rY = robot.robotWidth * Main.scale;
        double prevHeading = 0;
        if (sequence.get(0).getDuration() > 0)
            prevHeading = sequence.start().getHeading();
        double res;


        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if(segment == null) continue;
            if (segment instanceof TrajectorySegment) {

                Path path = ((TrajectorySegment) segment).getTrajectory().getPath();
                for (double j = 0; j < path.length();) {
                    Pose2d pose1 = path.get(j);
                    double temp = Math.min((2 * Math.PI) - Math.abs(pose1.getHeading() - prevHeading), Math.abs(pose1.getHeading() - prevHeading));
                    int x1 = (int) (pose1.getX()*Main.scale);
                    int y1 = (int) (pose1.getY()*Main.scale);

                    outLine.setToIdentity();
                    outLine.translate(x1, y1);
                    outLine.rotate(pose1.getHeading());
                    g.setTransform(outLine);
                    g.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) Main.scale * 2, (int) Main.scale * 2);

                    res = robot.resolution / ((robot.resolution) + temp); //* (1-(Math.abs(pose1.getHeading() - prevHeading)));
                    j += res;
                    prevHeading = pose1.getHeading();
                }
                if (path.length() > 0) {
                    Pose2d end = path.end();
                    outLine.setToIdentity();
                    outLine.translate(end.getX()*Main.scale, end.getY()*Main.scale);
                    outLine.rotate(end.getHeading());
                    g.setTransform(outLine);
                    g.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) Main.scale * 2, (int) Main.scale * 2);
                }


            } else if (segment instanceof TurnSegment || segment instanceof WaitSegment) {
                //
                Pose2d pose1 = segment.getStartPose();
                Pose2d end = segment.getEndPose();
                int x1 = (int) pose1.getX();
                int y1 = (int) pose1.getY();

                double h1 = Math.min(end.getHeading(), pose1.getHeading());
                double h2 = Math.max(end.getHeading(), pose1.getHeading());
                for (double j = h1; j < h2; j+= (robot.resolution/10)) {
                    outLine.setToIdentity();
                    outLine.translate(x1, y1);
                    outLine.rotate(j);
                    g.setColor(Color.red);
                    g.setTransform(outLine);
                    g.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) Main.scale * 2, (int) Main.scale * 2);
                }
            }
        }
    }
}
