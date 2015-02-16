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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.wikiflavor.WikiCreationRequest;
import org.xwiki.contrib.wikiflavor.WikiCreationStep;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.contrib.wikiflavor.WikiSource;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.wiki.descriptor.WikiDescriptor;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;
import org.xwiki.wiki.template.WikiTemplateManager;
import org.xwiki.wiki.template.WikiTemplateManagerException;
import org.xwiki.wiki.user.WikiUserManager;
import org.xwiki.wiki.user.WikiUserManagerException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Component that save the metadata of the wiki (pretty name, description, etc...) as well as the configuration (user
 * scope, membership type, etc...).
 *
 * @version $Id: $
 * @since 1.0
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named("metadata")
public class SaveWikiMetaDataStep implements WikiCreationStep
{
    private final static String WIKI_FLAVOR_CODE_SPACE = "WikiFlavorsCode";

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private WikiTemplateManager wikiTemplateManager;

    @Inject
    private WikiUserManager wikiUserManager;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Override
    public void execute(WikiCreationRequest request) throws WikiFlavorException
    {
        try {
            String wikiId = request.getWikiId();
            // Meta data about the wiki
            WikiDescriptor descriptor = wikiDescriptorManager.getById(wikiId);
            descriptor.setDescription(request.getDescription());
            descriptor.setPrettyName(request.getPrettyName());
            descriptor.setOwnerId(request.getOwnerId());
            wikiDescriptorManager.saveDescriptor(descriptor);

            // Meta data about the templates
            wikiTemplateManager.setTemplate(wikiId, request.isTemplate());

            // Meta data about the users
            wikiUserManager.setUserScope(wikiId, request.getUserScope());
            wikiUserManager.setMembershipType(wikiId, request.getMembershipType());

            // We also need to store what is the id of the main extension of that wiki
            maybeSaveMainExtensionId(request);

        } catch (WikiManagerException|WikiTemplateManagerException|WikiUserManagerException e) {
            throw new WikiFlavorException(
                String.format("Failed to set metadata to the wiki [%s].", request.getWikiId()), e);
        }
    }

    private void maybeSaveMainExtensionId(WikiCreationRequest request) throws WikiFlavorException
    {
        // We save the extension id of the main extension inside the wiki, so when an upgrade is executed, DW can know
        // which extension is used.
        // This information is stored inside the wiki in order to be copied when the wiki is copied (for example if it
        // is a template), so that the copied wiki can also be properly upgraded by DW.
        if (request.getWikiSource() == WikiSource.EXTENSION) {
            String wikiId = request.getWikiId();
            try {
                XWikiContext xcontext = xcontextProvider.get();
                XWiki xwiki = xcontext.getWiki();

                DocumentReference documentReference =
                        new DocumentReference(wikiId, WIKI_FLAVOR_CODE_SPACE, "MainExtensionId");
                DocumentReference classReference =
                        new DocumentReference(wikiId, WIKI_FLAVOR_CODE_SPACE, "MainExtensionIdClass");

                XWikiDocument document = xwiki.getDocument(documentReference, xcontext);
                document.setHidden(true);
                BaseObject obj = document.newXObject(classReference, xcontext);
                obj.setStringValue("extensionId", request.getExtensionId().getId());
                xwiki.saveDocument(document, "Save the main extension id at the wiki creation.", xcontext);
            } catch (XWikiException e) {
                throw new WikiFlavorException(
                    String.format("Failed to save the main extension id in the wiki [%s].", wikiId), e);
            }
        }
    }

    @Override
    public int getOrder()
    {
        return 2000;
    }
}
