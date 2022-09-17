package org.partiql.tests.kotlintestrunner

import com.amazon.ion.IonList
import com.amazon.ion.IonNull
import com.amazon.ion.IonSequence
import com.amazon.ion.IonSexp
import com.amazon.ion.IonStruct
import com.amazon.ion.IonSystem
import com.amazon.ion.IonValue
import org.partiql.lang.eval.ExprValue
import org.partiql.lang.eval.ExprValueFactory
import org.partiql.lang.eval.ExprValueType
import org.partiql.lang.eval.StructOrdering
import org.partiql.lang.eval.name
import org.partiql.lang.eval.namedValue
import org.partiql.lang.eval.stringValue

internal fun IonValue.toExprValue(exprValueFactory: ExprValueFactory): ExprValue {
    // Need to create a different IonValue to ExprValue conversion function because the default provided by
    // `ExprValueFactory`'s [newFromIonValue] relies on a different encoding of PartiQL-specific types than the
    // conformance tests (e.g. `ExprValueFactory` uses $partiql_bag rather than $bag)
    val elem = this
    val annotations = elem.typeAnnotations
    return when {
        (elem is IonList) && annotations.contains("\$bag") -> {
            val elemsAsExprValues = elem.map {
                it.toExprValue(exprValueFactory)
            }
            exprValueFactory.newBag(elemsAsExprValues)
        }
        elem is IonNull && elem.hasTypeAnnotation("\$missing") -> exprValueFactory.missingValue
        // TODO: other PartiQL types not in Ion
        elem is IonList -> exprValueFactory.newList(elem.map { it.toExprValue(exprValueFactory) })
        elem is IonSexp -> exprValueFactory.newSexp(elem.map { it.toExprValue(exprValueFactory) })
        elem is IonStruct -> {
            exprValueFactory.newStruct(elem.map { it.toExprValue(exprValueFactory).namedValue(exprValueFactory.newString(it.fieldName)) }, StructOrdering.UNORDERED)
        }
        else -> exprValueFactory.newFromIonValue(elem)
    }
}

internal fun ExprValue.toIonValue(ion: IonSystem): IonValue {
    fun <S : IonSequence> ExprValue.foldToIonSequence(initial: S): S =
        this.fold(initial) { seq, el -> seq.apply { add(el.toIonValue(ion)) } }

    return when (this.type) {
        ExprValueType.MISSING -> ion.singleValue("\$missing::null").clone()
        ExprValueType.NULL,
        ExprValueType.BOOL,
        ExprValueType.INT,
        ExprValueType.FLOAT,
        ExprValueType.DECIMAL,
        ExprValueType.DATE,
        ExprValueType.TIME,
        ExprValueType.TIMESTAMP,
        ExprValueType.SYMBOL,
        ExprValueType.STRING,
        ExprValueType.CLOB,
        ExprValueType.BLOB -> this.ionValue.clone()
        ExprValueType.LIST -> this.foldToIonSequence(ion.newEmptyList())
        ExprValueType.SEXP -> this.foldToIonSequence(ion.newEmptySexp())
        ExprValueType.STRUCT -> this.fold(ion.newEmptyStruct()) { struct, el ->
            struct.apply { add(el.name!!.stringValue(), el.toIonValue(ion)) }
        }
        ExprValueType.BAG -> {
            val bag = ion.newEmptyList().apply { addTypeAnnotation("\$bag") }
            this.foldToIonSequence(bag)
        }
    }
}
