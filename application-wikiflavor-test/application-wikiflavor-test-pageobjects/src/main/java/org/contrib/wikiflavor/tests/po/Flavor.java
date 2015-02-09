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
package org.contrib.wikiflavor.tests.po;

/**
 * @version $Id: $
 */
public class Flavor
{
    private String name;
    
    private String extensionId;

    private String extensionVersion;
    
    private String description;
    
    public Flavor()
    {
        
    }

    public Flavor(String name, String extensionId, String extensionVersion, String description)
    {
        this.name = name;
        this.extensionId = extensionId;
        this.extensionVersion = extensionVersion;
        this.description = description;
    }

    public String getExtensionId()
    {
        return extensionId;
    }

    public void setExtensionId(String extensionId)
    {
        this.extensionId = extensionId;
    }

    public String getExtensionVersion()
    {
        return extensionVersion;
    }

    public void setExtensionVersion(String extensionVersion)
    {
        this.extensionVersion = extensionVersion;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
