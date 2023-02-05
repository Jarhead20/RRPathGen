package jarhead

import com.acmerobotics.dashboard.canvas.Canvas
import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.*
import java.lang.Math.PI

class Builder {
    fun videoBuilder(beginPose: Pose2d = Pose2d(0.0, 0.0, 0.0)) =
        TrajectoryActionBuilder(
            { TurnAction(it) },
            { TrajectoryAction(it) },
            beginPose,
            1e-6,
            TurnConstraints(PI / 2, -PI / 2, PI / 2),
            MinVelConstraint(
                listOf(
                    TranslationalVelConstraint(50.0),
                    AngularVelConstraint(PI / 2),
                )
            ),
            ProfileAccelConstraint(-40.0, 40.0),
            0.25,
        )

    class TrajectoryAction(val t: TimeTrajectory) : Action {



        override fun run(p: TelemetryPacket): Boolean {
            TODO("Not yet implemented")
        }

        override fun preview(fieldOverlay: Canvas) {
            TODO("Not yet implemented")
        }

        override fun toString() = "Trajectory"
    }

    class TurnAction(val t: TimeTurn) : Action {
        override fun run(p: TelemetryPacket): Boolean {
            TODO("Not yet implemented")
        }

        override fun preview(fieldOverlay: Canvas) {
            TODO("Not yet implemented")
        }

        override fun toString() = "Turn"
    }
}





