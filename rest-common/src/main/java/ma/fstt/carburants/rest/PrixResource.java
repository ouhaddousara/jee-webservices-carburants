package ma.fstt.carburants.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import ma.fstt.carburants.api.dto.PrixStationDto;
import ma.fstt.carburants.core.ServiceLocator;
import ma.fstt.carburants.core.service.PrixService;

/**
 * The method of the subject for the REST services:
 * GET /prix?station=&lt;nom_station&gt;&amp;date=&lt;yyyy-MM-dd&gt; returns the prices of the station's fuels.
 */
@Path("/prix")
@Secured
public class PrixResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public PrixStationDto prixCarburants(@QueryParam("station") String station,
                                         @QueryParam("date") String date) {
        return ServiceLocator.get(PrixService.class).prixCarburants(station, date);
    }
}
