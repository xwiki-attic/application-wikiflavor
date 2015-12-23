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
package org.xwiki.contrib.wikiflavor.internal;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.platform.wiki.creationjob.WikiCreationException;
import org.xwiki.platform.wiki.creationjob.WikiCreationRequest;
import org.xwiki.platform.wiki.creationjob.WikiSource;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class WikiFlavorMetadataStepTest
{
    @Rule
    public MockitoComponentMockingRule<WikiFlavorMetadataStep> mocker =
            new MockitoComponentMockingRule<>(WikiFlavorMetadataStep.class);

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;

    @Before
    public void setUp() throws Exception
    {
        xcontextProvider = mocker.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xcontext = mock(XWikiContext.class);
        when(xcontextProvider.get()).thenReturn(xcontext);
        xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);
    }

    @Test
    public void execute() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        request.setWikiSource(WikiSource.EXTENSION);
        ExtensionId extensionId = new ExtensionId("id", "version");
        request.setExtensionId(extensionId);

        // Mock
        XWikiDocument document = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionId")),
                eq(xcontext))).thenReturn(document);
        BaseObject object = mock(BaseObject.class);
        when(document.newXObject(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionIdClass")),
                eq(xcontext))).thenReturn(object);

        // Test
        mocker.getComponentUnderTest().execute(request);

        // Verify
        verify(document).setHidden(true);
        verify(object).setStringValue("extensionId", "id");
        verify(xwiki).saveDocument(eq(document), eq("Save the main extension id at the wiki creation."), eq(xcontext));
    }

    @Test
    public void executeWithExceptionWhileSavingExtensionId() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        request.setWikiSource(WikiSource.EXTENSION);
        ExtensionId extensionId = new ExtensionId("id", "version");
        request.setExtensionId(extensionId);

        // Mock
        XWikiDocument document = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionId")),
                eq(xcontext))).thenReturn(document);
        BaseObject object = mock(BaseObject.class);
        when(document.newXObject(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionIdClass")),
                eq(xcontext))).thenReturn(object);
        Exception exception = new XWikiException();
        doThrow(exception).when(xwiki).saveDocument(any(XWikiDocument.class), anyString(), any(XWikiContext.class));

        // Test
        WikiCreationException caughtException = null;
        try {
            mocker.getComponentUnderTest().execute(request);
        } catch (WikiCreationException e) {
            caughtException = e;
        }

        // Verify
        assertEquals("Failed to save the main extension id in the wiki [wikiId].", caughtException.getMessage());
        assertEquals(exception, caughtException.getCause());
    }

    @Test
    public void getOrder() throws Exception
    {
        assertEquals(2500, mocker.getComponentUnderTest().getOrder());
    }
}
