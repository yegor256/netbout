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
package com.netbout.utils;

import com.netbout.hub.Hub;
import com.netbout.spi.Identity;
import com.ymock.util.Logger;
import org.apache.commons.lang.StringUtils;

// import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;
// import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
/**
 * Cipher and de-cipher texts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class Cipher {

    // /**
    //  * Password to use in encryption.
    //  */
    // private static final String PASSWORD = "j&^%hgfRR43$#&==_ )(00(0}{-~";
    //
    // /**
    //  * Encryptor.
    //  */
    // private final StandardPBEStringEncryptor encryptor =
    //     new StandardPBEStringEncryptor();

    // /**
    //  * Public ctor.
    //  */
    // public Cryptor() {
    //     this.encryptor.setPassword(this.PASSWORD);
    //     this.encryptor
    //        .setAlgorithm(StandardPBEByteEncryptor.DEFAULT_ALGORITHM);
    // }

    /**
     * Encrypt some text.
     * @param text The text to encrypt
     * @return Encrypted string
     */
    public String encrypt(final String text) {
        return text;
    }

    /**
     * Decrypt from hash.
     * @param hash The hash to use
     * @return The original text
     * @throws DecryptionException If we can't decrypt it
     */
    public String decrypt(final String hash) throws DecryptionException {
        return hash;
    }

}
