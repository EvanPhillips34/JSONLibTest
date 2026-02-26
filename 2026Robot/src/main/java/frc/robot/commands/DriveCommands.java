package frc.robot.commands;

import static edu.wpi.first.units.Units.*;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import org.littletonrobotics.junction.Logger;
import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.measure.AngularAcceleration;
import edu.wpi.first.units.measure.AngularVelocity;
import edu.wpi.first.units.measure.Time;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.drive.DriveConstants;
import frc.robot.subsystems.drive.DriveSubsystem;

public class DriveCommands {
    private static final double DEADBAND = 0.1;
	private static final double ANGLE_KP = 5.0;
	private static final double ANGLE_KD = 0.4;
	private static final AngularVelocity ANGLE_MAX_VELOCITY = RadiansPerSecond.of(8.0);
	private static final AngularAcceleration ANGLE_MAX_ACCELERATION = RadiansPerSecondPerSecond.of(20.0);
	private static final Time FF_START_DELAY = Seconds.of(2.0);
	private static final double FF_RAMP_RATE = 0.1; // Volts/Sec
	private static final AngularVelocity WHEEL_RADIUS_MAX_VELOCITY = RadiansPerSecond.of(0.25);
	private static final AngularAcceleration WHEEL_RADIUS_RAMP_RATE = RadiansPerSecondPerSecond.of(0.05);
    
    private static final PIDController xPid = new PIDController(3, 0, 0),
			yPid = new PIDController(3, 0, 0),
			omegaPid = new PIDController(5.5, 0, 0);

    private DriveCommands() {}

    private static Translation2d getLinearVelocityFromJoysticks(double x, double y) {
		double linearMagnitude = MathUtil.applyDeadband(Math.hypot(x, y), DEADBAND);
		Rotation2d linearDirection = new Rotation2d(Math.atan2(y, x));

		linearMagnitude = linearMagnitude * linearMagnitude;

		return new Pose2d(new Translation2d(), linearDirection)
				.transformBy(new Transform2d(linearMagnitude, 0.0, new Rotation2d()))
				.getTranslation();
	}

    public static Command joystickDrive(DriveSubsystem drive, DoubleSupplier xSupplier, DoubleSupplier ySupplier, DoubleSupplier omegaSupplier) {
         return Commands.run(
                () -> {
                    // Get linear velocity
                    Translation2d linearVelocity =
                            getLinearVelocityFromJoysticks(xSupplier.getAsDouble(), ySupplier.getAsDouble());

                    // Apply rotation deadband
                    double omega = MathUtil.applyDeadband(omegaSupplier.getAsDouble(), DEADBAND);
					Logger.recordOutput("Rotation/Rotation", omega);


                    // Square rotation value for more precise control
                    omega = Math.copySign(omega * omega, omega);
					Logger.recordOutput("Rotation/RotationSquared", omega);
					Logger.recordOutput("Rotation/MaxAngularSpeed", drive.getMaxAngularSpeedRadPerSec());
					Logger.recordOutput("Rotation/ChassisOmega", omega * drive.getMaxAngularSpeedRadPerSec());

                    // Convert to field relative speeds & send command
                    ChassisSpeeds speeds = new ChassisSpeeds(
                            linearVelocity.getX() * drive.getMaxLinearSpeedMetersPerSec(),
                            linearVelocity.getY() * drive.getMaxLinearSpeedMetersPerSec(),
                            omega * drive.getMaxAngularSpeedRadPerSec());
						
                    boolean isFlipped = DriverStation.getAlliance().isPresent()
                            && DriverStation.getAlliance().get() == Alliance.Red;
							
					Logger.recordOutput("typespeeds/speeds", speeds);
					Logger.recordOutput("typespeeds/relative%speeds", ChassisSpeeds.fromFieldRelativeSpeeds(
                            speeds,
                            isFlipped ? drive.getRotation().plus(new Rotation2d(Math.PI)) : drive.getRotation()));

                    drive.runVelocity(ChassisSpeeds.fromFieldRelativeSpeeds(
                            speeds,
                            isFlipped ? drive.getRotation().plus(new Rotation2d(Math.PI)) : drive.getRotation()));
                }, drive);
    }
    /** This command is useful for snapping onto an angle while driving, using a PID Controller to keep specified angle while driving (like a turret) */
     public static Command joystickDriveAtAngle(DriveSubsystem drive, DoubleSupplier xSupplier, DoubleSupplier ySupplier, Supplier<Rotation2d> rotationSupplier) {

        // Create PID controller
        ProfiledPIDController angleController = new ProfiledPIDController(
                ANGLE_KP, 0.0, ANGLE_KD, new TrapezoidProfile.Constraints(ANGLE_MAX_VELOCITY.in(RadiansPerSecond), ANGLE_MAX_ACCELERATION.in(RadiansPerSecondPerSecond)));
        angleController.enableContinuousInput(-Math.PI, Math.PI);

        return Commands.run(
                        () -> {
                            // Get linear velocity
                            Translation2d linearVelocity =
                                    getLinearVelocityFromJoysticks(xSupplier.getAsDouble(), ySupplier.getAsDouble());

                            // Calculate angular speed
                            double omega = angleController.calculate(
                                    drive.getRotation().getRadians(),
                                    rotationSupplier.get().getRadians());

                            // Convert to field relative speeds & send command
                            ChassisSpeeds speeds = new ChassisSpeeds(
                                    linearVelocity.getX() * drive.getMaxLinearSpeedMetersPerSec(),
                                    linearVelocity.getY() * drive.getMaxLinearSpeedMetersPerSec(),
                                    omega);
                            boolean isFlipped = DriverStation.getAlliance().isPresent()
                                    && DriverStation.getAlliance().get() == Alliance.Red;
                            drive.runVelocity(ChassisSpeeds.fromFieldRelativeSpeeds(
                                    speeds,
                                    isFlipped
                                            ? drive.getRotation().plus(new Rotation2d(Math.PI))
                                            : drive.getRotation()));
                        },
                        drive)

                // Reset PID controller when command starts
                .beforeStarting(() -> angleController.reset(drive.getRotation().getRadians()));
    }

