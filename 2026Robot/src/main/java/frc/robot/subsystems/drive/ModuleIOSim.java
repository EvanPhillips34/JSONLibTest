package frc.robot.subsystems.drive;

import static edu.wpi.first.units.Units.Radians;

import java.util.Arrays;

import org.ironmaple.simulation.drivesims.SwerveModuleSimulation;

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.swerve.SwerveModuleConstants;

import frc.robot.Util.Util;

public class ModuleIOSim extends ModuleIOTalon {
    private final SwerveModuleSimulation simulation;

    public ModuleIOSim(SwerveModuleConstants constants, SwerveModuleSimulation simulation) {
        super(Util.regulateModuleConstantForSimulation(constants));

        this.simulation = simulation;
        simulation.useDriveMotorController(new Util.TalonFXMotorControllerSim(driveTalon));

        simulation.useSteerMotorController(new Util.TalonFXMotorControllerWithRemoteCancoderSim(turnTalon, encoder));
    }
    @Override
    public void updateMotorConfigs() {
        
    }

    @Override
    public void updateInputs(ModuleIOInputs inputs) {
        super.updateInputs(inputs);

        inputs.odometryTimestamps = Util.getSimulationOdometryTimeStamps();

        inputs.odometryDrivePositionsRad = Arrays.stream(simulation.getCachedDriveWheelFinalPositions())
                .mapToDouble(angle -> angle.in(Radians))
                .toArray();

        inputs.turnPositionsRad = simulation.getCachedSteerAbsolutePositions();
    }
}
