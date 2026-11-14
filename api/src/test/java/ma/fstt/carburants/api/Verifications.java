package ma.fstt.carburants.api;

import ma.fstt.carburants.api.dto.PrixCarburantDto;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.api.logic.PriceResolver;
import ma.fstt.carburants.api.logic.Validation;
import ma.fstt.carburants.api.security.BasicCredentials;
import ma.fstt.carburants.api.security.CredentialStore;
import ma.fstt.carburants.api.security.JwtService;
import ma.fstt.carburants.api.security.LoginRateLimiter;
import ma.fstt.carburants.api.security.PasswordHasher;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * Automatic checks of the pure logic (no server, no database), run with:
 * {@code mvn -q -pl api test-compile exec:java -Dexec.mainClass=ma.fstt.carburants.api.Verifications
 * -Dexec.classpathScope=test}.
 *
 * <p>The report quotes the real total printed at the end, never a number written by hand.
 */
public final class Verifications {

    private static int checks = 0;
    private static int failures = 0;

    /** A clock whose time can be moved, to test expiry and lockout windows. */
    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advanceSeconds(long s) {
            now = now.plusSeconds(s);
        }

        @Override
        public java.time.ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private static void check(String label, boolean condition) {
        checks++;
        if (!condition) {
            failures++;
            System.out.println("  FAIL  " + label);
        }
    }

    private static void expectBusiness(String label, BusinessException.Code code, Runnable action) {
        checks++;
        try {
            action.run();
            failures++;
            System.out.println("  FAIL  " + label + " (no exception)");
        } catch (BusinessException e) {
            if (e.getCode() != code) {
                failures++;
                System.out.println("  FAIL  " + label + " (got " + e.getCode() + ", expected " + code + ")");
            }
        } catch (RuntimeException e) {
            failures++;
            System.out.println("  FAIL  " + label + " (unexpected " + e.getClass().getSimpleName() + ")");
        }
    }

    private static PriceResolver.Entry entry(String fuel, String date, String price) {
        return new PriceResolver.Entry(fuel, LocalDate.parse(date), new BigDecimal(price));
    }

    // ------------------------------------------------------------------ Batch 1: price in force (D4, D5)

    private static void priceResolution() {
        System.out.println("Batch 1 - price in force at a date");
        List<PriceResolver.Entry> rows = Arrays.asList(
                entry("Gasoil", "2026-11-01", "10.45"),
                entry("Gasoil", "2026-11-08", "10.62"),
                entry("Sans plomb 95", "2026-11-01", "11.60"),
                entry("Sans plomb 95", "2026-11-08", "11.75"));

        List<PrixCarburantDto> onChangeDay = PriceResolver.resolve(rows, LocalDate.parse("2026-11-08"));
        check("two fuels returned on the day of a change", onChangeDay.size() == 2);
        check("price of the day itself is used", onChangeDay.get(0).getPrix().compareTo(new BigDecimal("10.62")) == 0);
        check("fuels are ordered by name", "Gasoil".equals(onChangeDay.get(0).getCarburant())
                && "Sans plomb 95".equals(onChangeDay.get(1).getCarburant()));

        List<PrixCarburantDto> between = PriceResolver.resolve(rows, LocalDate.parse("2026-11-05"));
        check("between two changes the older price still applies", between.get(0).getPrix().compareTo(new BigDecimal("10.45")) == 0);
        check("the date of the row in force is reported", "2026-11-01".equals(between.get(0).getDatePrix()));

        List<PrixCarburantDto> afterAll = PriceResolver.resolve(rows, LocalDate.parse("2027-01-01"));
        check("after the last change the last price applies", afterAll.get(1).getPrix().compareTo(new BigDecimal("11.75")) == 0);

        check("before the first price there is nothing", PriceResolver.resolve(rows, LocalDate.parse("2026-10-31")).isEmpty());
        check("no history gives an empty list", PriceResolver.resolve(new ArrayList<>(), LocalDate.parse("2026-11-08")).isEmpty());

        List<PriceResolver.Entry> unsorted = Arrays.asList(
                entry("Gasoil", "2026-11-08", "10.62"),
                entry("Gasoil", "2026-11-01", "10.45"));
        check("input order does not matter",
                PriceResolver.resolve(unsorted, LocalDate.parse("2026-11-09")).get(0).getPrix().compareTo(new BigDecimal("10.62")) == 0);

        List<PriceResolver.Entry> oneFuelStarted = Arrays.asList(
                entry("Gasoil", "2026-11-01", "10.45"),
                entry("Sans plomb 95", "2026-11-06", "11.60"));
        List<PrixCarburantDto> partial = PriceResolver.resolve(oneFuelStarted, LocalDate.parse("2026-11-03"));
        check("a fuel without price yet is left out, the other one is kept", partial.size() == 1 && "Gasoil".equals(partial.get(0).getCarburant()));
    }

