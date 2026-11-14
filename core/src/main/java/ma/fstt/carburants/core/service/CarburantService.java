package ma.fstt.carburants.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import ma.fstt.carburants.api.dto.CarburantDto;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.api.logic.Validation;
import ma.fstt.carburants.core.entity.Carburant;

import java.util.List;
import java.util.stream.Collectors;

/** CRUD of fuel types. Same transaction model as {@link StationService}. */
@ApplicationScoped
@Transactional
public class CarburantService {

    @PersistenceContext(unitName = "carburantsPU")
    private EntityManager em;

    public List<CarburantDto> lister() {
        return em.createQuery("select c from Carburant c order by c.nom", Carburant.class)
                .getResultList().stream().map(Mapper::toDto).collect(Collectors.toList());
    }

    public CarburantDto trouver(Long id) {
        return Mapper.toDto(chargerOuEchouer(id));
    }

    public CarburantDto creer(CarburantDto dto) {
        if (dto == null) {
            throw BusinessException.validation("Le carburant est obligatoire.");
        }
        String nom = Validation.requireText(dto.getNom(), "nom", Validation.NOM_MAX);
        String description = Validation.optionalText(dto.getDescription(), "description", Validation.ADRESSE_MAX);
        verifierNomLibre(nom, null);
        Carburant c = new Carburant(nom, description);
        em.persist(c);
        flushEnConflit("Un carburant porte déjà ce nom.");
        return Mapper.toDto(c);
    }

    public CarburantDto modifier(CarburantDto dto) {
        if (dto == null) {
            throw BusinessException.validation("Le carburant est obligatoire.");
        }
        Carburant c = chargerOuEchouer(dto.getId());
        String nom = Validation.requireText(dto.getNom(), "nom", Validation.NOM_MAX);
        String description = Validation.optionalText(dto.getDescription(), "description", Validation.ADRESSE_MAX);
        verifierNomLibre(nom, c.getId());
        c.setNom(nom);
        c.setDescription(description);
        flushEnConflit("Un carburant porte déjà ce nom.");
        return Mapper.toDto(c);
    }

    public void supprimer(Long id) {
        Carburant c = chargerOuEchouer(id);
        Long prix = em.createQuery("select count(h) from HistoCarb h where h.carburant.id = :id", Long.class)
                .setParameter("id", c.getId()).getSingleResult();
        if (prix > 0) {
            throw BusinessException.conflict("Suppression refusée : ce carburant a " + prix
                    + " prix enregistré(s). Supprimez d'abord son historique.");
        }
        em.remove(c);
        flushEnConflit("Suppression impossible : le carburant est encore utilisé.");
    }

    // ------------------------------------------------------------------ helpers

    private Carburant chargerOuEchouer(Long id) {
        Validation.requireId(id, "carburant");
        Carburant c = em.find(Carburant.class, id);
        if (c == null) {
            throw BusinessException.notFound("Carburant introuvable (id " + id + ").");
        }
        return c;
    }

    private void verifierNomLibre(String nom, Long exceptId) {
        List<Long> ids = em.createQuery("select c.id from Carburant c where lower(c.nom) = :nom", Long.class)
                .setParameter("nom", nom.toLowerCase()).getResultList();
        for (Long other : ids) {
            if (!other.equals(exceptId)) {
                throw BusinessException.conflict("Un carburant porte déjà ce nom : « " + nom + " ».");
            }
        }
    }

    private void flushEnConflit(String message) {
        try {
            em.flush();
        } catch (PersistenceException e) {
            throw BusinessException.conflict(message);
        }
    }
}
