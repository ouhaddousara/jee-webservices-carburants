package ma.fstt.carburants.soap;

import jakarta.jws.WebService;
import ma.fstt.carburants.api.dto.CarburantDto;
import ma.fstt.carburants.api.dto.HistoCarbDto;
import ma.fstt.carburants.api.dto.PrixStationDto;
import ma.fstt.carburants.api.dto.StationDto;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.api.soap.CarburantsSoapService;
import ma.fstt.carburants.core.ServiceLocator;
import ma.fstt.carburants.core.service.CarburantService;
import ma.fstt.carburants.core.service.PrixService;
import ma.fstt.carburants.core.service.StationService;

import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAX-WS implementation of the backend. It only delegates to the CDI services of the core module.
 *
 * <p>Authentication is not done here: {@link SoapBasicAuthFilter} protects the endpoint URL before
 * the SOAP engine is reached. Errors: a {@link BusinessException} becomes a SOAP Fault whose text is
 * the business message; any other exception is logged and replaced by a generic fault.
 */
@WebService(
        endpointInterface = "ma.fstt.carburants.api.soap.CarburantsSoapService",
        serviceName = "CarburantsService",
        portName = "CarburantsSoapPort",
        targetNamespace = "http://carburants.fstt.ma/soap")
public class CarburantsSoapServiceImpl implements CarburantsSoapService {

    private static final Logger LOG = Logger.getLogger(CarburantsSoapServiceImpl.class.getName());

    private static StationService stations() {
        return ServiceLocator.get(StationService.class);
    }

    private static CarburantService carburants() {
        return ServiceLocator.get(CarburantService.class);
    }

    private static PrixService prix() {
        return ServiceLocator.get(PrixService.class);
    }

    /** Runs an action; business errors pass through, anything else is hidden behind a generic message. */
    private static <T> T guard(Supplier<T> action) {
        try {
            return action.get();
        } catch (BusinessException e) {
            throw e;
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Unexpected error in a SOAP call", e);
            throw new IllegalStateException("Erreur interne du service.");
        }
    }

    private static void guardVoid(Runnable action) {
        guard(() -> {
            action.run();
            return null;
        });
    }

    // ------------------------------------------------------------------ Station

    @Override
    public List<StationDto> listerStations() {
        return guard(() -> stations().lister());
    }

    @Override
    public StationDto trouverStation(Long id) {
        return guard(() -> stations().trouver(id));
    }

    @Override
    public StationDto creerStation(StationDto station) {
        return guard(() -> stations().creer(station));
    }

    @Override
    public StationDto modifierStation(StationDto station) {
        return guard(() -> stations().modifier(station));
    }

    @Override
    public void supprimerStation(Long id) {
        guardVoid(() -> stations().supprimer(id));
    }

    // ------------------------------------------------------------------ Carburant

    @Override
    public List<CarburantDto> listerCarburants() {
        return guard(() -> carburants().lister());
    }

    @Override
    public CarburantDto trouverCarburant(Long id) {
        return guard(() -> carburants().trouver(id));
    }

    @Override
    public CarburantDto creerCarburant(CarburantDto carburant) {
        return guard(() -> carburants().creer(carburant));
    }

    @Override
    public CarburantDto modifierCarburant(CarburantDto carburant) {
        return guard(() -> carburants().modifier(carburant));
    }

    @Override
    public void supprimerCarburant(Long id) {
        guardVoid(() -> carburants().supprimer(id));
    }

    // ------------------------------------------------------------------ Prices

    @Override
    public HistoCarbDto enregistrerPrix(HistoCarbDto prix) {
        return guard(() -> prix().enregistrer(prix));
    }

    @Override
    public HistoCarbDto modifierPrix(HistoCarbDto prix) {
        return guard(() -> prix().modifier(prix));
    }

    @Override
    public void supprimerPrix(Long id) {
        guardVoid(() -> prix().supprimer(id));
    }

    @Override
    public List<HistoCarbDto> historiqueStation(Long stationId) {
        return guard(() -> prix().historique(stationId));
    }

    @Override
    public PrixStationDto prixCarburants(String nomStation, String date) {
        return guard(() -> prix().prixCarburants(nomStation, date));
    }
}
