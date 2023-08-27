package com.mslfox.cloudStorageServices.util;

import org.springframework.beans.factory.annotation.Value;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtil {
    @Value("${file.system.storage.hash.algorithm:SHA-256}")
    private String algorithm;

    public String calcHash(byte[] fileBytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        int offset = 0;
        while (offset < fileBytes.length) {
            int length = Math.min(8192, fileBytes.length - offset);
            md.update(fileBytes, offset, length);
            offset += length;
        }
        return bytesToHex(md.digest());
    }

    public String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}