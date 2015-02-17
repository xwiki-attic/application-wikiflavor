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

import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.contrib.wikiflavor.Flavor;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class DefaultWikiFlavorManagerTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultWikiFlavorManager> mocker =
            new MockitoComponentMockingRule<>(DefaultWikiFlavorManager.class);

    private QueryManager queryManager;

    private WikiDescriptorManager wikiDescriptorManager;

    private AuthorizationManager authorizationManager;

    private DocumentReferenceResolver<String> documentReferenceResolver;

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;

    @Before
    public void setUp() throws Exception
    {
        // Injections
        queryManager = mocker.getInstance(QueryManager.class);
        wikiDescriptorManager = mocker.getInstance(WikiDescriptorManager.class);
        authorizationManager = mocker.getInstance(AuthorizationManager.class);
        documentReferenceResolver = mocker.getInstance(new DefaultParameterizedType(null,
                DocumentReferenceResolver.class, String.class));
        xcontextProvider = mocker.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xcontext = mock(XWikiContext.class);
        when(xcontextProvider.get()).thenReturn(xcontext);
        xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);
        
        // Mocks
        when(wikiDescriptorManager.getMainWikiId()).thenReturn("mainWikiId");
    }
    
    @Test
    public void getFlavors() throws Exception
    {   
        // Mocks
        Query query = mock(Query.class);
        when(queryManager.createQuery(
                "FROM doc.object(WikiFlavorsCode.WikiFlavorsClass) obj WHERE doc.space = 'WikiFlavors'", Query.XWQL))
                .thenReturn(query);
        when(query.setWiki("mainWikiId")).thenReturn(query);
        List<String> flavorDocuments = Arrays.asList("WikiFlavors.FlavorA", "WikiFlavors.FlavorB",
                "WikiFlavors.FlavorC");
        when(query.<String>execute()).thenReturn(flavorDocuments);

        DocumentReference flavorClassReference =
                new DocumentReference("mainWikiId", "WikiFlavorsCode", "WikiFlavorsClass");
        WikiReference mainWikiReference = new WikiReference("mainWikiId");

        // Flavor A
        XWikiDocument docFlavorA = mock(XWikiDocument.class);
        DocumentReference docFlavorAReference = new DocumentReference("mainWikiId", "WikiFlavors", "FlavorA");
        when(documentReferenceResolver.resolve("WikiFlavors.FlavorA")).thenReturn(docFlavorAReference);
        when(xwiki.getDocument(docFlavorAReference, xcontext)).thenReturn(docFlavorA);
        BaseObject objFlavorA = mock(BaseObject.class);
        when(docFlavorA.getXObject(flavorClassReference)).thenReturn(objFlavorA);
        when(objFlavorA.getStringValue("extensionId")).thenReturn("flavorAextensionId");
        when(objFlavorA.getStringValue("extensionVersion")).thenReturn("flavorAextensionVersion");
        when(objFlavorA.getStringValue("nameTranslationKey")).thenReturn("flavorAname");
        when(objFlavorA.getStringValue("descriptionTranslationKey")).thenReturn("flavorAdescription");
        when(objFlavorA.getStringValue("icon")).thenReturn("flavorAicon");
        DocumentReference flavorAauthor = new DocumentReference("mainWikiId", "XWiki", "UserWithPR");
        when(docFlavorA.getAuthorReference()).thenReturn(flavorAauthor);
        when(authorizationManager.hasAccess(eq(Right.PROGRAM), eq(flavorAauthor), eq(mainWikiReference)))
                .thenReturn(true);

        // Flavor B (not saved with PR)
        XWikiDocument docFlavorB = mock(XWikiDocument.class);
        DocumentReference docFlavorBReference = new DocumentReference("mainWikiId", "WikiFlavors", "FlavorB");
        when(documentReferenceResolver.resolve("WikiFlavors.FlavorB")).thenReturn(docFlavorBReference);
        when(xwiki.getDocument(docFlavorBReference, xcontext)).thenReturn(docFlavorB);
        BaseObject objFlavorB = mock(BaseObject.class);
        when(docFlavorB.getXObject(flavorClassReference)).thenReturn(objFlavorB);
        DocumentReference flavorBauthor = new DocumentReference("mainWikiId", "XWiki", "UserWithoutPR");
        when(docFlavorB.getAuthorReference()).thenReturn(flavorBauthor);
        when(authorizationManager.hasAccess(eq(Right.PROGRAM), eq(flavorBauthor), eq(mainWikiReference)))
                .thenReturn(false);

        // Flavor C (exception while reading it)
        DocumentReference docFlavorCReference = new DocumentReference("mainWikiId", "WikiFlavors", "FlavorC");
        when(documentReferenceResolver.resolve("WikiFlavors.FlavorC")).thenReturn(docFlavorCReference);
        XWikiException exception = new XWikiException();
        when(xwiki.getDocument(docFlavorCReference, xcontext)).thenThrow(exception);
        
        // Test
        List<Flavor> results = mocker.getComponentUnderTest().getFlavors();
        
        // Verify
        assertEquals(1, results.size());
        
        // Flavor A
        Flavor resultFlavor = results.get(0);
        assertEquals("flavorAextensionId", resultFlavor.getExtensionId());
        assertEquals("flavorAextensionVersion", resultFlavor.getExtensionVersion());
        assertEquals("flavorAname", resultFlavor.getNameTranslationKey());
        assertEquals("flavorAdescription", resultFlavor.getDescriptionTranslationKey());
        assertEquals("flavorAicon", resultFlavor.getIcon());
        
        // Flavor C
        verify(mocker.getMockedLogger()).warn("Unable to read the Wiki Flavor Document [{}]", 
                "WikiFlavors.FlavorC", exception);
    }

    @Test
    public void getFlavorsWithException() throws Exception
    {
        // Mocks
        QueryException queryException = new QueryException("Error when creating the query", null, null);
        when(queryManager.createQuery(
                "FROM doc.object(WikiFlavorsCode.WikiFlavorsClass) obj WHERE doc.space = 'WikiFlavors'", Query.XWQL))
                .thenThrow(queryException);
        
        // Test
        WikiFlavorException caughtException = null;
        try {
            mocker.getComponentUnderTest().getFlavors();
        } catch(WikiFlavorException e) {
            caughtException = e;
        }
        
        // Verify
        assertNotNull(caughtException);
        assertEquals("Failed to get the list of the flavors.", caughtException.getMessage());
        assertEquals(queryException, caughtException.getCause());
        verify(mocker.getMockedLogger()).error("Failed to get the list of the flavors.", queryException);
    }

}
