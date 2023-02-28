package utility;

import ticket.*;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Класс, отвечающий за организацию хранения и доступа к объектам класса {@link Ticket}. Тип коллекции, в которой хранятся объекты - Vector
 */
public class TicketVector {
    /**
     * Поле id.
     * Отвечает за уникальность id билетов
     */
    private long id = 0;
    /**
     * Поле даты и времени создания данного объекта
     */
    private final java.time.ZonedDateTime creationDate;
    /**
     * Поле коллекции, в которой хранятся объекты класса {@link Ticket}
     */
    private final Vector<Ticket> tv = new Vector<>();
    /**
     * Поле длинны коллекции tv
     *
     * @see TicketVector#tv
     */
    private long length = 0;

    public TicketVector() {
        creationDate = java.time.ZonedDateTime.now();
    }

    /**
     * Создает объект класса {@link Ticket#Ticket}
     *
     * @param name      название билета
     * @param x         координаты x
     * @param y         координаты y
     * @param price     цена
     * @param type      тип {@link TicketType}
     * @param capacity  вместительность {@link Venue}
     * @param venueType тип места назначения {@link VenueType}
     * @param street    название улицы {@link Venue}
     * @param zipCode   индекс места назначения {@link Venue}
     * @return возвращает объект класса {@link Ticket}
     */
    public Ticket createTicket(String name, int x, int y, Integer price, TicketType type, Long capacity, VenueType venueType, String street, String zipCode) {
        id++;
        return new Ticket(id, name, new Coordinates(x, y), java.time.LocalDateTime.now(), price, type, new Venue(id, name, capacity, venueType, new Address(street, zipCode)));
    }

    /**
     * Создает объект класса {@link Ticket#Ticket}
     *
     * @param id        id билета
     * @param name      название билета
     * @param x         координаты x
     * @param y         координаты y
     * @param price     цена
     * @param type      тип {@link TicketType}
     * @param capacity  вместительность {@link Venue}
     * @param venueType тип места назначения {@link VenueType}
     * @param street    название улицы {@link Venue}
     * @param zipCode   индекс места назначения {@link Venue}
     * @return возвращает объект класса {@link Ticket}
     */
    public Ticket createTicket(long id, String name, int x, int y, Integer price, TicketType type, Long capacity, VenueType venueType, String street, String zipCode) {
        if (this.id <= id) this.id = id++;
        return new Ticket(id, name, new Coordinates(x, y), java.time.LocalDateTime.now(), price, type, new Venue(id, name, capacity, venueType, new Address(street, zipCode)));
    }

    /**
     * Добавляет объект в коллекцию. При этом, если id объекта неоригинально, он не будет добавлен
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возврящает true если удалось добавить объект, false - если нет
     */
    public boolean add(Ticket ticket) {
        for (Ticket t : tv)
            if (t.getId() == ticket.getId() || t.getVenue().getId() == ticket.getVenue().getId()) return false;
        tv.add(ticket);
        length++;
        if (ticket.getId() >= id) id = ticket.getId();
        return true;
    }

    /**
     * Добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возвращает true, если удалось добавить объект, false - если нет
     */
    public boolean addIfMax(Ticket ticket) {
        if (maxTicket() == null) return add(ticket);
        if (ticket.compareTo(maxTicket()) > 0) return add(ticket);
        return false;
    }

    /**
     * Добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции
     *
     * @param ticket объект класса {@link  Ticket}
     * @return возвращает true, если удалось добавить объект, false - если нет
     */
    public boolean addIfMin(Ticket ticket) {
        if (minTicket() == null) return add(ticket);
        if (ticket.compareTo(minTicket()) < 0) return add(ticket);
        return false;
    }

    /**
     * Обновляет элемент коллекции с указанным id
     *
     * @param ticket объект типа {@link Ticket}
     * @param id     id объекта, который надо обновить. Предполагается, что id проверенно на корректность (в коллекции существует элемент с таким id) {@link TicketVector#validId}
     */
    public void update(Ticket ticket, long id) {
        for (Ticket t : tv) {
            if (t.getId() == id) {
                tv.remove(t);
                break;
            }
        }
        length--;
        add(ticket);
    }

    /**
     * Очищает коллекцию
     */
    public void clear() {
        tv.clear();
        length = 0;
    }

    /**
     * Удаляет элемент по переданному индексу
     *
     * @param index индекс элемента, который нужно удалить
     * @return возвращает true, если длинна коллекции больше указанного индекса, false - если нет
     */
    public boolean remove(int index) {
        if (index >= length) return false;
        tv.remove(index);
        length--;
        return true;
    }

    /**
     * Удаляет все элементы коллекции, меньшие переданного объекта
     *
     * @param ticket объект класса {@link Ticket}, с которым производится сравнение {@link Ticket#compareTo}
     * @return возвращает количество удаленных объектов
     */
    public int removeLower(Ticket ticket) {
        int v = 0;
        ArrayList<Ticket> delTickets = new ArrayList<>();
        for (Ticket t : tv)
            if (ticket.compareTo(t) > 0) {
                delTickets.add(t);
                v++;
            }
        length -= v;
        tv.removeAll(delTickets);
        return v;
    }

