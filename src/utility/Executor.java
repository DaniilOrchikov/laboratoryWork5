package utility;

import command.Command;
import command.CommandWithTicket;
import ticket.Ticket;
import ticket.TicketType;
import ticket.VenueType;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;

/**
 * Класс исполняющий команды, которые требуют обратиться к коллекции ({@link TicketVector}), управляющий работой {@link ConsoleAndFileParser} и {@link CSVReaderAndWriter}.
 * Поля - <b>exit</b>, <b>tv</b>, <b>cr</b> и <b>csvRW</b>
 */
public class Executor {
    /**
     * Поле {@link TicketVector}
     */
    private final TicketVector tv = new TicketVector();
    /**
     * Поле {@link ConsoleAndFileParser}
     */
    private ConsoleAndFileParser cp;
    /**
     * Поле {@link CSVReaderAndWriter}
     */
    private final CSVReaderAndWriter csvRW;
    private final ConsoleWriter cw;

    public Executor(CSVReaderAndWriter csvRW, ConsoleWriter cw) {
        this.csvRW = csvRW;
        this.cw = cw;
    }

    /**
     * С помощью {@link CSVReaderAndWriter} считывает объекты из csv файла и добавляет в вектор ({@link TicketVector})
     */
    public void createTQFromCSV() {
        long invalidId = 0, invalidTicket = 0;
        while (csvRW.hasNext()) {
            try {
                if (!tv.add(csvRW.nextTicket())) invalidId++;
            } catch (InputMismatchException e) {
                invalidTicket++;
            } catch (NoSuchElementException ignored) {
            }
        }
        if (invalidId > 0)
            cw.println("Объектов не добавлено по причине неоригинального id - " + invalidId);
        if (invalidTicket > 0)
            cw.println("Объектов не добавлено по причине несоответствия структуре - " + invalidTicket);
    }

    public void setConsoleReader(ConsoleAndFileParser cr) {
        this.cp = cr;
    }

//    /**
//     * Мгновенный выход из программы
//     */
//    public void exit() {
//        System.exit(0);
//    }

    /**
     * Исполнение команд не требующих создания объекта класса {@link Ticket}.<br>
     * Команды - <b>show</b>, <b>clear</b>, <b>remove_first</b>, <b>remove_at</b>, <b>remove_by_id</b>, <b>min_by_venue</b>, <b>filter_contains_name</b>, <b>filter_less_than_price</b>, <b>filter_by_price</b>, <b>save</b>, <b>info</b>, <b>count_greater_than_type</b>, <b>print_field_ascending_type</b>
     *
     * @param command массив строк
     */
    public void commandExecution(Command command) {
        switch (command.getCommand()[0]) {
            case ("show"):
                StringBuilder str = new StringBuilder();
                tv.getAll().stream().forEach(t -> str.append(t).append("\n"));
                cw.printIgnoringPrintStatus(str.toString());
                break;
            case ("clear"):
                tv.clear();
                cw.println("Коллекция очищена");
                break;
            case ("remove_first"):
                if (tv.remove(0))
                    cw.println("Первый элемент удален");
                else
                    cw.println("Массив пустой");
                break;
            case ("remove_at"):
                if (tv.remove(Integer.parseInt(command.getCommand()[1])))
                    cw.println("Элемент под индексом " + command.getCommand()[1] + " удален");
                else
                    cw.println("Индекс выходит за границы массива");
                break;
            case ("remove_by_id"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!tv.validId(id)) {
                    cw.println("Неверный id");
                    return;
                }
                tv.removeById(id);
                cw.println(String.format("Элемент с id %s удален", id));
                break;
            case ("min_by_venue"):
                cw.printIgnoringPrintStatus(tv.getMinByVenue());
                break;
            case ("filter_contains_name"):
                String name;
                if (command.getCommand().length > 1) name = command.getCommand()[1];
                else name = "";
                for (Ticket t : tv.filterContainsName(name)) cw.printIgnoringPrintStatus(t.toString());
                break;
            case ("filter_less_than_price"):
                int price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterLessThanPrice(price)) cw.printIgnoringPrintStatus(t.toString());
                break;
            case ("filter_by_price"):
                price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterByPrice(price)) cw.printIgnoringPrintStatus(t.toString());
                break;
            case ("save"):
                if (csvRW.writeToCSV(tv.getAll())) cw.println("Сохранение прошло успешно");
                else cw.println("Не удалось сохранить данные в связи с ошибкой записи в файл");
                break;
            case ("info"):
                cw.printIgnoringPrintStatus(tv.getInfo());
                break;
            case ("count_greater_than_type"):
                cw.printIgnoringPrintStatus(String.valueOf(tv.getCountGreaterThanType(TicketType.valueOf(command.getCommand()[1]))));
                break;
            case ("print_field_ascending_type"):
                cw.printIgnoringPrintStatus(tv.getFieldAscendingType());
        }
    }

    /**
     * Исполнение команд, требующих создание объекта класса {@link Ticket}.
     * Команды - <b>add</b>, <b>update</b>, <b>add_if_max</b>, <b>add_if_min</b>, <b>remove_lower</b>.<br>
     * Передаются параметры нужные для создания объекта класса {@link Ticket}({@link Ticket#Ticket})
     *
     * @param command массив строк
     */
    public void commandExecutionWithElement(CommandWithTicket command) {
        switch (command.getCommand()[0]) {
            case ("add"):;
                if (tv.add(command.getTicket())) cw.println("Объект добавлен");
                else cw.println("Объект не добавлен. Неоригинальный id");
                break;
            case ("update"):
                long id = Long.parseLong(command.getCommand()[1]);
                tv.update(command.getTicket(), id);
                cw.println("Объект обновлен");
                break;
            case ("add_if_max"):
                if (tv.addIfMax(command.getTicket())) cw.println("Объект добавлен");
                else cw.println("Объект не добавлен");
                break;
            case ("add_if_min"):
                if (tv.addIfMin(command.getTicket())) cw.println("Объект добавлен");
                else cw.println("Объект не добавлен");
                break;
            case ("remove_lower"):
                cw.println("Удалено " + tv.removeLower(command.getTicket()) + " элементов");
                break;
        }
    }

    /**
     * Цикл считывания команд с помощью {@link ConsoleAndFileParser}. Цикл завершается, если {@link Executor#exit} == true или если введен символ конца ввода
     */
    public void read() throws IOException {
        while (true) {
            String[] command = cp.read();
            cp.nextCommand(command);
        }
    }

    /**
     * @see TicketVector#validId
     */
    boolean validId(long id) {
        return tv.validId(id);
    }
}
