package frc.robot.subsystems.drive;

import com.ctre.phoenix6.BaseStatusSignal;
import com.ctre.phoenix6.StatusSignal;
import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.PositionTorqueCurrentFOC;
import com.ctre.phoenix6.controls.PositionVoltage;
import com.ctre.phoenix6.controls.TorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityTorqueCurrentFOC;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.controls.VoltageOut;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.hardware.DeviceIdentifier;
import com.ctre.phoenix6.hardware.ParentDevice;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.FeedbackSensorSourceValue;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;

import edu.wpi.first.math.filter.Debouncer;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.units.measure.*;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;

import static frc.robot.Util.Util.*;

import java.util.Queue;

import org.littletonrobotics.junction.Logger;

import frc.robot.Constants;
import frc.robot.Constants.*;


public class ModuleIOTalon implements ModuleIO {
    protected final SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> constants;

    protected final TalonFX driveTalon;
    protected final TalonFX turnTalon;
    protected final CANcoder encoder;

    protected final VoltageOut voltageRequest = new VoltageOut(0.0);
    protected final PositionVoltage positionVoltageRequest = new PositionVoltage(0.0);
    protected final VelocityVoltage velocityVoltageRequest = new VelocityVoltage(0.0);


    // TODO: requires phoenix pro
    // protected final TorqueCurrentFOC torqueCurrentRequest = new TorqueCurrentFOC(0);
    // protected final PositionTorqueCurrentFOC positionTorqueCurrentRequest = new PositionTorqueCurrentFOC(0.0);
    // protected final VelocityTorqueCurrentFOC velocityTorqueCurrentRequest = new VelocityTorqueCurrentFOC(0.0);


    private final Queue<Double> timestampQueue;
    private final Queue<Double> drivePositionQueue;
    private final Queue<Double> turnPositionQueue;


    protected final StatusSignal<Angle> drivePosition;
    protected final StatusSignal<AngularVelocity> driveVelocity;
    protected final StatusSignal<Current> driveCurrent;
    protected final StatusSignal<Voltage> driveAppliedVolts;

    protected final StatusSignal<Angle> turnAbsolutePosition;
    protected final StatusSignal<AngularVelocity> turnVelocity;
    protected final StatusSignal<Current> turnCurrent;
    protected final StatusSignal<Voltage> turnAppliedVolts;

    private final Debouncer driveConnectedDebounce = new Debouncer(0.5);
    private final Debouncer turnConnectedDebounce = new Debouncer(0.5);
    private final Debouncer turnEncoderConnectedDebounce = new Debouncer(0.5);
    TalonFXConfiguration driveConfig = new TalonFXConfiguration();
    TalonFXConfiguration turnConfig = new TalonFXConfiguration();

    public ModuleIOTalon(SwerveModuleConstants<TalonFXConfiguration, TalonFXConfiguration, CANcoderConfiguration> constants) {
        this.constants = constants;
        driveTalon = new TalonFX(constants.DriveMotorId);
        turnTalon = new TalonFX(constants.SteerMotorId);
        encoder = new CANcoder(constants.EncoderId);
        driveConfig = constants.DriveMotorInitialConfigs;
        // var statusD = DriveConstants.m_orchestra.addInstrument(driveTalon);
        // var statusT = DriveConstants.m_orchestra.addInstrument(turnTalon);
        // Logger.recordOutput("DriveMotorStatus", statusD);
        // Logger.recordOutput("TurnMotorStatus", statusT);






        driveConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
        driveConfig.Slot0 = constants.DriveMotorGains;
        driveConfig.TorqueCurrent.PeakForwardTorqueCurrent = constants.SlipCurrent;
        driveConfig.TorqueCurrent.PeakReverseTorqueCurrent = -constants.SlipCurrent;
        driveConfig.CurrentLimits.StatorCurrentLimit = constants.SlipCurrent;
        driveConfig.CurrentLimits.StatorCurrentLimitEnable = true;
        driveConfig.MotorOutput.Inverted = constants.DriveMotorInverted
                ? InvertedValue.Clockwise_Positive
                : InvertedValue.CounterClockwise_Positive;

        tryUntilOk(5, () -> driveTalon.getConfigurator().apply(driveConfig, 0.25));
        tryUntilOk(5, () -> driveTalon.setPosition(0.0, 0.25));

        turnConfig.MotorOutput.NeutralMode = NeutralModeValue.Brake;
        turnConfig.Slot0 = constants.SteerMotorGains;
        if (Constants.currentMode == Constants.Mode.SIM) {
            turnConfig.Slot0.withKD(0.5).withKS(0);
        }

        turnConfig.Feedback.FeedbackRemoteSensorID = constants.EncoderId;
        turnConfig.Feedback.FeedbackSensorSource = switch (constants.FeedbackSource) {
            case RemoteCANcoder -> FeedbackSensorSourceValue.RemoteCANcoder;
            case FusedCANcoder -> FeedbackSensorSourceValue.FusedCANcoder;
            case SyncCANcoder -> FeedbackSensorSourceValue.SyncCANcoder;
            default -> throw new RuntimeException("Cannot find correct feedback sensor source");
        };

        turnConfig.Feedback.RotorToSensorRatio = constants.SteerMotorGearRatio;
        turnConfig.MotionMagic.MotionMagicCruiseVelocity = 100.0 / constants.SteerMotorGearRatio;
        turnConfig.MotionMagic.MotionMagicAcceleration = turnConfig.MotionMagic.MotionMagicCruiseVelocity / 0.100;
        turnConfig.MotionMagic.MotionMagicExpo_kV = 0.12 * constants.SteerMotorGearRatio;
        turnConfig.MotionMagic.MotionMagicExpo_kA = 0.1;
        turnConfig.ClosedLoopGeneral.ContinuousWrap = true;
        turnConfig.MotorOutput.Inverted = constants.SteerMotorInverted
                ? InvertedValue.Clockwise_Positive
                : InvertedValue.CounterClockwise_Positive;
        tryUntilOk(5, () -> turnTalon.getConfigurator().apply(turnConfig, 0.25));

        CANcoderConfiguration encoderConfig = constants.EncoderInitialConfigs;
        encoderConfig.MagnetSensor.MagnetOffset = constants.EncoderOffset;
        encoderConfig.MagnetSensor.SensorDirection = constants.EncoderInverted
                ? SensorDirectionValue.Clockwise_Positive
                : SensorDirectionValue.CounterClockwise_Positive;
        encoder.getConfigurator().apply(encoderConfig);


        


        drivePosition = driveTalon.getPosition();
        driveVelocity = driveTalon.getVelocity();
        driveAppliedVolts = driveTalon.getMotorVoltage();
        driveCurrent = driveTalon.getStatorCurrent();

        turnAbsolutePosition = encoder.getAbsolutePosition();
        turnVelocity = turnTalon.getVelocity();
        turnAppliedVolts = turnTalon.getMotorVoltage();
        turnCurrent = turnTalon.getStatorCurrent();

        timestampQueue = PhoenixOdometryThread.getInstance().makeTimestampQueue();
        drivePositionQueue = PhoenixOdometryThread.getInstance().registerSignal(drivePosition.clone());
        turnPositionQueue = PhoenixOdometryThread.getInstance().registerSignal(turnAbsolutePosition.clone());

        BaseStatusSignal.setUpdateFrequencyForAll(DriveConstants.odometryFrequency, turnAbsolutePosition, drivePosition);
        BaseStatusSignal.setUpdateFrequencyForAll(50.0, driveVelocity, driveAppliedVolts, driveCurrent, turnVelocity, turnAppliedVolts, turnCurrent);
        ParentDevice.optimizeBusUtilizationForAll(driveTalon, turnTalon);

    }

