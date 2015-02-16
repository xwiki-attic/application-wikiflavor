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
package org.xwiki.contrib.wikiflavor.internal.steps;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.contrib.wikiflavor.WikiCreationRequest;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.contrib.wikiflavor.WikiSource;
import org.xwiki.extension.ExtensionId;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.wiki.descriptor.WikiDescriptor;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;
import org.xwiki.wiki.template.WikiTemplateManager;
import org.xwiki.wiki.user.MembershipType;
import org.xwiki.wiki.user.UserScope;
import org.xwiki.wiki.user.WikiUserManager;

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
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class SaveWikiMetaDataStepTest
{
    @Rule
    public MockitoComponentMockingRule<SaveWikiMetaDataStep> mocker =
            new MockitoComponentMockingRule<>(SaveWikiMetaDataStep.class);

    private WikiDescriptorManager wikiDescriptorManager;

    private WikiTemplateManager wikiTemplateManager;
    
    private WikiUserManager wikiUserManager;

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;

    @Before
    public void setUp() throws Exception
    {
        wikiDescriptorManager = mocker.getInstance(WikiDescriptorManager.class);
        wikiTemplateManager = mocker.getInstance(WikiTemplateManager.class);
        wikiUserManager = mocker.getInstance(WikiUserManager.class);
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
        request.setDescription("description");
        request.setPrettyName("pretty name");
        request.setOwnerId("ownerId");
        request.setTemplate(false);
        request.setUserScope(UserScope.GLOBAL_ONLY);
        request.setMembershipType(MembershipType.INVITE);
        request.setWikiSource(WikiSource.EXTENSION);
        ExtensionId extensionId = new ExtensionId("id", "version");
        request.setExtensionId(extensionId);

        // Mock
        WikiDescriptor descriptor = new WikiDescriptor("wikiId", "alias");
        when(wikiDescriptorManager.getById("wikiId")).thenReturn(descriptor);
        XWikiDocument document = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionId")),
                eq(xcontext))).thenReturn(document);
        BaseObject object = mock(BaseObject.class);
        when(document.newXObject(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionIdClass")),
                eq(xcontext))).thenReturn(object);
                
        // Test
        mocker.getComponentUnderTest().execute(request);

        // Verify
        assertEquals("description", descriptor.getDescription());
        assertEquals("pretty name", descriptor.getPrettyName());
        assertEquals("ownerId", descriptor.getOwnerId());
        verify(wikiDescriptorManager).saveDescriptor(descriptor);
        verify(wikiTemplateManager).setTemplate("wikiId", false);
        verify(wikiUserManager).setUserScope("wikiId", UserScope.GLOBAL_ONLY);
        verify(wikiUserManager).setMembershipType("wikiId", MembershipType.INVITE);
        verify(document).setHidden(true);
        verify(object).setStringValue("extensionId", "id");
        verify(xwiki).saveDocument(eq(document), eq("Save the main extension id at the wiki creation."), eq(xcontext));
    }

    @Test
    public void executeWithException() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");

        // Mock
        WikiDescriptor descriptor = new WikiDescriptor("wikiId", "alias");
        when(wikiDescriptorManager.getById("wikiId")).thenReturn(descriptor);

        Exception exception = new WikiManagerException("Exception on WikiManager.");
        doThrow(exception).when(wikiDescriptorManager).saveDescriptor(descriptor);

        // Test
        WikiFlavorException caughtException = null;
        try {
            mocker.getComponentUnderTest().execute(request);
        } catch (WikiFlavorException e) {
            caughtException = e;
        }

        // Verify
        assertEquals("Failed to set metadata to the wiki [wikiId].", caughtException.getMessage());
        assertEquals(exception, caughtException.getCause());
    }
    
    @Test
    public void executeWithExceptionWhileSavingExtensionId() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        request.setTemplate(true);
        request.setUserScope(UserScope.LOCAL_AND_GLOBAL);
        request.setMembershipType(MembershipType.REQUEST);
        request.setWikiSource(WikiSource.EXTENSION);
        ExtensionId extensionId = new ExtensionId("id", "version");
        request.setExtensionId(extensionId);

        // Mock
        WikiDescriptor descriptor = new WikiDescriptor("wikiId", "alias");
        when(wikiDescriptorManager.getById("wikiId")).thenReturn(descriptor);
        XWikiDocument document = mock(XWikiDocument.class);
        when(xwiki.getDocument(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionId")),
                eq(xcontext))).thenReturn(document);
        BaseObject object = mock(BaseObject.class);
        when(document.newXObject(eq(new DocumentReference("wikiId", "WikiFlavorsCode", "MainExtensionIdClass")),
                eq(xcontext))).thenReturn(object);
        Exception exception = new XWikiException();
        doThrow(exception).when(xwiki).saveDocument(any(XWikiDocument.class), anyString(), any(XWikiContext.class));

        // Test
        WikiFlavorException caughtException = null;
        try {
            mocker.getComponentUnderTest().execute(request);
        } catch (WikiFlavorException e) {
            caughtException = e;
        }

        // Verify
        assertEquals("Failed to save the main extension id in the wiki [wikiId].", caughtException.getMessage());
        assertEquals(exception, caughtException.getCause());
    }

    @Test
    public void executeWhenSourceIsTemplate() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        request.setTemplate(false);
        request.setUserScope(UserScope.LOCAL_ONLY);
        request.setMembershipType(MembershipType.OPEN);
        request.setWikiSource(WikiSource.TEMPLATE);

        // Mock
        WikiDescriptor descriptor = new WikiDescriptor("wikiId", "alias");
        when(wikiDescriptorManager.getById("wikiId")).thenReturn(descriptor);
        
        // Test
        mocker.getComponentUnderTest().execute(request);

        // Verify
        verify(wikiDescriptorManager).saveDescriptor(descriptor);
        verify(wikiUserManager).setUserScope("wikiId", UserScope.LOCAL_ONLY);
        verify(wikiUserManager).setMembershipType("wikiId", MembershipType.OPEN);
        verifyZeroInteractions(xcontextProvider);
    }

    @Test
    public void getOrder() throws Exception
    {
        assertEquals(2000, mocker.getComponentUnderTest().getOrder());
    }
}

