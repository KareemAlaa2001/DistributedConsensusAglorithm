import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    volatile List<Socket> connectedSockets;
    private volatile boolean allConnected;
    private volatile ArrayList<OutcomeMessage> outcomeMessages;
    private volatile String finalOutcome;
    CoordinatorLogger logger;

    public Coordinator(int coordPort, int loggerPort, int numParticipants, int timeout, List<String> options) throws IOException {
        this.coordPort = coordPort;
        this.loggerPort = loggerPort;
        this.numParticipants = numParticipants;
        this.voteOptions = options;
        connectedPorts = new HashMap<>();
        outcomeMessages = new ArrayList<>();
        allConnected = false;
        finalOutcome = "";
        CoordinatorLogger.initLogger(loggerPort,coordPort,timeout);
        logger = CoordinatorLogger.getLogger();
        connectedSockets = new ArrayList<>();
    }

    //  Listens out for client connections, making sure joins are sent.
    public void startListening(int port) throws IOException
    {
        ServerSocket listener = new ServerSocket(port);
        logger.startedListening(port);
        while (!checkAllConnected()) {
            Socket client = listener.accept();
            logger.connectionAccepted(client.getPort());
            CoordinatorThread clientThread = new CoordinatorThread(client);
            clientThread.start();
        }
        System.out.println("Coordinator reporting all connected, closing listener server socket");
        listener.close();
    }

    private synchronized boolean checkAllConnected() {
        if (allConnected) return true;
        else return false;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 5 ) throw new IllegalArgumentException("Can't have less than 5 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int numParticipants = Integer.parseInt(args[2]);
        int timeout = Integer.parseInt(args[3]);

        List<String> options = Arrays.asList(Arrays.copyOfRange(args, 4, args.length));

        Coordinator coordinator = new Coordinator(coordPort,loggerPort,numParticipants,timeout, options);
        coordinator.startListening(coordPort);
    }


    private synchronized boolean register(Integer port, Socket portSocket, PrintWriter outWriter) {
        if (this.connectedPorts.size() >= numParticipants)
            return false;
        if (connectedPorts.containsKey(port)) {
            return false;
        }
        try {
            connectedPorts.put(port, outWriter);
            connectedSockets.add(portSocket);
        }
        catch (NullPointerException e) {
            return false;
        }

        if (connectedPorts.size() == numParticipants) {
            allConnected = true;
            broadcastParticipantDetails();
            broadcastVoteRequest();
            notifyAll();
            System.out.println("All participants have joined the coordinator");
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

        List<Integer> portList = new ArrayList<>(connectedPorts.keySet());

        for (Integer port: portList) {
            logger.detailsSent(port, portList);
        }

        broadcastMessage(detailsMsg);

    }

    private synchronized void updateOutcomes(OutcomeMessage outcome) {
        if (this.outcomeMessages.size() < this.numParticipants)
            this.outcomeMessages.add(outcome);
        else throw new IllegalStateException("Somehow received an extra outcome message!"); // todo decide whether to throw error

        if (this.outcomeMessages.size() == this.numParticipants) {
            decideFinalOutcome();
        }
    }

    private synchronized boolean decideFinalOutcome() {
        String outcome = this.outcomeMessages.get(0).getOutcome();

        for (OutcomeMessage outcomeMessage : this.outcomeMessages) {
            if (!outcomeMessage.getOutcome().equals(outcome)) {
                System.out.println("Somehow one of the outcomes received by the coordinator doesn't match the rest!!");
                return false;
            }
        }

        this.finalOutcome = outcome;
        System.out.println("Coordinator has decided its final outcome to be: " + outcome);
        return true;
    }

    //  Sends out vote requests to all participants with voting options, message: "VOTE_OPTIONS <option1> <option2> ..."
    private synchronized void broadcastVoteRequest() {
        if (!allConnected) throw new IllegalStateException("Not all participants have been connected!");
        String optionsMsg = "VOTE_OPTIONS";

        for (String voteOption : voteOptions) {
            optionsMsg += " " + voteOption;
        }

        List<Integer> portList = new ArrayList<>(connectedPorts.keySet());

        for (Integer port: portList) {
            logger.voteOptionsSent(port, voteOptions);
        }

        broadcastMessage(optionsMsg);
    }

    private synchronized void broadcastMessage(String msg) {
        for (Map.Entry<Integer,PrintWriter> entry: connectedPorts.entrySet()) {
            entry.getValue().println(msg);
            entry.getValue().flush();
        }

        for (Socket socket: this.connectedSockets) {
            logger.messageSent(socket.getPort(), msg);
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
            //  buffered reader for string input from participant
            inpReader = new BufferedReader( new InputStreamReader( client.getInputStream() ) );

            //  print writer for string output to participant
            outWriter = new PrintWriter( new OutputStreamWriter( client.getOutputStream() ) );

            outWriter.flush();

            //todo figure out other params this will need
        }

        public void run() {
            try {

                String firstMessage = inpReader.readLine();
                logger.messageReceived(clientSocket.getPort(),firstMessage);

                if (!isJoinMessage(firstMessage)) {
                    clientSocket.close();
                    return;
                }

                JoinMessage joinMessage = (JoinMessage) MsgParser.parseMessage(firstMessage);
                logger.joinReceived(joinMessage.getSenderPort());


                if (!register(joinMessage.getSenderPort(), this.clientSocket, this.outWriter)) {
                    System.out.println("Problem encountered by coordinator while trying to register participant " + joinMessage.getSenderPort());
                    clientSocket.close();
                    return;
                }

                waitUntilAllNotified();

                String recMsg = "";
                try {
                    recMsg = inpReader.readLine();
                } catch (SocketException se) {
                    logger.participantCrashed(joinMessage.getSenderPort());
                    clientSocket.close();
                    return;
                }

                logger.messageReceived(clientSocket.getPort(),recMsg);


                OutcomeMessage outcome = (OutcomeMessage) MsgParser.parseMessage(recMsg);
                logger.outcomeReceived(joinMessage.getSenderPort(),outcome.getOutcome());

                System.out.println("Coordinator received outcome " + outcome.getOutcome() + " from participant " + joinMessage.getSenderPort());
                updateOutcomes(outcome);

                clientSocket.close();
                System.out.println("Exiting coordinator program");
                System.exit(0);

            } catch (IOException ioe) {
                ioe.printStackTrace();
                //  todo
            }
        }
    }
}
