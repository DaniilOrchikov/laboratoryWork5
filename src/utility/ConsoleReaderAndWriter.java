package utility;

import ticket.Ticket;
import ticket.TicketType;
import ticket.VenueType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;

/**
 * Класс для чтения и записи данных в консоль или файл с полями <b>in</b>, <b>scannerStack</b>, <b>fileNamesStack</b>, <b>ex</b>
 */
public class ConsoleReaderAndWriter {
    /**
     * Поле сканер ввода.
     * По умолчанию - системный ввод
     */
    private Scanner in = new Scanner(System.in);
    /**
     * Поле стек сканеров.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда в файле встречается команда исполнения нового файла, сюда помещается старый сканер
     */
    private final Stack<Scanner> scannerStack = new Stack<>();
    /**
     * Поле стек имен файлов.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда начинается чтение из файла сюда заносится его имя. При каждой попытке открытия нового потока считывания файла проверяется не занесено ли сюда его имя. Если это так - запрещается считывание.
     * Это исключает ситуации бесконечной рекурсии при чтении файлов.
     */
    private final Stack<String> fileNamesStack = new Stack<>();
    /**
     * Поле Исполнитель {@link Executor}
     */
    private final Executor ex;
    /**
     * Поле статуса ввода
     * 0 - стандартный ввод
     * 1 - ввод из файла
     * 2 - вспомогательный статус. Используется, когда при вводе из файла возникает ошибка и требуется ввод пользователя
     */
    private int inputStatus = 0;

    public ConsoleReaderAndWriter(Executor ex) {
        this.ex = ex;
    }

    public void setInputStatus(int status) {
        inputStatus = status;
    }

    /**
     * Проверяет не закончился ли считываемый файл. Если да - достает следующий сканер из {@link ConsoleReaderAndWriter#scannerStack}.
     * Если из стека достали последний сканер (ввод с консоли) устанавливает {@link ConsoleReaderAndWriter#inputStatus} 0
     */
    private void checkingScanner() {
        if (scannerStack.empty())
            setInputStatus(0);
        if (inputStatus == 1 || inputStatus == 2) {
            while (!in.hasNext()) {
                in = scannerStack.pop();
                fileNamesStack.pop();
                if (scannerStack.empty()) {
                    setInputStatus(0);
                    break;
                }
            }
        }
    }

    /**
     * Чтение следующей строки и разбиение ее на слова (разделитель - пробел)
     *
     * @return возвращает массив строк
     */
    public String[] read() {
        checkingScanner();
        return nextInput().split(" ");
    }

    /**
     * Считывание строки
     *
     * @return возвращает строку
     */
    private String nextInput() {
        print("Введите команду: ");
        String str = in.nextLine();
        str = str.trim();
        return str;
    }

    /**
     * При условии, что {@link ConsoleReaderAndWriter#inputStatus} != 1 печатает переданную строку с помощью System.out.println(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void println(String str) {
        if (inputStatus != 1) System.out.println(str);
    }

    /**
     * При условии, что {@link ConsoleReaderAndWriter#inputStatus} != 1 печатает переданную строку с помощью System.out.print(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void print(String str) {
        if (inputStatus != 1) System.out.print(str);
    }

    /**
     * Печатает переданную строку с помощью System.out.println(String)
     *
     * @param str строка, которую необходимо напечатать
     */
    public void printIgnoringPrintStatus(String str) {
        System.out.println(str);
    }

