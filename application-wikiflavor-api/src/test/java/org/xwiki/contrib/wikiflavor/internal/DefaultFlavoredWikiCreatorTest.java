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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.contrib.wikiflavor.WikiCreationRequest;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.job.Job;
import org.xwiki.job.JobException;
import org.xwiki.job.JobExecutor;
import org.xwiki.job.JobStatusStore;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @version $Id: $
 */
public class DefaultFlavoredWikiCreatorTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultFlavoredWikiCreator> mocker =
            new MockitoComponentMockingRule<>(DefaultFlavoredWikiCreator.class);

    private JobExecutor jobExecutor;

    private JobStatusStore jobStatusStore;
    
    @Before
    public void setUp() throws Exception
    {
        jobExecutor = mocker.getInstance(JobExecutor.class);
        jobStatusStore = mocker.getInstance(JobStatusStore.class);
    }
    
    @Test
    public void createWiki() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        
        // Mock
        Job job = mock(Job.class); 
        when(jobExecutor.execute("wikiflavorjob", request)).thenReturn(job);
        
        // Test
        assertEquals(job, mocker.getComponentUnderTest().createWiki(request));
        
        // Verify
        assertEquals(Arrays.asList("wikiflavor", "createandinstall", "wikiId"), request.getId());
    }

    @Test
    public void createWikiWithException() throws Exception
    {
        WikiCreationRequest request = new WikiCreationRequest();
        request.setWikiId("wikiId");
        
        // Mock
        JobException jobException = new JobException("Error in JobException");
        when(jobExecutor.execute("wikiflavorjob", request)).thenThrow(jobException);

        // Test
        WikiFlavorException caughtException = null;
        try {
            mocker.getComponentUnderTest().createWiki(request);
        } catch (WikiFlavorException e) {
            caughtException = e;
        }

        // Verify
        assertNotNull(caughtException);
    }
    
    @Test
    public void getJobStatus() throws Exception
    {
        // Mocks
        Job job = mock(Job.class);
        JobStatus jobStatus1 = mock(JobStatus.class);
        when(job.getStatus()).thenReturn(jobStatus1);
        when(jobExecutor.getJob(Arrays.asList("wikiflavor", "createandinstall", "wiki1"))).thenReturn(job);
        JobStatus jobStatus2 = mock(JobStatus.class);
        when(jobStatusStore.getJobStatus(Arrays.asList("wikiflavor", "createandinstall", "wiki2"))).
                thenReturn(jobStatus2);
        
        // Tests
        assertEquals(jobStatus1, mocker.getComponentUnderTest().getJobStatus("wiki1"));
        assertEquals(jobStatus2, mocker.getComponentUnderTest().getJobStatus("wiki2"));
    }
}
