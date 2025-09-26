## `partiql-tests` Schema Proposal

`partiql-tests`' data format should have the following goals in mind:
1. ease of writing -- it should be easy to understand the test schema and create new tests.
2. ease of integrating to test runner implementations -- it should be easy to parse the created test data.
3. extensibility for future test categories and test properties (e.g. check parsed ast, error codes, error contexts)

As of this proposal, we want to test these categories:
- syntax
- static analysis and type-checking
- end-to-end evaluation
- equivalence

---

The following is an abstraction to describe current and future tests we will have in the `partiql-tests` suite
```ion
{
    name: <string>,
    statement: <string>,
    <other fields relevant for corresponding test_category>
    assert: <struct> | <list<struct>>
}
```

The `assert` field can be a struct or a list of structs and provides flexibility for tests to check for additional 
expected behavior(s).

---

### Syntax Tests

Tests whether a given PartiQL statement is syntactically valid. For now, composed of these properties:

- test name (string)
- PartiQL statement (string)
- assert (struct or list of structs) with a syntax assertion

```ion
// syntax 'success' test with one assertion
{
    name: <string>,
    statement: <string>,
    assert: {
       result: SyntaxSuccess
    },
}

// syntax 'fail' test with one assertion
{
    name: <string>,
    statement: <string>,
    assert: {
        result: SyntaxFail
    },
}
```

The `assert` field could also be a list of structs if more test assertions are added in the future.

```ion
// syntax 'success' test with multiple assertions
{
    ...
    assert: [
        {
            result: SyntaxSuccess
        },
        {   // in the future, could add an assertion checking the statement parses to the expected ast
            ast: ...
        }
    ]
}

// syntax 'fail' test with multiple assertions
{
    ...
    assert: [
        {
            result: SyntaxFail
        },
        {   // in the future, could check the error code
            error_code: ...
        },
        {   // in the future, could check line and column of error
            line_of_err: ...,
            col_of_err: ...
        }
    ]
}
```

---

### Static Analysis Tests

Currently have a set of `fail` tests that error on the provided statement. These tests should error at some stage 
between parsing and evaluation. It's up to the implementation to decide at what stage these statements should error.

For now, composed of the same properties as the `syntax` `fail` tests. The only difference is the `assert`'s error 
(i.e. `StaticAnalysisFail`).

```ion
{
    name: <string>,
    statement: <string>,
    assert: {
        result: StaticAnalysisFail
    },
}
```

---

### Evaluation Tests

Tests whether a given PartiQL statement evaluates to the expected result. For now, composed of these properties:

- test `name` - string
- PartiQL `statement` - string
- [optional] input evaluation environment, `env` - struct
  - defaults to using environments specified in file (i.e. `envs`). If `envs` is unspecified, defaults to an empty
  environment with no bindings
- `assert` - struct or list of structs
  - `result` key maps to symbol
    - `EvaluatorSuccess` if evaluation succeeds for statement
    - `EvaluatorFail` if evaluation fails for statement
  - `evalMode` - evaluation mode to run the tests (symbol or list of symbols)
    - `EvalModeCoerce` - dynamic type mismatch returns `MISSING`
    - `EvalModeError` - dynamic type mismatch errors
  - expected `output` of evaluation (only for `EvaluatorSuccess` `result`s)

```ion
// eval 'success' test
{
    name: <string>,
    statement: <string>,
    env: <struct>,        // optional
    assert: {
        evalMode: <symbol> | <list<symbol>>,
        result: EvaluationSuccess,
        output: <ion>
    },
}

// eval 'fail' test
{
    name: <string>,
    statement: <string>,
    env: <struct>,        // optional
    assert: {
        evalMode: <symbol> | <list<symbol>>,
        result: EvaluationFail
    }
}
```

For ease of writing evaluation tests, it must be possible to declare environments which can be used by multiple test cases.

Specifying environments available for a given file:
```ion
envs::{
  'table1': [{a:1}, {a:2}, {a:3}],
  'table2': ...
}

// environment for test can be specified using one of the `envs` in the namespace/file
{
   name: "test using environment symbol",
   statement: "SELECT * FROM table1",
   ...
}
// or specified explicitly using the inlined `env`
{
   name: "test using explicit environement",
   statement: "SELECT * FROM table1",
   env: { table1: [{a:1}, {a:2}, {a:3}] }
   ...
}
```

#### Modeling PartiQL Types in Ion
PartiQL's type system encompasses Ion's types with a few additional types (e.g. missing, bag, date, time). There are a 
few approaches we could take to model PartiQL data:
- Annotations (using Ion)
- S-expressions (using Ion)
- PartiQL object notation

Of the above options, we've decided to go with the annotation approach. Values of these additional types will be denoted
using a `$<partiql_type>` annotation. This will be used for the output result and environments.

