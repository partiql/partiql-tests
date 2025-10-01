package org.partiql.tests.validator

import com.amazon.ion.IonList
import com.amazon.ion.IonStruct
import com.amazon.ion.IonText
import com.amazon.ion.IonValue
import com.amazon.ion.system.IonSystemBuilder
import java.io.File
import kotlin.collections.forEach
import kotlin.collections.plus

object TestLoader {
    private val ion = IonSystemBuilder.standard().build()

    fun load(path: String): List<TestCase> {
        val allFiles = File(path).walk()
            .filter { it.isFile }
            .filter { it.path.endsWith(".ion") }
            .toList()
        val filesAsNamespaces = allFiles.map { file ->
            parseTestFile(file)
        }

        val allTestCases = filesAsNamespaces.flatMap { ns ->
            allTestsFromNamespace(ns)
        }
        return allTestCases
    }

    private fun parseTestFile(file: File): Namespace {
        val loadedData = file.readText()
        val dataInIon = ion.loader.load(loadedData)
        val emptyNamespace = Namespace(
            name = file.name,
            namespaces = mutableListOf(),
            testCases = mutableListOf(),
        )
        dataInIon.forEach { d ->
            parseNamespace(emptyNamespace, d)
        }
        return emptyNamespace
    }

    private fun allTestsFromNamespace(ns: Namespace): List<TestCase> {
        return ns.testCases + ns.namespaces.fold(listOf()) { acc, subns ->
            acc + allTestsFromNamespace(subns)
        }
    }

    /**
     * Parses [data] with the [curNamespace] into a new [Namespace].
     */
    internal fun parseNamespace(curNamespace: Namespace, data: IonValue): Namespace {
        return when (data) {
            is IonList -> {
                val newNamespace = Namespace(
                    name = getNameSpaceName(data, curNamespace),
                    namespaces = mutableListOf(),
                    testCases = mutableListOf()
                )
                data.forEach { d ->
                    parseNamespace(newNamespace, d)
                }
                curNamespace.namespaces.add(newNamespace)
                curNamespace
            }
            is IonStruct -> {
                // environment, equivalence class, or test case. add to current namespace
                if (data.typeAnnotations.isEmpty()) {
                    // test case
                    curNamespace.testCases += parseTestCase(data, curNamespace)
                }

                curNamespace
            }
            else -> error("Document parsing requires an IonList or IonStruct")
        }
    }

    private fun parseTestCase(testStruct: IonStruct, curNamespace: Namespace): TestCase {
        val name = (testStruct.get("name") as IonText).stringValue()
        return TestCase(curNamespace.name, name)
    }

    private fun getNameSpaceName(ionList: IonList, prev: Namespace): String {
        return prev.name + if (ionList.typeAnnotations.any()) "::${ionList.typeAnnotations[0]}" else ""
    }
}

data class TestCase(
    val source: String, // FileName::namespace
    val name: String
)

data class Namespace(
    val name: String,
    val namespaces: MutableList<Namespace>,
    val testCases: MutableList<TestCase>
)
