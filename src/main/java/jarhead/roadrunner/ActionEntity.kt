package jarhead.roadrunner

import com.acmerobotics.roadrunner.*

class TrajectoryAction(val t: TimeTrajectory) : Action {
    override fun run(p: com.acmerobotics.dashboard.telemetry.TelemetryPacket) = TODO()
    override fun toString() = t.toString()
}
class TurnAction(val t: TimeTurn) : Action {
    override fun run(p: com.acmerobotics.dashboard.telemetry.TelemetryPacket) = TODO()
    override fun toString() = "${t.angle} ${t.beginPose} ${t.reversed}"
}

data class ActionEvent(
    val time: Double,
    val a: Action, // primitive action (not SequentialAction, ParallelAction)
)

typealias Timeline = List<ActionEvent>
typealias Duration = Double

fun actionTimeline(a: Action): Pair<Duration, Timeline> {
    val timeline = mutableListOf<ActionEvent>()

    // Adds the primitive actions to the timeline starting at time. Returns the time at which a completes.
    // Assumes custom actions complete instantly.
    fun add(time: Double, a: Action): Double =
        when (a) {
            is SequentialAction -> {
                a.initialActions.fold(time, ::add)
            }

            is ParallelAction -> {
                a.initialActions.maxOf {
                    add(time, it)
                }
            }

            is TrajectoryAction -> {
                timeline.add(ActionEvent(time, a))
                time + a.t.profile.duration
            }

            is TurnAction -> {
                timeline.add(ActionEvent(time, a))
                time + a.t.profile.duration
            }

            is SleepAction -> {
                time + a.dt
            }

            else -> {
                time
            }
        }

    val duration = add(0.0, a)

    timeline.sortBy { it.time }

    return Pair(duration, timeline)
}
