package com.devprogen.infrastructure.utility;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Utility {
    @Value("${spring.application.name}")
    public String applicationName;

    @Value("${spring.application.logoURL}")
    public String applicationLogoUrl;

    @Value("${spring.application.supportMail}")
    public String applicationSupportEmail;

    @Value("${spring.application.domainURL}")
    public String applicationDomainUrl;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text, boolean isHtml) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(applicationSupportEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, isHtml);

        javaMailSender.send(message);
    }

    public static String generateRandomAlphanumeric(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    public static File combineZipFiles(File zipFileBackEnd, File zipFileFrontEnd, String outputZipPath) throws IOException {
        Path combinedZipPath = Paths.get(outputZipPath);
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(combinedZipPath));
             FileInputStream fisBackEnd = new FileInputStream(zipFileBackEnd);
             FileInputStream fisFrontEnd = new FileInputStream(zipFileFrontEnd)) {

            ZipEntry backEndEntry = new ZipEntry(zipFileBackEnd.getName());
            zos.putNextEntry(backEndEntry);
            copyStream(fisBackEnd, zos);
            zos.closeEntry();

            ZipEntry frontEndEntry = new ZipEntry(zipFileFrontEnd.getName());
            zos.putNextEntry(frontEndEntry);
            copyStream(fisFrontEnd, zos);
            zos.closeEntry();
        }

        return combinedZipPath.toFile();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) > 0) {
            out.write(buffer, 0, length);
        }
    }

    public static void deleteDirectoryRecursively(File file) throws IOException {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                deleteDirectoryRecursively(subFile);
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file: " + file.getAbsolutePath());
        }
    }
}