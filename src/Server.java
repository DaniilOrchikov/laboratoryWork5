import utility.CSVReaderAndWriter;
import utility.ConsoleWriter;
import utility.Executor;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CSVReaderAndWriter csvRW = new CSVReaderAndWriter();
        if (args.length > 0)
            csvRW.setFile(args[0]);
        ConsoleWriter cw = new ConsoleWriter();
        csvRW.setFile("data.csv");
        Executor ex = new Executor(csvRW, cw);
        ex.createTQFromCSV();
        ex.acceptingConnections();
    }
}
