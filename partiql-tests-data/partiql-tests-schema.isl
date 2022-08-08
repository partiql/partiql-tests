schema_header::{
}

type::{
    name: PartiQLTestDocument,
    type: document,
    element: { one_of: [TestCase, Namespace, Environments, EquivalenceClass] },
    content: closed
}

type::{
    name: Namespace,
    type: list,
    element: { one_of: [TestCase, Namespace] },
    content: closed
}

type::{
    name: Environments,
    type: struct,
    annotations: required::[envs],
    content: closed
}

type::{
    name: EquivalenceClass,
    type: struct,
    annotations: required::[equiv_class],
    content: closed,
    fields: {
        id: symbol,
        statements: { type: list, element: string }
    }
}

type::{
    name: TestCase,
    type: struct,
    fields: {
        name: { type: string, occurs: required },
        statement: {
            type: {
                one_of: [
                    string,
                    symbol      // to reference an equiv_class identifier
                ]
            },
            occurs: required
        },
        env: { type: struct, occurs: optional },
        options: { type: struct, occurs: optional },
        assert: {
            type: {
                one_of: [
                    Assertion,
                    { type: list, element: Assertion }
                ]
            },
            occurs: required
        },
    },
    content: closed
}

type::{
    name: Assertion,
    type: {
        one_of: [
            SyntaxSuccessAssertion,
            SyntaxFailAssertion,
            StaticAnalysisFailAssertion,
            EvaluationSuccessAssertion,
            EvaluationFailAssertion
        ]
    }
}

type::{
    name: SyntaxSuccessAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [SyntaxSuccess], occurs: required }
    },
    content: closed
}

type::{
    name: SyntaxFailAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [SyntaxFail], occurs: required }
    },
    content: closed
}

type::{
    name: StaticAnalysisFailAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [StaticAnalysisFail], occurs: required }
    },
    content: closed
}

type::{
    name: EvaluationSuccessAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [EvaluationSuccess], occurs: required },
        output: { occurs: required }
    },
    content: closed
}

type::{
    name: EvaluationFailAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [EvaluationFail], occurs: required }
    },
    content: closed
}

schema_footer::{
}
