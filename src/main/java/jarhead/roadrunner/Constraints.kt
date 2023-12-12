package jarhead.roadrunner

data class Constraints(
    val maxVel: Double, val maxAccel: Double,
    val maxAngVel: Double, val maxAngAccel: Double,
    val trackWidth: Double
)