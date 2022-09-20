# PartiQL Kotlin Test Runner

This package checks whether the conformance tests run using the `partiql-lang-kotlin` implementation return the correct
result. The `partiql-tests-validator` package does schema level validation to assert the test data conforms to the
test model schema, whereas this package will do validation on the test data's results.

This package will allow us to:
1. verify conformance tests are defined correctly (e.g. verify evaluation environment is not missing a table)
2. identify areas in the Kotlin implementation that diverge from the PartiQL specification

Eventually, the Kotlin test runner can be ported to `partiql-lang-kotlin` ([partiql-lang-kotlin#789](https://github.com/partiql/partiql-lang-kotlin/issues/789))
and can replace the tests that were ported to `partiql-tests`.
