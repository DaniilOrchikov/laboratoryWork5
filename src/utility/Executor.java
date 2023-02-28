package utility;

import ticket.Ticket;
import ticket.TicketType;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

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
    /**
     * Поле {@link CSVReaderAndWriter}
     */
    private final CSVReaderAndWriter csvRW;
    private final ConsoleWriter cw;
    private ServerSocket serv;


    public Executor(CSVReaderAndWriter csvRW, ConsoleWriter cw) throws IOException {
        this.csvRW = csvRW;
        this.cw = cw;
        serv = new ServerSocket(5454);
    }

    public void acceptingConnections() throws IOException, ClassNotFoundException {
        while (true) {
            Socket sock = serv.accept();
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            Command command = (Command) ois.readObject();
            Answer answer;
            if (command.hasTicket) answer = commandExecutionWithElement(command);
            else answer = commandExecution(command);
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            oos.writeObject(answer);
        }
    }

    private void answer(SelectionKey key)
            throws IOException, ClassNotFoundException {
        SocketChannel client = (SocketChannel) key.channel();
        ObjectInputStream ois = new ObjectInputStream(client.socket().getInputStream());
        Command command = (Command) ois.readObject();
        Answer answer;
        if (command.hasTicket) answer = commandExecutionWithElement(command);
        else answer = commandExecution(command);
        ObjectOutputStream oos = new ObjectOutputStream(client.socket().getOutputStream());
        oos.writeObject(answer);
    }

    private void register(Selector selector, ServerSocketChannel serverSocket)
            throws IOException {
        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
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
    public Answer commandExecution(Command command) {
        switch (command.getCommand()[0]) {
            case ("show"):
                StringBuilder str = new StringBuilder();
                tv.getAll().stream().forEach(t -> str.append(t).append("\n"));
                return new Answer(str.toString());
            case ("clear"):
                tv.clear();
                cw.println("Коллекция очищена");
                return new Answer("Коллекция очищена");
            case ("remove_first"):
                if (tv.remove(0))
                    return new Answer("Первый элемент удален");
                else
                    return new Answer("Массив пустой");
            case ("remove_at"):
                if (tv.remove(Integer.parseInt(command.getCommand()[1])))
                    return new Answer("Элемент под индексом " + command.getCommand()[1] + " удален");
                else
                    return new Answer("Индекс выходит за границы массива");
            case ("remove_by_id"):
                long id = Long.parseLong(command.getCommand()[1]);
                if (!tv.validId(id))
                    return new Answer("Неверный id");
                tv.removeById(id);
                return new Answer(String.format("Элемент с id %s удален", id));
            case ("min_by_venue"):
                return new Answer(tv.getMinByVenue());
            case ("filter_contains_name"):
                StringBuilder sb = new StringBuilder();
                String name;
                if (command.getCommand().length > 1) name = command.getCommand()[1];
                else name = "";
                for (Ticket t : tv.filterContainsName(name)) sb.append(t.toString());
                return new Answer(sb.toString());
            case ("filter_less_than_price"):
                sb = new StringBuilder();
                int price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterLessThanPrice(price)) sb.append(t.toString());
                return new Answer(sb.toString());
            case ("filter_by_price"):
                sb = new StringBuilder();
                price = Integer.parseInt(command.getCommand()[1]);
                for (Ticket t : tv.filterByPrice(price)) cw.printIgnoringPrintStatus(t.toString());
                return new Answer(sb.toString());
            case ("save"):
                if (csvRW.writeToCSV(tv.getAll())) return new Answer("Сохранение прошло успешно");
                else return new Answer("Не удалось сохранить данные в связи с ошибкой записи в файл");
            case ("info"):
                return new Answer(tv.getInfo());
            case ("count_greater_than_type"):
                return new Answer(String.valueOf(tv.getCountGreaterThanType(TicketType.valueOf(command.getCommand()[1]))));
            case ("print_field_ascending_type"):
                return new Answer(tv.getFieldAscendingType());
        }
        return new Answer("error");
    }

    /**
     * Исполнение команд, требующих создание объекта класса {@link Ticket}.
     * Команды - <b>add</b>, <b>update</b>, <b>add_if_max</b>, <b>add_if_min</b>, <b>remove_lower</b>.<br>
     * Передаются параметры нужные для создания объекта класса {@link Ticket}({@link Ticket#Ticket})
     *
     * @param command массив строк
     */
    public Answer commandExecutionWithElement(Command command) {
        switch (command.getCommand()[0]) {
            case ("add"):
                if (tv.add(command.getTicket())) return new Answer("Объект добавлен");
                else return new Answer("Объект не добавлен. Неоригинальный id");
            case ("update"):
                long id = Long.parseLong(command.getCommand()[1]);
                tv.update(command.getTicket(), id);
                return new Answer("Объект обновлен");
            case ("add_if_max"):
                if (tv.addIfMax(command.getTicket())) return new Answer("Объект добавлен");
                else return new Answer("Объект не добавлен");
            case ("add_if_min"):
                if (tv.addIfMin(command.getTicket())) return new Answer("Объект добавлен");
                else return new Answer("Объект не добавлен");
            case ("remove_lower"):
                return new Answer("Удалено " + tv.removeLower(command.getTicket()) + " элементов");
        }
        return new Answer("error");
    }


    /**
     * @see TicketVector#validId
     */
    boolean validId(long id) {
        return tv.validId(id);
    }
}
