package ma.fstt.carburants.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import ma.fstt.carburants.api.dto.StationDto;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.api.logic.Validation;
import ma.fstt.carburants.core.entity.Station;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CRUD of stations. Transactions are container-managed (JTA): each public method is one transaction,
 * and a {@link BusinessException} (unchecked) rolls it back.
 */
@ApplicationScoped
@Transactional
public class StationService {

    @PersistenceContext(unitName = "carburantsPU")
    private EntityManager em;

    public List<StationDto> lister() {
        return em.createQuery("select s from Station s order by s.nom", Station.class)
                .getResultList().stream().map(Mapper::toDto).collect(Collectors.toList());
    }

    public StationDto trouver(Long id) {
        return Mapper.toDto(chargerOuEchouer(id));
    }

    public StationDto creer(StationDto dto) {
        if (dto == null) {
            throw BusinessException.validation("La station est obligatoire.");
        }
        String nom = Validation.requireText(dto.getNom(), "nom", Validation.NOM_MAX);
        String ville = Validation.requireText(dto.getVille(), "ville", Validation.NOM_MAX);
        String adresse = Validation.requireText(dto.getAdresse(), "adresse", Validation.ADRESSE_MAX);
        verifierNomLibre(nom, null);
        Station s = new Station(nom, ville, adresse);
        em.persist(s);
        flushEnConflit("Une station porte déjà ce nom.");
        return Mapper.toDto(s);
    }

    public StationDto modifier(StationDto dto) {
        if (dto == null) {
            throw BusinessException.validation("La station est obligatoire.");
        }
        Station s = chargerOuEchouer(dto.getId());
        String nom = Validation.requireText(dto.getNom(), "nom", Validation.NOM_MAX);
        String ville = Validation.requireText(dto.getVille(), "ville", Validation.NOM_MAX);
        String adresse = Validation.requireText(dto.getAdresse(), "adresse", Validation.ADRESSE_MAX);
        verifierNomLibre(nom, s.getId());
        s.setNom(nom);
        s.setVille(ville);
        s.setAdresse(adresse);
        flushEnConflit("Une station porte déjà ce nom.");
        return Mapper.toDto(s);
    }

    public void supprimer(Long id) {
        Station s = chargerOuEchouer(id);
        Long prix = em.createQuery("select count(h) from HistoCarb h where h.station.id = :id", Long.class)
                .setParameter("id", s.getId()).getSingleResult();
        if (prix > 0) {
            throw BusinessException.conflict("Suppression refusée : cette station a " + prix
                    + " prix enregistré(s). Supprimez d'abord son historique.");
        }
        em.remove(s);
        flushEnConflit("Suppression impossible : la station est encore utilisée.");
    }

    // ------------------------------------------------------------------ helpers

    private Station chargerOuEchouer(Long id) {
        Validation.requireId(id, "station");
        Station s = em.find(Station.class, id);
        if (s == null) {
            throw BusinessException.notFound("Station introuvable (id " + id + ").");
        }
        return s;
    }

    /** Friendly pre-check; the UNIQUE constraint remains the real guarantee under concurrency. */
    private void verifierNomLibre(String nom, Long exceptId) {
        List<Long> ids = em.createQuery("select s.id from Station s where lower(s.nom) = :nom", Long.class)
                .setParameter("nom", nom.toLowerCase()).getResultList();
        for (Long other : ids) {
            if (!other.equals(exceptId)) {
                throw BusinessException.conflict("Une station porte déjà ce nom : « " + nom + " ».");
            }
        }
    }

    private void flushEnConflit(String message) {
        try {
            em.flush();
        } catch (PersistenceException e) {
            // The raw SQL error is never shown to a client; the server log keeps the cause.
            throw BusinessException.conflict(message);
        }
    }
}
