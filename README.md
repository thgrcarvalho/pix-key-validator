# pix-key-validator

[![CI](https://github.com/thgrcarvalho/pix-key-validator/actions/workflows/ci.yml/badge.svg)](https://github.com/thgrcarvalho/pix-key-validator/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.thgrcarvalho/pix-key-validator)](https://central.sonatype.com/artifact/io.github.thgrcarvalho/pix-key-validator)
[![codecov](https://codecov.io/gh/thgrcarvalho/pix-key-validator/branch/main/graph/badge.svg)](https://codecov.io/gh/thgrcarvalho/pix-key-validator)

Zero-dependency Java 21 library for validating all five Brazilian Pix key types defined by Banco Central do Brasil. CPF and CNPJ validation includes full check-digit verification.

## Installation

**Gradle:**
```groovy
dependencies {
    implementation 'io.github.thgrcarvalho:pix-key-validator:0.1.0'
}
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.thgrcarvalho</groupId>
    <artifactId>pix-key-validator</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Usage

```java
// Validate with explicit type
PixKeyValidationResult result = PixKeyValidator.validate("529.982.247-25", PixKeyType.CPF);
result.valid();   // true
result.message(); // null on success, reason on failure

// Auto-detect type
Optional<PixKeyType> type = PixKeyValidator.detect("user@example.com"); // Optional[EMAIL]
boolean valid = PixKeyValidator.isValid("+5511987654321"); // true
```

## Supported key types

| Type | Format | Validation |
|------|--------|-----------|
| `CPF` | 11 digits — plain (`52998224725`) or formatted (`529.982.247-25`) | Modulo-11 check digits |
| `CNPJ` | 14 digits — plain or formatted (`11.222.333/0001-81`) | Modulo-11 check digits |
| `EMAIL` | Standard email, max 77 characters (BCB limit) | Format + length |
| `PHONE` | E.164 format (`+5511987654321`) | Format only |
| `EVP` | UUID v4 (`123e4567-e89b-42d3-a456-556642440000`) | UUID v4 format |

## Running tests

```bash
./gradlew test
```

## Tech

Java 21 · Gradle · JUnit 5 · Zero dependencies
