package rrpathgen.trajectory.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public final class TrajectorySegment extends SequenceSegment {
    private final Trajectory trajectory;

    public TrajectorySegment(Trajectory trajectory) {
        // Note: Markers are already stored in the `Trajectory` itself.
        // This class should not hold any markers
        super(trajectory.duration(), trajectory.start(), trajectory.end(), Collections.emptyList());
        this.trajectory = trajectory;
    }

    public Trajectory getTrajectory() {
        return this.trajectory;
    }

    @Override
    public Graphics renderSplines(Graphics g, double resolution, double scale) {
        Trajectory path = getTrajectory();

        for (double j = 0; j < path.duration(); j+= resolution) {
            Pose2d pose1 = path.get(j-resolution);
            Pose2d pose2 = path.get(j);
            int x1 = (int) (pose1.getX()*scale);
            int y1 = (int) (pose1.getY()*scale);
            int x2 = (int) (pose2.getX()*scale);
            int y2 = (int) (pose2.getY()*scale);
            g.drawLine(x1,y1,x2,y2);
        }
        return g;
    }
}
