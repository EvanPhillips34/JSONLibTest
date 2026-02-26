 package frc.robot.subsystems.intake;

 import com.revrobotics.spark.SparkClosedLoopController;
 import com.revrobotics.spark.SparkFlex;
 import com.revrobotics.spark.SparkMax;
 import com.revrobotics.spark.SparkBase.ControlType;
 import com.revrobotics.spark.SparkLowLevel.MotorType;

import org.littletonrobotics.junction.Logger;

import com.ctre.phoenix6.hardware.TalonFX;

 import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
 import edu.wpi.first.wpilibj.motorcontrol.Spark;
 import edu.wpi.first.wpilibj2.command.Command;
 import edu.wpi.first.wpilibj2.command.SubsystemBase;

 public class IntakeSubsystem extends SubsystemBase {

  IntakeIO io;
  IntakeIOInputsAutoLogged inputs = new IntakeIOInputsAutoLogged();
  
   public IntakeSubsystem(IntakeIO io) {
    this.io = io;
   }

   public void setKickupSpeed(double speed){
    io.setKickupSpeed(speed);
 }

   public void setPivotSpeed(double speed){
     io.setPivotSpeed(speed);
   }


  @Override
   public void periodic() {
    io.updateInputs(inputs);
    Logger.processInputs("Intake", inputs);   
  }

   @Override
   public void simulationPeriodic() {
     // This method will be called once per scheduler run during simulation
   }
 }
