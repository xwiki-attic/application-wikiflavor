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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.xwiki.test.ui.po.editor.EditPage;

/**
 * @version $Id: $
 */
public class WikiFlavorEntryEditPage extends EditPage
{
    @FindBy(id = "WikiFlavorsCode.WikiFlavorsClass_0_extensionId")
    private WebElement extensionId;

    @FindBy(id = "WikiFlavorsCode.WikiFlavorsClass_0_extensionVersion")
    private WebElement extensionVersion;

    @FindBy(id = "WikiFlavorsCode.WikiFlavorsClass_0_nameTranslationKey")
    private WebElement name;

    @FindBy(id = "WikiFlavorsCode.WikiFlavorsClass_0_descriptionTranslationKey")
    private WebElement description;

    @FindBy(id = "WikiFlavorsCode.WikiFlavorsClass_0_icon")
    private WebElement icon;
    
    public void setExtensionId(String extensionId)
    {
        this.extensionId.sendKeys(extensionId);
    }
    
    public void setExtensionVersion(String extensionVersion)
    {
        this.extensionVersion.sendKeys(extensionVersion);
    }
    
    public void setName(String name)
    {
        this.name.sendKeys(name);
    }
    
    public void setDescription(String description)
    {
        this.description.sendKeys(description);
    }
    
    public void setIcon(String icon)
    {
        this.icon.sendKeys(icon);
    }
}
