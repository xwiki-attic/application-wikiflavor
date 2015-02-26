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
package org.xwiki.contrib.wikiflavor.test.ui;

import java.util.List;

import org.contrib.wikiflavor.tests.po.CreateFlavoredWikiPage;
import org.contrib.wikiflavor.tests.po.Flavor;
import org.contrib.wikiflavor.tests.po.Template;
import org.contrib.wikiflavor.tests.po.WikiCreationProvisioningPage;
import org.contrib.wikiflavor.tests.po.WikiFlavorEntryEditPage;
import org.contrib.wikiflavor.tests.po.WikiFlavorsPage;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.ui.AbstractTest;
import org.xwiki.test.ui.SuperAdminAuthenticationRule;
import org.xwiki.test.ui.po.editor.WikiEditPage;
import org.xwiki.wiki.test.po.CreateWikiPageStepUser;
import org.xwiki.wiki.test.po.WikiHomePage;
import org.xwiki.wiki.test.po.WikiIndexPage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id: $
 */
public class WikiFlavorTest extends AbstractTest
{
    @Rule
    public SuperAdminAuthenticationRule superAdminAuthenticationRule =
            new SuperAdminAuthenticationRule(getUtil(), getDriver());
    
    private static final String FLAVOR_NAME = "Wiki with Flamingo Theme";
    
    private static final String FLAVOR_EXTENSION_ID = "org.xwiki.platform:xwiki-platform-flamingo-theme";
    
    private static final String FLAVOR_DESCRIPTION = "A wiki holding flamingo themes only. Quick to install.";
    
    private void setUpFlavor() throws Exception
    {
        // First, we need to fill a flavor
        WikiFlavorsPage wikiFlavorsPage = WikiFlavorsPage.gotoPage();
        WikiFlavorEntryEditPage wikiFlavorEntryEditPage = wikiFlavorsPage.addNewEntry(FLAVOR_NAME);
        wikiFlavorEntryEditPage.setName(FLAVOR_NAME);
        // we install the Flamingo Theme application because it is quick to install and it is easy to verify that it has
        // been correctly installed since this application provides some page objects.
        wikiFlavorEntryEditPage.setExtensionId(FLAVOR_EXTENSION_ID);
        wikiFlavorEntryEditPage.setDescription(FLAVOR_DESCRIPTION);
        wikiFlavorEntryEditPage.setIcon("wiki");
        wikiFlavorEntryEditPage.clickSaveAndView();
    }

    @Test
    public void createWikis() throws Exception
    {
        setUpFlavor();
        
        // Now we can create a subwiki        
        WikiIndexPage wikiIndexPage = WikiIndexPage.gotoPage();
        wikiIndexPage.createWiki();
        CreateFlavoredWikiPage createFlavoredWikiPage = new CreateFlavoredWikiPage();
        createFlavoredWikiPage.setPrettyName("My subwiki");
        createFlavoredWikiPage.setDescription("My subwiki which gonna be a template.");
        
        // Get the list of flavors
        List<Flavor> flavors = createFlavoredWikiPage.getFlavors();
        assertEquals(1, flavors.size());
        
        // Verify that the flavor is the one we just have created
        Flavor basicWikiFlavor = flavors.get(0);
        assertEquals(FLAVOR_NAME, basicWikiFlavor.getName());
        assertEquals(FLAVOR_DESCRIPTION, basicWikiFlavor.getDescription());
        assertEquals(FLAVOR_EXTENSION_ID, basicWikiFlavor.getExtensionId());
        assertEquals(null, basicWikiFlavor.getExtensionVersion());
        
        // Get the list of templates
        List<Template> templates = createFlavoredWikiPage.getTemplates();
        assertEquals(0, templates.size());
        
        // Set the type of wiki we want
        createFlavoredWikiPage.setFlavorOrExtension(FLAVOR_NAME);
        // This wiki will be a template
        createFlavoredWikiPage.setIsTemplate(true);
        
        // Step 2
        CreateWikiPageStepUser createWikiPageStepUser = createFlavoredWikiPage.goUserStep();
        createWikiPageStepUser.createWithTemplate();
        WikiCreationProvisioningPage wikiCreationProvisioningPage = new WikiCreationProvisioningPage();

        // Provisioning
        assertEquals("Wiki creation", wikiCreationProvisioningPage.getStepTitle());
        wikiCreationProvisioningPage.waitForFinalizeButton(30);
        assertFalse(wikiCreationProvisioningPage.hasLogError());

        // Finalization
        WikiHomePage wikiHomePage = wikiCreationProvisioningPage.finalizeCreation();
        
        // Go to the created subwiki
        // Look if the flavor have been correctly installed
        // ToDo: being able to navigate through a subwiki
        // ThemeApplicationWebHomePage themeApplicationWebHomePage = ThemeApplicationWebHomePage.gotoPage();
        // assertTrue(themeApplicationWebHomePage.exists());
        
        // Create a home page
        wikiHomePage.createPage();
        WikiEditPage editPage = new WikiEditPage();
        editPage.setContent("My Template");
        editPage.clickSaveAndView();
        
        // Let's go to create a new subwiki
        createWikiFromTemplate();
        // Doing it twice to check if we can create a wiki with the name of a deleted one
        createWikiFromTemplate();
        
        wikiIndexPage = WikiIndexPage.gotoPage();
        wikiHomePage = wikiIndexPage.getWikiLink("My subwiki").click();
        wikiHomePage.deleteWiki().confirm();
        WikiFlavorsPage.gotoPage().getFlavorPage(FLAVOR_NAME).delete().clickYes();
    }
    
    private void createWikiFromTemplate()
    {
        WikiIndexPage wikiIndexPage = WikiIndexPage.gotoPage();
        wikiIndexPage.createWiki();
        CreateFlavoredWikiPage createFlavoredWikiPage = new CreateFlavoredWikiPage();
        createFlavoredWikiPage.setPrettyName("My other subwiki");

        // Get the list of flavors
        List<Flavor> flavors = createFlavoredWikiPage.getFlavors();
        assertEquals(1, flavors.size());

        // Get the list of templates
        List<Template> templates = createFlavoredWikiPage.getTemplates();
        assertEquals(1, templates.size());

        // Verify that the template is the one we just have created
        Template myTemplate = templates.get(0);
        assertEquals("My subwiki (mysubwiki)", myTemplate.getName());
        assertEquals("My subwiki which gonna be a template.", myTemplate.getDescription());
        assertEquals("mysubwiki", myTemplate.getTemplateId());

        // Set the type of wiki we want
        createFlavoredWikiPage.setFlavorOrExtension("My subwiki (mysubwiki)");

        // Step 2
        CreateWikiPageStepUser createWikiPageStepUser = createFlavoredWikiPage.goUserStep();
        createWikiPageStepUser.createWithTemplate();
        WikiCreationProvisioningPage wikiCreationProvisioningPage = new WikiCreationProvisioningPage();

        // Provisioning
        assertEquals("Wiki creation", wikiCreationProvisioningPage.getStepTitle());

        // Finalization
        wikiCreationProvisioningPage.waitForFinalizeButton(30);
        assertFalse(wikiCreationProvisioningPage.hasLogError());
        WikiHomePage wikiHomePage = wikiCreationProvisioningPage.finalizeCreation();

        // Go to the subwiki and check that it has correctly be created with the template
        assertEquals("My Template", wikiHomePage.getContent());

        // Cleaning
        wikiHomePage.deleteWiki().confirm();
    }
    
}
