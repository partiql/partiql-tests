The following lists out the tests from `partiql-lang-kotlin`'s evaluation tests that are not part of `partiql-tests`:

Tests excluded:
- EvaluatorTestSuite.kt -- parameters: https://github.com/partiql/partiql-lang-kotlin/blob/main/lang/test/org/partiql/lang/eval/EvaluatorTestSuite.kt#L1266-L1272
```ion
envs::{
  parameterTestTable:[
    {
      bar:"baz"
    },
    {
      bar:"blargh"
    }
  ]
}

misc::[
  {
    name:"parameters",
    statement:"SELECT ? as b1, f.bar FROM parameterTestTable f WHERE f.bar = ?",
    assert:{
      result:EvaluationSuccess,
      output:$bag::[
        {
          b1:"spam",
          bar:"baz"
        }
      ]
    }
  }
]
```
- EvaluatorTestSuite.kt -- semicolon: https://github.com/partiql/partiql-lang-kotlin/blob/main/lang/test/org/partiql/lang/eval/EvaluatorTestSuite.kt#L1393-L1394
```ion
uncategorized::[  
  {
    name:"semicolonAtEndOfLiteral",
    statement:"1;",
    assert:{
      result:EvaluationSuccess,
      output:1
    }
  },
  {// to decide
    name:"semicolonAtEndOfExpression",
    statement:"SELECT * FROM <<1>>;",
    assert:{
      result:EvaluationSuccess,
      output:$bag::[
        {
          _1:1
        }
      ]
    }
  }
]
```
