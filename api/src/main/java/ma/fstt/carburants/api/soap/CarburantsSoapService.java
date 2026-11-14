package ma.fstt.carburants.api.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import ma.fstt.carburants.api.dto.CarburantDto;
import ma.fstt.carburants.api.dto.HistoCarbDto;
import ma.fstt.carburants.api.dto.PrixStationDto;
import ma.fstt.carburants.api.dto.StationDto;

import java.util.List;

/**
 * Service endpoint interface (SEI) of the JAX-WS backend: CRUD of stations and fuels, daily prices,
 * and the shared method "prices of the fuels of a station at a date".
 *
 * <p>It lives in the shared {@code api} module so the web client can build its SOAP proxy from the
 * same contract the service publishes. The namespace is fixed on purpose: the WSDL contract must not
 * change when a package is renamed.
 */
@WebService(name = "CarburantsSoap", targetNamespace = "http://carburants.fstt.ma/soap")
public interface CarburantsSoapService {

    // ------------------------------------------------------------------ Station CRUD

    @WebMethod(operationName = "listerStations")
    List<StationDto> listerStations();

    @WebMethod(operationName = "trouverStation")
    StationDto trouverStation(@WebParam(name = "id") Long id);

    @WebMethod(operationName = "creerStation")
    StationDto creerStation(@WebParam(name = "station") StationDto station);

    @WebMethod(operationName = "modifierStation")
    StationDto modifierStation(@WebParam(name = "station") StationDto station);

    @WebMethod(operationName = "supprimerStation")
    void supprimerStation(@WebParam(name = "id") Long id);

    // ------------------------------------------------------------------ Carburant CRUD

    @WebMethod(operationName = "listerCarburants")
    List<CarburantDto> listerCarburants();

    @WebMethod(operationName = "trouverCarburant")
    CarburantDto trouverCarburant(@WebParam(name = "id") Long id);

    @WebMethod(operationName = "creerCarburant")
    CarburantDto creerCarburant(@WebParam(name = "carburant") CarburantDto carburant);

    @WebMethod(operationName = "modifierCarburant")
    CarburantDto modifierCarburant(@WebParam(name = "carburant") CarburantDto carburant);

    @WebMethod(operationName = "supprimerCarburant")
    void supprimerCarburant(@WebParam(name = "id") Long id);

    // ------------------------------------------------------------------ Daily prices (HistoCarb)

    /** Records the price of a fuel in a station for a day (one price per station, fuel and day). */
    @WebMethod(operationName = "enregistrerPrix")
    HistoCarbDto enregistrerPrix(@WebParam(name = "prix") HistoCarbDto prix);

    @WebMethod(operationName = "modifierPrix")
    HistoCarbDto modifierPrix(@WebParam(name = "prix") HistoCarbDto prix);

    @WebMethod(operationName = "supprimerPrix")
    void supprimerPrix(@WebParam(name = "id") Long id);

    /** Price history of a station, most recent first. */
    @WebMethod(operationName = "historiqueStation")
    List<HistoCarbDto> historiqueStation(@WebParam(name = "stationId") Long stationId);

    // ------------------------------------------------------------------ Shared method of the subject

    /**
     * Prices of the fuels of a station at a date (decisions D4 and D5).
     *
     * @param nomStation exact station name
     * @param date       ISO date, yyyy-MM-dd
     */
    @WebMethod(operationName = "prixCarburants")
    PrixStationDto prixCarburants(@WebParam(name = "nom_station") String nomStation,
                                  @WebParam(name = "date") String date);
}
