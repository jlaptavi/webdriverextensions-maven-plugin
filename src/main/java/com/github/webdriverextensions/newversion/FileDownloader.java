package com.github.webdriverextensions.newversion;

import org.apache.maven.plugin.MojoExecutionException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

public interface FileDownloader {

    Path downloadFile(String url, Path toDirectory) throws URISyntaxException, MojoExecutionException;

    boolean isDownloaded(String url, Path downloadDirectory);

    Path getDownloadedFile(String url, Path downloadDirectory);

}
