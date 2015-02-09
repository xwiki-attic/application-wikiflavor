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
import org.contrib.wikiflavor.tests.po.WikiFlavorEntryEditPage;
import org.contrib.wikiflavor.tests.po.WikiFlavorsPage;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.ui.AbstractTest;
import org.xwiki.test.ui.SuperAdminAuthenticationRule;
import org.xwiki.test.ui.po.editor.WikiEditPage;
import org.xwiki.wiki.test.po.CreateWikiPageStepProvisioning;
import org.xwiki.wiki.test.po.CreateWikiPageStepUser;
import org.xwiki.wiki.test.po.WikiHomePage;
import org.xwiki.wiki.test.po.WikiIndexPage;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id: $
 */
public class WikiFlavorTest extends AbstractTest
{
    @Rule
    public SuperAdminAuthenticationRule superAdminAuthenticationRule =
            new SuperAdminAuthenticationRule(getUtil(), getDriver());

    @Test
    public void createWikis() throws Exception
    {
        // First, we need to fill a flavor
        WikiFlavorsPage wikiFlavorsPage = WikiFlavorsPage.gotoPage();
        WikiFlavorEntryEditPage wikiFlavorEntryEditPage = wikiFlavorsPage.addNewEntry("Basic Wiki");
        wikiFlavorEntryEditPage.setName("Basic Wiki");
        wikiFlavorEntryEditPage.setExtensionId("org.xwiki.enterprise:xwiki-enterprise-ui-wiki");
        wikiFlavorEntryEditPage.setDescription("A basic wiki from XE");
        wikiFlavorEntryEditPage.setIcon("wiki");
        wikiFlavorEntryEditPage.clickSaveAndView();
        
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
        assertEquals("Basic Wiki", basicWikiFlavor.getName());
        assertEquals("A basic wiki from XE", basicWikiFlavor.getDescription());
        assertEquals("org.xwiki.enterprise:xwiki-enterprise-ui-wiki", basicWikiFlavor.getExtensionId());
        assertEquals(null, basicWikiFlavor.getExtensionVersion());
        
        // Get the list of templates
        List<Template> templates = createFlavoredWikiPage.getTemplates();
        assertEquals(0, templates.size());
        
        // Set the type of wiki we want
        createFlavoredWikiPage.setFlavorOrExtension("Basic Wiki");
        // This wiki will be a template
        createFlavoredWikiPage.setIsTemplate(true);
        
        // Step 2
        CreateWikiPageStepUser createWikiPageStepUser = createFlavoredWikiPage.goUserStep();
        CreateWikiPageStepProvisioning createWikiPageStepProvisioning = createWikiPageStepUser.createWithTemplate();
        
        // Provisioning
        assertEquals("Wiki creation", createWikiPageStepProvisioning.getStepTitle());
        
        // Finalization
        WikiHomePage wikiHomePage = createWikiPageStepProvisioning.finalizeCreation();
        
        // Go to the create subwiki and change the title
        WikiEditPage wikiEditHomePage = wikiHomePage.editWiki();
        wikiEditHomePage.setTitle("My Template");
        wikiEditHomePage.clickSaveAndView();
        
        // Let's go to create a new subwiki
        wikiIndexPage = WikiIndexPage.gotoPage();
        wikiIndexPage.createWiki();
        createFlavoredWikiPage = new CreateFlavoredWikiPage();
        createFlavoredWikiPage.setPrettyName("My other subwiki");

        // Get the list of flavors
        flavors = createFlavoredWikiPage.getFlavors();
        assertEquals(1, flavors.size());

        // Get the list of templates
        templates = createFlavoredWikiPage.getTemplates();
        assertEquals(1, templates.size());

        // Verify that the template is the one we just have created
        Template myTemplate = templates.get(0);
        assertEquals("My subwiki (mysubwiki)", myTemplate.getName());
        assertEquals("My subwiki which gonna be a template.", myTemplate.getDescription());
        assertEquals("mysubwiki", myTemplate.getTemplateId());

        // Set the type of wiki we want
        createFlavoredWikiPage.setFlavorOrExtension("My subwiki (mysubwiki)");

        // Step 2
        createWikiPageStepUser = createFlavoredWikiPage.goUserStep();
        createWikiPageStepProvisioning = createWikiPageStepUser.createWithTemplate();

        // Provisioning
        assertEquals("The system is provisioning the wiki.", createWikiPageStepProvisioning.getStepTitle());

        // Finalization
        wikiHomePage = createWikiPageStepProvisioning.finalizeCreation();
        
        // Go to the subwiki and check that it has correctly be created with the template
        assertEquals("My Template", wikiHomePage.getDocumentTitle());
        
        // Cleaning
        wikiHomePage.deleteWiki();
        wikiIndexPage = WikiIndexPage.gotoPage();
        wikiHomePage = wikiIndexPage.getWikiLink("My subwiki").click();
        wikiHomePage.deleteWiki();
        wikiFlavorsPage = WikiFlavorsPage.gotoPage();
    }
    
}
