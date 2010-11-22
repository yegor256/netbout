<?php
/**
 * @version $Id: EnvironmentTest.php 1612 2010-04-11 10:17:07Z yegor256@yahoo.com $
 */

class EnvironmentTest extends PhpRack_Test
{

    public function testPhpConfiguration()
    {
        $this->assert->php->version
            ->atLeast('5.2');
        $this->assert->php->extensions
            ->isLoaded('SimpleXML')
            ->isLoaded('xml')
            ->isLoaded('date')
            ->isLoaded('dom')
            ->isLoaded('pdo')
            ->isLoaded('libxml')
            ->isLoaded('json')
            ->isLoaded('pdo_mysql')
            ->isLoaded('mysql')
            ->isLoaded('mcrypt')
            ->isLoaded('openssl')
            ;
    }

}
