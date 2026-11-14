package ma.fstt.carburants.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Transfer object for one daily price (the association class HistoCarb of the subject).
 *
 * <p>The date travels as an ISO string (yyyy-MM-dd): JAXB cannot map java.time out of the box and the
 * string keeps the WSDL and the JSON identical and readable.
 */
public class HistoCarbDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long stationId;
    private Long carburantId;
    /** Read-only convenience fields filled by the server when listing. */
    private String stationNom;
    private String carburantNom;
    private String date;
    private BigDecimal prix;

    public HistoCarbDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getCarburantId() {
        return carburantId;
    }

    public void setCarburantId(Long carburantId) {
        this.carburantId = carburantId;
    }

    public String getStationNom() {
        return stationNom;
    }

    public void setStationNom(String stationNom) {
        this.stationNom = stationNom;
    }

    public String getCarburantNom() {
        return carburantNom;
    }

    public void setCarburantNom(String carburantNom) {
        this.carburantNom = carburantNom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
}
