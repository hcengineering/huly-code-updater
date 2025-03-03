package com.hulylabs.updater;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hulylabs.updater.model.BuildInfo;
import com.hulylabs.updater.model.BuildInfoPatch;
import com.hulylabs.updater.model.ProductChannel;
import com.hulylabs.updater.model.Products;
import com.intellij.updater.Runner;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Enumeration;
import java.util.HashSet;

public class UpdateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(UpdateGenerator.class);
    private static final String BASE_URL = "https://dist.huly.io/code/";
    private static final String BASE_UPDATE_URL = "https://dist.huly.io/code/update/";
    private static final String UPDATE_XML = "updates.xml";
    private static final String TARGET_DIR = "build/update/";
    private static final String MAJOR_VERSION = "251";

    private final int version;

    UpdateGenerator(int version) {
        this.version = version;
    }

    public Integer process() throws Exception {
        logger.info("Generate update.xml for version '{}'", version);
        Path targetPath = Path.of(TARGET_DIR);
        Files.createDirectories(targetPath);

        // get update.xml
        XmlMapper xmlMapper = XmlMapper.xmlBuilder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .defaultUseWrapper(false)
                .build();
        Products products;
        try {
            String updateXmlStr = IOUtils.toString(URI.create(BASE_UPDATE_URL).resolve(UPDATE_XML), StandardCharsets.UTF_8);
            products = xmlMapper.readValue(updateXmlStr, Products.class);
        } catch (FileNotFoundException e) {
            logger.warn("update.xml not found, create default", e);
            products = Products.defaultProducts();
        } catch (IOException e) {
            logger.error("Failed to get update.xml", e);
            return 1;
        }

        ProductChannel channel = products.products.get(0).channel;

        // search previous version
        logger.info("Search previous version");
        int previousVersion = version - 1;
        boolean found = false;
        while (previousVersion > version - 10) {
            String prevVersion = MAJOR_VERSION + "." + previousVersion;
            if (channel.builds.stream().anyMatch(build -> build.number.equals(prevVersion))) {
                found = true;
                break;
            }
            previousVersion--;
        }

        String prevNumberStr = MAJOR_VERSION + "." + previousVersion;
        String numberStr = MAJOR_VERSION + "." + version;

        if (found) {
            logger.info("Previous version '{}'", previousVersion);

            for (Platform platform : Platform.values()) {
                logger.info("Create patch for platform {}", platform.getName());
                Path targetDir = targetPath.resolve(platform.getName());
                Files.createDirectories(targetDir);
                try {
                    downloadBinary(platform, prevNumberStr, targetDir);
                    downloadBinary(platform, numberStr, targetDir);
                    // Run patch creation
                    Process process = new ProcessBuilder(
                            System.getProperty("java.home") + "/bin/java",
                            "-Xms4096m",
                            "-Xmx8192m",
                            "-classpath",
                            "lib/com.intellij.updater.updater-3.0.jar",
                            Runner.class.getName(),
                            "create",
                            prevNumberStr,
                            numberStr,
                            targetDir.resolve(prevNumberStr).toString(),
                            targetDir.resolve(numberStr).toString(),
                            targetPath.resolve(platform.getPatchName(prevNumberStr, numberStr)).toString()
                    )
                            .inheritIO()
                            .start();
                    int exitCode = process.waitFor();
                    if (exitCode != 0) {
                        return exitCode;
                    }
                } catch (IOException | InterruptedException e) {
                    logger.error("Failed to create patch ignore", e);
                }
            }
            BuildInfoPatch patch = new BuildInfoPatch();
            patch.from = prevNumberStr;
            patch.size = String.valueOf(Files.size(targetPath.resolve(Platform.LINUX.getPatchName(prevNumberStr, numberStr))) / 1024 / 1024);
            patch.fullFrom = prevNumberStr;
            channel.builds.add(new BuildInfo(numberStr, "HulyCode EAP 2025.1 developer build is now available!", patch));
            try {
                for (Platform platform : Platform.values()) {
                    Path targetDir = targetPath.resolve(platform.getName());
                    FileUtils.deleteDirectory(targetDir.toFile());
                }
            } catch (Exception e) {
                logger.warn("Failed to delete directories", e);
            }
        } else {
            logger.info("Previous version not found, add build info without patch");
            channel.builds.add(new BuildInfo(numberStr, "HulyCode EAP 2025.1 developer build is now available!"));
        }

        // write updates.xml
        xmlMapper.writeValue(new File(TARGET_DIR + UPDATE_XML), products);
        FileUtils.write(new File(TARGET_DIR + "/version.js"), String.format("const version = '%d';", version), StandardCharsets.UTF_8);
        return 0;
    }

    private static void unzip(Path zipFilePath, Path targetDir, boolean trimRootDir) throws IOException {
        try (ZipFile zipFile = ZipFile.builder().setFile(zipFilePath.toFile()).get()) {
            Enumeration<? extends ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String name = entry.getName();
                if (trimRootDir) {
                    // trim applicaiton.app/Contents/ prefix for macos
                    name = name.substring(name.indexOf('/') + 1);
                    name = name.substring(name.indexOf('/') + 1);
                }
                if (name.isBlank()) continue;
                Path extractTo = targetDir.resolve(name);
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.createDirectories(extractTo.getParent());
                    zipFile.getInputStream(entry).transferTo(new FileOutputStream(extractTo.toFile()));
                    if (entry.getUnixMode() != 0) {
                        try {
                            Files.setPosixFilePermissions(extractTo, PosixFilePermissions.fromString("rwxr-xr-x"));
                        } catch (UnsupportedOperationException e) {
                            // ignore
                        }
                    }
                }
            }
        }
    }

    private static void downloadBinary(Platform platform, String numberStr, Path targetDir) throws IOException {
        Path archiveFile = targetDir.resolve(platform.getDownloadName(numberStr));
        String archiveUrl = BASE_URL + platform.getDownloadName(numberStr);
        logger.info("Download binary from {}", archiveUrl);
        FileUtils.copyURLToFile(URI.create(archiveUrl).toURL(), archiveFile.toFile());
        logger.info("Unpack binary from {}", archiveFile);
        switch (platform) {
            case WINDOWS:
                unzip(archiveFile, targetDir.resolve(numberStr), false);
                break;
            case MAC:
            case MAC_X64:
                unzip(archiveFile, targetDir.resolve(numberStr), true);
                break;
            case LINUX:
                ungzip(archiveFile, targetDir.resolve(numberStr));
                break;
        }
        FileUtils.delete(archiveFile.toFile());
    }

    private static void ungzip(Path gzipFilePath, Path targetDir) throws IOException {
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(gzipFilePath.toFile()))) {
            TarArchiveInputStream tar = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
            TarArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                String name = entry.getName().substring(entry.getName().indexOf('/') + 1);
                if (name.isBlank()) continue;
                Path extractTo = targetDir.resolve(name);
                if (entry.isDirectory()) {
                    Files.createDirectories(extractTo);
                } else {
                    Files.createDirectories(extractTo.getParent());
                    if (entry.isSymbolicLink()) {
                        Files.createSymbolicLink(extractTo, Path.of(entry.getLinkName()));
                    } else {
                        Files.copy(tar, extractTo);
                        //noinspection OctalInteger
                        if ((entry.getMode() & 0100) != 0) {
                            HashSet<PosixFilePermission> permissions = new HashSet<>(Files.getPosixFilePermissions(extractTo));
                            permissions.add(PosixFilePermission.OWNER_EXECUTE);
                            Files.setPosixFilePermissions(extractTo, permissions);
                        }
                    }
                }
            }
        }
    }

    public static void main(String... args) {
        int exitCode;
        try {
            exitCode = new UpdateGenerator(Integer.parseInt(args[0])).process();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.exit(exitCode);
    }
}
