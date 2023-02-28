package utility;

import ticket.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Класс отвечающий за чтение и запись в csv файл
 */
public class CSVReaderAndWriter {
    /**
     * Поле имя файла
     */
    private String fileName = "";
    /**
     * Поле сканера
     */
    private Scanner scanner;
    /**
     * Поле символ разделения колонок в csv файле
     */
    private String separator = ";";

    /**
     * Пытается установить поток считывания из файла
     *
     * @param fileName имя csv файла, в который будут записываться, и из которого будут читаться данные
     */
    public void setFile(String fileName) {
        this.fileName = fileName;
        try {
            File f = new File(this.fileName);
            if (!f.exists()) {
                System.out.println("Неверное имя файла");
            } else if (f.isDirectory()) {
                System.out.println("Невозможно исполнить директорию");
            } else if (!f.canRead()) {
                System.out.println("Недостаточно прав для чтения из файла");
            } else {
                scanner = new Scanner(Paths.get(fileName));
                scanner.useDelimiter("\n");
            }
        } catch (IOException e) {
            System.out.println("Не удалось настроить поток чтения из файла");
        }
    }

    /**
     * @return Возвращает true, если можно продолжить считывание, и false - если нельзя
     */
    public boolean hasNext() {
        if (scanner == null) return false;
        return scanner.hasNext();
    }

    /**
     * @return возвращает следующий объект из csv фала
     */
    public Ticket nextTicket() {
        return parseCSVLine(scanner.next());
    }

    /**
     * @param line строка, из которой необходимо создать объект
     * @return возвращает объект класса {@link Ticket}
     */
    private Ticket parseCSVLine(String line) {
        Scanner scanner = new Scanner(line.trim());
        scanner.useDelimiter(separator);
        long id = scanner.nextLong();
        String name = scanner.next();
        if (name.equals("")) throw new InputMismatchException();
        Coordinates coordinates = new Coordinates(scanner.nextInt(), scanner.nextInt());
        java.time.LocalDateTime creationDate = LocalDateTime.parse(scanner.next(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        int price = scanner.nextInt();
        TicketType type = TicketType.valueOf(scanner.next());
        long vId = scanner.nextLong();
        Long capacity = scanner.nextLong();
        VenueType vType = VenueType.valueOf(scanner.next());
        String street = scanner.next();
        String zipCode;
        try {
            zipCode = scanner.next();
        } catch (NoSuchElementException e) {
            throw new InputMismatchException();
        }
        if (street.equals("") || zipCode.equals("")) throw new InputMismatchException();
        Venue venue = new Venue(vId, name, capacity, vType, new Address(street, zipCode));
        return new Ticket(id, name, coordinates, creationDate, price, type, venue);
    }

    /**
     * Запись в файл в формате csv
     *
     * @param tickets массив объектов класса {@link Ticket}, которые необходимо записать в файл
     * @return возвращает true, если удалось настроить поток записи и записать объекты в файл, false - если что-то пошло не так
     */
    public boolean writeToCSV(Ticket[] tickets) {
        File f = new File(fileName);
        if (!f.exists() || f.isDirectory()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                System.out.println("Не удалось создать файл");
                return false;
            }
        } else if (!f.canWrite()) {
            System.out.println("Недостаточно прав на файл " + fileName);
            return false;
        }
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Ticket t : tickets) {
                writer.write(t.toCSVFormat(";") + "\n");
            }
            return true;
        } catch (IOException e) {
            return false;

        }
    }
}
