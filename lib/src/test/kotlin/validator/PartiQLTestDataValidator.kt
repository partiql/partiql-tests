package validator

import com.amazon.ion.IonException
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import com.amazon.ionschema.IonSchemaSystemBuilder
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ArgumentsSource
import java.io.File
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private const val PARTIQL_TEST_DATA_DIR = "../partiql-test-data"

/**
 * Checks all the PartiQL conformance test data in [PARTIQL_TEST_DATA_DIR] conforms to the test data schema.
 */
class PartiQLTestDataValidator {
    private val ion = IonSystemBuilder.standard().build()
    private val iss = IonSchemaSystemBuilder.standard().build()
    private val schema = iss.newSchema(File("$PARTIQL_TEST_DATA_DIR/partiql_test_schema.isl").readText()).getType("PartiQLTestDocument")

    private fun assertNoViolations(dataInIon: IonValue) {
        val violations = schema?.validate(dataInIon)
        assertNotNull(violations)
        assertTrue(violations.violations.isEmpty(), "Violations occurred when validating the test data to the schema. See violations:\n$violations")
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
    fun testSyntaxSuccess() {
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
    fun testSyntaxFail() {
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
    fun testStaticAnalysisFail() {
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
    fun testSyntaxSuccessAssertList() {
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
    fun testSyntaxFailAssertList() {
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
    fun testStaticAnalysisFailAssertList() {
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
    fun testMultipleSyntaxTests() {
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
    fun testNestedNamespaces() {
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
    fun testAllowOpenContent() {
        // ISL structs types are open content by default. Currently, see no reason to restrict these structs
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
        assertNoViolations(dataInIon)
    }

    @Test
    fun testAllowOpenContentInAssert() {
        // ISL structs types are open content by default. Currently, see no reason to restrict these structs
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
        assertNoViolations(dataInIon)
    }

    @Test
    fun testMultipleAssertions() {
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

    // Negative tests
    @Test
    fun testDisallowUndefinedAssertions() {
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
    fun testWrongAssertResultSyntaxSuccess() {
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
    fun testWrongNameType() {
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
    fun testWrongStatementType() {
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
    fun testWrongAssertType() {
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
    fun testNoNameField() {
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
    fun testNoStatementField() {
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
    fun testNoAssertField() {
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
    fun testNoResultInAssertField() {
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
}
