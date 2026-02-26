package frc.robot.subsystems.vision;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.photonvision.PhotonCamera;
import org.photonvision.targeting.MultiTargetPNPResult;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform3d;

public class VisionIOPhotonvision implements VisionIO {
    protected final PhotonCamera camera;
	protected final Transform3d robotToCamera;

	public VisionIOPhotonvision(String name, Transform3d robotToCamera) {
		camera = new PhotonCamera(name);
		this.robotToCamera = robotToCamera;
	}

	@Override
	public void updateInputs(VisionIOInputs inputs) {
		inputs.connected = camera.isConnected();

		Set<Short> tagIds = new HashSet<>();
		List<PoseObservation> poseObservations = new LinkedList<>();
		for (PhotonPipelineResult result : camera.getAllUnreadResults()) {
			if (result.hasTargets()) {
				inputs.latestTargetObservation = new TargetObservation(Rotation2d.fromDegrees(result.getBestTarget().getYaw()), Rotation2d.fromDegrees(result.getBestTarget().getPitch()), result.getBestTarget().bestCameraToTarget);
			} else {
				inputs.latestTargetObservation = new TargetObservation(new Rotation2d(), new Rotation2d(), new Transform3d());
			}

			if (result.multitagResult.isPresent()) {
				MultiTargetPNPResult multitagResult = result.multitagResult.get();

				Transform3d fieldToCamera = multitagResult.estimatedPose.best;
				Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
				Pose3d robotPose = new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());

				double totalTagDistance = 0.0;
				for (PhotonTrackedTarget target : result.targets) {
					totalTagDistance +=
							target.bestCameraToTarget.getTranslation().getNorm();
				}

				tagIds.addAll(multitagResult.fiducialIDsUsed);

				poseObservations.add(new PoseObservation(
						result.getTimestampSeconds(),
						robotPose,
						multitagResult.estimatedPose.ambiguity,
						multitagResult.fiducialIDsUsed.size(),
						totalTagDistance / result.targets.size(), PoseObservationType.PHOTONVISION));
			} else if (!result.targets.isEmpty()) {
				PhotonTrackedTarget target = result.targets.get(0);

				Optional<Pose3d> tagPose = VisionConstants.aprilTagLayout.getTagPose(target.fiducialId);
				if (tagPose.isPresent()) {
					Transform3d fieldToTarget = new Transform3d(
							tagPose.get().getTranslation(), tagPose.get().getRotation());
					Transform3d cameraToTarget = target.bestCameraToTarget;
					Transform3d fieldToCamera = fieldToTarget.plus(cameraToTarget.inverse());
					Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
					Pose3d robotPose = new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());

					tagIds.add((short) target.fiducialId);

					poseObservations.add(new PoseObservation(
							result.getTimestampSeconds(),
							robotPose,
							target.poseAmbiguity,
							1,
							cameraToTarget.getTranslation().getNorm(), PoseObservationType.PHOTONVISION));
				}
			}
		}

		inputs.poseObservations = new PoseObservation[poseObservations.size()];
		for (int i = 0; i < poseObservations.size(); i++) {
			inputs.poseObservations[i] = poseObservations.get(i);
		}

		inputs.tagIds = new int[tagIds.size()];
		int i = 0;
		for (int id : tagIds) {
			inputs.tagIds[i++] = id;
		}
	}
}
