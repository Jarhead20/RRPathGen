package rrpathgen.trajectory.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;

import java.awt.*;
import java.util.List;

public abstract class SequenceSegment {
    private final double duration;
    private final Pose2d startPose;
    private final Pose2d endPose;
    private final List<TrajectoryMarker> markers;

    protected SequenceSegment(
            double duration,
            Pose2d startPose, Pose2d endPose,
            List<TrajectoryMarker> markers
    ) {
        this.duration = duration;
        this.startPose = startPose;
        this.endPose = endPose;
        this.markers = markers;
    }

    public double getDuration() {
        return this.duration;
    }

    public Pose2d getStartPose() {
        return startPose;
    }

    public Pose2d getEndPose() {
        return endPose;
    }

    public List<TrajectoryMarker> getMarkers() {
        return markers;
    }

    public Graphics renderSplines(Graphics g, double resolution, double scale) {
        // TrajectorySegment overrides this
        Pose2d startPose = getStartPose();
        Pose2d endPose = getEndPose();
        g.drawLine((int)startPose.getX(), (int)startPose.getY(), (int)endPose.getX(), (int)endPose.getY());

        return g;
    }

    public Graphics renderPoints(Graphics g, double scale, double ovalScale) {
        // scale and ovalScale are needed since TrajectorySegment overrides this method
//        Pose2d startPose = getStartPose();
//        Pose2d endPose = getEndPose();
//        g.drawLine((int) startPose.getX(), (int) startPose.getY(), (int) endPose.getX(), (int) endPose.getY());
        return g;
    }
}
