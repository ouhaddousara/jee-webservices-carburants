package ma.fstt.carburants.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Answer of the method shared by the three services: "prices of the fuels of a station at a date".
 * An empty {@code prix} list with an explicit {@code message} means "no price in force at that date".
 */
public class PrixStationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String station;
    /** The requested date, ISO formatted (yyyy-MM-dd). */
    private String date;
    private List<PrixCarburantDto> prix = new ArrayList<>();
    private String message;

    public PrixStationDto() {
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<PrixCarburantDto> getPrix() {
        return prix;
    }

    public void setPrix(List<PrixCarburantDto> prix) {
        this.prix = prix == null ? new ArrayList<>() : prix;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
