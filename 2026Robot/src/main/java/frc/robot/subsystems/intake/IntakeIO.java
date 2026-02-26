package frc.robot.subsystems.intake;

import static edu.wpi.first.units.Units.Degrees;
import static edu.wpi.first.units.Units.DegreesPerSecond;
import static edu.wpi.first.units.Units.RPM;

import org.littletonrobotics.junction.AutoLog;

import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularVelocity;

public interface IntakeIO {
	@AutoLog
	public static class IntakeIOInputs {
		public boolean intakePivotConnected = false;
		public Angle intakePivotPosition = Degrees.of(0);
		public AngularVelocity intakePivotVelocity = DegreesPerSecond.of(0);
		public double intakePivotAppliedVolts = 0;
		public double intakePivotCurrentAmps = 0;

		public boolean intakeKickConnected = false;
		public Angle intakeKickPosition = Degrees.of(0);
		public AngularVelocity intakeKickVelocity = DegreesPerSecond.of(0);
		public double intakeKickAppliedVolts = 0;
		public double intakeKickCurrentAmps = 0;
	}

	public default void updateInputs(IntakeIOInputs inputs) {}

	public default void setPivotSpeed(double speed) {}

	public default void setKickupSpeed(double voltage) {}

	public default void stopMotors() {}
}