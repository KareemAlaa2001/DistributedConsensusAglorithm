import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*  */


public class Coordinator {

    private int coordPort;
    private int numParticipants;

    public static void main(String[] args) {
        if (args.length < 3 ) throw new IllegalArgumentException("Can't have less than 3 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int numParticipants = Integer.parseInt(args[2]);


        //  TODO add code for dealing with options
        List<String> options = new ArrayList<>();
        if (args.length > 3)
            options = Arrays.asList(Arrays.copyOfRange(args, 3, args.length));
    }
}
