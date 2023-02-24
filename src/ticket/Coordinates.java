package ticket;

import utility.CSVReaderAndWriter;

/**
 * Класс координат с полями <b>x</b> и <b>y</b>
 */
public class Coordinates {
    /**
     * Поля с координатами x и y
     */
    private final long x;
    private final double y;

    public Coordinates(long x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString(){
        return String.format("{x:%s, y:%s}", x, y);
    }
    /**
     * @param separator символ разделения колонок в csv файле {@link CSVReaderAndWriter#separator}
     * @return возвращает строку в формате для записи в csv файл
     */
    public String toCSVFormat(String separator){
        return x + separator + y;
    }
}
