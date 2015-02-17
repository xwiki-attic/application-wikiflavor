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

import java.util.Arrays;
import java.util.List;

import javax.inject.Provider;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.contrib.wikiflavor.Flavor;
import org.xwiki.contrib.wikiflavor.FlavoredWikiCreator;
import org.xwiki.contrib.wikiflavor.WikiCreationRequest;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.contrib.wikiflavor.WikiFlavorManager;
import org.xwiki.job.Job;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.security.authorization.AccessDeniedException;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class WikiFlavorScriptServicesTest
{
    @Rule
    public MockitoComponentMockingRule<WikiFlavorScriptServices> mocker = 
            new MockitoComponentMockingRule<>(WikiFlavorScriptServices.class);

    private FlavoredWikiCreator flavoredWikiCreator;

    private WikiFlavorManager wikiFlavorManager;

    private Execution execution;

    private AuthorizationManager authorizationManager;

    private WikiDescriptorManager wikiDescriptorManager;

    private Provider<XWikiContext> xcontextProvider;

    private XWikiContext xcontext;

    private XWiki xwiki;
    
    @Before
    public void setUp() throws Exception
    {
        flavoredWikiCreator = mocker.getInstance(FlavoredWikiCreator.class);
        wikiFlavorManager = mocker.getInstance(WikiFlavorManager.class);
        execution = mocker.getInstance(Execution.class);
        authorizationManager = mocker.getInstance(AuthorizationManager.class);
        wikiDescriptorManager = mocker.getInstance(WikiDescriptorManager.class);
        xcontextProvider = mocker.registerMockComponent(XWikiContext.TYPE_PROVIDER);
        xcontext = mock(XWikiContext.class);
        when(xcontextProvider.get()).thenReturn(xcontext);
        xwiki = mock(XWiki.class);
        when(xcontext.getWiki()).thenReturn(xwiki);
        
        when(wikiDescriptorManager.getMainWikiId()).thenReturn("mainWikiId");

        ExecutionContext executionContext = new ExecutionContext();
        when(execution.getContext()).thenReturn(executionContext);
    }
    
    @Test
    public void getFlavors() throws Exception
    {
        List<Flavor> flavorList = Arrays.asList(new Flavor("eId", "eVersion", "name", "description", "icon"));
        when(wikiFlavorManager.getFlavors()).thenReturn(flavorList);
        assertEquals(flavorList, mocker.getComponentUnderTest().getFlavors());
    }

    @Test
    public void getFlavorsWhenException() throws Exception
    {
        WikiFlavorException exception = new WikiFlavorException("Error in WikiFlavorManager");
        when(wikiFlavorManager.getFlavors()).thenThrow(exception);
        assertNull(mocker.getComponentUnderTest().getFlavors());
        assertEquals(exception, mocker.getComponentUnderTest().getLastError());
    }
    
    @Test
    public void createWiki() throws Exception
    {
        List<Flavor> flavorList = Arrays.asList(new Flavor("eId", "eVersion", "name", "description", "icon"));
        when(wikiFlavorManager.getFlavors()).thenReturn(flavorList);
        Job job = mock(Job.class);
        when(flavoredWikiCreator.createWiki(any(WikiCreationRequest.class))).thenReturn(job);
        
        WikiCreationRequest wikiCreationRequest = new WikiCreationRequest();
        wikiCreationRequest.setExtensionId("eId", "version");
        assertEquals(job, mocker.getComponentUnderTest().createWiki(wikiCreationRequest));
        assertNull(mocker.getComponentUnderTest().getLastError());
    }

    @Test
    public void createWikiWhenExtensionIsNotAuthorized() throws Exception
    {
        List<Flavor> flavorList = Arrays.asList(new Flavor("eId", "eVersion", "name", "description", "icon"));
        when(wikiFlavorManager.getFlavors()).thenReturn(flavorList);
        
        WikiCreationRequest wikiCreationRequest = new WikiCreationRequest();
        wikiCreationRequest.setExtensionId("badExtension", "version");
        
        assertNull(mocker.getComponentUnderTest().createWiki(wikiCreationRequest));
        Exception lastError = mocker.getComponentUnderTest().getLastError();
        assertNotNull(lastError);
        assertEquals("The flavor [badExtension-version] is not authorized.", lastError.getMessage());
        verify(mocker.getMockedLogger()).warn("Failed to create a new wiki.", lastError);
    }

    @Test
    public void createWikiWhenNoCreateWikiRight() throws Exception
    {
        DocumentReference currentUser = new DocumentReference("xwiki", "XWiki", "User");
        when(xcontext.getUserReference()).thenReturn(currentUser);
        AccessDeniedException exception = 
                new AccessDeniedException(Right.CREATE_WIKI, currentUser, new WikiReference("mainWikiId"));
        doThrow(exception).when(authorizationManager).checkAccess(eq(Right.CREATE_WIKI), eq(currentUser),
                eq(new WikiReference("mainWikiId")));

        WikiCreationRequest wikiCreationRequest = new WikiCreationRequest();
        wikiCreationRequest.setExtensionId("eId", "version");

        assertNull(mocker.getComponentUnderTest().createWiki(wikiCreationRequest));
        Exception lastError = mocker.getComponentUnderTest().getLastError();
        assertNotNull(lastError);
        assertEquals(exception, lastError);
    }

    @Test
    public void getJobStatus() throws Exception
    {
        JobStatus jobStatus = mock(JobStatus.class);
        when(flavoredWikiCreator.getJobStatus("wikiId")).thenReturn(jobStatus);
        assertEquals(jobStatus, mocker.getComponentUnderTest().getJobStatus("wikiId"));
    }

    @Test
    public void newWikiCreationRequest() throws Exception
    {
        assertNotNull(mocker.getComponentUnderTest().newWikiCreationRequest());
    }
}
