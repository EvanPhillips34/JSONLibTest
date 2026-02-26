package frc.autonomous;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;

public abstract class AutoBase extends SequentialCommandGroup{
    public static Timer timer = new Timer();

    //  public AutoBase(IntakeSubsystem intakeSubsystem, ShooterSubsystem shooterSubsystem, HopperSubsystem hopperSubsystem) {}
    public static PathPlannerPath getPathFromFile(String name) {
		try {
			PathPlannerPath path = PathPlannerPath.fromPathFile(name);
			return path;
		} catch (Exception e) {
			DriverStation.reportError("Cant Find Path : " + e.getMessage(), e.getStackTrace());
			SmartDashboard.putString("PathErrors", "Cant Find Path : " + name);
			return null;
		}
	}


    public static final Command wait(double time) {
		return new WaitCommand(time);
	}

    public static final Command delayStartTime() {
		return new FunctionalCommand(
				() -> {
					timer.reset();
					timer.start();
				},
				() -> {},
				(interupted) -> {
					timer.stop();
					timer.reset();
				},
				() -> {
					return timer.hasElapsed(SmartDashboard.getNumber("delayStartTime", 0));
				});
	}

    public static final Command followPath(PathPlannerPath path) {
		return AutoBuilder.followPath(path);
	}
}
