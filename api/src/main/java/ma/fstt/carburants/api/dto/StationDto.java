package ma.fstt.carburants.api.dto;

import java.io.Serializable;

/**
 * Transfer object for a station. DTOs are used instead of JPA entities so that SOAP and JSON never
 * see lazy associations or cycles, and so that the public contract stays stable if the schema changes.
 */
public class StationDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String nom;
    private String ville;
    private String adresse;

    public StationDto() {
    }

    public StationDto(Long id, String nom, String ville, String adresse) {
        this.id = id;
        this.nom = nom;
        this.ville = ville;
        this.adresse = adresse;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
