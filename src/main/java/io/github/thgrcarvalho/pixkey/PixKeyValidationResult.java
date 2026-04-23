package io.github.thgrcarvalho.pixkey;

/**
 * The result of a Pix key validation.
 *
 * @param valid   {@code true} if the key is valid for its type
 * @param keyType the type detected or validated against
 * @param message a human-readable description of the validation failure,
 *                or {@code null} when {@code valid} is {@code true}
 */
public record PixKeyValidationResult(boolean valid, PixKeyType keyType, String message) {

    /** Convenience factory for a successful validation. */
    static PixKeyValidationResult ok(PixKeyType type) {
        return new PixKeyValidationResult(true, type, null);
    }

    /** Convenience factory for a failed validation. */
    static PixKeyValidationResult fail(PixKeyType type, String message) {
        return new PixKeyValidationResult(false, type, message);
    }
}
