package jarhead

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import jarhead.roadrunner.DriveShim
import jarhead.roadrunner.Timeline
import jarhead.roadrunner.TrajectoryAction
import jarhead.roadrunner.TurnAction
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Path2D
import kotlin.math.roundToInt

class Render {
    fun generateTrajectory(driveShim: DriveShim, exclude: Node, scale: Double, manager: NodeManager): Action? {
        if (manager.size() < 2) return null
        val node: Node = exclude.shrink(scale)
        var builder = driveShim.actionBuilder(Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)))
        builder = builder.setReversed(exclude.reversed);
        //iterate over all the nodes in the node manager
        for (i in 0 until manager.size()) {
            val node = manager[i].shrink(scale)

            if(exclude.equals(manager.get(i))) continue //stops empty path segment error
            println(node)
            try {
                when (node.type) {
                    Node.Type.splineTo -> builder = builder.splineTo(
                        Vector2d(node.x, node.y),
                        Math.toRadians(-node.splineHeading - 90)
                    )

                    Node.Type.splineToSplineHeading -> builder = builder.splineToSplineHeading(
                        Pose2d(
                            node.x,
                            node.y,
                            Math.toRadians(-node.robotHeading - 90)
                        ), Math.toRadians(-node.splineHeading - 90)
                    )

                    Node.Type.splineToLinearHeading -> builder = builder.splineToLinearHeading(
                        Pose2d(
                            node.x,
                            node.y,
                            Math.toRadians(-node.robotHeading - 90)
                        ), Math.toRadians(-node.splineHeading - 90)
                    )

                    Node.Type.splineToConstantHeading -> builder = builder.splineToConstantHeading(
                        Vector2d(node.x, node.y),
                        Math.toRadians(-node.splineHeading - 90)
                    )
                }
                builder = builder.setReversed(node.reversed)
            } catch (e: Exception) {
                Main.undo(false)
                i.dec()
                e.printStackTrace()
            }
        }
        return if (manager.size() > 1) builder.build() else null
    }

    fun renderSplines(g: Graphics2D, timeline: Timeline, color: Color, ovalScale: Int, scale: Double) {
        val trajectoryDrawnPath = Path2D.Double()
        var first = true
        g.color = color
        for ((t0, action) in timeline) {
            println(action.javaClass.simpleName)
            when (action) {
                is TrajectoryAction -> {
                    println("trajectoryaction")
                    val displacementSamples = (action.t.path.length() / 1).roundToInt()

                    val displacements = (0..displacementSamples).map {
                        it / displacementSamples.toDouble() * action.t.path.length()
                    }

                    val poses = displacements.map { action.t.path[it, 1].value() }

                    for (pose in poses.drop(1)) {
                        val coord = pose.position
                        if (first) {
                            trajectoryDrawnPath.moveTo(coord.x*scale, coord.y*scale)
                            first = false
                            println(coord.x*scale)
                        } else {
                            trajectoryDrawnPath.lineTo(coord.x*scale, coord.y*scale)
                            println(coord.x*scale)
                        }
                    }
                }
//                is TurnAction -> {
//                    val turnEntity = TurnIndicatorEntity(
//                        meepMeep, colorScheme, action.t.beginPose.position,
//                        action.t.beginPose.heading,
//                        action.t.angle,
//                    )
//                    turnEntityList.add(turnEntity)
//                    meepMeep.requestToAddEntity(turnEntity)
//                }
            }
        }
        g.draw(trajectoryDrawnPath)
    }
}