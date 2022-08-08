/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *       http://aws.amazon.com/apache2.0/
 *
 *  or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 *  language governing permissions and limitations under the License.
 */

package org.partiql.tests.validator

import com.amazon.ion.IonException
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaSystemBuilder
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import java.nio.file.Paths
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val PARTIQL_TEST_DATA_DIR = System.getenv("PARTIQL_TESTS_DATA")

/**
 * Checks all the PartiQL conformance test data in [PARTIQL_TEST_DATA_DIR] conforms to the test data schema.
 */
class PartiQLTestDataValidator {

    private val ion = IonSystemBuilder.standard().build()
    private val iss = IonSchemaSystemBuilder.standard().build()

    private var schema =
        iss.newSchema(Paths.get(PARTIQL_TEST_DATA_DIR, "partiql-tests-schema.isl").toFile().readText()).getType("PartiQLTestDocument")

    private fun assertNoViolations(dataInIon: IonValue) {
        val violations = schema?.validate(dataInIon)
        assertNotNull(violations)
        assertTrue(
            violations.violations.isEmpty(),
            "Violations occurred when validating the test data to the schema. See violations:\n$violations"
        )
    }

    private fun assertViolationsOccurred(dataInIon: IonValue) {
        val violations = schema?.validate(dataInIon)
        assertNotNull(violations)
        assertTrue(violations.violations.isNotEmpty(), "Violations were expected but no violations occurred")
    }

    // Positive tests
    @ParameterizedTest
    @ArgumentsSource(PartiQLTestDataCases::class)
    fun validatePartiQLTestData(tc: File) {
        val dataInIon = ion.loader.load(tc)
        assertNoViolations(dataInIon)
    }

    class PartiQLTestDataCases : ArgumentsProviderBase() {
        override fun getParameters(): List<Any> {
            return File(PARTIQL_TEST_DATA_DIR).walk()
                .filter { it.isFile }
                .filter { it.path.endsWith(".ion") }
                .toList()
        }
    }

    @Test
    fun testSyntaxSuccessSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testSyntaxFailSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: SyntaxFail
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testStaticAnalysisFailSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: StaticAnalysisFail
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testSyntaxSuccessAssertListSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: SyntaxSuccess
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testSyntaxFailAssertListSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: SyntaxFail
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testStaticAnalysisFailAssertListSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: StaticAnalysisFail
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testMultipleSyntaxSuccessSchemas() {
        val testData =
            """
            'some-namespace'::[
                {
                    name: "some string in namespace 1",
                    statement: "some string in namespace 1",
                    assert: [
                        {
                            result: SyntaxSuccess
                        }
                    ]
                },
                {
                    name: "some string in namespace 2",
                    statement: "some string in namespace 2",
                    assert: [
                        {
                            result: SyntaxSuccess
                        }
                    ]
                }
            ]
            {
                name: "some string outside namespace",
                statement: "some string outside namespace",
                assert: [
                    {
                        result: SyntaxSuccess
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testNestedNamespacesSchema() {
        val testData =
            """
            'ns-1'::[
                'ns-2'::[
                    'ns-3'::[
                        'ns-4'::[
                            {
                                name: "some string in namespace 4",
                                statement: "some string in namespace 4",
                                assert: {
                                    result: SyntaxSuccess
                                }
                            }
                        ],
                        {
                            name: "some string in namespace 3",
                            statement: "some string in namespace 3",
                            assert: {
                                result: SyntaxSuccess
                            }
                        }
                    ],
                    {
                        name: "some string in namespace 2",
                        statement: "some string in namespace 2",
                        assert: {
                            result: SyntaxSuccess
                        }
                    }
                ],
                {
                    name: "some string in namespace 1",
                    statement: "some string in namespace 1",
                    assert: {
                        result: SyntaxSuccess
                    }
                }
            ]
            {
                name: "some string outside namespace",
                statement: "some string outside namespace",
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testMultipleAssertionsSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: SyntaxSuccess
                    },
                    {
                        result: StaticAnalysisFail
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testGlobalEnvsSchema() {
        val testData =
            """
            envs::{
                table1: [{a: 1}, {a: 2}, {a: 3}],
                table2: {some_key: some_value}
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testEvaluationSuccessSchema() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationSuccess,
                    output: some_output
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testEvaluationFailSchema() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationFail
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testEquivalenceTestSchema() {
        val testData =
            """
            equiv_class::{
                id: some_identifier,
                statements: [
                    "some statement",
                    "some equivalent statement 1",
                    "some equivalent statement 2"
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testEvaluationSuccessSchemaWithEquivalence() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationSuccess,
                    output: some_output,
                    equiv_class: some_equiv_class
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testEvaluationSuccessSchemaWithOptionalFields() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                env: { some_key: some_value },
                options: { some_option_key: some_option_value },
                assert: {
                    result: EvaluationSuccess,
                    output: some_output
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    // Negative tests
    @Test
    fun testNonSpecifiedFieldInStructSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: SyntaxSuccess
                },
                some_other_field: "some other string"
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testOtherNonSpecifiedFieldInAssertStructSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: SyntaxSuccess,
                    some_other_field: "some other string"
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testDisallowUndefinedAssertionsInSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: SyntaxSuccess,
                    },
                    {
                        some_undefined_assertion: "some other string"
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertResultSyntaxSuccessSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: {
                    result: SyntaxSucesssssss
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongNameTypeInSchema() {
        val testData =
            """
            {
                name: not_a_string,
                statement: "some string",
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongStatementTypeInSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: not_a_string,
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertTypeInSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: [
                    [
                        {
                            result: SyntaxSuccess
                        }
                    ]
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoNameFieldInSchema() {
        val testData =
            """
            {
                statement: "some string",
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoStatementFieldInSchema() {
        val testData =
            """
            {
                name: "some string",
                assert: {
                    result: SyntaxSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoAssertFieldInSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string"
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoResultInAssertFieldSchema() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string",
                assert: { }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testMalformedIon() {
        val testData =
            """
            {
                name: "some string",
                statement: "some string"
                assert: {
                    result: SyntaxSuccess
                }
            """
        assertThrows<IonException> {
            ion.loader.load(testData)
        }
    }

    @Test
    fun testNoOutputInEvaluationSuccessSchema() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationSuccess
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoOutputInEvaluationSuccessSchemaWithEquivalenceClass() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationSuccess,
                    equiv_class: some_equivalence_class
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testEvaluationFailSchemaWithUnexpectedOutput() {
        val testData =
            """
            {
                name: "some name",
                statement: "some statement",
                assert: {
                    result: EvaluationFail,
                    output: some_unexpected_output
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testIncorrectEnvAnnotationInSchema() {
        val testData =
            """
            envssss::{
                ints: [1, 2, 3],
                simple_struct: {
                    'a': 1,
                    'b': 2
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testIncorrectEquivalenceClassAnnotationInSchema() {
        val testData =
            """
            equiv_clas::{
                id: some_id,
                statements: [
                    "some statement",
                    "some equivalent statement"
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }
}