    /**
     * @return возвращает строковое представление коллекции
     * @see Ticket#toString
     */
    public String print() {
        return tv.toString();
    }

    /**
     * @return возвращает массив со всеми элементами коллекции
     */
    public Ticket[] getAll() {
        return tv.toArray(new Ticket[0]);
    }

    /**
     * Удаляет элемент коллекции с указанным id
     *
     * @param id id элемента, который нужно удалить. Предполагается, что id проверенно на корректность (в коллекции существует элемент с таким id) {@link TicketVector#validId}
     */
    public void removeById(long id) {
        for (Ticket t : tv) {
            if (t.getId() == id) {
                tv.remove(t);
                length--;
                return;
            }
        }
    }

    /**
     * @return возвращает минимальный элемент коллекции в строковом представлении. Сравнение ведется по полю venue {@link Venue#compareTo}
     */
    public String getMinByVenue() {
        Ticket mt = tv.get(0);
        for (Ticket t : tv) {
            if (t.getVenue().compareTo(mt.getVenue()) < 0) {
                mt = t;
            }
        }
        return mt.toString();
    }

    /**
     * @param str строка, по которой ведется поиск
     * @return возвращает элементы, значение поля name которых содержит заданную подстроку
     */
    public Vector<Ticket> filterContainsName(String str) {
        Vector<Ticket> ticketArrayList = new Vector<>();
        for (Ticket t : tv)
            if (t.getName().contains(str)) ticketArrayList.add(t);
        return ticketArrayList;
    }

    /**
     * @param price число
     * @return возвращает элементы, значение поля price которых меньше заданного
     */
    public Vector<Ticket> filterLessThanPrice(int price) {
        Vector<Ticket> ticketArrayList = new Vector<>();
        for (Ticket t : tv)
            if (t.getPrice() < price) ticketArrayList.add(t);
        return ticketArrayList;
    }

    /**
     * @param price число
     * @return возвращает элементы, значение поля price которых равно заданному
     */
    public Vector<Ticket> filterByPrice(int price) {
        Vector<Ticket> ticketArrayList = new Vector<>();
        for (Ticket t : tv)
            if (t.getPrice() == price) ticketArrayList.add(t);
        return ticketArrayList;
    }

    /**
     * @return возвращает значения поля type всех элементов в порядке возрастания {@link TicketType}
     */
    public String getFieldAscendingType() {
        StringBuilder str = new StringBuilder();
        Vector<Ticket> stv = new Vector<>(tv);
        stv.sort((t1, t2) -> t2.getType().compareTo(t1.getType()));
        for (Ticket t : stv) {
            str.append(String.format("id:%s - type:%s\n", t.getId(), t.getType()));
        }
        return str.toString();
    }

    /**
     * @param type тип билета {@link TicketType}
     * @return возвращает количество элементов коллекции, тип которых превышает переданный
     */
    public int getCountGreaterThanType(TicketType type) {
        int v = 0;
        for (Ticket t : tv)
            if (type.compareTo(t.getType()) > 0) v++;
        return v;
    }

    /**
     * @return возвращает максимальный элемент коллекции {@link Ticket#compareTo}.
     * Если коллекция пуста вернет null
     */
    private Ticket maxTicket() {
        if (tv.isEmpty()) return null;
        Ticket[] ta = tv.toArray(new Ticket[0]);
        Arrays.sort(ta);
        return ta[(int) length - 1];
    }

    /**
     * @return возвращает минимальный элемент коллекции {@link Ticket#compareTo}.
     * Если коллекция пуста вернет null
     */
    private Ticket minTicket() {
        if (tv.isEmpty()) return null;
        Ticket[] ta = tv.toArray(new Ticket[0]);
        Arrays.sort(ta);
        return ta[0];
    }

    /**
     * @return возвращает информацию об коллекции <b>дата инициализации {@link TicketVector#creationDate}</b>, <b>количество элементов {@link TicketVector#length}</b>, <b>максимальный элемент {@link TicketVector#maxTicket}</b>, <b>минимальный элемент {@link TicketVector#minTicket}</b>
     */
    public String getInfo() {
        return String.format("Тип - Vector\nДата инициализации - %s\nКоличество элементов - %s\nМаксимальный элемент - %s\nМинимальный элемент - %s", creationDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ")), length, maxTicket(), minTicket());
    }

    /**
     * Проверяет, что элемент с указанным id есть в коллекции
     *
     * @param id число
     * @return возвращает true, если в коллекции есть элемент с указанным id, false - если нет
     */
    public boolean validId(long id) {
        for (Ticket t : tv)
            if (t.getId() == id) return true;
        return false;
    }
}
