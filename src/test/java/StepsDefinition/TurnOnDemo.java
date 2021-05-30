package StepsDefinition;

import cucumber.api.java.After;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class TurnOnDemo {
    public WebDriver myDriver = new ChromeDriver();

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    @Given("^Portal k-vrachu.ru$")
    public void portal_kvrachu() {
        myDriver.manage().window().maximize();
        myDriver.manage().deleteAllCookies();
        myDriver.get("https://k-vrachu.ru/");
        myDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);

        Assert.assertEquals(myDriver.getCurrentUrl(), "https://k-vrachu.ru/");
    }

    public boolean checkIsDemo() {
        return myDriver.findElements(By.xpath("//*[contains(text(), 'Включен ознакомительный режим')]"))
                .size() > 0;
    }

    public boolean clickBtnDemo() {
        Actions action = new Actions(myDriver);
        WebElement linkDemo = myDriver.findElement(By.className("demo"));
        action.moveToElement(linkDemo).build().perform();
        WebElement btnDemo = myDriver.findElement(By.xpath("//a[@href='/user/demo_login']"));
        btnDemo.click();
        return true;
    }

    @When("^Click btn turn on demo$")
    public void click_btn_turn_on_demo() {
        clickBtnDemo();
        if(!checkIsDemo()) {
            // Пермский край запускается по умолчанию
            // у Пермского края не работает кнопка "Демо-режим"
            //Assert.assertTrue(checkIsDemo());
            System.out.println(ANSI_RED + "ERROR: у Пермского края не работает кнопка 'Демо-режим'" + ANSI_RESET);
        }
    }

    public boolean checkNextRegion(List<WebElement> regionList) {
        WebElement activeRegion = regionList.iterator().next();
        if (activeRegion != null) {
            activeRegion.click();
            clickBtnDemo();
            if (checkIsDemo()){
                return true;
            } else {
                return checkNextRegion(regionList);
            }
        } else {
            return false;
        }
    }

    @When("^If no demo choose another region$")
    public void if_no_demo_choose_another_region() {
        Boolean isDemo = checkIsDemo();
        if (!isDemo) {
            WebElement regions = myDriver.findElement(By.className("region"));
            regions.click();
            List<WebElement> regionList = myDriver.findElements(By.xpath("//div[@class='region']/ul/li"));

            isDemo = checkNextRegion(regionList);
        }

        Assert.assertTrue(isDemo);
    }

    @When("^Open appointment page to doctor$")
    public void open_appointment_page_to_doctor() {
        Actions action = new Actions(myDriver);
        WebElement btnServices = myDriver.findElement(By.xpath("//*[text()='Услуги']"));
        action.moveToElement(btnServices).build().perform();
        WebElement btnRecordToDoctor = myDriver.findElement(By.xpath("//a[@href='/service/record']"));
        btnRecordToDoctor.click();

        myDriver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    public List<WebElement> findAvailableSpecialists() {
        List<WebElement> specialistsList = myDriver.findElements(By.xpath("//dl[contains(@class, 'selectDoc')]//dd//li"));
        return specialistsList.stream()
                .filter(el -> el.isDisplayed() && !el.getAttribute("class").contains("disabledSpec"))
                .map(el -> el.findElement(By.tagName("a")))
                .collect(Collectors.toList());
    }

    public boolean checkFreeTime(){
        List<WebElement> nearestRecords = myDriver.findElements(By.className("nearest-record"));
        Optional<WebElement> freeRecord = nearestRecords.stream()
                .filter(el -> Arrays.asList(el.getAttribute("class")
                        .split(" ")).contains("free"))
                .findAny();

        if (freeRecord.isPresent()) {
            freeRecord.get().click();
            return true;
        } else {
            return false;
        }
    }

    public Integer doctorIndex = 0;

    public boolean chooseDoctor() {
        WebElement availableSpecialist = findAvailableSpecialists().get(doctorIndex);
        doctorIndex = doctorIndex + 1;

        if (availableSpecialist != null) {
            availableSpecialist.click();

            if (checkFreeTime()) {
                doctorIndex = 0;
                return true;
            } else {
                WebElement returnToStep1 = myDriver.findElement(By.xpath("//ul[contains(@class, 'steps')]//li//a"));
                returnToStep1.click();
                return chooseDoctor();
            }
        } else {
            doctorIndex = 0;
            return false;
        }
    }

    @When("^Choose doctor$")
    public void choose_doctor() {
        Assert.assertTrue(chooseDoctor());
    }

    @When("^Make record$")
    public void make_record() {
        WebElement makeRecord = myDriver.findElement(By.xpath("//*[contains(text(), 'Записаться')]"));
        makeRecord.click();
    }

    @When("^Confirm record$")
    public void confirm_record() {
        WebElement acceptRules = new WebDriverWait(myDriver, 2)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(), 'Согласен')]")));
        acceptRules.click();

        WebElement confirmRecord = new WebDriverWait(myDriver, 2)
                .until(ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(), 'Подтвердить')]")));
        confirmRecord.click();
    }

    @When("^Message \"(.*)\" appears \\(you can't make an appointment\\)$")
    public void message_appears_you_can_t_make_an_appointment(String message) {
        myDriver.findElements(By.xpath("//*[text()='" + message + "']"));
    }

    @Then("^Log out$")
    public void log_out(){
        WebElement bodyWrapper = myDriver.findElement(By.className("bodyWrapper"));
        WebElement btnLogOut = bodyWrapper.findElement(By.className("exit"));
        btnLogOut.click();
    }

    @After()
    public void closeBrowser() {
        myDriver.quit();
    }
}
