package utility;

import ticket.Ticket;
import ticket.TicketType;
import ticket.VenueType;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Класс исполняющий команды, которые требуют обратиться к коллекции ({@link TicketVector}), управляющий работой {@link ConsoleReaderAndWriter} и {@link CSVReaderAndWriter}.
 * Поля - <b>exit</b>, <b>tv</b>, <b>cr</b> и <b>csvRW</b>
 */
public class Executor {
    /**
     * Поле выхода из цикла исполнения программы.
     * Если станет true, программа завершится
     */
    private boolean exit = false;
    /**
     * Поле {@link TicketVector}
     */
    private final TicketVector tv = new TicketVector();
    /**
     * Поле {@link ConsoleReaderAndWriter}
     */
    private ConsoleReaderAndWriter cr;
    /**
     * Поле {@link CSVReaderAndWriter}
     */
    private final CSVReaderAndWriter csvRW;

    public Executor(CSVReaderAndWriter csvRW) {
        this.csvRW = csvRW;
    }

    /**
     * С помощью {@link CSVReaderAndWriter} считывает объекты из csv файла и добавляет в вектор ({@link TicketVector})
     */
    public void createTQFromCSV() {
        long invalidId = 0, invalidTicket = 0;
        while (csvRW.hasNext()) {
            try {
                if (!tv.add(csvRW.nextTicket())) invalidId++;
            } catch (InputMismatchException | IllegalArgumentException | DateTimeParseException e) {
                invalidTicket++;
            }catch (NoSuchElementException ignored){}
        }
        if (invalidId > 0)
            cr.println("Объектов не добавлено по причине неоригинального id - " + invalidId);
        if (invalidTicket > 0)
            cr.println("Объектов не добавлено по причине несоответствия структуре - " + invalidTicket);
    }

    public void setConsoleReader(ConsoleReaderAndWriter cr) {
        this.cr = cr;
    }

    public void exit() {
        this.exit = true;
    }

    /**
     * Исполнение команд не требующих создания объекта класса {@link Ticket}.<br>
     * Команды - <b>show</b>, <b>clear</b>, <b>remove_first</b>, <b>remove_at</b>, <b>remove_by_id</b>, <b>min_by_venue</b>, <b>filter_contains_name</b>, <b>filter_less_than_price</b>, <b>filter_by_price</b>, <b>save</b>, <b>info</b>, <b>count_greater_than_type</b>, <b>print_field_ascending_type</b>
     * @param command массив строк
     */
    public void commandExecution(String[] command) {
        switch (command[0]) {
            case ("show"):
                cr.printIgnoringPrintStatus(tv.print());
                break;
            case ("clear"):
                tv.clear();
                cr.println("Коллекция очищена");
                break;
            case ("remove_first"):
                if (tv.remove(0))
                    cr.println("Первый элемент удален");
                else
                    cr.println("Массив пустой");
                break;
            case ("remove_at"):
                if (tv.remove(Integer.parseInt(command[1])))
                    cr.println("Элемент под индексом " + command[1] + " удален");
                else
                    cr.println("Индекс выходит за границы массива");
                break;
            case ("remove_by_id"):
                long id = Long.parseLong(command[1]);
                if (!tv.validId(id)) {
                    cr.println("Неверный id");
                    return;
                }
                tv.removeById(id);
                cr.println(String.format("Элемент с id %s удален", id));
                break;
            case ("min_by_venue"):
                cr.printIgnoringPrintStatus(tv.getMinByVenue());
                break;
            case ("filter_contains_name"):
                String name;
                if (command.length > 1) name = command[1];
                else name = "";
                for (Ticket t : tv.filterContainsName(name)) cr.printIgnoringPrintStatus(t.toString());
                break;
            case ("filter_less_than_price"):
                int price = Integer.parseInt(command[1]);
                for (Ticket t : tv.filterLessThanPrice(price)) cr.printIgnoringPrintStatus(t.toString());
                break;
            case("filter_by_price"):
                price = Integer.parseInt(command[1]);
                for (Ticket t : tv.filterByPrice(price)) cr.printIgnoringPrintStatus(t.toString());
                break;
            case ("save"):
                if (csvRW.writeToCSV(tv.getAll())) cr.println("Сохранение прошло успешно");
                else cr.println("Не удалось сохранить данные в связи с ошибкой записи в файл");
                break;
            case ("info"):
                cr.printIgnoringPrintStatus(tv.getInfo());
                break;
            case("count_greater_than_type"):
                cr.printIgnoringPrintStatus(String.valueOf(tv.getCountGreaterThanType(TicketType.valueOf(command[1]))));
                break;
            case("print_field_ascending_type"):
                cr.printIgnoringPrintStatus(tv.getFieldAscendingType());
        }
    }

