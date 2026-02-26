package frc.robot.subsystems.shooter;

import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.hardware.TalonFX;

import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.shooter.ShooterIO;

import com.orangefrc.annotation.GenerateJson;  






public class ShooterSubsystem extends SubsystemBase {

  ShooterIO io;
  ShooterIOInputsAutoLogged inputs = new ShooterIOInputsAutoLogged();

  @GenerateJson
  public class ShooterTuner {
    double flywheelSpeed;
    double kickupSpeed;
  }

  public ShooterSubsystem(ShooterIO io) {
    this.io = io;
  }

  public void setvelocity(double speed){
    io.runMotors(speed);
  }

  @Override
  public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Shooter", inputs);
  }

  @Override
  public void simulationPeriodic() {
    // This method will be called once per scheduler run during simulation
  }
}
