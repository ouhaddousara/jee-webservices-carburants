package ma.fstt.carburants.web;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * One filter for the whole web client (same practices as workshop 2):
 * <ul>
 *   <li>UTF-8 on requests and defensive headers on every answer (CSP, nosniff, no framing);</li>
 *   <li>pages other than the login and the public assets need an authenticated session;</li>
 *   <li>every POST (except the login) must carry the session's CSRF token.</li>
 * </ul>
 */
@WebFilter(urlPatterns = "/*")
public class SecurityFilter implements Filter {

    static final String SESSION_AUTH = "auth";
    static final String SESSION_USER = "user";
    static final String SESSION_CSRF = "csrf";

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "same-origin");
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; script-src 'self'; style-src 'self'; img-src 'self' data:; "
                        + "frame-ancestors 'none'; form-action 'self'; base-uri 'self'");

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (isPublic(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SESSION_AUTH) == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String token = (String) session.getAttribute(SESSION_CSRF);
        request.setAttribute("csrf", token);
        request.setAttribute("user", session.getAttribute(SESSION_USER));

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String sent = request.getParameter("_csrf");
            if (token == null || sent == null || !MessageDigest.isEqual(
                    token.getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Jeton CSRF invalide.");
                return;
            }
        }
        chain.doFilter(req, res);
    }

    private static boolean isPublic(String path) {
        return path.equals("/login") || path.startsWith("/assets/") || path.equals("/jersey.html");
    }
}
