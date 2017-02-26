package com.github.webdriverextensions;

import com.github.webdriverextensions.newversion.FileDownloader;
import com.github.webdriverextensions.newversion.FileDownloaderImpl;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.github.webdriverextensions.Utils.quote;

public class DriverDownloader {

    private final InstallDriversMojo mojo;
    private final FileDownloader fileDownloader;

    public DriverDownloader(InstallDriversMojo mojo) throws MojoExecutionException {
        this.mojo = mojo;
        this.fileDownloader = new FileDownloaderImpl(ProxyUtils.getProxyFromSettings(mojo));
    }

    public Path downloadFile(Driver driver, Path downloadDirectory) throws MojoExecutionException {
        try {
            if (!mojo.keepDownloadedWebdrivers) {
                cleanupDriverDownloadDirectory(downloadDirectory);
            }

            if (mojo.keepDownloadedWebdrivers && fileDownloader.isDownloaded(driver.getUrl(), downloadDirectory)) {
                Path downloadedFile = fileDownloader.getDownloadedFile(driver.getUrl(), downloadDirectory);
                mojo.getLog().info("  Using cached driver from " + quote(downloadedFile));
                return downloadedFile;
            } else {
                mojo.getLog().info("  Downloading " + quote(driver.getUrl()) + " to " + quote(downloadDirectory));
                Path downloadedFile = fileDownloader.downloadFile(driver.getUrl(), downloadDirectory);
                if (driverFileIsCorrupt(downloadedFile)) {
                    printXmlFileContetIfPresentInDonwloadedFile(downloadedFile);
                    cleanupDriverDownloadDirectory(downloadDirectory);
                    throw new InstallDriversMojoExecutionException("Failed to download a non corrupt driver", mojo, driver);
                }
                return downloadedFile;
            }
        } catch (Exception e) {
            if (e instanceof InstallDriversMojoExecutionException) {
                throw (InstallDriversMojoExecutionException) e;
            } else {
                throw new InstallDriversMojoExecutionException("Failed to download driver from " + quote(driver.getUrl()) + " cause of " + e.getCause(), e, mojo, driver);
            }
        }
    }

    private void printXmlFileContetIfPresentInDonwloadedFile(Path downloadFilePath) {
        try {
            List<String> fileContent = Files.readAllLines(downloadFilePath, StandardCharsets.UTF_8);
            if (fileContent.get(0).startsWith("<?xml")) {
                mojo.getLog().info("  Downloaded driver file contains the following error message");
                for (String line : fileContent) {
                    mojo.getLog().info("  " + line);
                }
            }
        } catch (Exception e) {
            // no file  or file content to read
        }
    }

    private boolean driverFileIsCorrupt(Path downloadFilePath) {
        if (Utils.hasExtension(downloadFilePath, "zip")) {
            return !Utils.validateZipFile(downloadFilePath);
        } else if (Utils.hasExtension(downloadFilePath, "bz2")) {
            if (!Utils.validateBz2File(downloadFilePath)) {
                return true;
            } else {
                return !Utils.validateFileIsLargerThanBytes(downloadFilePath, 1000);
            }
        } else {
            return false;
        }
    }


    public void cleanupDriverDownloadDirectory(Path downloadDirectory) throws MojoExecutionException {
        try {
            FileUtils.deleteDirectory(downloadDirectory.toFile());
        } catch (IOException e) {
            throw new InstallDriversMojoExecutionException("Failed to delete driver cache directory:" + System.lineSeparator()
                    + Utils.directoryToString(downloadDirectory), e);
        }
    }
}
