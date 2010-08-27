<?php
/**
 * @version $Id: LogTest.php 1561 2010-04-08 11:39:56Z yegor256@yahoo.com $
 */

class QosTest extends PhpRack_Test
{

    public function testLatency()
    {
        $this->assert->qos->latency(
            array(
                'scenario' => array(
                    'http://www.netbout.com',
                ),
                'averageMs' => 500, // 500ms average per request
                'peakMs' => 2000, // 2s maximum per request
            )
        );
    }

}   