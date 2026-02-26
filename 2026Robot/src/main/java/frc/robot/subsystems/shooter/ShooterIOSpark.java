package frc.robot.subsystems.shooter;

import static edu.wpi.first.units.Units.*;
import static frc.robot.Util.Util.tryUntilOk;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkFlexConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

public class ShooterIOSpark implements ShooterIO {
    private final SparkFlex shooterMotor;
    private final SparkClosedLoopController shooterPID;
    private final SparkFlex kickUpMotor;
    private final SparkClosedLoopController kickUpPID;

    public ShooterIOSpark() {
        shooterMotor = new SparkFlex(ShooterConstants.shooterMotorID, MotorType.kBrushless);
        shooterPID = shooterMotor.getClosedLoopController();
        SparkFlexConfig shooterConfig = new SparkFlexConfig();

        kickUpMotor = new SparkFlex(ShooterConstants.shooterKickupID, MotorType.kBrushless);
        kickUpPID = kickUpMotor.getClosedLoopController();
        SparkFlexConfig kickUpConfig = new SparkFlexConfig();
        shooterConfig.idleMode(IdleMode.kBrake);
        kickUpConfig.idleMode(IdleMode.kBrake);

        tryUntilOk(shooterMotor, 5, () -> shooterMotor.configure(shooterConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters));
        tryUntilOk(kickUpMotor, 5, () -> kickUpMotor.configure(kickUpConfig, ResetMode.kNoResetSafeParameters, PersistMode.kPersistParameters));

    }

    @Override
    public void updateInputs(ShooterIOInputs inputs) {
        inputs.shooterAppliedVolts = shooterMotor.getAppliedOutput();
        inputs.shooterConnected = true;
        inputs.shooterCurrentAmps = shooterMotor.getOutputCurrent();
        inputs.shooterPosition = Degrees.of(shooterMotor.getEncoder().getPosition());
        inputs.shooterVelocity = DegreesPerSecond.of(shooterMotor.getEncoder().getVelocity());

        inputs.kickUpAppliedVolts = shooterMotor.getAppliedOutput();
        inputs.kickUpConnected = true;
        inputs.kickUpCurrentAmps = shooterMotor.getOutputCurrent();
        inputs.kickUpPosition = Degrees.of(shooterMotor.getEncoder().getPosition());
        inputs.kickUpVelocity = DegreesPerSecond.of(shooterMotor.getEncoder().getVelocity());
    }

    @Override
    public void setAngularSpeed(double speed) {
        shooterPID.setSetpoint(speed, ControlType.kVoltage);
        kickUpPID.setSetpoint(speed, ControlType.kVoltage);
    }
}
