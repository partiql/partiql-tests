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
 * Validates all the PartiQL conformance test data in [PARTIQL_TEST_DATA_DIR] conform to the test data schema.
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
    fun testPassParse() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testFailParse() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseError
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testFailSemantic() {
        val testData =
            """
            semantic::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: SemanticError
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testPassParseAssertList() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: ParseOk
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testFailParseAssertList() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: ParseError
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testFailSemanticAssertList() {
        val testData =
            """
            semantic::{
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: SemanticError
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testMultipleParseTests() {
        val testData =
            """
            'some-namespace'::[
                parse::{
                    name: "some string in namespace 1",
                    statement: "some string in namespace 1",
                    assert: [
                        {
                            result: ParseOk
                        }
                    ]
                },
                parse::{
                    name: "some string in namespace 2",
                    statement: "some string in namespace 2",
                    assert: [
                        {
                            result: ParseOk
                        }
                    ]
                }
            ]
            parse::{
                name: "some string outside namespace",
                statement: "some string outside namespace",
                assert: [
                    {
                        result: ParseOk
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testNestedNamespaces() {
        val testData =
            """
            'ns-1'::[
                'ns-2'::[
                    'ns-3'::[
                        'ns-4'::[
                            parse::{
                                name: "some string in namespace 4",
                                statement: "some string in namespace 4",
                                assert: {
                                    result: ParseOk
                                }
                            }
                        ],
                        parse::{
                            name: "some string in namespace 3",
                            statement: "some string in namespace 3",
                            assert: {
                                result: ParseOk
                            }
                        }
                    ],
                    parse::{
                        name: "some string in namespace 2",
                        statement: "some string in namespace 2",
                        assert: {
                            result: ParseOk
                        }
                    }
                ],
                parse::{
                    name: "some string in namespace 1",
                    statement: "some string in namespace 1",
                    assert: {
                        result: ParseOk
                    }
                }
            ]
            parse::{
                name: "some string outside namespace",
                statement: "some string outside namespace",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testAllowOpenContent() {
        // ISL structs types are open content by default. Currently, see no reason to restrict these structs
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseOk
                },
                some_other_field: "some other string"
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testAllowOpenContentInAssert() {
        // ISL structs types are open content by default. Currently, see no reason to restrict these structs
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseOk,
                    some_other_field: "some other string"
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    @Test
    fun testAllowOtherAssertionInAssertList() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: [
                    {
                        result: ParseOk,
                    },
                    {
                        some_other_assertion: "some other string"
                    }
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertNoViolations(dataInIon)
    }

    // Negative tests
    @Test
    fun testWrongAnnotationPassParse() {
        val testData =
            """
            wrong_annotation::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertResultPassParse() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseOOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertResultFailParse() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: SemanticError
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertResultFailSemantic() {
        val testData =
            """
            semantic::{
                name: "some string",
                statement: "some string",
                assert: {
                    result: ParseError
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongNameType() {
        val testData =
            """
            parse::{
                name: not_a_string,
                statement: "some string",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongStatementType() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: not_a_string,
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testWrongAssertType() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string",
                assert: [
                    [
                        {
                            result: ParseOk
                        }
                    ]
                ]
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoNameField() {
        val testData =
            """
            parse::{
                statement: "some string",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoStatementField() {
        val testData =
            """
            parse::{
                name: "some string",
                assert: {
                    result: ParseOk
                }
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testNoAssertField() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string"
            }
            """
        val dataInIon = ion.loader.load(testData)
        assertViolationsOccurred(dataInIon)
    }

    @Test
    fun testMalformedIon() {
        val testData =
            """
            parse::{
                name: "some string",
                statement: "some string"
                assert: {
                    result: ParseOk
                }
            """
        assertThrows<IonException> {
            ion.loader.load(testData)
        }
    }
}