    // ------------------------------------------------------------------ Batch 2: validation

    private static void validation() {
        System.out.println("Batch 2 - input validation");
        check("text is trimmed", "Tanger".equals(Validation.requireText("  Tanger ", "ville", 100)));
        expectBusiness("blank required text refused", BusinessException.Code.VALIDATION, () -> Validation.requireText("   ", "nom", 100));
        expectBusiness("null required text refused", BusinessException.Code.VALIDATION, () -> Validation.requireText(null, "nom", 100));
        expectBusiness("too long text refused", BusinessException.Code.VALIDATION, () -> Validation.requireText("x".repeat(101), "nom", 100));
        check("optional blank text becomes null", Validation.optionalText("  ", "description", 255) == null);
        check("optional text is trimmed", "diesel".equals(Validation.optionalText(" diesel ", "description", 255)));

        check("valid ISO date parsed", LocalDate.of(2026, 11, 8).equals(Validation.parseDate("2026-11-08")));
        check("date with spaces parsed", LocalDate.of(2026, 11, 8).equals(Validation.parseDate(" 2026-11-08 ")));
        expectBusiness("impossible date refused", BusinessException.Code.VALIDATION, () -> Validation.parseDate("2026-02-30"));
        expectBusiness("wrong format refused", BusinessException.Code.VALIDATION, () -> Validation.parseDate("08/11/2026"));
        expectBusiness("empty date refused", BusinessException.Code.VALIDATION, () -> Validation.parseDate(""));
        expectBusiness("null date refused", BusinessException.Code.VALIDATION, () -> Validation.parseDate(null));

        check("price keeps two decimals", new BigDecimal("10.50").compareTo(Validation.requirePrice(new BigDecimal("10.5"))) == 0);
        check("price scale is normalised to 2", Validation.requirePrice(new BigDecimal("10.5")).scale() == 2);
        check("zero price accepted", Validation.requirePrice(BigDecimal.ZERO).signum() == 0);
        check("trailing zeros beyond two decimals accepted", Validation.requirePrice(new BigDecimal("10.500")).compareTo(new BigDecimal("10.50")) == 0);
        expectBusiness("negative price refused", BusinessException.Code.VALIDATION, () -> Validation.requirePrice(new BigDecimal("-0.01")));
        expectBusiness("three decimals refused", BusinessException.Code.VALIDATION, () -> Validation.requirePrice(new BigDecimal("10.123")));
        expectBusiness("null price refused", BusinessException.Code.VALIDATION, () -> Validation.requirePrice(null));
        expectBusiness("huge price refused", BusinessException.Code.VALIDATION, () -> Validation.requirePrice(new BigDecimal("1000000")));
        expectBusiness("null id refused", BusinessException.Code.VALIDATION, () -> Validation.requireId(null, "station"));
        expectBusiness("zero id refused", BusinessException.Code.VALIDATION, () -> Validation.requireId(0L, "station"));
        check("positive id accepted", Validation.requireId(5L, "station") == 5L);
    }

