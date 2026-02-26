package frc.robot;

import static edu.wpi.first.units.Units.Hertz;

import edu.wpi.first.units.measure.Frequency;
import edu.wpi.first.wpilibj.RobotBase;

public class Constants {
    
    public static final Mode simMode = Mode.SIM;
	public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

	public static enum Mode {
		REAL,
		SIM,
		REPLAY
	}

	public static final Frequency logFrequency = Hertz.of(50);
}
