package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ma.fstt.carburants.api.soap.CarburantsSoapService;

import java.io.IOException;
import java.math.BigDecimal;

/** Shared helpers of the servlets: SOAP proxy of the signed-in user, flash messages, JSP forwarding. */
abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String FLASH_OK = "flashOk";
    private static final String FLASH_ERROR = "flashError";

    /** SOAP proxy that authenticates with the credentials entered at login. */
    protected CarburantsSoapService soap(HttpServletRequest request) {
        String authorization = (String) request.getSession().getAttribute(SecurityFilter.SESSION_AUTH);
        return SoapClients.port(authorization);
    }

    protected void flashOk(HttpServletRequest request, String message) {
        request.getSession().setAttribute(FLASH_OK, message);
    }

    protected void flashError(HttpServletRequest request, String message) {
        request.getSession().setAttribute(FLASH_ERROR, message);
    }

    /** Moves the flash messages of the session to the request (shown once), then forwards to a JSP. */
    protected void render(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            moveFlash(session, request, FLASH_OK);
            moveFlash(session, request, FLASH_ERROR);
        }
        request.getRequestDispatcher("/WEB-INF/jsp/" + view + ".jsp").forward(request, response);
    }

    private static void moveFlash(HttpSession session, HttpServletRequest request, String key) {
        Object value = session.getAttribute(key);
        if (value != null) {
            request.setAttribute(key, value);
            session.removeAttribute(key);
        }
    }

    /** Post/Redirect/Get: avoids a duplicate submission when the page is refreshed. */
    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected static Long parseId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Accepts "10.5" and "10,5". Returns null when the text is not a number. */
    protected static BigDecimal parsePrice(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
