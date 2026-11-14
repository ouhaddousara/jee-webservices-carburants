package ma.fstt.carburants.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A fuel station. The name is unique (it is the lookup key of the shared price method). */
@Entity
@Table(name = "station")
public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100, unique = true)
    private String nom;

    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

    @Column(name = "adresse", nullable = false, length = 255)
    private String adresse;

    protected Station() {
        // required by JPA
    }

    public Station(String nom, String ville, String adresse) {
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
