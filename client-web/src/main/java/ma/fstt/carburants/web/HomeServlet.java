package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Home page: the menu and a short summary (number of stations and fuels). */
@WebServlet(urlPatterns = {"", "/home"})
public class HomeServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("nbStations", soap(request).listerStations().size());
            request.setAttribute("nbCarburants", soap(request).listerCarburants().size());
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.message(e));
        }
        render(request, response, "home");
    }
}
