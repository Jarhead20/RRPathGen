package rrpathgen.trajectory;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.Path;
import com.acmerobotics.roadrunner.path.PathSegment;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static rrpathgen.Main.getCurrentManager;

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
    public boolean isReady() {
        return sequence != null;
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
    public double totalDuration() {
        return sequence.duration();
    }

    @Override
    public double duration(int i) {
        return sequence.get(i).getDuration();
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
        if(!isReady()) return midPoints;
        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if(segment == null) continue;
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
            if(segment == null) continue;
            if (segment instanceof TrajectorySegment) {
                TrajectorySegment trajectorySegment = (TrajectorySegment) segment;

                Pose2d mid = trajectorySegment.getTrajectory().get(trajectorySegment.getTrajectory().duration() / 2);

                double x = mid.getX()*scale;
                double y = mid.getY()*scale;
                g.fillOval((int) (x - (ovalScale * scale)), (int) (y - (ovalScale * scale)), (int) (2 * ovalScale * scale), (int) (2 * ovalScale * scale));

            }
        }
    }

    @Override
    public void resetPath() {
        sequence = null;
    }

    @Override
    public void renderMarkers(Graphics g, double scale, double ovalScale, Color color) {
        for (int i = 0; i < this.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if(segment == null) continue;
            if (segment instanceof TrajectorySegment) {
                g.setColor(color);
                TrajectorySegment trajectorySegment = (TrajectorySegment) segment;

                List<TrajectoryMarker> markers = trajectorySegment.getTrajectory().getMarkers();
                markers.forEach(trajectoryMarker -> {
                    Pose2d mid = trajectorySegment.getTrajectory().get(trajectoryMarker.getTime());
                    double x = mid.getX() * scale;
                    double y = mid.getY() * scale;
                    g.fillOval((int) (x - (ovalScale * scale)), (int) (y - (ovalScale * scale)), (int) (2 * ovalScale * scale), (int) (2 * ovalScale * scale));
                });
            }
        }
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
//                    g.setColor(color2);
                    continue;
//                    break;
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

        for (int i = 0; i < sequence.size(); i++) {
            SequenceSegment segment = sequence.get(i);
            if(segment == null) continue;
            if (segment instanceof TrajectorySegment) {

                com.acmerobotics.roadrunner.trajectory.Trajectory trajectory = ((TrajectorySegment) segment).getTrajectory();
                for (double j = 0; j < trajectory.duration(); j+= robot.resolution) {
                    Pose2d pose1 = trajectory.get(j);
                    int x1 = (int) (pose1.getX()*Main.scale);
                    int y1 = (int) (pose1.getY()*Main.scale);

                    outLine.setToIdentity();
                    outLine.translate(x1, y1);
                    outLine.rotate(pose1.getHeading());
                    g.setTransform(outLine);
                    g.fillRoundRect((int) Math.floor(-rX / 2), (int) Math.floor(-rY / 2), (int) Math.floor(rX), (int) Math.floor(rY), (int) Main.scale * 2, (int) Main.scale * 2);

                }
                if (trajectory.duration() > 0) {
                    Pose2d end = trajectory.end();
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

    @Override
    public Node.Type[] getValidNodeTypes() {
        return new Node.Type[]{
                Node.Type.splineTo,
                Node.Type.splineToSplineHeading,
                Node.Type.splineToLinearHeading,
                Node.Type.splineToConstantHeading,
                Node.Type.lineTo,
                Node.Type.lineToSplineHeading,
                Node.Type.lineToLinearHeading,
                Node.Type.lineToConstantHeading,
        };
    }

    @Override
    public Node.Type[] getValidMarkerTypes() {
        return new Node.Type[]{
                Node.Type.addTemporalMarker
        };
    }


    @Override
    public String constructExportString() {
        Node node = getCurrentManager().getNodes().get(0);
        double x = Main.toInches(node.x);
        double y = Main.toInches(node.y);

        StringBuilder sb = new StringBuilder();
        if(Main.exportPanel.addDataType) sb.append("TrajectorySequence ");
        sb.append(String.format("%s = drive.trajectorySequenceBuilder(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n",getCurrentManager().name, x, -y, (node.robotHeading +90)));
        //sort the markers
        List<Marker> markers = getCurrentManager().getMarkers();
        markers.sort(Comparator.comparingDouble(n -> n.displacement));
        for (Marker marker : markers) {
            sb.append(String.format(".UNSTABLE_addTemporalMarkerOffset(%.2f,() -> {%s})%n", marker.displacement, marker.code));
        }
        boolean prev = false;
        for (int i = 0; i < getCurrentManager().size(); i++) {
            node = getCurrentManager().get(i);
            if(node.equals(getCurrentManager().getNodes().get(0))) {
                if(node.reversed != prev){
                    sb.append(String.format(".setReversed(%s)%n", node.reversed));
                    prev = node.reversed;
                }
                continue;
            }
            x = Main.toInches(node.x);
            y = Main.toInches(node.y);


            switch (node.getType()){
                case splineTo:
                    sb.append(String.format(".splineTo(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90)));
                    break;
                case splineToSplineHeading:
                    sb.append(String.format(".splineToSplineHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90)));
                    break;
                case splineToLinearHeading:
                    sb.append(String.format(".splineToLinearHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)), Math.toRadians(%.2f))%n", x, -y, (node.robotHeading +90), (node.splineHeading +90)));
                    break;
                case splineToConstantHeading:
                    sb.append(String.format(".splineToConstantHeading(new Vector2d(%.2f, %.2f), Math.toRadians(%.2f))%n", x, -y, (node.splineHeading +90)));
                    break;
                case lineTo:
                    sb.append(String.format(".lineTo(new Vector2d(%.2f, %.2f))%n", x, -y));
                    break;
                case lineToSplineHeading:
                    sb.append(String.format(".lineToSplineHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.robotHeading +90)));
                    break;
                case lineToLinearHeading:
                    sb.append(String.format(".lineToLinearHeading(new Pose2d(%.2f, %.2f, Math.toRadians(%.2f)))%n", x, -y, (node.robotHeading +90)));
                    break;
                case lineToConstantHeading:
                    sb.append(String.format(".lineToConstantHeading(new Vector2d(%.2f, %.2f))%n", x, -y, (node.splineHeading +90)));
                    break;
                case addTemporalMarker:
                    break;
                default:
                    sb.append("couldn't find type");
                    break;
            }
            if(node.reversed != prev){
                sb.append(String.format(".setReversed(%s)%n", node.reversed));
                prev = node.reversed;
            }
        }
        sb.append(String.format(".build();%n"));
        if(Main.exportPanel.addPoseEstimate) sb.append(String.format("drive.setPoseEstimate(%s.start());", getCurrentManager().name));
        return sb.toString();
    }
}
