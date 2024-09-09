package jarhead

import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import jarhead.roadrunner.DriveShim
import jarhead.roadrunner.Timeline
import jarhead.roadrunner.TrajectoryAction
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class Render {

    var tx = AffineTransform()
    var outLine = AffineTransform()
    var xPoly = intArrayOf(0, -2, 0, 2)
    var yPoly = intArrayOf(0, -4, -3, -4)
    var poly = Polygon(xPoly, yPoly, xPoly.size)
    private val preRenderedSplines: BufferedImage? = null

    fun generateTrajectory(driveShim: DriveShim, exclude: Node, scale: Double, manager: NodeManager): Action? {
        if (manager.size() < 2) return null
        val node: Node = exclude.shrink(scale)
        var builder = driveShim.actionBuilder(Pose2d(node.x, node.y, Math.toRadians(-node.robotHeading-90)))
        builder = builder.setReversed(exclude.reversed);
        //iterate over all the nodes in the node manager
        for (i in 0 until manager.size()) {
            val node = manager[i].shrink(scale)

            if(exclude.equals(manager.get(i))) continue //stops empty path segment error
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

    fun renderRobotPath(g: Graphics2D, timeline: Timeline, color: Color, transparency: Float, width: Int, height: Int) {
        var image: BufferedImage? = null
        if(width != height) println("width and height are not equal")
        if(height > 0)
            image = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
        else
            image = BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)
        val g2 = image.createGraphics()
        g2.color = color
        var rX = Main.properties.robotLength * Main.scale/2
        var rY = Main.properties.robotWidth * Main.scale/2
        val robot = Polygon(
            intArrayOf(-rX.toInt(), -rX.toInt(), rX.toInt(), rX.toInt()),
            intArrayOf(-rY.toInt(), rY.toInt(), rY.toInt(), -rY.toInt()),
            4
        )
        for ((t0, action) in timeline) {
            when (action) {
                is TrajectoryAction -> {
                    val displacementSamples = (action.t.path.length() / 0.1).roundToInt()
                    val displacements = (0..displacementSamples).map {
                        it / displacementSamples.toDouble() * action.t.path.length()
                    }
                    val poses = displacements.map { action.t.path[it, 1].value() }
                    for (pose in poses) {
                        val coord = pose.position
                        tx.setToIdentity()
                        val heading = pose.heading.log()
                        println(-heading)
                        tx.translate(coord.x*Main.scale, coord.y*Main.scale)
                        tx.rotate(heading)
//                        tx.scale(Main.scale, Main.scale)
                        g2.transform = tx
                        g2.fill(robot)
                    }
                }
            }
        }
        g.drawImage(image, 0, 0, null)
    }

    fun renderSplines(g: Graphics2D, timeline: Timeline, color: Color, ovalScale: Int, scale: Double) {
        val trajectoryDrawnPath = Path2D.Double()
        var first = true
        g.color = color
        for ((t0, action) in timeline) {
            when (action) {
                is TrajectoryAction -> {
                    val displacementSamples = (action.t.path.length() / 1).roundToInt()

                    val displacements = (0..displacementSamples).map {
                        it / displacementSamples.toDouble() * action.t.path.length()
                    }

                    val poses = displacements.map { action.t.path[it, 1].value() }
//                    get the first pose
                    val coord = poses[0].position
                    trajectoryDrawnPath.moveTo(coord.x*scale, coord.y*scale)

//                    get the mid point
                    val midPoint = poses[displacementSamples/2].position
                    g.drawOval(
                        (midPoint.x*scale).toInt() - ovalScale / 2,
                        (midPoint.y*scale).toInt() - ovalScale / 2,
                        ovalScale, ovalScale)

                    for (pose in poses.drop(1)) {
                        val coord = pose.position
                        trajectoryDrawnPath.lineTo(coord.x*scale, coord.y*scale)
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

//    fun renderArrows(g: Graphics2D, manager: NodeManager, color1: Color, color2: Color, color3: Color, ovalScale: Int, scale: Double) {
//        g.color = color1
//        for (i in 0 until manager.size()) {
//            val node = manager[i]
//            g.drawOval(
//                (node.x * scale).toInt() - ovalScale / 2,
//                (node.y * scale).toInt() - ovalScale / 2,
//                ovalScale,
//                ovalScale
//            )
//        }
//        val bufferedImage =
//            BufferedImage(preRenderedSplines.getWidth(), preRenderedSplines.getHeight(), BufferedImage.TYPE_4BYTE_ABGR)
//        val g2 = bufferedImage.createGraphics()
//        val nodes: List<Node> = nodeM.getNodes()
//        for (node in nodes) {
//            tx.setToIdentity()
//            tx.translate(node.x, node.y)
//            if (!node.reversed) tx.rotate(Math.toRadians(-node.robotHeading + 180)) else tx.rotate(Math.toRadians(-node.robotHeading))
//            tx.scale(Main.scale, Main.scale)
//            g2.transform = tx
//            g2.color = color1
//            g2.fillOval(-ovalScale, -ovalScale, 2 * ovalScale, 2 * ovalScale)
//            when (node.type) {
//                Node.Type.splineTo -> g2.color = color2
//                Node.Type.splineToSplineHeading -> g2.color = color2.brighter()
//                Node.Type.splineToLinearHeading -> g2.color = Color.magenta
//                Node.Type.splineToConstantHeading -> g2.color = color3.brighter()
//                else -> g2.color = color3.brighter()
//            }
//            g2.fill(poly)
//        }
//        g2d.drawImage(bufferedImage, 0, 0, null)
//    }


}