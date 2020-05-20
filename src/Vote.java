import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Vote {

	private final int participantPort;
	private final String vote;
	
	public Vote(int participantPort, String vote) {
		this.participantPort = participantPort;
		this.vote = vote;
	}

	public int getParticipantPort() {
		return participantPort;
	}

	public String getVote() {
		return vote;
	}

	@Override
	public String toString() {
		return "<" + participantPort + ", " + vote + ">";
	}

	public static List<Vote> fromVoteMessage(VoteMessage voteMessage) {
		List<Vote> msgVotes = new ArrayList<>();
		for (Map.Entry<Integer,String> vote: voteMessage.getVotes().entrySet()) {
			msgVotes.add(new Vote(vote.getKey(),vote.getValue()));
		}
		return msgVotes;
	}
	
}
