/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.contrib.wikiflavor.tests.po;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.ViewPage;

/**
 * @version $Id: $
 */
public class WikiFlavorsPage extends ViewPage
{
    @FindBy(xpath = "//a[@href='#AddNewEntry']")
    private WebElement addNewEntry;
    
    @FindBy(xpath = "//div[@id='entryNamePopup']/input[@type='text']")
    private WebElement entryNameInput;

    @FindBy(xpath = "//div[@id='entryNamePopup']/input[@type='image']")
    private WebElement entryNameButton;

    @FindBy(xpath = "//table[@id='wikiflavors']//td[contains(@class, 'doc_name')]/a\n")
    private List<WebElement> flavors;
    
    /**
     * Opens the home page.
     */
    public static WikiFlavorsPage gotoPage()
    {
        getUtil().gotoPage("WikiFlavors", "WebHome");
        return new WikiFlavorsPage();
    }
    
    public WikiFlavorEntryEditPage addNewEntry(String entryName)
    {
        addNewEntry.click();
        entryNameInput.sendKeys(entryName);
        entryNameButton.click();
        return new WikiFlavorEntryEditPage();
    }
    
    public List<String> getFlavors()
    {
        List<String> results = new ArrayList<>();
        for (WebElement flavor : flavors) {
            results.add(flavor.getText());
        }
        return results;
    }
    
    public ViewPage getFlavorPage(String flavorName)
    {
        for (WebElement flavor : flavors) {
            if (flavorName.equals(flavor.getText())) {
                flavor.click();
                return new ViewPage();
            }
        }
        
        return null;
    }
    
}
