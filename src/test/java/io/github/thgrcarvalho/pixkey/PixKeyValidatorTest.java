package io.github.thgrcarvalho.pixkey;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PixKeyValidatorTest {

    // ── CPF ──────────────────────────────────────────────────────────────────

    @Nested
    class CpfTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "529.982.247-25",  // formatted
                "52998224725",     // plain digits
                "111.444.777-35",  // another valid CPF
        })
        void validCpf(String key) {
            assertTrue(PixKeyValidator.validate(key, PixKeyType.CPF).valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "111.111.111-11",  // all same digits
                "000.000.000-00",
                "529.982.247-26",  // bad check digit
                "1234567890",      // 10 digits
                "123456789012",    // 12 digits
                "abc.def.ghi-jk",  // non-numeric
        })
        void invalidCpf(String key) {
            assertFalse(PixKeyValidator.validate(key, PixKeyType.CPF).valid());
        }

        @Test
        void failureIncludesMessage() {
            PixKeyValidationResult result = PixKeyValidator.validate("111.111.111-11", PixKeyType.CPF);
            assertFalse(result.valid());
            assertNotNull(result.message());
        }
    }

    // ── CNPJ ─────────────────────────────────────────────────────────────────

    @Nested
    class CnpjTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "11.222.333/0001-81",  // formatted
                "11222333000181",      // plain digits
                "45.997.418/0001-53",  // another valid CNPJ
        })
        void validCnpj(String key) {
            assertTrue(PixKeyValidator.validate(key, PixKeyType.CNPJ).valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "11.111.111/1111-11",  // all same digits
                "11.222.333/0001-82",  // bad check digit
                "1234567890123",       // 13 digits
                "123456789012345",     // 15 digits
        })
        void invalidCnpj(String key) {
            assertFalse(PixKeyValidator.validate(key, PixKeyType.CNPJ).valid());
        }
    }

    // ── Email ─────────────────────────────────────────────────────────────────

    @Nested
    class EmailTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "user@example.com",
                "thgrcarvalho@gmail.com",
                "user+tag@domain.com.br",
                "u@d.io",
        })
        void validEmail(String key) {
            assertTrue(PixKeyValidator.validate(key, PixKeyType.EMAIL).valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "notanemail",
                "@nodomain.com",
                "user@",
                "user @example.com",
        })
        void invalidEmail(String key) {
            assertFalse(PixKeyValidator.validate(key, PixKeyType.EMAIL).valid());
        }

        @Test
        void emailExceeding77CharsIsInvalid() {
            String longEmail = "a".repeat(70) + "@example.com"; // > 77
            assertFalse(PixKeyValidator.validate(longEmail, PixKeyType.EMAIL).valid());
        }

        @Test
        void emailExactly77CharsIsValid() {
            // local(65) + @ + domain(11) = 77
            String email = "a".repeat(65) + "@example.com";
            assertEquals(77, email.length());
            assertTrue(PixKeyValidator.validate(email, PixKeyType.EMAIL).valid());
        }
    }

    // ── Phone ─────────────────────────────────────────────────────────────────

    @Nested
    class PhoneTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "+5511987654321",   // Brazilian mobile (11 digits)
                "+5511987654321",   // Same
                "+5521987654321",   // Rio de Janeiro
                "+14155552671",     // US number
                "+442071234567",    // UK number
        })
        void validPhone(String key) {
            assertTrue(PixKeyValidator.validate(key, PixKeyType.PHONE).valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "5511987654321",    // missing +
                "+55",              // too short
                "+55 11 98765-4321", // spaces and hyphens
                "+0011987654321",   // country code starts with 0
        })
        void invalidPhone(String key) {
            assertFalse(PixKeyValidator.validate(key, PixKeyType.PHONE).valid());
        }
    }

    // ── EVP ──────────────────────────────────────────────────────────────────

    @Nested
    class EvpTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "123e4567-e89b-42d3-a456-556642440000",
                "550e8400-e29b-41d4-a716-446655440000",
                "6ba7b810-9dad-41d1-80b4-00c04fd430c8",
        })
        void validEvp(String key) {
            assertTrue(PixKeyValidator.validate(key, PixKeyType.EVP).valid());
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "not-a-uuid",
                "123e4567-e89b-12d3-a456-556642440000",  // version not 4
                "123e4567e89b42d3a456556642440000",       // missing hyphens
                "123e4567-e89b-42d3-c456-556642440000",  // invalid variant (c)
        })
        void invalidEvp(String key) {
            assertFalse(PixKeyValidator.validate(key, PixKeyType.EVP).valid());
        }
    }

    // ── Auto-detection ────────────────────────────────────────────────────────

    @Nested
    class DetectionTests {

        @Test
        void detectsCpf() {
            assertEquals(Optional.of(PixKeyType.CPF), PixKeyValidator.detect("529.982.247-25"));
            assertEquals(Optional.of(PixKeyType.CPF), PixKeyValidator.detect("52998224725"));
        }

        @Test
        void detectsCnpj() {
            assertEquals(Optional.of(PixKeyType.CNPJ), PixKeyValidator.detect("11.222.333/0001-81"));
        }

        @Test
        void detectsEmail() {
            assertEquals(Optional.of(PixKeyType.EMAIL), PixKeyValidator.detect("user@example.com"));
        }

        @Test
        void detectsPhone() {
            assertEquals(Optional.of(PixKeyType.PHONE), PixKeyValidator.detect("+5511987654321"));
        }

        @Test
        void detectsEvp() {
            assertEquals(Optional.of(PixKeyType.EVP),
                    PixKeyValidator.detect("123e4567-e89b-42d3-a456-556642440000"));
        }

        @Test
        void returnsEmptyForInvalidKey() {
            assertTrue(PixKeyValidator.detect("this-is-not-a-pix-key").isEmpty());
        }

        @Test
        void returnsEmptyForNull() {
            assertTrue(PixKeyValidator.detect(null).isEmpty());
        }

        @Test
        void isValidReturnsTrueForValidKey() {
            assertTrue(PixKeyValidator.isValid("529.982.247-25"));
            assertTrue(PixKeyValidator.isValid("user@example.com"));
            assertTrue(PixKeyValidator.isValid("+5511987654321"));
        }

        @Test
        void isValidReturnsFalseForInvalidKey() {
            assertFalse(PixKeyValidator.isValid("111.111.111-11")); // all same digits
            assertFalse(PixKeyValidator.isValid("notvalid"));
        }
    }

    // ── Null / blank guards ───────────────────────────────────────────────────

    @Test
    void nullKeyReturnsFailure() {
        PixKeyValidationResult result = PixKeyValidator.validate(null, PixKeyType.CPF);
        assertFalse(result.valid());
        assertNotNull(result.message());
    }

    @Test
    void blankKeyReturnsFailure() {
        assertFalse(PixKeyValidator.validate("  ", PixKeyType.EMAIL).valid());
    }
}