    public static Command feedforwardCharacterization(DriveSubsystem drive) {
		List<Double> velocitySamples = new LinkedList<>();
		List<Double> voltageSamples = new LinkedList<>();
		Timer timer = new Timer();

		return Commands.sequence(
				// Reset data
				Commands.runOnce(() -> {
					velocitySamples.clear();
					voltageSamples.clear();
				}),

				// Allow modules to orient
				Commands.run(
								() -> {
									drive.runCharacterization(0.0);
								},
								drive)
						.withTimeout(FF_START_DELAY),

				// Start timer
				Commands.runOnce(timer::restart),

				// Accelerate and gather data
				Commands.run(
								() -> {
									double voltage = timer.get() * FF_RAMP_RATE;
									drive.runCharacterization(voltage);
									velocitySamples.add(drive.getFFCharacterizationVelocity());
									voltageSamples.add(voltage);
								},
								drive)

						// When cancelled, calculate and print results
						.finallyDo(() -> {
							int n = velocitySamples.size();
							double sumX = 0.0;
							double sumY = 0.0;
							double sumXY = 0.0;
							double sumX2 = 0.0;
							for (int i = 0; i < n; i++) {
								sumX += velocitySamples.get(i);
								sumY += voltageSamples.get(i);
								sumXY += velocitySamples.get(i) * voltageSamples.get(i);
								sumX2 += velocitySamples.get(i) * velocitySamples.get(i);
							}
							double kS = (sumY * sumX2 - sumX * sumXY) / (n * sumX2 - sumX * sumX);
							double kV = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX);

							NumberFormat formatter = new DecimalFormat("#0.00000");
							System.out.println("********** Drive FF Characterization Results **********");
							System.out.println("\tkS: " + formatter.format(kS));
							System.out.println("\tkV: " + formatter.format(kV));
						}));
	}

    
	public static Command wheelRadiusCharacterization(DriveSubsystem drive) {
		SlewRateLimiter limiter = new SlewRateLimiter(WHEEL_RADIUS_RAMP_RATE.in(RadiansPerSecondPerSecond));
		WheelRadiusCharacterizationState state = new WheelRadiusCharacterizationState();

		return Commands.parallel(
				// Drive control sequence
				Commands.sequence(
						// Reset acceleration limiter
						Commands.runOnce(() -> {
							limiter.reset(0.0);
						}),

						// Turn in place, accelerating up to full speed
						Commands.run(
								() -> {
									double speed = limiter.calculate(WHEEL_RADIUS_MAX_VELOCITY.in(RadiansPerSecond));
									drive.runVelocity(new ChassisSpeeds(0.0, 0.0, speed));
								},
								drive)),

				// Measurement sequence
				Commands.sequence(
						// Wait for modules to fully orient before starting measurement
						Commands.waitSeconds(1.0),

						// Record starting measurement
						Commands.runOnce(() -> {
							state.positions = Arrays.stream(drive.getWheelRadiusCharacterizationPositions()).toArray();
							state.lastAngle = drive.getRotation();
							state.gyroDelta = 0.0;
						}),

						// Update gyro delta
						Commands.run(() -> {
									var rotation = drive.getRotation();
									state.gyroDelta += Math.abs(
											rotation.minus(state.lastAngle).getRadians());
									state.lastAngle = rotation;
								})

								// When cancelled, calculate and print results
								.finallyDo(() -> {
									double[] positions = Arrays.stream(drive.getWheelRadiusCharacterizationPositions()).toArray();
									double wheelDelta = 0.0;
									for (int i = 0; i < 4; i++) {
										wheelDelta += Math.abs(positions[i] - state.positions[i]) / 4.0;
									}
									double wheelRadius =
											(state.gyroDelta * DriveConstants.DRIVE_BASE_RADIUS.in(Meters)) / wheelDelta;

									NumberFormat formatter = new DecimalFormat("#0.000");
									System.out.println("********** Wheel Radius Characterization Results **********");
									System.out.println("\tWheel Delta: " + formatter.format(wheelDelta) + " radians");
									System.out.println(
											"\tGyro Delta: " + formatter.format(state.gyroDelta) + " radians");
									System.out.println("\tWheel Radius: "
											+ formatter.format(wheelRadius)
											+ " meters, "
											+ formatter.format(Units.metersToInches(wheelRadius))
											+ " inches");
								})));
	}


    private static class WheelRadiusCharacterizationState {
		double[] positions = new double[4];
		Rotation2d lastAngle = new Rotation2d();
		double gyroDelta = 0.0;
	}

}
