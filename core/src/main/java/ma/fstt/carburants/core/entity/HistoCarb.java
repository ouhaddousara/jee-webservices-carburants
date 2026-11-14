package ma.fstt.carburants.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * The association class of the subject (Station - Carburant, with a date and a price), mapped as an
 * entity of its own: it has two foreign keys and two attributes, and a surrogate key so a price can
 * be addressed by REST/SOAP clients. The business key is (station, carburant, date).
 */
@Entity
@Table(name = "histo_carb")
public class HistoCarb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carburant_id", nullable = false)
    private Carburant carburant;

    @Column(name = "date_prix", nullable = false)
    private LocalDate datePrix;

    @Column(name = "prix", nullable = false, precision = 8, scale = 2)
    private BigDecimal prix;

    protected HistoCarb() {
        // required by JPA
    }

    public HistoCarb(Station station, Carburant carburant, LocalDate datePrix, BigDecimal prix) {
        this.station = station;
        this.carburant = carburant;
        this.datePrix = datePrix;
        this.prix = prix;
    }

    public Long getId() {
        return id;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public Carburant getCarburant() {
        return carburant;
    }

    public void setCarburant(Carburant carburant) {
        this.carburant = carburant;
    }

    public LocalDate getDatePrix() {
        return datePrix;
    }

    public void setDatePrix(LocalDate datePrix) {
        this.datePrix = datePrix;
    }

    public BigDecimal getPrix() {
        return prix;
    }

    public void setPrix(BigDecimal prix) {
        this.prix = prix;
    }
}
