//package com.github.webdriverextensions.newversion;
//
//import com.github.webdriverextensions.LoggedTemporaryFolder;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.TemporaryFolder;
//
//import java.net.URL;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.is;
//import static org.junit.Assert.*;
//
//public class FileDownloaderImplTest {
//
//    @Rule
//    public TemporaryFolder temporaryFolder = new LoggedTemporaryFolder();
//
//    private Path toDirectory;
//
//    @Before
//    public void setUp() throws Exception {
//        toDirectory = temporaryFolder.newFolder("to-directory").toPath();
//    }
//
//    @Test
//    public void downloadFile_should_download_file_from_the_url() throws Exception {
//        // Given
//        URL driverUrl = new URL();
//        FileDownloaderImpl fileDownloader = new FileDownloaderImpl();
//
//        // When
//        fileDownloader.downloadFile(singleFileZip, toDirectory);
//
//        // Then
//        assertThat(toDirectory.toFile().listFiles().length, is(1));
//        assertThat(toDirectory.resolve("single-file").toFile().exists(), is(true));
//    }
//
//}