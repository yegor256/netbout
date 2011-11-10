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
package com.netbout.rest.page;

import com.rexsl.core.Stylesheet;
import com.ymock.util.Logger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;
import org.apache.commons.lang.StringUtils;

/**
 * Page builder, a singleton.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @param <T> Class of page to build
 */
public final class PageBuilder {

    /**
     * Static initialization of Javassist.
     */
    static {
        ClassPool.getDefault().insertClassPath(
            new ClassClassPath(PageBuilder.class)
        );
        Logger.debug(
            PageBuilder.class,
            "#PageBuilder(): javassist initialized"
        );
    }

    /**
     * Stylesheet to use.
     */
    private String stylesheet = "none";

    /**
     * Public ctor.
     */
    public PageBuilder() {
        // intentionally empty
    }

    /**
     * Configure the stylesheet to be used.
     * @param xsl Name of stylesheet
     * @return This object
     */
    public PageBuilder stylesheet(final String xsl) {
        this.stylesheet = xsl;
        return this;
    }

    /**
     * Create new class.
     * @param base Parent class, which will be inherited
     * @return The instance of the class just created
     * @param <T> The type of result expected
     */
    public <T> T build(final Class<T> base) {
        T page;
        try {
            page = (T) this.createOrFind(base).newInstance();
        } catch (InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        }
        Logger.debug(
            PageBuilder.class,
            // @checkstyle LineLength (1 line)
            "#build(%s): page of class %s created",
            base.getName(),
            page.getClass().getName()
        );
        return page;
    }

    /**
     * Create and return a new class for the given stylesheet, or find an
     * existing one and return it.
     * @param base Parent class, which will be inherited
     * @return The class just created or found
     */
    private Class createOrFind(final Class base) {
        synchronized (PageBuilder.class) {
            final String name = String.format(
                "%s$%s$%d",
                base.getName(),
                this.stylesheet.replaceAll("[^\\w]", ""),
                Math.abs(this.stylesheet.hashCode())
            );
            Class cls;
            if (ClassPool.getDefault().getOrNull(name) != null) {
                try {
                    cls = Class.forName(name);
                } catch (ClassNotFoundException ex) {
                    throw new IllegalStateException(ex);
                }
                // let's double check that the class found really is the
                // class we're looking for
                assert ((Stylesheet) cls.getAnnotation(Stylesheet.class))
                    .value().equals(this.stylesheet);
            } else {
                cls = this.construct(name, base);
            }
            return cls;
        }
    }

    /**
     * Construct a new class with given name.
     * @param name The name of the class to construct
     * @param base Parent class, which will be inherited
     * @return The class just created
     */
    private Class construct(final String name, final Class base) {
        final ClassPool pool = ClassPool.getDefault();
        try {
            final CtClass parent = pool.get(base.getName());
            final CtClass ctc = pool.makeClass(name, parent);
            final ClassFile file = ctc.getClassFile();
            final AnnotationsAttribute attribute = new AnnotationsAttribute(
                file.getConstPool(),
                AnnotationsAttribute.visibleTag
            );
            final Annotation annotation = new Annotation(
                Stylesheet.class.getName(),
                file.getConstPool()
            );
            annotation.addMemberValue(
                "value",
                new StringMemberValue(this.stylesheet, file.getConstPool())
            );
            attribute.addAnnotation(annotation);
            for (Annotation existing : this.annotations(ctc, parent)) {
                attribute.addAnnotation(existing);
            }
            file.addAttribute(attribute);
            final Class cls = ctc.toClass();
            Logger.debug(
                this,
                "#construct('%s', %s): class %s created",
                name,
                base.getName(),
                cls.getName()
            );
            return cls;
        } catch (javassist.NotFoundException ex) {
            throw new IllegalStateException(ex);
        } catch (javassist.CannotCompileException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Get list of existing annotations. Maybe we should filter out some of
     * them and not copy to the new class, I don't know.
     * @param dest Destination class, where these annotations will be used
     * @param parent Parent class, which will be inherited
     * @return The list of them
     */
    private Collection<Annotation> annotations(final CtClass dest,
        final CtClass parent) {
        final AnnotationsAttribute attrib =
            (AnnotationsAttribute) parent.getClassFile()
                .getAttribute(AnnotationsAttribute.visibleTag)
                .copy(dest.getClassFile().getConstPool(), new HashMap());
        final Annotation[] all = attrib.getAnnotations();
        final Collection<Annotation> result = new ArrayList<Annotation>();
        final List<String> names = new ArrayList<String>();
        for (Annotation annotation : all) {
            result.add(annotation);
            names.add(annotation.getTypeName());
        }
        Logger.debug(
            this,
            // @checkstyle LineLength (1 line)
            "#annotations(%s, %s): %d found in base class, %d of them are copied: %s",
            dest.getName(),
            parent.getName(),
            all.length,
            result.size(),
            StringUtils.join(names, ", ")
        );
        return result;
    }

}
