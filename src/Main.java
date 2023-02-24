import utility.CSVReaderAndWriter;
import utility.ConsoleReaderAndWriter;
import utility.Executor;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            CSVReaderAndWriter csvRW = new CSVReaderAndWriter();
            if (args.length > 0)
                csvRW.setFile(args[0]);
            Executor ex = new Executor(csvRW);
            ConsoleReaderAndWriter cr = new ConsoleReaderAndWriter(ex);
            ex.setConsoleReader(cr);
            ex.createTQFromCSV();
            ex.read();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}