    @Override
    public void updateMotorConfigs() {
        turnConfig.Slot0 = constants.SteerMotorGains;
        driveConfig.Slot0 = constants.DriveMotorGains;
        tryUntilOk(5, () -> turnTalon.getConfigurator().apply(turnConfig, 0.25));

        tryUntilOk(5, () -> driveTalon.getConfigurator().apply(driveConfig, 0.25));



    }

    @Override
    public void updateInputs(ModuleIOInputs inputs) {
        var driveStatus = BaseStatusSignal.refreshAll(drivePosition, driveVelocity, driveAppliedVolts, driveCurrent);
        var turnStatus = BaseStatusSignal.refreshAll(turnVelocity, turnAppliedVolts, turnCurrent);
        var turnEncoderStatus = BaseStatusSignal.refreshAll(turnAbsolutePosition);


        // Update the inputs - if the drive, turn motors are connected and the encoders
        inputs.odometryTimestamps = timestampQueue.stream().mapToDouble((Double value) -> value).toArray();
        //Logger.recordOutput("constants/stream", drivePositionQueue.stream().mapToDouble(null));
        Logger.recordOutput("constants/drviepositionsignal", drivePosition.getValueAsDouble());
        inputs.odometryDrivePositionsRad =
        drivePositionQueue.stream()
            .mapToDouble((Double value) -> Units.rotationsToRadians(value))
            .toArray();
        

        inputs.turnPositionsRad =
        turnPositionQueue.stream()
            .map((Double value) -> Rotation2d.fromRotations(value))
            .toArray(Rotation2d[]::new);
            
    timestampQueue.clear();
    drivePositionQueue.clear();
    turnPositionQueue.clear();
        inputs.driveConnected = driveConnectedDebounce.calculate(driveStatus.isOK());
        inputs.drivePositionRad =
                Units.rotationsToRadians(drivePosition.getValueAsDouble()) / constants.DriveMotorGearRatio;
        inputs.driveVelocityRadPerSec =
                Units.rotationsToRadians(driveVelocity.getValueAsDouble()) / constants.DriveMotorGearRatio;
        inputs.driveAppliedVolts = driveAppliedVolts.getValueAsDouble();
        inputs.driveCurrentAmps = driveCurrent.getValueAsDouble();

        inputs.turnConnected = turnConnectedDebounce.calculate(turnStatus.isOK());
        inputs.turnEncoderConnected = turnEncoderConnectedDebounce.calculate(turnEncoderStatus.isOK());
        inputs.turnAbsolutePosition = Rotation2d.fromRotations(turnAbsolutePosition.getValueAsDouble());
        inputs.turnVelocityRadPerSec = Units.rotationsToRadians(turnVelocity.getValueAsDouble());
        inputs.turnAppliedVolts = turnAppliedVolts.getValueAsDouble();
        inputs.turnCurrentAmps = turnCurrent.getValueAsDouble();
    }

    @Override
    public void setDriveOpenLoop(double output) {
        driveTalon.setControl(voltageRequest.withOutput(output));
    }

    @Override
    public void setTurnOpenLoop(double output) {
        turnTalon.setControl(voltageRequest.withOutput(output));
    }

    @Override
    public void setDriveVelocity(double veloctiyRadPerSec) {
        double motorVeloctyRotationsPerSec = Units.radiansToRotations(veloctiyRadPerSec) * constants.DriveMotorGearRatio;
        
        driveTalon.setControl(velocityVoltageRequest.withVelocity(motorVeloctyRotationsPerSec));
    }

    // Sets the turn positions
    @Override
    public void setTurnPosition(Rotation2d rotation) {
        turnTalon.setControl(positionVoltageRequest.withPosition(rotation.getRotations()));
    }
}
