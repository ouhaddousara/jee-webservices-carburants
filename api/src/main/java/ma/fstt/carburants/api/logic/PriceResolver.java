package ma.fstt.carburants.api.logic;

import ma.fstt.carburants.api.dto.PrixCarburantDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decision D4 / D5 as pure code: for each fuel, the price in force at a date is the most recent entry
 * whose date is on or before the requested date. A price stays valid until the next one is set.
 *
 * <p>Kept free of JPA so it can be tested without a database; the repository only supplies the rows.
 */
public final class PriceResolver {

    private PriceResolver() {
    }

    /** One raw price row, as read from the history table. */
    public static final class Entry {
        private final String carburant;
        private final LocalDate date;
        private final BigDecimal prix;

        public Entry(String carburant, LocalDate date, BigDecimal prix) {
            this.carburant = carburant;
            this.date = date;
            this.prix = prix;
        }

        public String getCarburant() {
            return carburant;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getPrix() {
            return prix;
        }
    }

    /**
     * @return one {@link PrixCarburantDto} per fuel that has at least one entry on or before {@code at},
     *         ordered by fuel name; empty if there is none
     */
    public static List<PrixCarburantDto> resolve(List<Entry> entries, LocalDate at) {
        Map<String, Entry> best = new LinkedHashMap<>();
        for (Entry e : entries) {
            if (e.getDate().isAfter(at)) {
                continue;
            }
            Entry current = best.get(e.getCarburant());
            if (current == null || e.getDate().isAfter(current.getDate())) {
                best.put(e.getCarburant(), e);
            }
        }
        List<PrixCarburantDto> result = new ArrayList<>();
        for (Entry e : best.values()) {
            result.add(new PrixCarburantDto(e.getCarburant(), e.getPrix(), e.getDate().toString()));
        }
        result.sort(Comparator.comparing(PrixCarburantDto::getCarburant, String.CASE_INSENSITIVE_ORDER));
        return result;
    }
}
