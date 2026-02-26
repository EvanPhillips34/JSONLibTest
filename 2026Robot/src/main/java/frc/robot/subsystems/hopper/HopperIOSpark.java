package frc.robot.subsystems.hopper;

import static edu.wpi.first.units.Units.*;

import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

public class HopperIOSpark implements HopperIO {
    private final SparkFlex hopperMotor;
    private final SparkClosedLoopController hopperPID;

    public HopperIOSpark() {
        hopperMotor = new SparkFlex(HopperConstants.hopperMotorID, MotorType.kBrushless);
        hopperPID = hopperMotor.getClosedLoopController();
        SparkFlexConfig hopperConfig = new SparkFlexConfig();
        hopperConfig.idleMode(IdleMode.kBrake);
    }

    @Override
    public void updateInputs(HopperIOInputs inputs) {
        inputs.hopperAppliedVolts = hopperMotor.getAppliedOutput();
        inputs.hopperConnected = true;
        inputs.hopperCurrentAmps = hopperMotor.getOutputCurrent();
        inputs.hopperPosition = Degrees.of(hopperMotor.getEncoder().getPosition());
        inputs.hopperVelocity = DegreesPerSecond.of(hopperMotor.getEncoder().getVelocity());
    }

    @Override
    public void setAngularSpeed(double speed) {
        hopperPID.setSetpoint(speed, ControlType.kVoltage);
    }
}