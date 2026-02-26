package frc.robot.subsystems.drive;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Alert;
import edu.wpi.first.wpilibj.Alert.AlertType;

public class Module {

    // Initialize many variables
    private final ModuleIO io;
    private final ModuleIOInputsAutoLogged inputs = new ModuleIOInputsAutoLogged();
    private final int index;
    public SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> constants;

    private final Alert driveDisconnectedAlert;
    private final Alert turnDisconnectedAlert;
    private final Alert turnEncoderDisconnectedAlert;

    private SwerveModulePosition[] odometryPositions = new SwerveModulePosition[] {};


    // Method for instantiating each module object
    public Module(ModuleIO io, int index, SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> constants) {
        this.io = io;
        this.index = index;
        this.constants = constants;
        driveDisconnectedAlert = new Alert("Disconnected drive motor on module " + Integer.toString(index) + ".", AlertType.kError);
        turnDisconnectedAlert = new Alert("Disconnected turn motor on module " + Integer.toString(index) + ".", AlertType.kError);
        turnEncoderDisconnectedAlert = new Alert("Disconnected turn encoder on module " + Integer.toString(index) + ".", AlertType.kError);
    }

    // Periodic function for each module, runs once every period (20ms)
    public void periodic() {
        // Update inputs for logging
        io.updateInputs(inputs);

        Logger.processInputs("Drive/Module" + Integer.toString(index), inputs);

         int sampleCount = inputs.odometryTimestamps.length;
        odometryPositions = new SwerveModulePosition[sampleCount];
        for (int i = 0; i < sampleCount; i++) {
            Logger.recordOutput("constants/WheeLRadius", constants.WheelRadius);
            double positionMeters = (inputs.odometryDrivePositionsRad[i] * constants.WheelRadius) / constants.DriveMotorGearRatio;
            Logger.recordOutput("constants/posmeters", positionMeters);
                        Logger.recordOutput("constants/odoDrivePosRad", inputs.odometryDrivePositionsRad[i]);

            Rotation2d angle = inputs.turnPositionsRad[i];
            odometryPositions[i] = new SwerveModulePosition(positionMeters, angle);
        }

        driveDisconnectedAlert.set(!inputs.driveConnected);
        turnDisconnectedAlert.set(!inputs.turnConnected);
        turnEncoderDisconnectedAlert.set(!inputs.turnEncoderConnected);
    }

    public Rotation2d getAngle() {
        return inputs.turnAbsolutePosition;
    }

    public void updatePID() {
        io.updateMotorConfigs();
    }

    public void runSetpoint(SwerveModuleState state) {
        // Optimize velocity setpoint
        state.optimize(getAngle());
        state.cosineScale(inputs.turnAbsolutePosition);

        io.setDriveVelocity(state.speedMetersPerSecond / constants.WheelRadius);
        io.setTurnPosition(state.angle);
    }

    /** Runs the module with the specified output while controlling to zero degrees. (Straight line) */
    public void runCharacterization(double output) {
        io.setDriveOpenLoop(output);
        io.setTurnPosition(new Rotation2d());
    }

    public void stop() {
        io.setDriveOpenLoop(0.0);
        io.setTurnOpenLoop(0.0);
    }

    public double getPositionMeters() {
        return inputs.drivePositionRad * constants.WheelRadius;
    }

    public double getVelocityMetersPerSec() {
        return inputs.driveVelocityRadPerSec * constants.WheelRadius;
    }

    public SwerveModulePosition getPosition() {
        return new SwerveModulePosition(getPositionMeters(), getAngle());
    }
    public SwerveModuleState getState() {
        return new SwerveModuleState(getVelocityMetersPerSec(), getAngle());
    }

    public SwerveModulePosition[] getOdometryPositions() {
        return odometryPositions;
    }

    public double[] getOdometryTimestamps() {
        return inputs.odometryTimestamps;
    }

     public double getWheelRadiusCharacterizationPosition() {
        return inputs.drivePositionRad;
    }

    public double getFFCharacterizationVelocity() {
        return Units.radiansToRotations(inputs.driveVelocityRadPerSec);
    }


}
