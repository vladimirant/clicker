package com.clicker;

import com.clicker.exception.RegistrationException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.clicker.util.Randomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Clicker {

    private static final Logger logger = LoggerFactory.getLogger(Clicker.class);

    private static String MAIN_URL = "http://socgain.com";
    private static String PATH_URL = "elike";
    private WebDriver driver;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Callable<Boolean> callable = new ClickableTask();
    private Future<Boolean> status;
    private AtomicInteger failCounter = new AtomicInteger();
    private AtomicInteger likeCounter = new AtomicInteger();
    private volatile AtomicBoolean isRunningService = new AtomicBoolean(false);

    private class ClickableTask implements Callable<Boolean> {
        @Override
        public Boolean call() throws Exception {
            logger.info("Start clickable task");
            isRunningService.set(true);
            try {
                while (true) {
                    int sleepTime = Randomizer.getRandomTime(16000, 21000);
                    logger.info("Sleep {} sec before next click", sleepTime / 1000);

                    sleep(sleepTime);

                    logger.info("****************");
                    logger.info("Clicking: START!");
                    driver.findElement(By.xpath("//*[@id=\"vip\"]/div/a[1]")).click();

                    sleep(2000);

                    String winHandleBefore = driver.getWindowHandle();
                    for (String winHandle : driver.getWindowHandles()) {
                        if (!winHandleBefore.equals(winHandle)) {
                            driver.switchTo().window(winHandle);
                        }
                    }

                    sleep(Randomizer.getRandomTime(3000, 4000));

                    WebElement elementForLike = driver.findElement(By.xpath("//*[@id=\"react-root\"]/section/main/div/div/article/div[2]/section[1]/a[1]"));
                    logger.info(elementForLike.getText() + " #" + likeCounter.incrementAndGet());

                    if (elementForLike.getText().equals("Like")) {
                        elementForLike.click();
                    } else {
                        elementForLike.click();//if has been liked that at first set unlike and than set a like
                        sleep(1000);
                        elementForLike.click();
                    }

                    sleep(Randomizer.getRandomTime(2500, 3500));

                    driver.close();
                    driver.switchTo().window(winHandleBefore);
                    logger.info("Clicking: FINISH!");
                    logger.info("****************");
                }
            } catch (Exception e) {
                logger.error("Error in click process.", e);
            }finally {
                isRunningService.set(false);
                return false;
            }
        }
    }

    public static void main(String[] args) {

        String username = args[0];
        String pass = args[1];
        String pathToDriver = args[2];

        if (username == null || username.isEmpty() || pass == null || pass.isEmpty() || pathToDriver == null || pathToDriver.isEmpty()) {
            throw new IllegalArgumentException("Username and password and path to driver can't be empty");
        }

        try {
            new Clicker().startService(username, pass, pathToDriver);
        } catch (Error error) {
            error.printStackTrace();
        }
    }

    private void startService(String username, String pass, String pathToDriver) throws Error {
        while (!isRunningService.get()) {
            initService(username, pass, pathToDriver);
            sleep(10000);
        }

        while (true) {
            sleep(30000);

            try {
                /* This thread will be blocked while workThread don't return 'false'
                   WorkThread return 'false' only when will throws any Exception */
                status.get();
                logger.info("************ Fail #" + failCounter.incrementAndGet() + " ************");

                while (!isRunningService.get()) {
                    initService(username, pass, pathToDriver);
                    sleep(10000);
                }

                sleep(10000);

            } catch (Exception e) {
                logger.error("Error while start clicker", e);
                throw new Error(e.getMessage());
            }
        }
    }

    private void initService(String username, String pass, String pathToDriver) {
        initDriver(pathToDriver);
        try {
            registration(driver, MAIN_URL, username, pass);
        } catch (RegistrationException e){
            logger.error("Error in registration process", e);
            return;
        }
        status = executor.submit(callable);
    }

    private void initDriver(String driverLocation) {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
        System.setProperty("webdriver.chrome.driver", driverLocation);
        driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    private void registration(WebDriver driver, String mainUrl, String username, String pass) throws RegistrationException {
        try {
            driver.get(mainUrl);
            driver.findElement(By.xpath("//*[@id='info']/div[3]/div/div[1]/a")).click();

            //switch to new window for registration
            driver.findElement(By.xpath("//*[@id=\"popup1\"]/div/div[2]/div/form/div[1]/label[1]/a")).click();

            //switch to new window for registration
            String winHandleBefore = driver.getWindowHandle();
            for(String winHandle : driver.getWindowHandles()){
                driver.switchTo().window(winHandle);
            }
            sleep(4000);
            driver.findElement(By.name("username")).sendKeys(username);

            WebElement passElement = driver.findElement(By.name("password"));
            passElement.sendKeys(pass);
            passElement.submit();

            sleep(2000);
            String tokenUrl = driver.getCurrentUrl();
            //handle close registration window
            for (String winHandle : driver.getWindowHandles()){
                if (!winHandle.equals(winHandleBefore)) {
                    driver.switchTo().window(winHandle);
                    driver.close();
                }
            }

            driver.switchTo().window(winHandleBefore);

            sleep(2000);

            driver.findElement(By.xpath("//*[@id=\"popup1\"]/div/div[2]/div/form/div[1]/input")).sendKeys(tokenUrl);
            driver.findElement(By.xpath("//*[@id=\"popup1\"]/div/div[2]/div/form/div[2]/button")).click();
            sleep(2000);

            driver.navigate().to(MAIN_URL + "/" + PATH_URL);

            sleep(2000);
        } catch(Exception e) {
            throw new RegistrationException(e);
        }
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
}
