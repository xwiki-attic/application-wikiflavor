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
package org.xwiki.contrib.wikiflavor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @version $Id: $
 */
public class WikiFlavorExceptionTest
{
    @Test
    public void equals() throws Exception
    {
        Throwable throwable = new Throwable();
        assertEquals(new WikiFlavorException("message", throwable), new WikiFlavorException("message", throwable));
        assertNotEquals(new WikiFlavorException("message2", throwable), new WikiFlavorException("message", throwable));
        assertNotEquals(new WikiFlavorException("message", throwable),
                new WikiFlavorException("message", new Throwable()));
        assertNotEquals(new WikiFlavorException("message", throwable),
                new Exception("message", new Throwable()));
    }
}
