package atlasdsl;

import java.util.Arrays;
import java.util.List;

public class StaticParticipants extends GoalParticipants {
	public enum Spec {
		ALL_ROBOTS
	}
	
	List<Robot> participants;
	
	public StaticParticipants(List<Robot> participants) {
		this.participants = participants;
	}
	
	public StaticParticipants(Robot [] participants, Mission mission) {
		this.participants = Arrays.asList(participants);
	}
	
	public StaticParticipants(Spec mt, Mission mission) {
		this.participants = mission.getAllRobots();
	}

	public List<Robot> getParticipants() {
		// TODO Auto-generated method stub
		return participants;
	}
}
