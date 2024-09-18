package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import rrpathgen.data.Node;
import rrpathgen.data.NodeManager;
import rrpathgen.trajectory.trajectorysequence.TrajectorySequence;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.SequenceSegment;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.TrajectorySegment;
import rrpathgen.trajectory.trajectorysequence.sequencesegment.WaitSegment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OldRRTrajectory implements Trajectory{

    private TrajectorySequence sequence;

    @Override
    public void generateTrajectory(NodeManager manager, Node exclude) {

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
    public Pose2d get(int i, double j) {
        SequenceSegment segment = sequence.get(i);
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
    public void renderSplines(Graphics g, double resolution, double scale) {

    }

    @Override
    public void renderPoints(Graphics g, double scale, double ovalScale) {

    }

    @Override
    public void renderMarkers(Graphics g, double scale, double ovalScale) {

    }

    @Override
    public void renderRobot(Graphics g, double scale, double ovalScale, Pose2d robotPose) {

    }


}
