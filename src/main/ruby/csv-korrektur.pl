#!/usr/bin/perl -w
use strict;
use warnings;
use utf8;
use open qw(:std :utf8);
use Unicode::Normalize;

# \r\n für echtes Zeilenende
# \n für Zeilenumbruch in einer Zelle

use File::Slurp;

my $csv = $ARGV[0];

my $text = read_file($csv, binmode => ':utf8');

if ($text =~ m!\$!) { die "Die Tabelle enthält \$\n"; }
if ($text =~ m!::!) { die "Die Tabelle enthält ::\n"; }

$text =~ s!\r\n!\$!g;
$text =~ s!\n! :: !g;
$text =~ s!\$!\n!g;

$csv =~ s!\.csv$!-korrigiert\.csv!;

open (FILE, ">".$csv);
print FILE $text;
close (FILE);
