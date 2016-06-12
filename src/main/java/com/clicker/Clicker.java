package com.clicker;

import com.clicker.exception.RegistrationException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.clicker.util.Randomizer;
import java.io.File;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Clicker {
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
            isRunningService.set(true);
            try {
                while (true) {
                    try {
                        Thread.sleep(Randomizer.getRandomTime(18000, 22000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("****************");
                    System.out.println("Clicking: START!");
                    driver.findElement(By.xpath("//*[@id=\"pjax-container\"]/div[1]/div[2]/div[1]/div/a")).click();

                    String winHandleBefore = driver.getWindowHandle();
                    for(String winHandle : driver.getWindowHandles()){
                        driver.switchTo().window(winHandle);
                    }
                    try {
                        Thread.sleep(Randomizer.getRandomTime(3000, 4000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    WebElement elementForLike = driver.findElement(By.xpath("//*[@id=\"react-root\"]/section/main/div/div/article/div[2]/section[2]/a"));

                    System.out.println(elementForLike.getText()+ " #" +likeCounter.incrementAndGet());

                    if (elementForLike.getText().equals("Like")) {
                        elementForLike.click();
                    } else {
                        elementForLike.click();//if has been liked that at first set unlike and than set a like
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        elementForLike.click();
                    }

                    try {
                        Thread.sleep(Randomizer.getRandomTime(2500, 3500));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    driver.close();

                    driver.switchTo().window(winHandleBefore);
                    System.out.println("Clicking: FINISH!");
                    System.out.println("****************");
                }
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
            //TODO send mail
        }
    }

    private void startService(String username, String pass, String pathToDriver) throws Error {
        while (!isRunningService.get()) {
            initService(username, pass, pathToDriver);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        while (true) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                /* This thread will be blocked while workThread don't return 'false'
                   WorkThread return 'false' only when will throws any Exception */
                status.get();
                System.out.println("************ Fail #" + failCounter.incrementAndGet()+ " ************");

                while (!isRunningService.get()) {
                    initService(username, pass, pathToDriver);
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new Error(e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new Error(e.getMessage());
            }
        }
    }

    private void initService(String username, String pass, String pathToDriver) {
        initDriver(pathToDriver);
        try {
            registration(driver, MAIN_URL, username, pass);
        } catch (RegistrationException e){
            e.printStackTrace();
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
            driver.findElement(By.xpath("//*[@id=\"uLogin\"]/div/a")).click();

            //switch to new window for registration
            String winHandleBefore = driver.getWindowHandle();
            for(String winHandle : driver.getWindowHandles()){
                driver.switchTo().window(winHandle);
            }

            driver.findElement(By.name("username")).sendKeys(username);

            WebElement passElement = driver.findElement(By.name("password"));
            passElement.sendKeys(pass);
            passElement.submit();

            Thread.sleep(2000);

            //handle close registration window
            driver.switchTo().window(winHandleBefore);

            Thread.sleep(1000);

            String targetPath = MAIN_URL + "/" + PATH_URL;
            driver.navigate().to(targetPath);

            Thread.sleep(2000);

            //fix response error while login
            if (!driver.getCurrentUrl().equals(targetPath)) {
                try{
                    driver.findElement(By.xpath("//*[@id=\"info\"]/div[3]/div/div[1]/a")).click();
                } catch (Exception e){
                    e.printStackTrace();
                }
                driver.navigate().to(targetPath);
            }
        } catch(Exception e) {
            throw new RegistrationException(e.getMessage());
        }
    }
}
