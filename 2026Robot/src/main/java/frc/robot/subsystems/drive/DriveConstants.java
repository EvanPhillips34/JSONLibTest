package frc.robot.subsystems.drive;

import static edu.wpi.first.units.Units.*;

import org.ironmaple.simulation.drivesims.COTS;
import org.ironmaple.simulation.drivesims.configs.DriveTrainSimulationConfig;
import org.ironmaple.simulation.drivesims.configs.SwerveModuleSimulationConfig;

import com.ctre.phoenix6.Orchestra;
import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;

import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.units.measure.Distance;
import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.units.measure.LinearAcceleration;
import edu.wpi.first.units.measure.LinearVelocity;
import frc.robot.generated.TunerConstants;

public class DriveConstants {
        public static final Frequency odometryFrequency = Hertz.of(100);
        // Robot Mass
        public static final double ROBOT_MASS_KG = 74.088;
        // Moment of Inertia
        public static final double ROBOT_MOI = 6.883;
        // Wheel Coefficient of Friction
        public static final double WHEEL_COF = 1.2;

        // Dist between middle of left and right wheels
        public static final Distance trackWidth = Inches.of(21.875);

        // Dist between middle of front and back wheels
	public static final Distance wheelBase = Inches.of(21.875);

        public static final Translation2d[] moduleTranslations = new Translation2d[] {
		new Translation2d(trackWidth.div(2).in(Meters), wheelBase.div(2).in(Meters)),
		new Translation2d(
				trackWidth.div(2).in(Meters), wheelBase.div(2).unaryMinus().in(Meters)),
		new Translation2d(
				trackWidth.div(2).unaryMinus().in(Meters), wheelBase.div(2).in(Meters)),
		new Translation2d(
				trackWidth.div(2).unaryMinus().in(Meters),
				wheelBase.div(2).unaryMinus().in(Meters))
	};

        // Radius of Drive Base
	public static final Distance DRIVE_BASE_RADIUS = Meters.of(Math.max(
                Math.max(
                    Math.hypot(TunerConstants.FrontLeft.LocationX, TunerConstants.FrontLeft.LocationY),
                    Math.hypot(TunerConstants.FrontRight.LocationX, TunerConstants.FrontRight.LocationY)),
            Math.max(
                    Math.hypot(TunerConstants.BackLeft.LocationX, TunerConstants.BackLeft.LocationY),
                    Math.hypot(TunerConstants.BackRight.LocationX, TunerConstants.BackRight.LocationY))));

        //Pathplanner Config for robot
        public static final RobotConfig PP_CONFIG = new RobotConfig(
            ROBOT_MASS_KG,
            ROBOT_MOI,
            new ModuleConfig(
                    TunerConstants.FrontLeft.WheelRadius,
                    TunerConstants.kSpeedAt12Volts.in(MetersPerSecond),
                    WHEEL_COF,
                    DCMotor.getKrakenX60Foc(1).withReduction(TunerConstants.FrontLeft.DriveMotorGearRatio),
                    TunerConstants.FrontLeft.SlipCurrent,
                    1),
            moduleTranslations);
        // Drivetrain for Simulation
	 public static final DriveTrainSimulationConfig mapleSimConfig = DriveTrainSimulationConfig.Default()
            .withRobotMass(Kilograms.of(ROBOT_MASS_KG))
            .withCustomModuleTranslations(moduleTranslations)
            .withGyro(COTS.ofPigeon2())
            .withSwerveModule(new SwerveModuleSimulationConfig(
                    DCMotor.getKrakenX60(1),
                    DCMotor.getFalcon500(1),
                    TunerConstants.FrontLeft.DriveMotorGearRatio,
                    TunerConstants.FrontLeft.SteerMotorGearRatio,
                    Volts.of(TunerConstants.FrontLeft.DriveFrictionVoltage),
                    Volts.of(TunerConstants.FrontLeft.SteerFrictionVoltage),
                    Meters.of(TunerConstants.FrontLeft.WheelRadius),
                    KilogramSquareMeters.of(TunerConstants.FrontLeft.SteerInertia),
                    WHEEL_COF));
                   public static Orchestra m_orchestra = new Orchestra();

    //TODO: GET THESE THINGS FINALIZED WHEN POSSIBLE 
    // public static final LinearVelocity maxSpeed = MetersPerSecond.of(5.3);
	// public static final LinearVelocity maxSpeedCoral = MetersPerSecond.of(2);
	// public static final LinearAcceleration maxAccel = FeetPerSecondPerSecond.of(3);
	// public static final LinearAcceleration maxAccelAuto = MetersPerSecondPerSecond.of(1.5);

	// public static final Distance driveBaseRadius =
	// 		Meters.of(Math.hypot(trackWidth.div(2).in(Meters), wheelBase.div(2).in(Meters)));
	
}
