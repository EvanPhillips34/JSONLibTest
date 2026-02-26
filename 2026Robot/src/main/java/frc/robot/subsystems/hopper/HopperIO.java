package frc.robot.subsystems.hopper;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.RPM;

import org.littletonrobotics.junction.AutoLog;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public interface HopperIO {

	@AutoLog
	public static class HopperIOInputs {
		public boolean hopperConnected = false;
		public Angle hopperPosition = Degrees.of(0);
		public AngularVelocity hopperVelocity = DegreesPerSecond.of(0);
		public double hopperAppliedVolts = 0;
		public double hopperCurrentAmps = 0;
	}

	public default void updateInputs(HopperIOInputs inputs) {}

	public default void setAngularSpeed(double speed) {}

	public default void runMotors(double voltage) {}

	public default void stopMotors() {}
}
