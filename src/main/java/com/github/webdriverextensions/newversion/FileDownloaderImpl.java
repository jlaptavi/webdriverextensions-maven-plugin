package com.github.webdriverextensions.newversion;

import com.github.webdriverextensions.InstallDriversMojoExecutionException;
import com.github.webdriverextensions.ProxyUtils;
import com.github.webdriverextensions.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.settings.Proxy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.webdriverextensions.Utils.quote;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

public class FileDownloaderImpl implements FileDownloader {

    public static final int FILE_DOWNLOAD_READ_TIMEOUT = 30 * 60 * 1000; // 30 min
    public static final int FILE_DOWNLOAD_CONNECT_TIMEOUT = 30 * 1000; // 30 seconds
    public static final int FILE_DOWNLOAD_RETRY_ATTEMPTS = 3;
    private final Proxy proxySettings;

    public FileDownloaderImpl(Proxy proxySettings) {
        this.proxySettings = proxySettings;
    }

    public static void main(String[] args) throws URISyntaxException {
        String url = "http://www.google.com/oke/ajaj.zip";
        System.out.println(Paths.get(new URI(url).getPath()).getFileName());
    }

    @Override
    public Path downloadFile(String url, Path toDirectory) throws URISyntaxException, MojoExecutionException {
        Path fileToDownload = toDirectory.resolve(getFileName(url));
        if (isDownloaded(url, toDirectory)) {
            return fileToDownload;
        }
        try {
            FileUtils.deleteDirectory(toDirectory.toFile());
        } catch (IOException e) {
            throw new InstallDriversMojoExecutionException("Failed to download file since it was not possible to clean download directory:" + System.lineSeparator() + Utils.directoryToString(toDirectory), e);
        }
        HttpClientBuilder httpClientBuilder = prepareHttpClientBuilderWithTimeoutsAndProxySettings(proxySettings);
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(FILE_DOWNLOAD_RETRY_ATTEMPTS, true));
        try (CloseableHttpClient httpClient = httpClientBuilder.build()) {
            try (CloseableHttpResponse fileDownloadResponse = httpClient.execute(new HttpGet(new URI(url)))) {
                HttpEntity remoteFileStream = fileDownloadResponse.getEntity();
                copyInputStreamToFile(remoteFileStream.getContent(), fileToDownload.toFile());
            }
        } catch (Exception e) {
            throw new InstallDriversMojoExecutionException("Failed to download file from " + quote(url) + " to " + quote(fileToDownload) + " cause of " + e.getCause(), e);
        }
        createDownloadCompletedFile(toDirectory);
        return fileToDownload;
    }

    @Override
    public boolean isDownloaded(String url, Path downloadDirectory) {
        Path downloadedFile = getDownloadedFile(url, downloadDirectory);
        return downloadedFile.toFile().exists() && downloadCompletedFileExists(downloadDirectory);
    }

    @Override
    public Path getDownloadedFile(String url, Path downloadDirectory) {
        return downloadDirectory.resolve(getFileName(url));
    }

    private Path getFileName(String url) {
        try {
            return Paths.get(new URI(url).getPath()).getFileName();
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to get filename from url=" + url, e);
        }
    }

    private HttpClientBuilder prepareHttpClientBuilderWithTimeoutsAndProxySettings(Proxy proxySettings) throws MojoExecutionException {
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(FILE_DOWNLOAD_READ_TIMEOUT).build();
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(FILE_DOWNLOAD_CONNECT_TIMEOUT)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES).build();
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        httpClientBuilder
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .disableContentCompression();
        HttpHost proxy = ProxyUtils.createProxyFromSettings(proxySettings);
        if (proxy != null) {
            httpClientBuilder.setProxy(proxy);
            CredentialsProvider proxyCredentials = ProxyUtils.createProxyCredentialsFromSettings(proxySettings);
            if (proxyCredentials != null) {
                httpClientBuilder.setDefaultCredentialsProvider(proxyCredentials);
            }
        }
        return httpClientBuilder;
    }

    private boolean downloadCompletedFileExists(Path toDirectory) {
        Path downloadCompletedFile = toDirectory.resolve("download.completed");
        return downloadCompletedFile.toFile().exists();
    }

    private void createDownloadCompletedFile(Path toDirectory) throws InstallDriversMojoExecutionException {
        Path downloadCompletedFile = toDirectory.resolve("download.completed");
        try {
            Files.createFile(downloadCompletedFile);
        } catch (IOException e) {
            throw new InstallDriversMojoExecutionException("Failed to create download.completed file at " + downloadCompletedFile, e);

        }
    }
}
