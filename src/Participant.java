public class Participant {
    int coordPort;
    int loggerPort;
    int commsPort;
    int timeout;

    public Participant(int coordPort, int loggerPort, int commsPort,int timeout) {
        this.timeout = timeout;
        this.commsPort = commsPort;
        this.coordPort = coordPort;
        this.loggerPort = loggerPort;
    }



    public static void main(String[] args) {
        if (args.length < 4 ) throw new IllegalArgumentException("Can't have less than 4 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int partPort = Integer.parseInt(args[2]);
        int partTimeout = Integer.parseInt(args[3]);

    }
}
