import ticket.TicketBuilder;
import utility.CSVReaderAndWriter;
import utility.ConsoleAndFileParser;
import utility.ConsoleWriter;
import utility.Executor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            CSVReaderAndWriter csvRW = new CSVReaderAndWriter();
            if (args.length > 0)
                csvRW.setFile(args[0]);
            ConsoleWriter cw = new ConsoleWriter();
            csvRW.setFile("data.csv");
            Executor ex = new Executor(csvRW, cw);
            ConsoleAndFileParser cr = new ConsoleAndFileParser(ex, cw);
            ex.setConsoleReader(cr);
            ex.createTQFromCSV();
            ex.read();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}