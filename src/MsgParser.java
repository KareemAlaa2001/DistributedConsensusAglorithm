import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class MsgParser {
    public MsgParser() {

    }


    public static Message parseMessage(String msg) {

        StringTokenizer tokenizer = new StringTokenizer(msg);
        if (!(tokenizer.hasMoreTokens())) return null; // TODO decide whether to return null or exception

        String firstToken = tokenizer.nextToken();

        if (firstToken.equals("JOIN")) {
            if (tokenizer.hasMoreTokens()) return new JoinMessage(msg, Integer.parseInt(tokenizer.nextToken()));
            else return null;
        } else if (firstToken.equals("DETAILS")) {
            //  TODO could implement robustness in checking number of ints
            if (tokenizer.hasMoreTokens()) return parseDetailsMessage(msg,tokenizer);
            else return null;
        } else if (firstToken.equals("VOTE_OPTIONS")) {
            if (tokenizer.hasMoreTokens()) return parseOptionsMessage(msg,tokenizer);
            else return null;
        } else if (firstToken.equals("VOTE")) {
            if (tokenizer.hasMoreTokens()) return parseVoteMessage(msg,tokenizer);
            else return null;
        }
    }

    private static Message parseVoteMessage(String msg, StringTokenizer tokenizer) {

        Map<Integer, String> votes;

        do {
            options.add(tokenizer.nextToken());
        } while (tokenizer.hasMoreTokens());

        return new OptionsMessage(msg, options);
    }

    private static OptionsMessage parseOptionsMessage(String msg, StringTokenizer tokenizer) {
        List<String> options = new ArrayList<>();

        do {
            options.add(tokenizer.nextToken());
        } while (tokenizer.hasMoreTokens());

        return new OptionsMessage(msg, options);
    }

    private static DetailsMessage parseDetailsMessage(String message, StringTokenizer tokenizer) {
        List<Integer> ports = new ArrayList<>();
        do {
            ports.add(Integer.parseInt(tokenizer.nextToken()));
        } while (tokenizer.hasMoreTokens());

        return new DetailsMessage(message,ports);
    }


}

abstract class Message {
    String message;
}

class JoinMessage extends Message {
    int senderPort;

    public JoinMessage(String message, int senderPort) {
        this.message = message;
        this.senderPort = senderPort;
    }
}

class DetailsMessage extends Message {
    List<Integer> ports;

    public DetailsMessage(String message, List<Integer> ports) {
        this.message = message;
        this.ports = ports;
    }
}

class OptionsMessage extends Message {
    List<String> options;

    public OptionsMessage(String message,List<String> options) {
        this.message = message;
        this.options = options;
    }
}

class VoteMessage extends Message {
    int senderPort;
    Map<Integer, String> votes;

    public VoteMessage(String message, int senderPort, Map<Integer,String > votes) {
        this.message = message;
        this.senderPort = senderPort;
        this.votes  = votes;
    }
}

class OutcomeMessage extends Message {
    String outcome;
    List<Integer> ports;

    public OutcomeMessage(String message, String outcome, List<Integer> ports) {
        this.message = message;
        this.outcome = outcome;
        this.ports = ports;
    }
}