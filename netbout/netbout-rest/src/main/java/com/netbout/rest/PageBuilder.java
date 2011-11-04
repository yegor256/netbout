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

import com.rexsl.core.Stylesheet;
import com.ymock.util.Logger;
import java.util.Collection;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

/**
 * Page builder, a singleton.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PageBuilder {

    /**
     * Public instance.
     */
    public static final PageBuilder INSTANCE = new PageBuilder();

    /**
     * Javassist class pool.
     */
    private final ClassPool pool = ClassPool.getDefault();

    /**
     * It's a singleton class, you can't instantiate it directly.
     */
    private PageBuilder() {
        this.pool.insertClassPath(new ClassClassPath(this.getClass()));
        Logger.debug(
            this,
            "#PageBuilder(): javassist initialized"
        );
    }

    /**
     * Create new class.
     * @param home Where this page is built from
     * @param stylesheet The XSL stylesheet to use with this class
     * @return The instance of the class just created
     */
    public Page build(final Resource home, final String stylesheet) {
        Page page;
        try {
            page = (Page) this.create(stylesheet)
                .getDeclaredConstructor(Resource.class)
                .newInstance(home);
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.debug(
            this,
            "#build(%s, '%s'): page of class %s created",
            home.getClass().getName(),
            stylesheet,
            page.getClass().getName()
        );
        return page;
    }

    /**
     * Create new collection of JAXB elements.
     * @param name Name of XML parent element
     * @param list Collection of elements
     * @return The object that can be added to the Page
     *  with {@link #append(Object)}
     */
    public Object group(final String name, final Collection list) {
        // todo
        return null;
    }

    /**
     * Create and return a new class for the given stylesheet.
     * @param stylesheet The XSL stylesheet to use with this class
     * @return The class just created or found
     */
    private Class<? extends Page> create(final String stylesheet) {
        final String name = String.format(
            "com.netbout.rest.Page$%s$%d",
            stylesheet.replaceAll("[^\\w]", ""),
            Math.abs(stylesheet.hashCode())
        );
        Class cls;
        if (this.pool.getOrNull(name) != null) {
            try {
                cls = (Class<Page>) Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(ex);
            }
            // let's double check that the class found really is the
            // class we're looking for
            assert ((Stylesheet) cls.getAnnotation(Stylesheet.class))
                .value().equals(stylesheet);
        } else {
            cls = this.construct(name, stylesheet);
        }
        return cls;
    }

    /**
     * Construct a new class with given name.
     * @param name The name of the class to construct
     * @param stylesheet The XSL stylesheet to use with this class
     * @return The class just created
     */
    private Class<? extends Page> construct(final String name,
        final String stylesheet) {
        try {
            final CtClass ctc = this.pool.getAndRename(
                DefaultPage.class.getName(),
                name
            );
            final ClassFile file = ctc.getClassFile();
            final AnnotationsAttribute attribute =
                (AnnotationsAttribute) file.getAttribute(
                    AnnotationsAttribute.visibleTag
                );
            final Annotation annotation = new Annotation(
                Stylesheet.class.getName(),
                file.getConstPool()
            );
            annotation.addMemberValue(
                "value",
                new StringMemberValue(stylesheet, file.getConstPool())
            );
            attribute.addAnnotation(annotation);
            final Class cls = ctc.toClass();
            Logger.debug(
                this,
                "#construct('%s', '%s'): class %s created",
                name,
                stylesheet,
                cls.getName()
            );
            return cls;
        } catch (javassist.NotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (javassist.CannotCompileException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
