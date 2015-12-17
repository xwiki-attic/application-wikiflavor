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
import org.xwiki.text.StringUtils;
import org.xwiki.wiki.test.po.CreateWikiPage;

/**
 * @version $Id: $
 */
public class CreateFlavoredWikiPage extends CreateWikiPage
{
    @FindBy(className = "xwiki-custom-select-option")
    private List<WebElement> types;

    // Bug: descriptionField does not always work (maybe because there is a meta tag called "description".
    @FindBy(id = "description")
    private WebElement currentDescriptionField;
    
    public List<Flavor> getFlavors()
    {
        List<Flavor> results = new ArrayList<>();
        for (WebElement el : types) {
            String value = el.findElement(By.tagName("input")).getAttribute("value");
            if (StringUtils.startsWith(value, "extension:")) {
                Flavor flavor = new Flavor();
                flavor.setName(el.findElement(By.tagName("label")).getText());
                flavor.setDescription(el.findElement(By.className("xHint")).getText());
                String[] extension = value.substring("extension:".length()).split("::");
                flavor.setExtensionId(extension[0]);
                if (extension.length > 1) {
                    flavor.setExtensionVersion(extension[1]);
                }
                results.add(flavor);
            }
        }
        return results;
    }
    
    public List<Template> getTemplates()
    {
        List<Template> results = new ArrayList<>();
        for (WebElement el : types) {
            String value = el.findElement(By.tagName("input")).getAttribute("value");
            if (StringUtils.startsWith(value, "template:")) {
                Template template = new Template();
                template.setName(el.findElement(By.tagName("label")).getText());
                if (getDriver().findElementWithoutWaiting(By.className("xHint")) != null) {
                    template.setDescription(el.findElement(By.className("xHint")).getText());
                }
                template.setTemplateId(value.substring("template:".length()));
                results.add(template);
            }
        }
        return results;
    }
    
    public void setFlavorOrExtension(String name)
    {
        for (WebElement el : types) {
            WebElement label = el.findElement(By.tagName("label"));
            if (name.equals(label.getText())) {
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