    /**
     * Проверяет, что строка соответствует одному из значений {@link TicketType}
     *
     * @param str строка
     * @return возвращает true если соответствует, false - если нет
     */
    private boolean validTicketType(String str) {
        for (TicketType c : TicketType.values()) {
            if (c.name().equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет, что строка соответствует одному из значений {@link VenueType}
     *
     * @param str строка
     * @return возвращает true если соответствует, false - если нет
     */
    private boolean validVenueType(String str) {
        for (VenueType c : VenueType.values()) {
            if (c.name().equals(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Если при создании объекта класса {@link Ticket} были переданы некорректные параметры можно пересоздать его с помощью этого метода.
     * Он автоматически заполнит корректно введенные поля и попросит ввести остальные.
     * Работает и при вводе с консоли и из файла.
     *
     * @param command введенная команда
     * @param okList  массив содержащий 5 значений типа boolean, соответствующих полям <b>name</b>, <b>price</b>, <b>capacity</b>, <b>street</b>, <b>zipCode</b>. Если значение true - поле введено корректно, если false - поле нужно пересоздать
     */
    public void recreatingObject(String[] command, boolean[] okList, String name, long x, double y, int price, TicketType type, Long capacity, VenueType venueType, String street, String zipCode) {
        if (inputStatus != 0)
            printIgnoringPrintStatus("В файле " + fileNamesStack.peek() + " введены неверные данные для создания объекта. Хотите пересоздать объект в ручном режиме? Все корректно введенные поля будут заполнены автоматически. При отказе исполнение файла прервется. \nДа - Y, нет - N");
        else
            println("Некоторые поля некорректны. Хотите пересоздать объект? Все корректно введенные поля будут заполнены автоматически.\nДа - Y, нет - N");
        if (!reentryAttempt()) return;
        if (!okList[0]) {
            print("Введите имя: ");
            name = in.nextLine();
        } else println("Введите имя: " + name);
        println("Введите первую координату: " + x);
        println("Введите вторую координату: " + y);
        if (!okList[1]) {
            while (true) {
                print("Введите стоимость: ");
                try {
                    price = Integer.parseInt(in.nextLine());
                } catch (NumberFormatException e) {
                    println("Неверный формат ввода. Ожидалось целое число");
                    continue;
                }
                break;
            }
        } else println("Введите стоимость: " + price);
        println("Введите тип билета " + Arrays.toString(TicketType.values()) + ": " + type.toString());
        if (!okList[2]) {
            while (true) {
                print("Введите вместимость: ");
                try {
                    capacity = Long.parseLong(in.nextLine());
                } catch (NumberFormatException e) {
                    println("Неверный формат ввода. Ожидалось целое число");
                    continue;
                }
                break;
            }
        } else println("Введите вместимость: " + capacity);
        println("Введите тип места назначения " + Arrays.toString(VenueType.values()) + ": " + venueType);
        if (!okList[3]) {
            print("Введите название улицы: ");
            street = in.nextLine();
        } else println("Введите название улицы: " + street);
        if (!okList[4]) {
            print("Введите почтовый индекс: ");
            zipCode = in.nextLine();
        } else println("Введите почтовый индекс: " + zipCode);
        in = scannerStack.pop();
        if (!scannerStack.empty()) {
            setInputStatus(1);
        }
        ex.commandExecutionWithElement(command, name, x, y, price, type, capacity, venueType, street, zipCode);
    }

    /**
     * Выборка команды.
     * Если команда не требует обращения к коллекции - исполнение команды.
     * Если команда не требует ввод объекта - команда передается Исполнителю {@link Executor}({@link Executor#commandExecution}).
     * Если команда требует ввода объекта - просит ввести поля и проверяет введенные данные на соответствие типу данных по схеме:<br>
     * 1 Если требуется ввести числовые данные<br>
     * 1.1 Считывает вводимые данные<br>
     * 1.2 Пытается преобразовать введенные данные в нужный тип<br>
     * 1.3 Если удалось - переход к введению следующего поля (если ввод совершался из файла и была выявлена ошибка ({@link ConsoleReaderAndWriter#inputStatus} == 2), удаляет созданный на шаге 1.4 сканер и загружает старый из {@link ConsoleReaderAndWriter#scannerStack})<br>
     * 1.4 Если не удалось - проверяет, откуда производился ввод<br>
     * 1.5 Вызывается метод {@link ConsoleReaderAndWriter#reentryAttempt}. Тем самым создается новый сканер стандартного ввода<br>
     * 1.6 Переходит к шагу 1.1<br>
     * 2 Если требуется ввести строку - считывание строки<br>
     * 3 Если требуется ввести тип<br>
     * 3.1 Считывает вводимые данные<br>
     * 3.2 С помощью методов {@link ConsoleReaderAndWriter#validVenueType} и {@link ConsoleReaderAndWriter#validTicketType} проверяет соответствие введенной строки одному из типов<br>
     * 3.3 Если найдено соответствие - переход к введению следующего поля (если ввод совершался из файла и была выявлена ошибка ({@link ConsoleReaderAndWriter#inputStatus} == 2), удаляет созданный на шаге 1.4 сканер и загружает старый из {@link ConsoleReaderAndWriter#scannerStack})<br>
     * 3.4 Вызывается метод {@link ConsoleReaderAndWriter#reentryAttempt}. Тем самым создается новый сканер стандартного ввода<br>
     * 3.5 Переходит к шагу 3.1<br>
     * Далее передает данные и команду Исполнителю {@link Executor}({@link Executor#commandExecutionWithElement})
     *
     * @param command массив строк (команда, которую необходимо исполнить и, при необходимости, параметры)
     */
    public void nextCommand(String[] command) throws IOException {
        try {
            if (!checkingCompositeCommands(command)) return;
            switch (command[0]) {
                case ("update"):
                case ("add_if_max"):
                case ("add_if_min"):
                case ("add"):
                case ("remove_lower"):
                    print("Введите имя: ");
                    String name = in.nextLine();
                    long x;
                    while (true) {
                        print("Введите первую координату: ");
                        try {
                            x = Long.parseLong(in.nextLine()); // попытка преобразовать в нужный тип
                        } catch (NumberFormatException e) {
                            if (inputStatus == 1) { // если ввод происходит из файла и пока не найдена ошибка в этом поле(2)
                                printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле первая координата. Хотите ввести значение вручную?\nДа - Y, нет - N");
                                if (!reentryAttempt()) return;
                                continue;
                            }
                            println("Неверный формат ввода. Ожидалось целое число");
                            continue;
                        }
                        break;
                    }
                    if (inputStatus == 2) { // если ввод происходил из файла и было введено неверное значение
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    double y;
                    while (true) {
                        print("Введите вторую координату: ");
                        try {
                            y = Double.parseDouble(in.nextLine());
                        } catch (NumberFormatException e) {
                            if (inputStatus == 1) {
                                printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле вторая координата. Хотите ввести значение вручную?\nДа - Y, нет - N");
                                if (!reentryAttempt()) return;
                                continue;
                            }
                            println("Неверный формат ввода. Ожидалась десятичная дробь");
                            continue;
                        }
                        break;
                    }
                    if (inputStatus == 2) {
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    int price;
                    while (true) {
                        print("Введите стоимость: ");
                        try {
                            price = Integer.parseInt(in.nextLine());
                        } catch (NumberFormatException e) {
                            if (inputStatus == 1) {
                                printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле стоимость. Хотите ввести значение вручную?\nДа - Y, нет - N");
                                if (!reentryAttempt()) return;
                                continue;
                            }
                            println("Неверный формат ввода. Ожидалось целое число");
                            continue;
                        }
                        break;
                    }
                    if (inputStatus == 2) {
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    String strType;
                    while (true) {
                        print("Введите тип билета " + Arrays.toString(TicketType.values()) + ": ");
                        strType = in.nextLine();
                        if (validTicketType(strType)) break;
                        if (inputStatus == 1) {
                            printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле тип билета. Хотите ввести значение вручную?\nДа - Y, нет - N");
                            if (!reentryAttempt()) return;
                            continue;
                        }
                        println("Неверный тип");
                    }
                    if (inputStatus == 2) {
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    TicketType ticketType = TicketType.valueOf(strType);
                    Long capacity;
                    while (true) {
                        try {
                            print("Введите вместимость: ");
                            capacity = Long.parseLong(in.nextLine());
                        } catch (NumberFormatException e) {
                            if (inputStatus == 1) {
                                printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле вместимость. Хотите ввести значение вручную?\nДа - Y, нет - N");
                                if (!reentryAttempt()) return;
                                continue;
                            }
                            println("Неверный формат ввода. Ожидалось целое число");
                            continue;
                        }
                        break;
                    }
                    if (inputStatus == 2) {
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    while (true) {
                        print("Введите тип места назначения" + Arrays.toString(VenueType.values()) + ": ");
                        strType = in.nextLine();
                        if (validVenueType(strType)) break;
                        if (inputStatus == 1) {
                            printIgnoringPrintStatus("Не удалось загрузить объект в связи с неправильным форматом данных в поле тип места назначения. Хотите ввести значение вручную?\nДа - Y, нет - N");
                            if (!reentryAttempt()) return;
                            continue;
                        }
                        println("Неверный тип");
                    }
                    if (inputStatus == 2) {
                        in = scannerStack.pop();
                        if (!scannerStack.empty()) {
                            setInputStatus(1);
                        } else setInputStatus(0);
                    }
                    VenueType venueType = VenueType.valueOf(strType);
                    print("Введите название улицы: ");
                    String street = in.nextLine();
                    print("Введите почтовый индекс: ");
                    String zipCode = in.nextLine();
                    ex.commandExecutionWithElement(command, name, x, y, price, ticketType, capacity, venueType, street, zipCode);
                case ("info"):
                case ("show"):
                case ("remove_by_id"):
                case ("remove_at"):
                case ("clear"):
                case ("save"):
                case ("remove_first"):
                case ("min_by_venue"):
                case ("filter_contains_name"):
                case ("filter_less_than_price"):
                case ("filter_by_price"):
                case ("count_greater_than_type"):
                case ("print_field_ascending_type"):
                    ex.commandExecution(command);
                    break;
                case ("help"):
                    printIgnoringPrintStatus("""
                            help: вывести справку по доступным командам
                            info: вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)
                            show: вывести в стандартный поток вывода все элементы коллекции в строковом представлении
                            add {element}: добавить новый элемент в коллекцию
                            update id {element}: обновить значение элемента коллекции, id которого равен заданному
                            remove_by_id id: удалить элемент из коллекции по его id
                            clear: очистить коллекцию
                            save: сохранить коллекцию в файл
                            execute_script file_name: считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.
                            exit: завершить программу (без сохранения в файл)
                            remove_first: удалить первый элемент из коллекции
                            add_if_max {element}: добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции
                            add_if_min {element}: добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
                            min_by_venue: вывести любой объект из коллекции, значение поля venue которого является минимальным
                            filter_contains_name name: вывести элементы, значение поля name которых содержит заданную подстроку
                            filter_less_than_price price: вывести элементы, значение поля price которых меньше заданного
                            remove_at index : удалить элемент, находящийся в заданной позиции коллекции (index)
                            remove_lower {element} : удалить из коллекции все элементы, меньшие, чем заданный
                            count_greater_than_type type : вывести количество элементов, значение поля type которых больше заданного
                            filter_by_price price : вывести элементы, значение поля price которых равно заданному
                            print_field_ascending_type : вывести значения поля type всех элементов в порядке возрастания""");
                    break;
                case ("execute_script"):
                    setInputStatus(1);
                    scannerStack.add(in);
                    this.in = new Scanner(Paths.get(command[1]));
                    break;
                case ("exit"):
                    ex.exit();
                    break;
                default:
                    println("Введена неверная команда. Для просмотра справки по доступным командам введите команду help");
            }
        } catch (NoSuchElementException e) {
            printIgnoringPrintStatus("Исполнение файла " + fileNamesStack.peek() + " прервано в связи с ошибкой при выполнении команды " + command[0]);
        }
    }

    /**
     * Используется для организации ввода с консоли при возникновении ошибки некорректного типа данных при вводе из файла при создании объекта
     *
     * @return возвращает true если пользователь хочет ввести поле заново (введен Y), false - если нет
     */
    private boolean reentryAttempt() {
        scannerStack.add(in); // добавляем в стек сканер
        in = new Scanner(System.in); // создаем стандартный сканер
        setInputStatus(2); // статус для обработки исключений при вводе неправильного типа данных из файла
        String str = nextInput(); // хочет ли пользователь ввести поле заново
        if (!str.equalsIgnoreCase("y")) { // возвращаем старый сканер и выходим из файла
            scannerStack.pop();
            in = scannerStack.pop();
            fileNamesStack.pop();
            return false;
        }
        return true;
    }

    /**
     * Проверка аргументов команд с аргументами на соответствие требованиям типов данных, валидности id и т.д.
     *
     * @param command команда
     * @return возвращает true, если введенный аргумент соответствует требованиям, false - если не соответствует
     */
    private boolean checkingCompositeCommands(String[] command) {
        switch (command[0]) {
            case ("update"):
            case ("remove_by_id"):
                try {
                    long id = Long.parseLong(command[1]);
                    if (!ex.validId(id)) {
                        println("Неверный id");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    println("Неверный формат id");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    println("Вы не ввели id");
                    return false;
                }
                break;
            case ("count_greater_than_type"):
                try {
                    TicketType.valueOf(command[1]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    println("Вы не ввели тип");
                    return false;
                } catch (IllegalArgumentException e) {
                    println("Неверный тип");
                    return false;
                }
                break;
            case ("filter_less_than_price"):
            case ("filter_by_price"):
                try {
                    if (Long.parseLong(command[1]) <= 0) {
                        println("Цена должна быть больше 0");
                        return false;
                    }
                } catch (NumberFormatException e) {
                    println("Неверный формат поля price");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    println("Вы не ввели поле price");
                    return false;
                }
                break;
            case ("filter_contains_name"):
                break;
            case ("execute_script"):
                if (command.length < 2) {
                    println("Вы не ввели имя файла");
                    return false;
                }
                for (String fileName : fileNamesStack) {
                    if (fileName.equals(command[1])) {
                        printIgnoringPrintStatus("Невозможно исполнить файл " + "\"" + command[1] + "\" из-за возникновения рекурсии");
                        return false;
                    }
                }
                fileNamesStack.add(command[1]);
                File f = new File(command[1]);
                if (!f.exists()) {
                    println("Неверное имя файла");
                    return false;
                } else if (f.isDirectory()) {
                    println("Невозможно исполнить директорию");
                    return false;
                }
                break;
            case ("remove_at"):
                try {
                    Integer.parseInt(command[1]);
                } catch (NumberFormatException e) {
                    println("Неверный формат индекса");
                    return false;
                } catch (ArrayIndexOutOfBoundsException e) {
                    println("Вы не ввели индекс");
                    return false;
                }
                break;
            default:
                if (command.length > 1) {
                    println("Введена неверная команда. Для просмотра справки по доступным командам введите команду help");
                    return false;
                }
        }
        return true;
    }
}

