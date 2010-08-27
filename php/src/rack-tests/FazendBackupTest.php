<?php
/**
 * @version $Id: FazendBackupTest.php 130 2010-08-04 15:48:32Z yegor256@yahoo.com $
 */

class FazendBackupTest extends PhpRack_Test
{

    public function testExecute()
    {
        // execute fazend backup script
        $this->assert->shell->exec(
            'cd ' . escapeshellarg(APPLICATION_PATH . '/../public') . '; env -i php index.php FzBackup 2>&1',
            '/FaZend/'
        );
    }

}