## `partiql-tests` Schema Proposal

TODO:
- add ISL definitions for this schema proposal
- move the doc to `partiql-docs`

`partiql-tests`' data format should have the following goals in mind:
1. ease of writing -- it should be easy to understand the test schema and create new tests.
2. ease of integrating to test runner implementations -- it should be easy to parse the created test data.
3. extensibility for future test categories and test properties (e.g. check parsed ast, error codes, error contexts)

As of this proposal, we want to test these categories:
- parsing
- end-to-end evaluation
- (in the future) type-checking

---

The following is an abstraction to describe current and future tests we will have in the `partiql-tests` suite
```
// <test_category> -- parse, eval, type_check
<test_category>::{
    name: <string>,
    statement: <string>,
    <other fields relevant for corresponding test_category>
    assert: <list<struct>>
}
```

The `assert` list provides flexibility for future tests to check for additional expected behaviors when running a test.

---

### Parser Tests

Tests whether a given PartiQL statement successfully parses (no syntax error). For now, composed of these properties:

- test name (string)
- PartiQL statement (string)


```
// parse 'pass' test
parse::{
    name: <string>,
    statement: <string>,
    assert: [
        {
           result: ParseOk
        },
        // in the future, could add an assertion checking the statement parses to the expected ast
    ]
}

// parse 'fail' test
parse::{
    name: <string>,
    statement: <string>,
    assert: [
        {
            result: ParseError
        },
        // in the future, can also add an assertion checking for `error_code` or other error details
    ]
}
```

---

### Evaluation Tests

Tests whether a given PartiQL statement evaluates to the expected result. For now, composed of these properties:

- test name (string)
- PartiQL statement (string)
- [optional] input evaluation environment (symbol or struct) — defaults to empty environment
- expected evaluation result (Ion) — only for pass tests
- [optional] evaluation options (list of symbols) other than the defaults -- could also be represented using a struct
    - e.g. `TYPING_MODE_PERMISSIVE`

For ease of writing evaluation tests, it’s necessary to provide a way to specify environments that can be referred to 
outside a given test.

Specifying environments available for a given file:
```
envs::{
  'table1': [{a:1}, {a:2}, {a:3}],
  'table2': ...
}

// environment for test can be specified using one of `envs` in the file:
env: table1
// or specified explicitly with a struct
env: { 'table1': [{a:1}, {a:2}, {a:3}] }
```

```
// eval 'pass' test
eval::{
    name: <string>,
    statement: <string>,
    env: <symbol | struct>,
    options: <list<symbol>>,
    assert: [
        {
            result: <ion>
        },
    ]
}

// eval 'fail' test
eval::{
    name: <string>,
    statement: <string>,
    env: <symbol | struct>,
    options: <list<symbol>>,
    assert: [
        {
            result: EvaluationError
        },
        // in the future, can also add an assertion checking for `error_code` or other error details
    ]
}
```

---

### Additional inclusions
The concept of namespacing tests can help categorize groups of tests and can reduce duplication in test names. This can 
be used by the test runner to prepend additional text to a test name. E.g. namespace of "literals" can be prepended to test names of "int" and "null" to get "literals - int" and 
"literals - null"

```
// namespacing/grouping (using a symbol annotation)
<namespace_symbol>::[
    parse::{
        name: <string>,
        statement: <string>,
        assert: [
            {
                result: ParseOk
            }
        ]
    },
    ...
]
```
