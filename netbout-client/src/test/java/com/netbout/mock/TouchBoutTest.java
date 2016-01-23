package com.netbout.mock;

import com.netbout.spi.Bout;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link TouchBout].
 *
 * @author Dmitry Zaytsev (dmitr.zaytsev@gmail.com)
 * @version $Id$
 * @since 2.23
 */
public final class TouchBoutTest {
    /**
     * TouchBout can change update attribute of the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesUpdateAttribute() throws Exception {
        final Sql sql = new H2Sql();
        final Bout bout = new MkBase(sql).randomBout();
        final Long last = bout.updated().getTime();
        new TouchBout(sql, bout.number()).act();
        final Long pause = 100L;
        Thread.sleep(pause);
        MatcherAssert.assertThat(
            bout.updated().getTime(), Matchers.greaterThan(last)
        );
    }
}