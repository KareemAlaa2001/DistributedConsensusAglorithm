import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/*  ROLE: initiates a run of the consensus algorithm and collects the vote outcome
 *  Actual algorithm runs between participants.
 *   */
public class Coordinator {

    private int coordPort;
    private int loggerPort;
    private int numParticipants;
    private List<String> voteOptions;
    volatile Map<Integer, PrintWriter> connectedPorts;
    private volatile boolean allConnected;
    private volatile ArrayList<OutcomeMessage> outcomeMessages;
    private volatile String finalOutcome;

    public Coordinator(int coordPort, int loggerPort, int numParticipants, List<String> options) {
        this.coordPort = coordPort;
        this.loggerPort = loggerPort;
        this.numParticipants = numParticipants;
        this.voteOptions = options;
        connectedPorts = new HashMap<>();
        outcomeMessages = new ArrayList<>();
        allConnected = false;
        finalOutcome = "";
    }

    //  Listens out for client connections, making sure joins are sent.
    public void startListening(int port) throws IOException
    {
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            Socket client = listener.accept();
            CoordinatorThread clientThread = new CoordinatorThread(client);
            clientThread.start();
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4 ) throw new IllegalArgumentException("Can't have less than 4 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int numParticipants = Integer.parseInt(args[2]);

        List<String> options = Arrays.asList(Arrays.copyOfRange(args, 3, args.length));

        Coordinator coordinator = new Coordinator(coordPort,loggerPort,numParticipants,options);
        coordinator.startListening(coordPort);
    }


    private synchronized boolean register(Integer port, PrintWriter outWriter) {
        if (this.connectedPorts.size() >= numParticipants)
            return false;
        if (connectedPorts.containsKey(port)) {
            return false;
        }
        try {
            connectedPorts.put(port, outWriter);
        }
        catch (NullPointerException e) {
            return false;
        }

        if (connectedPorts.size() == numParticipants) {
            allConnected = true;
            broadcastParticipantDetails();
            broadcastVoteRequest();
            notifyAll();
        }

        return true;
    }

    private boolean isJoinMessage(String msg) {
        if (MsgParser.parseMessage(msg) instanceof JoinMessage)
            return true;
        else
            return false;
    }


    /*  Send details (ports) of all other participants to each participant
     *  Executed after all participants join. Broadcasts message "DETAILS <port1> <port2> ...."
     *
     *  @param participantPorts list of participant ports
     *
     * */
    private synchronized void broadcastParticipantDetails() {
        if (!allConnected) throw new IllegalStateException("Not all participants have been connected!");
        String detailsMsg = "DETAILS";

        for (Integer i: connectedPorts.keySet()) {
            detailsMsg += " " + i.toString();
        }

        broadcastMessage(detailsMsg);

    }

    private synchronized void updateOutcomes(OutcomeMessage outcome) {
        if (this.outcomeMessages.size() < this.numParticipants)
            this.outcomeMessages.add(outcome);
        else return; // todo decide whether to throw error

        if (this.outcomeMessages.size() == this.numParticipants) {
            decideFinalOutcome();
        }
    }

    private synchronized boolean decideFinalOutcome() {
        String outcome = this.outcomeMessages.get(0).getOutcome();

        for (OutcomeMessage outcomeMessage : this.outcomeMessages) {
            if (!outcomeMessage.getOutcome().equals(outcome)) return false;
        }

        this.finalOutcome = outcome;
        return true;
    }

    //  Sends out vote requests to all participants with voting options, message: "VOTE_OPTIONS <option1> <option2> ..."
    private synchronized void broadcastVoteRequest() {
        if (!allConnected) throw new IllegalStateException("Not all participants have been connected!");
        String optionsMsg = "VOTE_OPTIONS";

        for (String voteOption : voteOptions) {
            optionsMsg += " " + voteOption;
        }

        broadcastMessage(optionsMsg);
    }

    private synchronized void broadcastMessage(String msg) {
        for (Map.Entry<Integer,PrintWriter> entry: connectedPorts.entrySet()) {
            entry.getValue().println(msg);
        }
    }


    private synchronized void waitUntilAllNotified() {
        while(allConnected == false) {
            try {
                wait();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }

    private class CoordinatorThread extends Thread {
        Socket clientSocket;
        BufferedReader inpReader;
        PrintWriter outWriter;


        public CoordinatorThread(Socket client) throws IOException {
            clientSocket = client;
            //  buffered reader for string input from client
            inpReader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );

            //  print writer for string output to client
            outWriter = new PrintWriter( new OutputStreamWriter( client.getOutputStream() ) );

            outWriter.flush();

            //todo figure out other params this will need
        }

        public void run() {
            try {
                String firstMessage = inpReader.readLine();
                if (!isJoinMessage(firstMessage)) {
                    clientSocket.close();
                    return;
                }

                JoinMessage joinMessage = (JoinMessage) MsgParser.parseMessage(firstMessage);

                if (!register(joinMessage.getSenderPort(),this.outWriter)) {
                    clientSocket.close();
                    return;
                }

                waitUntilAllNotified();

                System.out.println("Thread " + joinMessage.getSenderPort() + "Has been notified of all participants joining");

                String recMsg = "";
                recMsg = inpReader.readLine();

                while (!(MsgParser.parseMessage(recMsg) instanceof OutcomeMessage)) {
                    //  invalid message received, todo decide whether to ignore or throw exception
                    recMsg = inpReader.readLine();
                }

                OutcomeMessage outcome = (OutcomeMessage) MsgParser.parseMessage(recMsg);

                updateOutcomes(outcome);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                //  todo
            }
        }
    }
}
