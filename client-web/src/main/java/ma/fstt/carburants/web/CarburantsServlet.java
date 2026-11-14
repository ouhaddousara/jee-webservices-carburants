package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.fstt.carburants.api.dto.CarburantDto;

import java.io.IOException;

/** Fuel CRUD through the SOAP service. */
@WebServlet("/carburants")
public class CarburantsServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("carburants", soap(request).listerCarburants());
            Long edit = parseId(request.getParameter("edit"));
            if (edit != null) {
                request.setAttribute("edition", soap(request).trouverCarburant(edit));
            }
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.message(e));
        }
        render(request, response, "carburants");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        try {
            if ("create".equals(action)) {
                CarburantDto created = soap(request).creerCarburant(fromForm(request, null));
                flashOk(request, "Carburant « " + created.getNom() + " » créé.");
            } else if ("update".equals(action)) {
                CarburantDto updated = soap(request).modifierCarburant(fromForm(request, parseId(request.getParameter("id"))));
                flashOk(request, "Carburant « " + updated.getNom() + " » modifié.");
            } else if ("delete".equals(action)) {
                soap(request).supprimerCarburant(parseId(request.getParameter("id")));
                flashOk(request, "Carburant supprimé.");
            } else {
                flashError(request, "Action inconnue.");
            }
        } catch (RuntimeException e) {
            flashError(request, Errors.message(e));
        }
        redirect(request, response, "/carburants");
    }

    private static CarburantDto fromForm(HttpServletRequest request, Long id) {
        return new CarburantDto(id, request.getParameter("nom"), request.getParameter("description"));
    }
}
