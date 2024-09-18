package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;

import java.util.List;

public interface Trajectory {
    public List<Pose2d> starts();
    public List<Pose2d> ends();
    public double duration();

    Pose2d get(int i, double j);

    public List<Pose2d> midPoints();
}
