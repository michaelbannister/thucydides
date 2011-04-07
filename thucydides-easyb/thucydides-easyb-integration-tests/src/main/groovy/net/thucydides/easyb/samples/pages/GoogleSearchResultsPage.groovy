package net.thucydides.easyb.samples.pages;

import net.thucydides.core.pages.PageObject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class GoogleSearchResultsPage extends PageObject {

    public GoogleSearchResultsPage(WebDriver driver) {
        super(driver);
    }
 
    def getTopicTitles() {

        def titles = []
        def titleElements = getDriver().findElements(By.cssSelector(".r"))

        titleElements.each { element ->
            titles += element.text
        }

        return titles;
    }


    def clickOnSearchResult(String title) {
        driver.findElement(By.xpath("//a[.='$title']")).click()
    }
}