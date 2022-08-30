Tests in this directory should have statements that can be evaluated and return the same result as the 
expected result. Additionally, the tests in this directory assert the additional PartiQL statements evaluate to the
same expected result.

TODO: as part of https://github.com/partiql/partiql-tests/issues/32, need to define how to refer to these equivalence
classes outside a given file or namespace. This will allow for greater reuse of the equivalence classes for other 
testing classes (e.g. planner tests). A possible outcome of resolving #32 could be moving the currently defined
equivalence classes to separate files.
