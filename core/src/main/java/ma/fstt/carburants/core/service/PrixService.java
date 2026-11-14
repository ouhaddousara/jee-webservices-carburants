package ma.fstt.carburants.core.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import ma.fstt.carburants.api.dto.HistoCarbDto;
import ma.fstt.carburants.api.dto.PrixStationDto;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.api.logic.PriceResolver;
import ma.fstt.carburants.api.logic.Validation;
import ma.fstt.carburants.core.entity.Carburant;
import ma.fstt.carburants.core.entity.HistoCarb;
import ma.fstt.carburants.core.entity.Station;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Daily prices (the HistoCarb association) and the method shared by the three web services:
 * {@link #prixCarburants(String, String)}.
 */
@ApplicationScoped
@Transactional
public class PrixService {

    @PersistenceContext(unitName = "carburantsPU")
    private EntityManager em;

    // ------------------------------------------------------------------ Daily prices

    public HistoCarbDto enregistrer(HistoCarbDto dto) {
        if (dto == null) {
            throw BusinessException.validation("Le prix est obligatoire.");
        }
        Station station = chargerStation(dto.getStationId());
        Carburant carburant = chargerCarburant(dto.getCarburantId());
        LocalDate date = Validation.parseDate(dto.getDate());
        BigDecimal prix = Validation.requirePrice(dto.getPrix());
        verifierJourLibre(station.getId(), carburant.getId(), date, null);

        HistoCarb h = new HistoCarb(station, carburant, date, prix);
        em.persist(h);
        flushEnConflit("Un prix existe déjà pour cette station, ce carburant et cette date.");
        return Mapper.toDto(h);
    }

    /** Changes the date and/or the price of an existing entry (station and fuel stay fixed). */
    public HistoCarbDto modifier(HistoCarbDto dto) {
        if (dto == null) {
            throw BusinessException.validation("Le prix est obligatoire.");
        }
        HistoCarb h = chargerPrix(dto.getId());
        LocalDate date = Validation.parseDate(dto.getDate());
        BigDecimal prix = Validation.requirePrice(dto.getPrix());
        verifierJourLibre(h.getStation().getId(), h.getCarburant().getId(), date, h.getId());
        h.setDatePrix(date);
        h.setPrix(prix);
        flushEnConflit("Un prix existe déjà pour cette station, ce carburant et cette date.");
        return Mapper.toDto(h);
    }

    public void supprimer(Long id) {
        em.remove(chargerPrix(id));
    }

    /** Full history of a station, most recent first. */
    public List<HistoCarbDto> historique(Long stationId) {
        Station station = chargerStation(stationId);
        return em.createQuery("select h from HistoCarb h join fetch h.carburant join fetch h.station "
                        + "where h.station.id = :sid order by h.datePrix desc, h.carburant.nom", HistoCarb.class)
                .setParameter("sid", station.getId()).getResultList()
                .stream().map(Mapper::toDto).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ Shared method (D4, D5)

    /**
     * Prices of the fuels of a station at a date: for each fuel that has a history, the entry in force
     * at that date (the latest one on or before it). An unknown station is a 404; a known station with
     * no price in force gives an empty list and an explicit message instead of an error.
     */
    public PrixStationDto prixCarburants(String nomStation, String date) {
        String nom = Validation.requireText(nomStation, "nom_station", Validation.NOM_MAX);
        LocalDate at = Validation.parseDate(date);

        List<Station> stations = em.createQuery("select s from Station s where lower(s.nom) = :nom", Station.class)
                .setParameter("nom", nom.toLowerCase()).getResultList();
        if (stations.isEmpty()) {
            throw BusinessException.notFound("Station introuvable : « " + nom + " ».");
        }
        Station station = stations.get(0);

        List<HistoCarb> rows = em.createQuery("select h from HistoCarb h join fetch h.carburant "
                        + "where h.station.id = :sid and h.datePrix <= :d", HistoCarb.class)
                .setParameter("sid", station.getId()).setParameter("d", at).getResultList();

        List<PriceResolver.Entry> entries = new ArrayList<>();
        for (HistoCarb h : rows) {
            entries.add(new PriceResolver.Entry(h.getCarburant().getNom(), h.getDatePrix(), h.getPrix()));
        }

        PrixStationDto result = new PrixStationDto();
        result.setStation(station.getNom());
        result.setDate(at.toString());
        result.setPrix(PriceResolver.resolve(entries, at));
        if (result.getPrix().isEmpty()) {
            result.setMessage("Aucun prix en vigueur pour cette station à la date " + at + ".");
        }
        return result;
    }

    // ------------------------------------------------------------------ helpers

    private Station chargerStation(Long id) {
        Validation.requireId(id, "station");
        Station s = em.find(Station.class, id);
        if (s == null) {
            throw BusinessException.notFound("Station introuvable (id " + id + ").");
        }
        return s;
    }

    private Carburant chargerCarburant(Long id) {
        Validation.requireId(id, "carburant");
        Carburant c = em.find(Carburant.class, id);
        if (c == null) {
            throw BusinessException.notFound("Carburant introuvable (id " + id + ").");
        }
        return c;
    }

    private HistoCarb chargerPrix(Long id) {
        Validation.requireId(id, "prix");
        HistoCarb h = em.find(HistoCarb.class, id);
        if (h == null) {
            throw BusinessException.notFound("Prix introuvable (id " + id + ").");
        }
        return h;
    }

    /** Friendly pre-check of the UNIQUE (station, fuel, day) rule. */
    private void verifierJourLibre(Long stationId, Long carburantId, LocalDate date, Long exceptId) {
        List<Long> ids = em.createQuery("select h.id from HistoCarb h where h.station.id = :s "
                        + "and h.carburant.id = :c and h.datePrix = :d", Long.class)
                .setParameter("s", stationId).setParameter("c", carburantId).setParameter("d", date)
                .getResultList();
        for (Long other : ids) {
            if (!other.equals(exceptId)) {
                throw BusinessException.conflict("Un prix existe déjà pour cette station, ce carburant et la date "
                        + date + ". Modifiez-le au lieu d'en créer un second.");
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
