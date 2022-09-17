package org.partiql.tests.kotlintestrunner

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSymbol
import com.amazon.ion.IonValue
import org.partiql.lang.eval.CompileOptions
import org.partiql.lang.eval.TypingMode
import org.partiql.lang.util.asIonStruct
import org.partiql.lang.util.stringValue

internal fun parseTestCase(testStruct: IonStruct, curNamespace: Namespace): List<TestCase> {
    val testCases = mutableListOf<TestCase>()
    val name = testStruct.get("name").stringValue() ?: error("Expected test case to have field `name`")
    val statement = testStruct.get("statement").stringValue() ?: error("Expected test case to have field `statement`")
    val env = testStruct.get("env") ?: curNamespace.env
    val assertList = when (val assert = testStruct.get("assert") ?: error("Expected test case to have field `assert`")) {
        is IonStruct -> listOf(assert)
        is IonList -> assert.toList()
        else -> error("Expect `assert` field to be an IonStruct or IonList")
    }
    testCases += assertList.map { assertion ->
        val assertionStruct = assertion as IonStruct
        val evalModeList = when (val evalModeIonValue = assertionStruct.get("evalMode")) {
            is IonSymbol -> listOf(evalModeIonValue.stringValue())
            is IonList -> evalModeIonValue.toList().map { it.stringValue() }
            else -> error("evalMode expects IonSymbol or IonList")
        }

        evalModeList.map { evalMode ->
            val compileOption = when (evalMode) {
                "EvalModeError" -> CompileOptions.build { typingMode(TypingMode.LEGACY) }
                "EvalModeCoerce" -> CompileOptions.build { typingMode(TypingMode.PERMISSIVE) }
                else -> error("unsupported eval modes")
            }
            val result = assertionStruct.get("result").stringValue()
            val evalResult: Assertion = when (result) {
                "EvaluationSuccess" -> Assertion.EvaluationSuccess(assertionStruct.get("output"))
                "EvaluationFail" -> Assertion.EvaluationFailure
                else -> error("expected one of EvaluationSuccess or EvaluationFail")
            }

            EvalTestCase(
                name = name,
                statement = statement,
                env = env.asIonStruct(),
                compileOptions = compileOption,
                assertion = evalResult
            )
        }
    }.flatten()
    return testCases
}

internal fun parseNamespace(curNamespace: Namespace, data: IonValue): Namespace {
    return when (data) {
        is IonList -> {
            val newNamespace = Namespace(
                env = curNamespace.env,
                namespaces = mutableListOf(),
                testCases = mutableListOf(),
                equivClasses = mutableListOf()
            )
            data.forEach { d ->
                parseNamespace(newNamespace, d)
            }
            curNamespace.namespaces.add(newNamespace)
            curNamespace
        }
        is IonStruct -> {
            // environment, equivalence class, or test case. add to current namespace
            val annotations = data.typeAnnotations
            when {
                annotations.contains("envs") -> {
                    curNamespace.env = data
                }
                annotations.contains("equiv_class") -> {
                    // equivalence class
                    // TODO: parsing of equivalence classes
                }
                annotations.isEmpty() -> {
                    // test case
                    curNamespace.testCases.addAll(parseTestCase(data, curNamespace))
                }
            }
            curNamespace
        }
        else -> error("Document parsing requires an IonList or IonStruct")
    }
}
