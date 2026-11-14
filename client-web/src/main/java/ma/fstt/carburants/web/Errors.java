package ma.fstt.carburants.web;

import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.http.HTTPException;
import jakarta.xml.ws.soap.SOAPFaultException;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Turns client-side exceptions into messages that are safe and useful to show on a page. */
public final class Errors {

    private static final Logger LOG = Logger.getLogger(Errors.class.getName());

    private Errors() {
    }

    /** True when the failure means "wrong credentials" (HTTP 401 from the SOAP endpoint). */
    public static boolean isUnauthorized(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof HTTPException && ((HTTPException) c).getStatusCode() == 401) {
                return true;
            }
            String m = c.getMessage();
            if (m != null && m.contains("401")) {
                return true;
            }
        }
        return false;
    }

    /** True when the endpoint answered 429 (login lockout). */
    public static boolean isLocked(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c instanceof HTTPException && ((HTTPException) c).getStatusCode() == 429) {
                return true;
            }
            String m = c.getMessage();
            if (m != null && m.contains("429")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Message for the user. A SOAP fault carries the business message written by the server; any other
     * failure is logged and replaced by a generic text.
     */
    public static String message(Throwable t) {
        if (t instanceof SOAPFaultException) {
            String m = ((SOAPFaultException) t).getFault().getFaultString();
            return m == null || m.isBlank() ? "Erreur du service." : m;
        }
        if (isUnauthorized(t)) {
            return "Session expirée ou identifiants refusés par le service.";
        }
        if (isLocked(t)) {
            return "Trop de tentatives. Réessayez dans quelques minutes.";
        }
        if (t instanceof WebServiceException) {
            LOG.log(Level.WARNING, "SOAP call failed", t);
            SoapClients.reset();
            return "Le service SOAP est injoignable. Vérifiez qu'il est déployé et démarré.";
        }
        LOG.log(Level.SEVERE, "Unexpected error", t);
        return "Erreur interne de l'application.";
    }
}
