package ma.fstt.carburants.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** The price in force for one fuel: its name, the price, and the day this price was set. */
public class PrixCarburantDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String carburant;
    private BigDecimal prix;
    /** ISO date (yyyy-MM-dd) of the HistoCarb row that is in force; may be earlier than the requested date. */
    private String datePrix;

    public PrixCarburantDto() {
    }

    public PrixCarburantDto(String carburant, BigDecimal prix, String datePrix) {
        this.carburant = carburant;
        this.prix = prix;
        this.datePrix = datePrix;
    }

    public String getCarburant() {
        return carburant;
    }

    public void setCarburant(String carburant) {
        this.carburant = carburant;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }

    public String getDatePrix() {
        return datePrix;
    }

    public void setDatePrix(String datePrix) {
        this.datePrix = datePrix;
    }
}
