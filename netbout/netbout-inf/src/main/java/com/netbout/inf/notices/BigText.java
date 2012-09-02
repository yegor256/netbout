/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
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
package com.netbout.inf.notices;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.commons.lang.CharEncoding;

/**
 * Big text manipulator.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id: MessageNotice.java 2943 2012-07-19 15:50:03Z guard $
 */
final class BigText {

    /**
     * The text itself.
     */
    private final transient String text;

    /**
     * Public ctor.
     * @param txt The text
     */
    public BigText(final String txt) {
        this.text = txt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Write text to stream.
     * @param stream Stream to write to
     * @throws IOException If cant write
     */
    public void write(final DataOutputStream stream) throws IOException {
        final byte[] bytes = this.text.getBytes(CharEncoding.UTF_8);
        stream.writeInt(bytes.length);
        for (byte data : bytes) {
            stream.writeByte(data);
        }
    }

    /**
     * Read text from stream.
     * @param stream Stream to read from
     * @return New instance of this class, with text inside
     * @throws IOException If cant write
     */
    public static BigText read(final DataInputStream stream)
        throws IOException {
        final byte[] bytes = new byte[stream.readInt()];
        for (int num = 0; num < bytes.length; ++num) {
            bytes[num] = stream.readByte();
        }
        return new BigText(new String(bytes, CharEncoding.UTF_8));
    }

}