    // ------------------------------------------------------------------ Batch 3: JWT

    private static void jwt() {
        System.out.println("Batch 3 - JWT");
        String secret = "0123456789abcdef0123456789abcdef-test";
        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));
        JwtService jwt = new JwtService(secret, 900, clock);

        String token = jwt.issue("admin");
        check("token has three parts", token.split("\\.").length == 3);
        check("valid token gives back the user", "admin".equals(jwt.verify(token)));
        check("Bearer header accepted", "admin".equals(jwt.verifyBearerHeader("Bearer " + token)));
        check("Bearer scheme is case-insensitive", "admin".equals(jwt.verifyBearerHeader("bearer " + token)));

        expectBusiness("missing header refused", BusinessException.Code.UNAUTHORIZED, () -> jwt.verifyBearerHeader(null));
        expectBusiness("Basic header refused as Bearer", BusinessException.Code.UNAUTHORIZED, () -> jwt.verifyBearerHeader("Basic abc"));
        expectBusiness("null token refused", BusinessException.Code.UNAUTHORIZED, () -> jwt.verify(null));
        expectBusiness("garbage token refused", BusinessException.Code.UNAUTHORIZED, () -> jwt.verify("not-a-token"));
        expectBusiness("two-part token refused", BusinessException.Code.UNAUTHORIZED, () -> jwt.verify("a.b"));

        String[] parts = token.split("\\.");
        String tamperedSig = parts[0] + "." + parts[1] + "." + parts[2].substring(0, parts[2].length() - 2) + "AA";
        expectBusiness("altered signature refused", BusinessException.Code.UNAUTHORIZED, () -> jwt.verify(tamperedSig));

        String forgedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"sub\":\"root\",\"iat\":1,\"exp\":99999999999}".getBytes(StandardCharsets.UTF_8));
        expectBusiness("payload swapped under the old signature refused", BusinessException.Code.UNAUTHORIZED,
                () -> jwt.verify(parts[0] + "." + forgedPayload + "." + parts[2]));

        String noneHeader = Base64.getUrlEncoder().withoutPadding().encodeToString(
                "{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
        expectBusiness("alg=none token refused", BusinessException.Code.UNAUTHORIZED,
                () -> jwt.verify(noneHeader + "." + parts[1] + "."));

        JwtService other = new JwtService("another-secret-another-secret-123456", 900, clock);
        expectBusiness("token signed with another key refused", BusinessException.Code.UNAUTHORIZED, () -> other.verify(token));

        clock.advanceSeconds(899);
        check("token still valid one second before expiry", "admin".equals(jwt.verify(token)));
        clock.advanceSeconds(1);
        expectBusiness("token refused at expiry", BusinessException.Code.UNAUTHORIZED, () -> jwt.verify(token));

        check("quote in user name survives a round trip", "a\"b".equals(jwt.verify(new JwtService(secret, 900, clock).issue("a\"b"))));
        check("backslash in user name survives a round trip", "a\\b".equals(jwt.verify(new JwtService(secret, 900, clock).issue("a\\b"))));

        boolean shortSecretRefused = false;
        try {
            new JwtService("too-short", 900);
        } catch (IllegalArgumentException e) {
            shortSecretRefused = true;
        }
        check("secret shorter than 32 bytes refused", shortSecretRefused);

        boolean zeroTtlRefused = false;
        try {
            new JwtService(secret, 0);
        } catch (IllegalArgumentException e) {
            zeroTtlRefused = true;
        }
        check("non-positive lifetime refused", zeroTtlRefused);
    }

    // ------------------------------------------------------------------ Batch 4: passwords, Basic, lockout

    private static void authentication() {
        System.out.println("Batch 4 - passwords, Basic header, login lockout");
        String h1 = PasswordHasher.hash("s3cret!");
        String h2 = PasswordHasher.hash("s3cret!");
        check("hash has the documented format", h1.startsWith("pbkdf2$120000$"));
        check("two hashes of the same password differ (random salt)", !h1.equals(h2));
        check("right password matches", PasswordHasher.matches("s3cret!", h1));
        check("wrong password does not match", !PasswordHasher.matches("s3cret", h1));
        check("null password does not match", !PasswordHasher.matches(null, h1));
        check("garbage stored hash does not match", !PasswordHasher.matches("x", "garbage"));
        check("stored hash with bad base64 does not match", !PasswordHasher.matches("x", "pbkdf2$1000$!!$!!"));

        BasicCredentials ok = BasicCredentials.parse("Basic " + Base64.getEncoder().encodeToString("admin:p:w".getBytes(StandardCharsets.UTF_8)));
        check("Basic user parsed", "admin".equals(ok.getUsername()));
        check("Basic password may contain a colon", "p:w".equals(ok.getPassword()));
        expectBusiness("missing Basic header refused", BusinessException.Code.UNAUTHORIZED, () -> BasicCredentials.parse(null));
        expectBusiness("Bearer header refused as Basic", BusinessException.Code.UNAUTHORIZED, () -> BasicCredentials.parse("Bearer abc"));
        expectBusiness("bad base64 refused", BusinessException.Code.UNAUTHORIZED, () -> BasicCredentials.parse("Basic !!!"));
        expectBusiness("no colon refused", BusinessException.Code.UNAUTHORIZED,
                () -> BasicCredentials.parse("Basic " + Base64.getEncoder().encodeToString("nocolon".getBytes(StandardCharsets.UTF_8))));

        MutableClock clock = new MutableClock(Instant.parse("2026-11-12T10:00:00Z"));
        LoginRateLimiter limiter = new LoginRateLimiter(3, 300, clock);
        CredentialStore store = new CredentialStore(limiter).addPlain("admin", "s3cret!");

        store.authenticate("admin", "s3cret!");
        check("valid credentials accepted", true);
        expectBusiness("wrong password refused", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("admin", "nope"));
        expectBusiness("unknown user refused like a wrong password", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("ghost", "x"));
        expectBusiness("empty user refused", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("", "x"));
        expectBusiness("null password refused", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("admin", null));

        // The failure above already counted 1 for "admin"; two more reach the limit of 3.
        expectBusiness("second failure", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("admin", "bad2"));
        expectBusiness("third failure", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("admin", "bad3"));
        expectBusiness("fourth attempt is locked (429)", BusinessException.Code.TOO_MANY_REQUESTS, () -> store.authenticate("admin", "bad4"));
        expectBusiness("even the right password is refused while locked", BusinessException.Code.TOO_MANY_REQUESTS, () -> store.authenticate("admin", "s3cret!"));
        clock.advanceSeconds(301);
        store.authenticate("admin", "s3cret!");
        check("lock released after the window", true);

        store.authenticate("admin", "s3cret!");
        expectBusiness("a different user is not locked by admin's failures", BusinessException.Code.UNAUTHORIZED, () -> store.authenticate("ghost", "x"));
        check("HTTP status of UNAUTHORIZED is 401", BusinessException.unauthorized("x").getHttpStatus() == 401);
        check("HTTP status of VALIDATION is 400", BusinessException.validation("x").getHttpStatus() == 400);
        check("HTTP status of NOT_FOUND is 404", BusinessException.notFound("x").getHttpStatus() == 404);
        check("HTTP status of CONFLICT is 409", BusinessException.conflict("x").getHttpStatus() == 409);
        check("HTTP status of TOO_MANY_REQUESTS is 429", BusinessException.tooManyRequests("x").getHttpStatus() == 429);
    }

    public static void main(String[] args) {
        priceResolution();
        validation();
        jwt();
        authentication();
        System.out.println();
        System.out.println("TOTAL CHECKS: " + checks + "   FAILURES: " + failures);
        if (failures > 0) {
            System.exit(1);
        }
    }
}
