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
    type: { one_of: [PassParseTestCase, FailParseTestCase, FailSemanticTestCase] }
}

type::{
    name: PassParseTestCase,
    type: struct,
    annotations: required::[parse],
    fields: {
        name: { type: string, occurs: required },
        statement: { type: string, occurs: required },
        assert: {
            type: {
                one_of: [
                    ParseOkAssertion,
                    { type: list, contains: [{result: ParseOk}] }
                ]
            },
            occurs: required
        }
    }
}

type::{
    name: FailParseTestCase,
    type: struct,
    annotations: required::[parse],
    fields: {
        name: { type: string, occurs: required },
        statement: { type: string, occurs: required },
        assert: {
            type: {
                one_of: [
                    ParseErrorAssertion,
                    { type: list, contains: [{result: ParseError}] }
                ]
            },
            occurs: required
        }
    }
}

type::{
    name: FailSemanticTestCase,
    type: struct,
    annotations: required::[semantic],
    fields: {
        name: { type: string, occurs: required },
        statement: { type: string, occurs: required },
        assert: {
            type: {
                one_of: [
                    SemanticErrorAssertion,
                    { type: list, contains: [{result: SemanticError}] }
                ]
            },
            occurs: required
        }
    }
}

type::{
    name: ParseOkAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [ParseOk], occurs: required }
    }
}

type::{
    name: ParseErrorAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [ParseError], occurs: required }
    }
}

type::{
    name: SemanticErrorAssertion,
    type: struct,
    fields: {
        result: { type: symbol, valid_values: [SemanticError], occurs: required }
    }
}

schema_footer::{
}
