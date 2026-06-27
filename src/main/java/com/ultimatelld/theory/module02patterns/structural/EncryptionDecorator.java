package com.ultimatelld.theory.module02patterns.structural;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * DECORATOR — adds reversible obfuscation (XOR cipher) on write and reverses it on read.
 * <p>
 * A real system would use AES; XOR keeps the demo dependency-free while still proving the
 * decorator stacks correctly (encrypt(compress(data)) round-trips back to the original).
 */
public final class EncryptionDecorator extends DataSourceDecorator {

    private static final byte KEY = 0x5A;

    public EncryptionDecorator(DataSource wrappee) {
        super(wrappee);
    }

    @Override
    public void write(String data) {
        super.write(xorBase64(data));
    }

    @Override
    public String read() {
        return unXorBase64(super.read());
    }

    private static String xorBase64(String data) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= KEY;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String unXorBase64(String data) {
        byte[] bytes = Base64.getDecoder().decode(data);
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] ^= KEY;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
