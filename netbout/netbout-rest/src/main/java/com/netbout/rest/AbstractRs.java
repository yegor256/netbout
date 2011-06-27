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
 * incident to the author by email: privacy@netbout.com.
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

// API from com.netbout:netbout-engine
import com.netbout.engine.User;

// for JAX-RS
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * Abstract JAX-RS entry point.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public abstract class AbstractRs {

    /**
     * URI information.
     */
    private UriInfo uriInfo;

    /**
     * Security context.
     */
    private SecurityContext securityContext;

    /**
     * Save URI information.
     * @param uinfo URI Information (injected by JAX-RS impl)
     */
    @Context
    public final void setUriInfo(final UriInfo uinfo) {
        this.uriInfo = uinfo;
    }

    /**
     * Save security context.
     * @param ctx The context
     */
    @Context
    public final void setSecurityContext(final SecurityContext ctx) {
        this.securityContext = ctx;
    }

    /**
     * Get currently logged in user.
     * @return The user
     */
    protected final User user() {
        return new Auth(this.securityContext()).user();
    }

    /**
     * Get URI information.
     * @return URI Information (injected by JAX-RS impl)
     */
    protected final UriInfo uriInfo() {
        return this.uriInfo;
    }

    /**
     * Get security context.
     * @return The context
     */
    protected final SecurityContext securityContext() {
        return this.securityContext;
    }

}
