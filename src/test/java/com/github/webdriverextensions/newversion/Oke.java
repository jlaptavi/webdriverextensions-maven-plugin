package com.github.webdriverextensions.newversion;

import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import java.util.concurrent.TimeUnit;

public class Oke {

    @Test
    public void test1() {
        WebDriver driver = new InternetExplorerDriver();
        driver.get("https://www.google.se/");
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String windowHandle = driver.getWindowHandle();
        Assert.assertNotNull(windowHandle);
    }
}
