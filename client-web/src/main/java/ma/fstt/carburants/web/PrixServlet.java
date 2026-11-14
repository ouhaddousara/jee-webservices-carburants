package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.fstt.carburants.api.dto.HistoCarbDto;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Daily prices through the SOAP service: pick a station to see its history, record a price for a
 * fuel and a day, correct it, or delete it.
 */
@WebServlet("/prix")
public class PrixServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("stations", soap(request).listerStations());
            request.setAttribute("carburants", soap(request).listerCarburants());
            Long stationId = parseId(request.getParameter("stationId"));
            if (stationId != null) {
                request.setAttribute("stationId", stationId);
                request.setAttribute("historique", soap(request).historiqueStation(stationId));
            }
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.message(e));
        }
        render(request, response, "prix");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        Long stationId = parseId(request.getParameter("stationId"));
        try {
            if ("create".equals(action)) {
                HistoCarbDto dto = new HistoCarbDto();
                dto.setStationId(stationId);
                dto.setCarburantId(parseId(request.getParameter("carburantId")));
                dto.setDate(request.getParameter("date"));
                dto.setPrix(priceOrFail(request));
                soap(request).enregistrerPrix(dto);
                flashOk(request, "Prix enregistré.");
            } else if ("update".equals(action)) {
                HistoCarbDto dto = new HistoCarbDto();
                dto.setId(parseId(request.getParameter("id")));
                dto.setDate(request.getParameter("date"));
                dto.setPrix(priceOrFail(request));
                soap(request).modifierPrix(dto);
                flashOk(request, "Prix modifié.");
            } else if ("delete".equals(action)) {
                soap(request).supprimerPrix(parseId(request.getParameter("id")));
                flashOk(request, "Prix supprimé.");
            } else {
                flashError(request, "Action inconnue.");
            }
        } catch (IllegalArgumentException e) {
            flashError(request, e.getMessage());
        } catch (RuntimeException e) {
            flashError(request, Errors.message(e));
        }
        String back = stationId == null ? "/prix" : "/prix?stationId=" + URLEncoder.encode(String.valueOf(stationId), StandardCharsets.UTF_8);
        redirect(request, response, back);
    }

    private static BigDecimal priceOrFail(HttpServletRequest request) {
        BigDecimal price = parsePrice(request.getParameter("prix"));
        if (price == null) {
            throw new IllegalArgumentException("Le prix doit être un nombre (exemple : 10.45).");
        }
        return price;
    }
}
