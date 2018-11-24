# SPARQL Queries
It is recommended to follow the patterns of SPARQL queries strictly (in order to avoid errors).

## Examples
Examples shown below are related to LUBM benchmark.

### Simple Query with LIMIT
```
SELECT ?author ?publication
WHERE {
	?publication :publicationAuthor ?author .
}
LIMIT 20
```

### UNION of two Queries
```
SELECT *
WHERE {
    { ?X :type :UndergraduateStudent . }
    UNION
    {
        ?X :type :GraduateStudent .
        ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    }
}
```

### Query with FILTER
```
SELECT ?X
WHERE {
    ?X :type :GraduateStudent .
    ?X :takesCourse <http://www.Department0.University0.edu/GraduateCourse0> .
    FILTER (?X != <http://www.Department0.University0.edu/GraduateStudent101>)
}
```