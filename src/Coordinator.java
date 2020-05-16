import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*  ROLE: initiates a run of the consensus algorithm and collects the vote outcome
 *  Actual algorithm runs between participants.
 *   */
public class Coordinator {

    private int coordPort;
    private int loggerPort;
    private int numParticipants;
    private List<String> voteOptions;

    public Coordinator(int coordPort, int loggerPort, int numParticipants, List<String> options) {
        this.coordPort = coordPort;
        this.loggerPort = loggerPort;
        this.numParticipants = numParticipants;
        this.voteOptions = options;
    }

    public static void main(String[] args) {
        if (args.length < 4 ) throw new IllegalArgumentException("Can't have less than 4 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int numParticipants = Integer.parseInt(args[2]);


        //  TODO add code for dealing with VOTING options
        List<String> options = Arrays.asList(Arrays.copyOfRange(args, 3, args.length));


    }


    /*  Send details (ports) of all other participants to each participant
     *  Executed after all participants join. Broadcasts message "DETAILS <port1> <port2> ...."
     *
     *  @param participantPorts list of participant ports
     *
     * */
    public void broadcastParticipantDetails(List<Integer> participantPorts) {
        //  TODO
    }

    //  Sends out vote requests to all participants with voting options, message: "VOTE_OPTIONS <option1> <option2> ..."
    public void broadcastVoteRequest(List<String> voteOptions) {
        //  TODO
    }
}
