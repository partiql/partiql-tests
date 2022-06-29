schema_header::{
}

type::{
    name: PartiQLTestDocument,
    type: document,
    element: { one_of: [TestCase, Namespace] }
}

type::{
    name: Namespace,
    type: list,
    element: { one_of: [TestCase, Namespace] }
}

type::{
    name: TestCase,
    type: struct,
    fields: {
        name: { type: string, occurs: required },
        statement: { type: string, occurs: required }, // partiql-tests#17 may change this definition to a list
        assert: {
            type: {
                one_of: [
                    SyntaxSuccessAssertion,
                    SyntaxFailAssertion,
                    StaticAnalysisFail,
                    { type: list, element: Assertion }
                ]
            },
            occurs: required
        }
    }
}

type::{
    name: Assertion,
    type: {
        one_of: [
            SyntaxSuccessAssertion,
            SyntaxFailAssertion,
            StaticAnalysisFail
        ]
    }
}

type::{
    name: SyntaxSuccessAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [SyntaxSuccess], occurs: required }
    }
}

type::{
    name: SyntaxFailAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [SyntaxFail], occurs: required }
    }
}

type::{
    name: StaticAnalysisFail,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [StaticAnalysisFail], occurs: required }
    }
}

schema_footer::{
}
