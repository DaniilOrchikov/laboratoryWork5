import utility.ConsoleAndFileParser;
import utility.ConsoleWriter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            ConsoleWriter cw = new ConsoleWriter();
            ConsoleAndFileParser cr = new ConsoleAndFileParser(cw);
            cr.readingCycle();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}