    /**
     * Исполнение команд, требующих создание объекта класса {@link Ticket}.
     * Перед исполнением данные проверяются на соответствие условиям и, при несоответствии заносятся в okList – массив содержащий 5 значений типа boolean, соответствующих полям <b>name</b>, <b>price</b>, <b>capacity</b>, <b>street</b>, <b>zipCode</b>. Если значение true - поле введено корректно, если false - поле нужно пересоздать. И вызывается метод {@link ConsoleReaderAndWriter#recreatingObject}.<br>
     * Команды - <b>add</b>, <b>update</b>, <b>add_if_max</b>, <b>add_if_min</b>, <b>remove_lower</b>.<br>
     * Передаются параметры нужные для создания объекта класса {@link Ticket}({@link Ticket#Ticket})
     * @param command массив строк
     */
    public void commandExecutionWithElement(String[] command, String name, long x, double y, int price, TicketType type, Long capacity, VenueType venueType, String street, String zipCode) {
        Ticket ticket;
        boolean[] okList = {true, true, true, true, true};
        if (name == null || name.isEmpty() || name.trim().length() == 0) {
            okList[0] = false;
        }
        if (price <= 0) {
            okList[1] = false;
        }
        if (capacity == null || capacity <= 0) {
            okList[2] = false;
        }
        if (street == null || street.isEmpty() || street.trim().length() == 0) {
            okList[3] = false;
        }
        if (zipCode == null) {
            okList[4] = false;
        }
        if (!(okList[0] & okList[1] & okList[2] & okList[3] & okList[4])) {
            cr.recreatingObject(command, okList, name, x, y, price, type, capacity, venueType, street, zipCode);
            return;
        } else cr.println("Объект создан корректно");
        switch (command[0]) {
            case ("add"):
                ticket = tv.createTicket(name, x, y, price, type, capacity, venueType, street, zipCode);
                if (tv.add(ticket)) cr.println("Объект добавлен");
                else cr.println("Объект не добавлен. Неоригинальный id");
                break;
            case ("update"):
                long id = Long.parseLong(command[1]);
                ticket = tv.createTicket(id, name, x, y, price, type, capacity, venueType, street, zipCode);
                tv.update(ticket, id);
                cr.println("Объект обновлен");
                break;
            case ("add_if_max"):
                ticket = tv.createTicket(name, x, y, price, type, capacity, venueType, street, zipCode);
                if (tv.addIfMax(ticket)) cr.println("Объект добавлен");
                else cr.println("Объект не добавлен");
                break;
            case ("add_if_min"):
                ticket = tv.createTicket(name, x, y, price, type, capacity, venueType, street, zipCode);
                if (tv.addIfMin(ticket)) cr.println("Объект добавлен");
                else cr.println("Объект не добавлен");
                break;
            case ("remove_lower"):
                ticket = tv.createTicket(name, x, y, price, type, capacity, venueType, street, zipCode);
                cr.println("Удалено " + tv.removeLower(ticket) + " элементов");
                break;
        }
    }

    /**
     * Цикл считывания команд с помощью {@link ConsoleReaderAndWriter}. Цикл завершается, если {@link Executor#exit} == true или если введен символ конца ввода
     */
    public void read() throws IOException {
        while (!exit) {
            try {
                String[] command = cr.read();
                cr.nextCommand(command);
            }
            catch (NoSuchElementException e){
                cr.printIgnoringPrintStatus("Экстренный выход");
                exit = true;
            }
        }
    }

    /**
     * @see TicketVector#validId
     */
    boolean validId(long id) {
        return tv.validId(id);
    }
}
