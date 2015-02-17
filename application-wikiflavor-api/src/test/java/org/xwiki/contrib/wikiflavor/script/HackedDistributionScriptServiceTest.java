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
package org.xwiki.contrib.wikiflavor.script;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.distribution.internal.DistributionManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class HackedDistributionScriptServiceTest
{
    @Rule
    public MockitoComponentMockingRule<HackedDistributionScriptService> mocker =
            new MockitoComponentMockingRule<>(HackedDistributionScriptService.class);

    private DocumentAccessBridge documentAccessBridge;

    private DistributionManager distributionManager;

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;
    
    @Before
    public void setUp() throws Exception
    {
        documentAccessBridge = mocker.getInstance(DocumentAccessBridge.class);
        distributionManager = mocker.getInstance(DistributionManager.class);
        xcontextProvider = mocker.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xcontext = mock(XWikiContext.class);
        when(xcontextProvider.get()).thenReturn(xcontext);
        xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);
    }
    
    @Test
    public void getUIExtensionIdWhenMainWiki() throws Exception
    {
        when(xcontext.isMainWiki("mainWikiId")).thenReturn(true);
        ExtensionId extensionId = new ExtensionId("extensionOfTheMainWiki", "version");
        when(distributionManager.getMainUIExtensionId()).thenReturn(extensionId);
        assertEquals(extensionId, mocker.getComponentUnderTest().getUIExtensionId("mainWikiId"));
    }

    @Test
    public void getUIExtensionIdWhenFlavoredWiki() throws Exception
    {
        when(xcontext.isMainWiki("subwiki")).thenReturn(false);
        when(documentAccessBridge.getProperty(
                eq(new DocumentReference("subwiki", "WikiFlavorsCode", "MainExtensionId")),
                eq(new DocumentReference("subwiki", "WikiFlavorsCode", "MainExtensionIdClass")), eq("extensionId")))
                .thenReturn("extensionOfTheSubwiki");
        when(xwiki.getVersion()).thenReturn("XWiki Version");
        assertEquals(new ExtensionId("extensionOfTheSubwiki", "XWiki Version"),
                mocker.getComponentUnderTest().getUIExtensionId("subwiki"));
    }

    @Test
    public void getUIExtensionIdWhenNoFlavoredWiki() throws Exception
    {
        when(xcontext.isMainWiki("subwiki")).thenReturn(false);
        ExtensionId extensionId = new ExtensionId("extensionOfTheSubwiki", "version");
        when(distributionManager.getWikiUIExtensionId()).thenReturn(extensionId);
        assertEquals(extensionId, mocker.getComponentUnderTest().getUIExtensionId("subwiki"));
    }
}
