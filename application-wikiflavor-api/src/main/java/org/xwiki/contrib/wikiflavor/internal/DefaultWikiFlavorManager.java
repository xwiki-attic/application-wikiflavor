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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.query.Query;
import org.xwiki.query.QueryException;
import org.xwiki.query.QueryManager;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import org.xwiki.contrib.wikiflavor.Flavor;
import org.xwiki.contrib.wikiflavor.WikiFlavorException;
import org.xwiki.contrib.wikiflavor.WikiFlavorManager;

/**
 * Default implementation for {@link org.xwiki.contrib.wikiflavor.WikiFlavorManager}.
 *
 * @version $Id: $
 * @since 1.0
 */
@Component
@Singleton
public class DefaultWikiFlavorManager implements WikiFlavorManager
{
    @Inject
    private QueryManager queryManager;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private AuthorizationManager authorizationManager;

    @Inject
    private DocumentReferenceResolver<String> documentReferenceResolver;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    @Inject
    private Logger logger;

    @Override
    public List<Flavor> getFlavors() throws WikiFlavorException
    {
        List<Flavor> results = new ArrayList<>();
        try {
            String mainWikiId = wikiDescriptorManager.getMainWikiId();
            XWikiContext xcontext = xcontextProvider.get();
            XWiki xwiki = xcontext.getWiki();

            // Query to get all flavor documents
            Query query = queryManager.createQuery(
                    "FROM doc.object(WikiFlavorsCode.WikiFlavorsClass) obj WHERE doc.space = 'WikiFlavors'",
                    Query.XWQL).setWiki(mainWikiId);
            for (String result : query.<String>execute()) {
                try {
                    XWikiDocument document =
                            xwiki.getDocument(documentReferenceResolver.resolve(result), xcontext);
                    DocumentReference classReference =
                            new DocumentReference(mainWikiId, "WikiFlavorsCode", "WikiFlavorsClass");
                    BaseObject obj = document.getXObject(classReference);

                    Flavor flavor =
                            new Flavor(obj.getStringValue("extensionId"), obj.getStringValue("extensionVersion"),
                                    obj.getStringValue("nameTranslationKey"),
                                    obj.getStringValue("descriptionTranslationKey"),
                                    obj.getStringValue("icon"));

                    // Check that the flavor document have been saved with programming right
                    if (authorizationManager.hasAccess(Right.PROGRAM, document.getAuthorReference(),
                            new WikiReference(mainWikiId)))
                    {
                        results.add(flavor);
                    }
                } catch (XWikiException e) {
                    logger.warn("Unable to read the Wiki Flavor Document [{}]", result, e);
                }
            }

        } catch (QueryException e) {
            String errorMessage = "Failed to get the list of the flavors.";
            logger.error(errorMessage, e);
            throw new WikiFlavorException(errorMessage, e);
        }

        return results;
    }
}
