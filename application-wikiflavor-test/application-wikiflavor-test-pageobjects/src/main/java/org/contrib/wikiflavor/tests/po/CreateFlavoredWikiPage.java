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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.wiki.test.po.CreateWikiPage;

/**
 * @version $Id: $
 */
public class CreateFlavoredWikiPage extends CreateWikiPage
{
    @FindBy(xpath = "//input[@name = 'flavor']")
    private List<WebElement> flavors;

    @FindBy(xpath = "//input[@name = 'template']")
    private List<WebElement> templates;

    // Bug: descriptionField does not always work (maybe because there is a meta tag called "description".
    @FindBy(id = "description")
    private WebElement currentDescriptionField;
    
    @FindBy(xpath = "//dd[@class='flavor_template']/ul[@class = 'xwikitabbar']/li/a[@href = '#flavors']")
    private WebElement flavorTabTitle;

    @FindBy(xpath = "//dd[@class='flavor_template']/ul[@class = 'xwikitabbar']/li/a[@href = '#templates']")
    private WebElement templateTabTitle;
    
    public void selectFlavorTab()
    {
        flavorTabTitle.click();
        getDriver().waitUntilElementHasAttributeValue(By.id("flavors-tab"), "style", "display: block;");
    }
    
    public void selectTemplateTab()
    {
        templateTabTitle.click();
        getDriver().waitUntilElementHasAttributeValue(By.id("templates-tab"), "style", "display: block;");
    }
    
    public List<Flavor> getFlavors()
    {
        List<Flavor> results = new ArrayList<>();
        for (WebElement el : flavors) {
            String value = el.getAttribute("value");
            Flavor flavor = new Flavor();
            flavor.setName(el.findElement(By.xpath("..//label")).getText());
            flavor.setDescription(el.findElement(By.xpath("..//p[contains(@class, 'xHint')]")).getText());
            String[] extension = value.split(":::");
            flavor.setExtensionId(extension[0]);
            if (extension.length > 1) {
                flavor.setExtensionVersion(extension[1]);
            }
            results.add(flavor);
        }
        return results;
    }
    
    public List<Template> getTemplates()
    {
        List<Template> results = new ArrayList<>();
        for (WebElement el : templates) {
            Template template = new Template();
            template.setName(el.findElement(By.xpath("..//label")).getText());
            template.setDescription(el.findElement(By.xpath("..//p[contains(@class, 'xHint')]")).getText());
            template.setTemplateId(el.getAttribute("value"));
            results.add(template);
        }
        return results;
    }
    public void setFlavor(String flavorName)
    {
        for (WebElement el : flavors) {
            WebElement label = el.findElement(By.xpath("..//label")); 
            String name = label.getText();
            if (flavorName.equals(name)) {
                label.click();
                return;
            }
        }
    }
    
    public void setTemplate(String templateName)
    {
        for (WebElement el : templates) {
            WebElement label = el.findElement(By.xpath("..//label"));
            String name = label.getText();
            if (templateName.equals(name)) {
                label.click();
                return;
            }
        }
    }

    @Override
    public void setDescription(String description)
    {
        currentDescriptionField.clear();
        currentDescriptionField.sendKeys(description);
    }
}
