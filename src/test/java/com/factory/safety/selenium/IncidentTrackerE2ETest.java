package com.factory.safety.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IncidentTrackerE2ETest {

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private String baseUrl;

    @BeforeAll
    static void setupClass() {
        try {
            WebDriverManager.chromedriver().setup();
        } catch (Exception ignored) {
        }
    }

    @BeforeEach
    void setupTest() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--remote-allow-origins=*");

        try {
            driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        } catch (Exception e) {
            // If cached chromedriver has version mismatch, clear system property and retry with Selenium 4 Manager
            System.clearProperty("webdriver.chrome.driver");
            try {
                driver = new ChromeDriver(options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
            } catch (Exception ex) {
                System.err.println("Notice: Skipping UI E2E test due to environment browser/driver mismatch: " + ex.getMessage());
                org.junit.jupiter.api.Assumptions.assumeTrue(false, "Skipping E2E test due to browser driver mismatch: " + ex.getMessage());
            }
        }
        baseUrl = "http://localhost:" + port + "/incidents";
    }

    @AfterEach
    void teardown(TestInfo testInfo) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception ignored) {
            }
        }
    }

    private void takeScreenshot(String testName) {
        if (driver instanceof TakesScreenshot) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            try {
                Path destDir = Paths.get("target/screenshots");
                if (!Files.exists(destDir)) {
                    Files.createDirectories(destDir);
                }
                Path destFile = destDir.resolve(testName + ".png");
                Files.copy(screenshot.toPath(), destFile, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Screenshot saved to: " + destFile.toAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loginAs(String username, String password) {
        if (driver.getCurrentUrl() == null || !driver.getCurrentUrl().contains("/login")) {
            driver.get("http://localhost:" + port + "/login");
        }
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

        WebElement userField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        userField.clear();
        userField.sendKeys(username);
        if (!username.equals(userField.getAttribute("value"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", userField, username);
        }

        WebElement passField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passField.clear();
        passField.sendKeys(password);
        if (!password.equals(passField.getAttribute("value"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", passField, password);
        }

        WebElement submitBtn = driver.findElement(By.cssSelector("button[type='submit']"));
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
        wait.until(ExpectedConditions.urlContains("/incidents"));
    }

    @Test
    void testCreateNewIncidentJourney(TestInfo testInfo) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // 1. Verify unauthenticated access redirects to login
            driver.get(baseUrl);
            assertTrue(driver.getCurrentUrl().contains("/login"), "Unauthenticated user must be redirected to login");

            // 2. Sign in as worker
            loginAs("worker1", "worker123");
            
            WebElement newButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText("Log New Incident")));
            String newIncidentUrl = newButton.getAttribute("href");
            if (newIncidentUrl != null && !newIncidentUrl.isEmpty()) {
                driver.get(newIncidentUrl);
            } else {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", newButton);
            }
            
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));

            WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("title")));
            titleInput.clear();
            titleInput.sendKeys("Automated UI Test Incident");
            if (!"Automated UI Test Incident".equals(titleInput.getAttribute("value"))) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = 'Automated UI Test Incident';", titleInput);
            }

            WebElement descInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("description")));
            descInput.clear();
            descInput.sendKeys("Generated by Selenium WebDriver");
            if (!"Generated by Selenium WebDriver".equals(descInput.getAttribute("value"))) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = 'Generated by Selenium WebDriver';", descInput);
            }

            WebElement locInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("location")));
            locInput.clear();
            locInput.sendKeys("Testing Server");
            if (!"Testing Server".equals(locInput.getAttribute("value"))) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].value = 'Testing Server';", locInput);
            }
            
            Select severitySelect = new Select(driver.findElement(By.id("severity")));
            severitySelect.selectByValue("HIGH");

            // Attach photo evidence to incident
            Path imgPath = Paths.get("src/test/resources/test-hazard.png").toAbsolutePath();
            if (!Files.exists(imgPath)) {
                imgPath = Paths.get("image.png").toAbsolutePath();
            }
            if (Files.exists(imgPath)) {
                WebElement photoInput = driver.findElement(By.id("photo"));
                photoInput.sendKeys(imgPath.toString());
            }
            
            // Submitting the form - deliberately searching for element id="submit"
            WebElement submitButton = driver.findElement(By.id("submit"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitButton);
            try {
                submitButton.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
            }
            
            wait.until(ExpectedConditions.urlMatches(".*/incidents$"));
            assertTrue(driver.getCurrentUrl().endsWith("/incidents"), "Should redirect to dashboard");
            
            List<WebElement> rows = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.tagName("td")));
            boolean found = rows.stream().anyMatch(td -> td.getText().contains("Automated UI Test Incident"));
            assertTrue(found, "The newly created incident should be visible in the table");
        } catch (AssertionError | Exception e) {
            takeScreenshot(testInfo.getDisplayName());
            throw e; 
        }
    }

    @Test
    void testDashboardViewAndFilterJourney(TestInfo testInfo) {
        try {
            loginAs("worker1", "worker123");
            driver.get(baseUrl);
            WebElement header = driver.findElement(By.tagName("h2"));
            assertTrue(header.getText().contains("Dashboard"), "Header should contain 'Dashboard'");
        } catch (AssertionError | Exception e) {
            takeScreenshot(testInfo.getDisplayName());
            throw e;
        }
    }

    @Test
    void testUserAuthenticationAndRoleJourney(TestInfo testInfo) {
        try {
            // 1. Worker Sign In
            loginAs("worker1", "worker123");
            assertTrue(driver.getCurrentUrl().contains("/incidents"), "Worker should land on incidents dashboard");
            String pageSource = driver.getPageSource();
            assertTrue(pageSource.contains("Worker Portal") || pageSource.contains("worker1"), "Worker portal indicator should be present");

            // 2. Logout
            driver.get("http://localhost:" + port + "/logout");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.urlContains("/login"));

            // 3. Admin Sign In (Mayuresh / mayu)
            loginAs("mayu", "mayu");
            assertTrue(driver.getCurrentUrl().contains("/incidents"), "Admin should land on incidents dashboard");
            String adminPageSource = driver.getPageSource();
            assertTrue(adminPageSource.contains("Admin Console") || adminPageSource.contains("Mayuresh") || adminPageSource.contains("mayu"), "Admin console indicator should be present");
        } catch (AssertionError | Exception e) {
            takeScreenshot(testInfo.getDisplayName());
            throw e;
        }
    }

    @Test
    void testAdminResolutionAndWorkDoneJourney(TestInfo testInfo) {
        try {
            // Log in as Admin first (Mayuresh / mayu)
            loginAs("mayu", "mayu");
            
            // Navigate to an existing incident detail page
            driver.get("http://localhost:" + port + "/incidents/1");
            
            String detailSource = driver.getPageSource();
            assertTrue(detailSource.contains("Hazard Report Information") || detailSource.contains("Incident Information"), "Incident details should load");

            // Verify admin management section is present
            WebElement statusSelect = driver.findElement(By.id("status"));
            Select select = new Select(statusSelect);
            select.selectByValue("CLOSED");

            WebElement notesInput = driver.findElement(By.id("resolutionNotes"));
            notesInput.clear();
            notesInput.sendKeys("Remediation verified by automated Selenium suite. Hazard eliminated.");

            // Submit resolution
            WebElement updateBtn = driver.findElement(By.cssSelector("#adminResolutionForm button[type='submit']"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", updateBtn);
            try {
                updateBtn.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", updateBtn);
            }

            // Verify status is CLOSED and notes are updated
            String updatedSource = driver.getPageSource();
            assertTrue(updatedSource.contains("CLOSED"), "Incident status should be CLOSED after admin update");
            assertTrue(updatedSource.contains("Remediation verified by automated Selenium suite"), "Resolution notes should be visible on page");
        } catch (AssertionError | Exception e) {
            takeScreenshot(testInfo.getDisplayName());
            throw e;
        }
    }

    @Test
    void testNotificationDropdownInteraction(TestInfo testInfo) {
        try {
            loginAs("worker1", "worker123");
            driver.get(baseUrl);
            WebElement bellBtn = driver.findElement(By.cssSelector(".notification-btn, #notificationDropdownBtn"));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", bellBtn);

            WebElement dropdown = driver.findElement(By.cssSelector(".notification-dropdown"));
            assertTrue(dropdown.isDisplayed() || driver.getPageSource().contains("Notifications"), "Notifications dropdown should open");
        } catch (AssertionError | Exception e) {
            takeScreenshot(testInfo.getDisplayName());
            throw e;
        }
    }
}
