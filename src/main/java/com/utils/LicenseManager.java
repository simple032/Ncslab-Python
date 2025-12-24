package com.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class LicenseManager {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final String SECRET_KEY = generateKey();

    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            SecureRandom secureRandom = new SecureRandom();
            keyGen.init(256, secureRandom);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encrypt(String user, LocalDate startDate, LocalDate endDate, String cpuid) {
        try {
            long timestamp = Instant.now().getEpochSecond();
            String data = String.format("{\"user\":\"%s\",\"start_date\":\"%s\",\"end_date\":\"%s\",\"timestamp\":%d,\"cpuid\":\"%s\"}", user, startDate, endDate, timestamp, cpuid);
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean validateLicense(String licenseKey, String cpuid) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(Base64.getDecoder().decode(SECRET_KEY), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(licenseKey));
            String data = new String(decryptedBytes, StandardCharsets.UTF_8);

            String[] parts = data.replaceAll("[{}\"]", "").split(",");
            String user = parts[0].split(":")[1];
            LocalDate startDate = LocalDate.parse(parts[1].split(":")[1]);
            LocalDate endDate = LocalDate.parse(parts[2].split(":")[1]);
            long timestamp = Long.parseLong(parts[3].split(":")[1]);
            String storedCpuid = parts[4].split(":")[1];

            if (!storedCpuid.equals(cpuid)) {
                return false;
            }

            LocalDate now = LocalDate.now();
            return !now.isBefore(startDate) && !now.isAfter(endDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void checkAndExpire(String cpuid) {
        LocalDate start_date = LocalDate.of(2025, 7, 1);
        LocalDate end_date = start_date.plusDays(30);
        String encrypt_string = LicenseManager.encrypt("18", start_date, end_date, cpuid);
        if (LicenseManager.validateLicense(encrypt_string, cpuid)) {
            System.out.println("Validate license");
        } else {
            System.err.println("Failed to validate license");
            System.exit(1);
        }
    }

    /**
     * 获取当前计算机的 CPU ID
     * 支持 Windows 和 Linux 系统
     * @return CPU ID 字符串，如果获取失败则返回 null
     */
    private static String getCPUID() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                // Windows 系统通过 wmic 命令获取 CPU ID
                // Use ProcessBuilder instead of deprecated Runtime.exec()
                ProcessBuilder processBuilder = new ProcessBuilder("wmic", "cpu", "get", "processorid");
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("ProcessorId")) {
                        return line;
                    }
                }
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                // Linux 或 macOS 系统通过 dmidecode 命令获取 CPU 信息
                // Use ProcessBuilder instead of deprecated Runtime.exec()
                ProcessBuilder processBuilder = new ProcessBuilder("dmidecode", "-t", "processor");
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("ID:")) {
                        return line.substring(3).trim();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null; // 获取失败时返回 null
    }

    /**
     * 自动获取 CPU ID 并检查许可证
     */
    public static void checkAndExpire() {
        String cpuid = getCPUID();
        if (cpuid == null) {
            System.err.println("无法获取 CPU ID，授权验证失败");
            System.exit(1);
        }

        List<String> whiteList = new ArrayList<>();
        whiteList.add("178BFBFF00A70F41");
        whiteList.add("178BFBFF00860F01");
        whiteList.add("BFEBFBFF000A0652");
        whiteList.add("BFEBFBFF00050654"); //Sustech server
        if(!whiteList.contains(cpuid)){
            System.err.println("CPU ID 不在白名单中，授权验证失败");
            System.exit(1);
        }

        checkAndExpire(cpuid);
    }
}
