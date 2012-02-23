/**
 * Copyright (c) 2009-2011, netBout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code occasionally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.rest;

import com.netbout.rest.page.JaxbBundle;
import com.netbout.spi.Identity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Page.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public interface Page {

    /**
     * Add new link.
     * @param name The name of it
     * @param href HREF of the link
     * @return This object
     */
    Page link(String name, String href);

    /**
     * Add new link.
     * @param name The name of it
     * @param builder Builder of URI
     * @return This object
     */
    Page link(String name, UriBuilder builder);

    /**
     * Append new JAXB-annotated element.
     * @param element The element
     * @return This object
     */
    Page append(Object element);

    /**
     * Append new bundle.
     * @param bundle The DOM bundle
     * @return This object
     */
    Page append(JaxbBundle bundle);

    /**
     * Render content, if this method is not called the
     * page will be empty.
     * @return This object
     */
    Page render();

    /**
     * Create and return a JAX-RS response, for a page that is viewed by an
     * athenticated user (we know who is logged in now).
     * @param identity The identity
     * @return The response builder
     */
    Response.ResponseBuilder authenticated(Identity identity);

    /**
     * Create and return a JAX-RS response for anonymous user.
     * @return The response builder
     */
    Response.ResponseBuilder anonymous();

    /**
     * Create and return a JAX-RS response for the same user as logged in now
     * (if nobody is logged in - make it anonymous).
     * @return The response builder
     */
    Response.ResponseBuilder preserved();

}
