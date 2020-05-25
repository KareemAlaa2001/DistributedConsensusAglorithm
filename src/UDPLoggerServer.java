import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class UDPLoggerServer {
    int loggerPort;
    File logFile;
    PrintStream logWriter;

    public UDPLoggerServer(int loggerPort, long launchTime) throws IOException {
        this.loggerPort = loggerPort;
        this.logFile = new File("logger_server_" + launchTime + ".log");
        this.logWriter = new PrintStream(logFile);
    }


    public static void main(String[] args) throws IOException {
        long launchTime = System.currentTimeMillis();
        if (args.length == 0) throw new IllegalArgumentException("Must pass an argument for logger port!");
        int loggerPort = Integer.parseInt(args[0]);
        UDPLoggerServer loggerServer = new UDPLoggerServer(loggerPort,launchTime);
    }
}