```ion
// bag -- list annotated with $bag
$bag::[1, 2, 3]

// missing -- null value annotated with $missing
$missing::null

// date -- struct annotated with $date
$date::{ year: 2022, month: 2, day: 22 } // All fields are required

// TIME/TIMEZ -- struct annotated with $time
// Fields: hour (int), minute (int), second (decimal, scale ≤ 9), offset (int?, minutes from UTC)
// offset: null = TIME WITHOUT TIME ZONE
// offset: int  = TIME WITH TIME ZONE (0 = UTC)
$time::{ hour: 2, minute: 30, second: 59.0, offset: 0 } // All fields required; offset nullable

// TIMESTAMP/TIMESTAMPZ -- struct annotated with $timestamp
// Fields: year (int), month (int), day (int), hour (int), minute (int), second (decimal, scale ≤ 9), offset (int?, minutes from UTC)
// offset: null = TIMESTAMP WITHOUT TIME ZONE
// offset: int  = TIMESTAMP WITH TIME ZONE (0 = UTC)
$timestamp::{ year: 2025, month: 1, day: 1, hour: 1, minute: 1, second: 1.111, offset: -2 } // All fields required; offset nullable

// interval year-month -- struct annotated with $interval_ym
// All fields are optional
// Default value -- sign: "+", years(int): 0, months(int): 0
// Normalization rules -- sign = "+"/"-"; 0 <= years <= 999999999; 0 <= months <= 11
$interval_ym::{ sign: "+", years: 1, months: 6 }

// interval day-time -- struct annotated with $interval_dt
// All fields are optional
// Default value -- sign: "+", days(int): 0, hours(int): 0, minutes(int): 0, seconds(int): 0, nanos(int): 0
// Normalization rules -- sign = "+"/"-"; 0 <= days <= 999999999; 0 <= hours <= 23; 0 <= minutes <=  59;
// 0 <= seconds <= 59; 0 <= nanos <= 999999999
$interval_dt::{ sign: "+", days: 5, hours: 4, minutes: 30, seconds: 15, nanos: 500000000 }
```

Similarly, graphs defined with the Ion-based format for "external" graphs 
([org.partiql.schemas/graph.isl](https://github.com/partiql/partiql-lang-kotlin/blob/main/partiql-lang/src/main/resources/org/partiql/schemas/graph.isl))
are annotated with `$graph`:

```ion
// graph -- a struct in graph.isl format, annotated with $graph
$graph::{ nodes: [ {id: n1, payload: 1} ], 
          edges: [ {id: d1, payload: 1.1, ends: (n1 -> n1) } ] }
```

---

#### PartiQL Evaluation Modes
The PartiQL specification defines two dynamic type mismatching evaluation modes (i.e. when a PartiQL statement is run 
without schema). As defined in the PartiQL specification, these modes are:
- Permissive mode -- dynamic typing mismatches are neglected and PartiQL returns `MISSING`
- Type checking mode -- dynamic type mismatches result in evaluation errors

The naming of these modes can be somewhat confusing especially "type checking mode", which is sometimes referred to as
`STRICT` mode in the specification and Kotlin reference implementation. For the purposes of this document and the
conformance tests, we will refer to permissive mode as `EvalModeCoerce` and type checking mode as `EvalModeError`. 
These names can be changed in the future once we improve the terminology in the specification (see 
[partiql-docs#24](https://github.com/partiql/partiql-docs/issues/24)).

```ion
// Test case using `EvalModeCoerce`
{
    name: "coerce eval mode tuple navigation missing attribute dot notation",
    statement: "{'a':1, 'b':2}.noSuchAttribute",
    assert: {
        evalMode: EvalModeCoerce,
        result: EvaluationSuccess,
        output: $missing::null
    }
}

// Test case using `EvalModeError`
{
    name: "error eval mode tuple navigation missing attribute dot notation",
    statement: "{'a':1, 'b':2}.noSuchAttribute",
    assert: {
        evalMode: EvalModeError,
        result: EvaluationFail
    }
}

// Test case using both eval modes in assertions
{
    name: "tuple navigation missing attribute dot notation",
    statement: "{'a':1, 'b':2}.noSuchAttribute",
    assert: [
        {
            evalMode: EvalModeError,
            result: EvaluationFail
        },
        {
            evalMode: EvalModeCoerce,
            result: EvaluationSuccess,
            output: $missing::null
        },
    ]
}

// Test case using both eval modes in assertions with same result
{
    name: "tuple navigation for attribute dot notation",
    statement: "{'a':1, 'b':2}.a",
    assert: {
        evalMode: [EvalModeError, EvalModeCoerce],
        result: EvaluationSuccess,
        output: 1
    }
}
```

---

### Equivalence
The PartiQL specification mentions some PartiQL statements that could be rewritten using a different PartiQL syntax 
(e.g. wildcard expressions). A common use case could be to assert that such PartiQL statements evaluate to the same 
result or have the same plan. Users can specify an equivalence class as follows.

```ion
equiv_class::{
    id: <symbol>,               // identifier that can be referred to in tests (e.g. evaluation)
    statements: <list<string>>  // list of equivalent PartiQL statements as strings
}
```

Evaluation tests can check that an equivalence class defined in the file/namespace have statements that evaluate to the
same result by referencing the equivalence class' symbol identifier in the `statement` field.

```ion
// evaluation equivalence test
{
    name: <string>,
    statement: <symbol>     // identifier to equivalence class
    ...                     // same other evaluation test fields    
    assert: {
        result: EvaluationSuccess,
        output: <ion>
    }
}
```

As a simple example, the following would be how to write an evaluation equivalence test:
```ion
// equivalence class definition
equiv_class::{
    id: ten,
    statements: [
        "5 * 2",
        "20 / 2",
        "1 + 2 + 3 + 4",
    ]
}

// evaluation test with equivalence class assertion
{
    name: "equivalence class test sample",
    statement: ten,
    assert: {
        result: EvaluationSuccess,
        output: 10
    }
}
```

---

### Additional inclusions
The concept of namespacing tests can help categorize groups of tests and can reduce duplication in test names. This can 
be used by the test runner to prepend additional text to a test name. E.g. namespace of "literals" can be prepended to test names of "int" and "null" to get "literals - int" and 
"literals - null"

```ion
// namespacing/grouping (using a symbol annotation)
<namespace_symbol>::[
    {
        name: <string>,
        statement: <string>,
        assert: {
            result: SyntaxSuccess
        }
    },
    ...
]
```
