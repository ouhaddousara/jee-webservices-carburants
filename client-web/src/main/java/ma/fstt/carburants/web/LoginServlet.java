package ma.fstt.carburants.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import ma.fstt.carburants.api.soap.CarburantsSoapService;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Login and logout of the web client. The credentials are checked by the SOAP service itself (a cheap
 * authenticated call), so this application never stores or compares passwords. The Authorization
 * header is kept in the server-side session only.
 */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/logout")) {
            redirect(request, response, "/login");
            return;
        }
        render(request, response, "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().endsWith("/logout")) {
            logout(request, response);
            return;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            request.setAttribute("flashError", "Identifiant et mot de passe obligatoires.");
            render(request, response, "login");
            return;
        }
        String authorization = SoapClients.basicHeader(username.trim(), password);
        try {
            CarburantsSoapService port = SoapClients.port(authorization);
            port.listerCarburants(); // any authenticated call proves the credentials
        } catch (RuntimeException e) {
            request.setAttribute("flashError", Errors.isUnauthorized(e) ? "Identifiant ou mot de passe incorrect."
                    : Errors.message(e));
            render(request, response, "login");
            return;
        }

        // New session id after authentication (session fixation protection).
        HttpSession session = request.getSession(true);
        request.changeSessionId();
        session.setAttribute(SecurityFilter.SESSION_AUTH, authorization);
        session.setAttribute(SecurityFilter.SESSION_USER, username.trim());
        session.setAttribute(SecurityFilter.SESSION_CSRF, newToken());
        redirect(request, response, "/home");
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        redirect(request, response, "/login");
    }

    private static String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
