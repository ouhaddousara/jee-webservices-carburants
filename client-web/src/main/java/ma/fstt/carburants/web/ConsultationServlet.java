package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Part 2, client 2 of the subject: the web interface that calls the JAX-WS method
 * "prices of the fuels for (nom_station, date)". A GET with both parameters runs the query.
 */
@WebServlet("/consultation")
public class ConsultationServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("stations", soap(request).listerStations());
            String station = request.getParameter("station");
            String date = request.getParameter("date");
            request.setAttribute("date", date == null || date.isBlank() ? LocalDate.now().toString() : date);
            request.setAttribute("station", station);
            if (station != null && !station.isBlank()) {
                request.setAttribute("resultat", soap(request).prixCarburants(station, request.getAttribute("date").toString()));
            }
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.message(e));
        }
        render(request, response, "consultation");
    }
}
