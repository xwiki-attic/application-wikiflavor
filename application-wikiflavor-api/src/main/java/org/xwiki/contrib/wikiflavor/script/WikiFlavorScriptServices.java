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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.contrib.wikiflavor.Flavor;
import org.xwiki.contrib.wikiflavor.FlavoredWikiCreator;
import org.xwiki.contrib.wikiflavor.WikiCreationRequest;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.contrib.wikiflavor.WikiFlavorManager;
import org.xwiki.extension.ExtensionId;
import org.xwiki.job.Job;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.script.service.ScriptService;

/**
 * Script services for the creation of flavored wikis.
 *
 * @version $Id: $
 * @since 1.0
 */
@Component
@Singleton
@Named("wikiflavor")
public class WikiFlavorScriptServices implements ScriptService
{
    /**
     * The key under which the last encountered error is stored in the current execution context.
     */
    private static final String ERROR_KEY = "scriptservice.wikiflavor.error";
            
    @Inject
    private FlavoredWikiCreator flavoredWikiCreator;

    @Inject
    private WikiFlavorManager wikiFlavorManager;

    @Inject
    private Execution execution;
    
    @Inject
    private Logger logger;

    /**
     * Asynchronously create a wiki with a flavor.
     *
     * @param request creation wiki request containing all information about the wiki to create
     * @return the job that creates the wiki
     */
    public Job createWiki(WikiCreationRequest request)
    {
        try {
            if (request.getExtensionId() != null) {
                if (!isAuthorizedFlavor(request.getExtensionId())) {
                    // The extension id is not an authorized flavor, we do not install it
                    return null;
                }
            }
            return flavoredWikiCreator.createWiki(request);
        } catch (WikiFlavorException e) {
            setLastError(e);
            logger.error("Failed to create a new wiki.", e);
        }

        return null;
    }

    /**
     * @param wikiId id of the wiki
     * @return the job status corresponding to the creation of the wiki
     */
    public JobStatus getJobStatus(String wikiId)
    {
        return flavoredWikiCreator.getJobStatus(wikiId);
    }

    /**
     * @return a new request for the creation of a new wiki
     */
    public WikiCreationRequest newWikiCreationRequest()
    {
        return new WikiCreationRequest();
    }

    /**
     * @return the list of available flavors
     */
    public List<Flavor> getFlavors()
    {
        try {
            return wikiFlavorManager.getFlavors();
        } catch (WikiFlavorException e) {
            setLastError(e);
        }

        return null;
    }

    private boolean isAuthorizedFlavor(ExtensionId extensionId) throws WikiFlavorException
    {
        for (Flavor flavor : wikiFlavorManager.getFlavors()) {
            if (flavor.getExtensionId().equals(extensionId.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the error generated while performing the previously called action.
     * @return an eventual exception or {@code null} if no exception was thrown
     * @since 1.1
     */
    public WikiFlavorException getLastError()
    {
        return (WikiFlavorException) this.execution.getContext().getProperty(ERROR_KEY);
    }

    /**
     * Store a caught exception in the context, so that it can be later retrieved using {@link #getLastError()}.
     *
     * @param e the exception to store, can be {@code null} to clear the previously stored exception
     * @see #getLastError()
     * @since 1.1
     */
    private void setLastError(WikiFlavorException e)
    {
        this.execution.getContext().setProperty(ERROR_KEY, e);
    }
}
