TinyTable is a succinct fingerprint hash table implementation.
That is, instead of storing values like a regular hash table, TinyTable only store a fingerprint
generated from the item.  Since two different items may have the same fingerprint, TinyTable
may experience false positives. However, these can be made arbitrary rare by making fingerprints
long enough.

TinyTable can be used to approximate a set, and it supports additions, removals and contains.
In that operation mode it is similar to counting Bloom filters. For many configurations, TinyTable is
more space efficient than even plain Bloom filters making it an attractive data structure to use.

TinyTable is also able to associate fingerprints with values, and can therefore also be used for counting up to large
numbers, or for associating items with arbitrary data. This capability makes TinyTable very flexible and it can therefore
be used for a large variety of problems.