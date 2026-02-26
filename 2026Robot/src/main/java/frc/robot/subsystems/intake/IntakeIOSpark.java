package frc.robot.subsystems.intake;

import static edu.wpi.first.units.Units.*;
import static frc.robot.Util.Util.tryUntilOk;

import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

public class IntakeIOSpark implements IntakeIO {
    private final SparkFlex intakeMotor;
    private final SparkClosedLoopController intakePID;
    private final SparkMax intakeLift;
    private final SparkClosedLoopController intakeLiftPID;

    public IntakeIOSpark() {
        intakeMotor = new SparkFlex(IntakeConstants.intakeMotorID, MotorType.kBrushless);
        intakeLift = new SparkMax(IntakeConstants.intakeLiftID, MotorType.kBrushless);
        intakePID = intakeMotor.getClosedLoopController();
        intakeLiftPID = intakeLift.getClosedLoopController();
        SparkFlexConfig intakeConfig = new SparkFlexConfig();
        SparkMaxConfig intakeLiftConfig = new SparkMaxConfig();
        intakeConfig.idleMode(IdleMode.kBrake);
        intakeLiftConfig.idleMode(IdleMode.kBrake);
    }

    @Override
    public void updateInputs(IntakeIOInputs inputs) {
        inputs.intakeKickAppliedVolts = intakeMotor.getAppliedOutput();
        inputs.intakePivotAppliedVolts = intakeLift.getAppliedOutput();
        inputs.intakeKickConnected = true;
        inputs.intakePivotConnected = true;

        inputs.intakeKickCurrentAmps = intakeMotor.getOutputCurrent();
        inputs.intakePivotCurrentAmps = intakeLift.getOutputCurrent();
        inputs.intakeKickPosition = Degrees.of(intakeMotor.getEncoder().getPosition());
        inputs.intakePivotPosition = Degrees.of(intakeLift.getEncoder().getPosition());
        inputs.intakeKickVelocity = DegreesPerSecond.of(intakeMotor.getEncoder().getVelocity());
        inputs.intakePivotVelocity = DegreesPerSecond.of(intakeLift.getEncoder().getVelocity());  

    }

    @Override
    public void setKickupSpeed(double voltage) {
        intakePID.setSetpoint(voltage, ControlType.kVoltage);
    }

    @Override
    public void setPivotSpeed(double voltage) {
        intakeLiftPID.setSetpoint(voltage, ControlType.kVoltage);
    }
}


