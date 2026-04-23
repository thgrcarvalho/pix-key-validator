package io.github.thgrcarvalho.pixkey;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Validates Brazilian Pix keys for all five key types defined by BCB.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Validate with explicit type
 * PixKeyValidationResult result = PixKeyValidator.validate("12345678909", PixKeyType.CPF);
 *
 * // Auto-detect type and validate
 * Optional<PixKeyType> type = PixKeyValidator.detect("12345678909");
 * boolean valid = PixKeyValidator.isValid("12345678909");
 * }</pre>
 *
 * <p>CPF and CNPJ validation includes full check-digit verification.
 * Formatted inputs (with {@code .}, {@code -}, {@code /} punctuation) are accepted.</p>
 */
public final class PixKeyValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+[1-9]\\d{6,14}$");
    private static final Pattern EVP_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);
    private static final int EMAIL_MAX_LENGTH = 77;

    private PixKeyValidator() {}

    /**
     * Validates {@code key} against the given {@code type}.
     *
     * @param key  the Pix key to validate
     * @param type the expected key type
     * @return the validation result
     */
    public static PixKeyValidationResult validate(String key, PixKeyType type) {
        if (key == null || key.isBlank()) {
            return PixKeyValidationResult.fail(type, "Key must not be blank");
        }
        return switch (type) {
            case CPF -> validateCpf(key.trim());
            case CNPJ -> validateCnpj(key.trim());
            case EMAIL -> validateEmail(key.trim());
            case PHONE -> validatePhone(key.trim());
            case EVP -> validateEvp(key.trim());
        };
    }

    /**
     * Attempts to detect the type of {@code key} and returns the type if it passes
     * validation, or {@link Optional#empty()} if no type matches.
     *
     * @param key the Pix key to detect
     * @return the detected and validated key type, or empty
     */
    public static Optional<PixKeyType> detect(String key) {
        if (key == null || key.isBlank()) return Optional.empty();
        String trimmed = key.trim();

        for (PixKeyType type : inferCandidates(trimmed)) {
            if (validate(trimmed, type).valid()) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns {@code true} if {@code key} is valid for any Pix key type.
     *
     * @param key the Pix key to test
     * @return {@code true} if valid
     */
    public static boolean isValid(String key) {
        return detect(key).isPresent();
    }

    // ── CPF ─────────────────────────────────────────────────────────────────

    private static PixKeyValidationResult validateCpf(String raw) {
        String digits = raw.replaceAll("[.\\-]", "");
        if (!digits.matches("\\d{11}")) {
            return PixKeyValidationResult.fail(PixKeyType.CPF, "CPF must have 11 digits");
        }
        if (allSameDigit(digits)) {
            return PixKeyValidationResult.fail(PixKeyType.CPF, "CPF must not have all identical digits");
        }
        if (!checkCpfDigit(digits, 9) || !checkCpfDigit(digits, 10)) {
            return PixKeyValidationResult.fail(PixKeyType.CPF, "CPF check digits are invalid");
        }
        return PixKeyValidationResult.ok(PixKeyType.CPF);
    }

    private static boolean checkCpfDigit(String digits, int position) {
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += (digits.charAt(i) - '0') * (position + 1 - i);
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return digits.charAt(position) - '0' == expected;
    }

    // ── CNPJ ────────────────────────────────────────────────────────────────

    private static PixKeyValidationResult validateCnpj(String raw) {
        String digits = raw.replaceAll("[.\\-/]", "");
        if (!digits.matches("\\d{14}")) {
            return PixKeyValidationResult.fail(PixKeyType.CNPJ, "CNPJ must have 14 digits");
        }
        if (allSameDigit(digits)) {
            return PixKeyValidationResult.fail(PixKeyType.CNPJ, "CNPJ must not have all identical digits");
        }
        if (!checkCnpjDigit(digits, 12) || !checkCnpjDigit(digits, 13)) {
            return PixKeyValidationResult.fail(PixKeyType.CNPJ, "CNPJ check digits are invalid");
        }
        return PixKeyValidationResult.ok(PixKeyType.CNPJ);
    }

    private static boolean checkCnpjDigit(String digits, int position) {
        int[] weights = position == 12
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < position; i++) {
            sum += (digits.charAt(i) - '0') * weights[i];
        }
        int remainder = sum % 11;
        int expected = remainder < 2 ? 0 : 11 - remainder;
        return digits.charAt(position) - '0' == expected;
    }

    // ── Email ────────────────────────────────────────────────────────────────

    private static PixKeyValidationResult validateEmail(String key) {
        if (key.length() > EMAIL_MAX_LENGTH) {
            return PixKeyValidationResult.fail(PixKeyType.EMAIL,
                    "Email must not exceed " + EMAIL_MAX_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(key).matches()) {
            return PixKeyValidationResult.fail(PixKeyType.EMAIL, "Invalid email format");
        }
        return PixKeyValidationResult.ok(PixKeyType.EMAIL);
    }

    // ── Phone ────────────────────────────────────────────────────────────────

    private static PixKeyValidationResult validatePhone(String key) {
        if (!PHONE_PATTERN.matcher(key).matches()) {
            return PixKeyValidationResult.fail(PixKeyType.PHONE,
                    "Phone must be in E.164 format (e.g. +5511987654321)");
        }
        return PixKeyValidationResult.ok(PixKeyType.PHONE);
    }

    // ── EVP ──────────────────────────────────────────────────────────────────

    private static PixKeyValidationResult validateEvp(String key) {
        if (!EVP_PATTERN.matcher(key).matches()) {
            return PixKeyValidationResult.fail(PixKeyType.EVP,
                    "EVP must be a UUID v4 (e.g. 123e4567-e89b-42d3-a456-556642440000)");
        }
        return PixKeyValidationResult.ok(PixKeyType.EVP);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean allSameDigit(String digits) {
        char first = digits.charAt(0);
        for (int i = 1; i < digits.length(); i++) {
            if (digits.charAt(i) != first) return false;
        }
        return true;
    }

    private static PixKeyType[] inferCandidates(String key) {
        if (key.startsWith("+")) return new PixKeyType[]{PixKeyType.PHONE};
        if (key.contains("@")) return new PixKeyType[]{PixKeyType.EMAIL};
        if (EVP_PATTERN.matcher(key).matches()) return new PixKeyType[]{PixKeyType.EVP};
        String digits = key.replaceAll("[.\\-/]", "");
        if (digits.length() == 11 && digits.matches("\\d+")) return new PixKeyType[]{PixKeyType.CPF};
        if (digits.length() == 14 && digits.matches("\\d+")) return new PixKeyType[]{PixKeyType.CNPJ};
        return new PixKeyType[]{PixKeyType.EMAIL, PixKeyType.EVP};
    }
}
