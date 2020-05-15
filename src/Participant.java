public class Participant {

    public static void main(String[] args) {
        if (args.length < 4 ) throw new IllegalArgumentException("Can't have less than 4 args!");

        int coordPort = Integer.parseInt(args[0]);
        int loggerPort = Integer.parseInt(args[1]);
        int partPort = Integer.parseInt(args[2]);
        int partTimeout = Integer.parseInt(args[3]);

    }
}
