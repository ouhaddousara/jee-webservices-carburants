package ma.fstt.carburants.api.logic;

import ma.fstt.carburants.api.exception.BusinessException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Input validation shared by the SOAP service, the REST services and the services layer. */
public final class Validation {

    public static final int NOM_MAX = 100;
    public static final int ADRESSE_MAX = 255;
    public static final BigDecimal PRIX_MAX = new BigDecimal("999999.99");

    private Validation() {
    }

    /** Trims and checks a required text: not blank, not longer than {@code max}. */
    public static String requireText(String value, String label, int max) {
        if (value == null || value.trim().isEmpty()) {
            throw BusinessException.validation("Le champ « " + label + " » est obligatoire.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw BusinessException.validation("Le champ « " + label + " » ne doit pas dépasser " + max + " caractères.");
        }
        return trimmed;
    }

    /** Trims an optional text; returns null when blank. */
    public static String optionalText(String value, String label, int max) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > max) {
            throw BusinessException.validation("Le champ « " + label + " » ne doit pas dépasser " + max + " caractères.");
        }
        return trimmed;
    }

    /** Parses a strict ISO date (yyyy-MM-dd). A real calendar date is required: 2026-02-30 is refused. */
    public static LocalDate parseDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw BusinessException.validation("La date est obligatoire (format AAAA-MM-JJ).");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw BusinessException.validation("Date invalide : « " + value.trim() + " ». Format attendu : AAAA-MM-JJ.");
        }
    }

    /** A price is a non-negative amount with at most two decimals, below 1 000 000. */
    public static BigDecimal requirePrice(BigDecimal value) {
        if (value == null) {
            throw BusinessException.validation("Le prix est obligatoire.");
        }
        if (value.signum() < 0) {
            throw BusinessException.validation("Le prix ne peut pas être négatif.");
        }
        if (value.scale() > 2 && value.stripTrailingZeros().scale() > 2) {
            throw BusinessException.validation("Le prix ne peut pas avoir plus de deux décimales.");
        }
        if (value.compareTo(PRIX_MAX) > 0) {
            throw BusinessException.validation("Le prix est trop élevé.");
        }
        return value.setScale(2, java.math.RoundingMode.UNNECESSARY);
    }

    public static Long requireId(Long id, String label) {
        if (id == null || id <= 0) {
            throw BusinessException.validation("L'identifiant « " + label + " » est invalide.");
        }
        return id;
    }
}
