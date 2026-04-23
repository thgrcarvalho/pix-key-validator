package io.github.thgrcarvalho.pixkey;

/**
 * The five key types supported by Brazil's Pix instant payment network,
 * as defined by Banco Central do Brasil (BCB).
 */
public enum PixKeyType {
    /** Brazilian individual taxpayer number (11 digits, with check-digit verification). */
    CPF,
    /** Brazilian company taxpayer number (14 digits, with check-digit verification). */
    CNPJ,
    /** Email address (max 77 characters per BCB specification). */
    EMAIL,
    /** Mobile phone number in E.164 format (e.g. {@code +5511987654321}). */
    PHONE,
    /** Random virtual address — a UUID v4 assigned by the participant institution. */
    EVP
}
