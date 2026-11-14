package ma.fstt.carburants.core.service;

import ma.fstt.carburants.api.dto.CarburantDto;
import ma.fstt.carburants.api.dto.HistoCarbDto;
import ma.fstt.carburants.api.dto.StationDto;
import ma.fstt.carburants.core.entity.Carburant;
import ma.fstt.carburants.core.entity.HistoCarb;
import ma.fstt.carburants.core.entity.Station;

/** Entity to DTO conversions. Entities never leave the services layer. */
final class Mapper {

    private Mapper() {
    }

    static StationDto toDto(Station s) {
        return new StationDto(s.getId(), s.getNom(), s.getVille(), s.getAdresse());
    }

    static CarburantDto toDto(Carburant c) {
        return new CarburantDto(c.getId(), c.getNom(), c.getDescription());
    }

    static HistoCarbDto toDto(HistoCarb h) {
        HistoCarbDto dto = new HistoCarbDto();
        dto.setId(h.getId());
        dto.setStationId(h.getStation().getId());
        dto.setStationNom(h.getStation().getNom());
        dto.setCarburantId(h.getCarburant().getId());
        dto.setCarburantNom(h.getCarburant().getNom());
        dto.setDate(h.getDatePrix().toString());
        dto.setPrix(h.getPrix());
        return dto;
    }
}
