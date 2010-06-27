<?php

function rnd(array $lst, $max = null)
{
    if (is_null($max)) {
        $max = count($lst);
    }
    $indexes = array_rand($lst, rand(1, $max));
    if (!is_array($indexes)) {
        $indexes = array($indexes);
    }
    shuffle($indexes);
    $result = array();
    foreach ($indexes as $i) {
        $result[] = $lst[$i];
    }
    return $result;
}

function lorem($length)
{
    $txt = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.';
    $txt = str_repeat($txt, floor($length / strlen($txt)) + 1);
    return substr($txt, 0, $length) . '...';
}

/**
 * Generate and return a random name
 */
function name()
{
    $names = array('alex89', 'Dmitry Solodov', 'Tarzan', 'David Underhill', 'Antal S-Z');
    return $names[array_rand($names, 1)];
}

$page = isset($_GET['p']) ? $_GET['p'] : 'front';

include 'layout.phtml';
