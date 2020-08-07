PrayerTimes is a public-domain application that provides prayer times.

TODO :

- add forceResultsForToday to get correct rise/set/transit moon times for today not yesterday or tomorrow
when timezone is different then UTC.
search forceResultsForToday on http://conga.oan.es/~alonso/doku.php?id=blog:sun_moon_position

- this HijriQuae issue is fixed by using floorDiv and floorMod:

    2020-01-31 => 1441-06-05 (cjdn = 2458880)
    2020-02-01 => 1441-06-05 (cjdn = 2458880)

