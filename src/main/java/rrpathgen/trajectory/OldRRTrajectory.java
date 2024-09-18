package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
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
import rrpathgen.trajectory.trajectorysequence.sequencesegment.WaitSegment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OldRRTrajectory implements Trajectory{

    private TrajectorySequence sequence;

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
    public void renderPoints(Graphics g, double scale, double ovalScale, Polygon poly, Color color) {
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
    public void renderRobot(Graphics g, double scale, double ovalScale, Pose2d robotPose) {

    }


}
