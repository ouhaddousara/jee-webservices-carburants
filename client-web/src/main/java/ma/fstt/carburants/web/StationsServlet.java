package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.fstt.carburants.api.dto.StationDto;

import java.io.IOException;

/** Station CRUD through the SOAP service. GET lists (and prefills the form with ?edit=id); POST writes. */
@WebServlet("/stations")
public class StationsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("stations", soap(request).listerStations());
            Long edit = parseId(request.getParameter("edit"));
            if (edit != null) {
                request.setAttribute("edition", soap(request).trouverStation(edit));
            }
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.message(e));
        }
        render(request, response, "stations");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                StationDto created = soap(request).creerStation(fromForm(request, null));
                flashOk(request, "Station « " + created.getNom() + " » créée.");
            } else if ("update".equals(action)) {
                StationDto updated = soap(request).modifierStation(fromForm(request, parseId(request.getParameter("id"))));
                flashOk(request, "Station « " + updated.getNom() + " » modifiée.");
            } else if ("delete".equals(action)) {
                soap(request).supprimerStation(parseId(request.getParameter("id")));
                flashOk(request, "Station supprimée.");
            } else {
                flashError(request, "Action inconnue.");
            }
        } catch (RuntimeException e) {
            flashError(request, Errors.message(e));
        }
        redirect(request, response, "/stations");
    }

    private static StationDto fromForm(HttpServletRequest request, Long id) {
        return new StationDto(id, request.getParameter("nom"), request.getParameter("ville"), request.getParameter("adresse"));
    }
}
