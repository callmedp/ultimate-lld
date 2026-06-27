package com.ultimatelld.theory.module02patterns.structural;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * DECORATOR — adds real GZIP compression on write and decompression on read. Because it conforms
 * to {@link DataSource}, it can wrap the bare component OR another decorator, in any order.
 */
public final class CompressionDecorator extends DataSourceDecorator {

    public CompressionDecorator(DataSource wrappee) {
        super(wrappee);
    }

    @Override
    public void write(String data) {
        super.write(compress(data));
    }

    @Override
    public String read() {
        return decompress(super.read());
    }

    private static String compress(String data) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                gzip.write(data.getBytes(StandardCharsets.UTF_8));
            }
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("compression failed", e);
        }
    }

    private static String decompress(String data) {
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
                return new String(gzip.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new IllegalStateException("decompression failed", e);
        }
    }
}
