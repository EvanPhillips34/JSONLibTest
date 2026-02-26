package frc.robot.subsystems.shooter;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.RPM;

import org.littletonrobotics.junction.AutoLog;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public interface ShooterIO {
	@AutoLog
	public static class ShooterIOInputs {
		public boolean shooterConnected = false;
		public Angle shooterPosition = Degrees.of(0);
		public AngularVelocity shooterVelocity = DegreesPerSecond.of(0);
		public double shooterAppliedVolts = 0;
		public double shooterCurrentAmps = 0;

		public boolean kickUpConnected = false;
		public Angle kickUpPosition = Degrees.of(0);
		public AngularVelocity kickUpVelocity = DegreesPerSecond.of(0);
		public double kickUpAppliedVolts = 0;
		public double kickUpCurrentAmps = 0;
	}

	public default void updateInputs(ShooterIOInputs inputs) {}

	public default void setAngularSpeed(double speed) {}

	public default void runMotors(double voltage) {}

	public default void stopMotors() {}
}