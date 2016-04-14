package com.github.webdriverextensions;

import java.io.File;
import java.io.SyncFailedException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.github.webdriverextensions.Utils.*;
import static java.io.File.separator;

class DriverPathPropertyLoader {

    private final InstallDriversMojo mojo;
    private static final String CHROME_DRIVER_PROPERTY_NAME = "webdriver.chrome.driver";
    private static final String IE_DRIVER_PROPERTY_NAME = "webdriver.ie.driver";

    DriverPathPropertyLoader(InstallDriversMojo mojo) {
        this.mojo = mojo;
    }

    public void setDriverPaths() {

        if (isMac()) {
            setChromeDriverPathPropertyIfNotExists(Paths.get(mojo.installationDirectory.getPath(), "chromedriver-mac-32bit"));
        } else if (isLinux()) {
            if (is64Bit()) {
                setChromeDriverPathPropertyIfNotExists(Paths.get(mojo.installationDirectory.getPath(), "chromedriver-linux-64bit"));
            } else {
                setChromeDriverPathPropertyIfNotExists(Paths.get(mojo.installationDirectory.getPath(), "chromedriver-linux-32bit"));
            }
        } else { // assume it's a windows machine
            setChromeDriverPathPropertyIfNotExists(Paths.get(mojo.installationDirectory.getPath(), "chromedriver-windows-32bit"));
            setInternetExplorerDriverPathPropertyIfNotExists(Paths.get(mojo.installationDirectory.getPath(), "internetexplorerdriver-windows-32bit"));
        }

    }

    private void setChromeDriverPathPropertyIfNotExists(Path path) {
        if (!propertyExists(CHROME_DRIVER_PROPERTY_NAME)) {
            mojo.getLog().info( "  " + CHROME_DRIVER_PROPERTY_NAME + " = " + quote(path));
            System.setProperty(CHROME_DRIVER_PROPERTY_NAME, path.toString());
        }
    }

    private void setInternetExplorerDriverPathPropertyIfNotExists(Path path) {
        if (!propertyExists(IE_DRIVER_PROPERTY_NAME)) {
            mojo.getLog().info( "  " + IE_DRIVER_PROPERTY_NAME + " = " + quote(path));
            System.setProperty(IE_DRIVER_PROPERTY_NAME, path.toString());
        }
    }

    private boolean propertyExists(String key) {
        return System.getProperty(key) != null;
    }
}
