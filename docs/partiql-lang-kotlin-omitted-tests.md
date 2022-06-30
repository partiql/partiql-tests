The following lists out the tests from `partiql-lang-kotlin`'s parse tests that are not part of `partiql-tests`:

Tests excluded:
- Custom type cast
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L631-L644
  - [SqlParserCastTests.kt](https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserCastTests.kt)
  - [SqlParserCustomTypeCatalogTests.kt](https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserCustomTypeCatalogTests.kt) 
- ORDER BY tests testing defaults for sort spec and null spec
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L1793-L1888
  - dependent on when parsed ast checking is added or up to the implementation to test via correspondence (see comment here https://github.com/partiql/partiql-tests/pull/11/files#r865515909)
    - alternatively, this may be better tested in the evaluator
- Many nested NOT regression test
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L4239-L4271
  - performance test shouldn't be included in parse tests
- LET clause parsing (not part of spec formally yet)
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L4066-L4128
  - `expectedAsForLet` https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L431-L443
  - `expectedIdentForAliasLet` https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L473-L485
- `idIsNotStringLiteral` -- logic tested elsewhere
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L768-L780
- DML, DDL tests (DML, DDL not defined in spec yet)
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L2129-L3855
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L3857-L3939
  - `unexpectedKeywordUpdateInSelectList` - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L132-L142
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1291-L1457
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1980-L2662
- Stored procedure calls (EXEC) not in spec yet
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L2760-L2846
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L4179-L4237
- semicolon semantic tests
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1860-L1886
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L4025-L4064
- TIME constructor with additional arguments
  - AM/PM: https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserDateTimeTests.kt#L420-L437
  - specifying timezone abbreviations (e.g. PST): https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserDateTimeTests.kt#L685-L693

Unsure if should be included:
- `expectedCastAsIntArity`, `expectedCastAsRealArity` -- type supplied with parameter; not sure is parse error
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L172-L204
- `expectedUnsupportedLiteralsGroupBy` -- not part of the PartiQL spec; SQL92 says group by must use a column name
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L417-L429
  - also for `groupByOrdinal`, `groupByOutOfBoundsOrdinal`, `groupByBadOrdinal`, `groupByStringConstantOrdinal` https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L953-L1007
- `idIsNotGroupMissing` -- type surrounded by parens
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L782-L794
- `castToVarCharToTooBigLength`, `castToDecimalToTooBigLength_1`, `castToDecimalToTooBigLength_2`, `castToNumericToTooBigLength_1`, `castToNumericToTooBigLength_2` -- not quite a parse error; more so a semantic error
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L220-L288
  - Similarly for `castNegativeArg`
  https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L883-L895
- `offsetBeforeLimit` -- not part of spec; postgresql allows; kotlin impl+mysql+sqlite don't allow; included as test for now
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1221-L1233
- date_add/date_diff tests - date_add and date_diff aren't part of specs
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserTest.kt#L957-L1036
  - https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1743-L1858
- `countExpressionStar`
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1966-L1978
- `*` and path `*` with other select list items
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1903-L1936
- `callTrimSpecificationMissingFrom` -- PostgreSQL supports; unsure of utility of creating this fail test
  - https://github.com/~~partiql~~/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L683-L696
  - More details provided by Josh in this comment on the ambiguity https://github.com/partiql/partiql-tests/pull/11/files#r865505304
Repeated tests (tests repeated at some other place):
- `expectedExpectedTypeName`
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L78-L90
- `likeWrongOrderOfArgs`, `likeMissingEscapeValue`, `likeMissingPattern`
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1487-L1527
- `nullIsNullIonLiteral`, `idIsStringLiteral`
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1585-L1611
- `selectWithFromAtAndAs`
https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/errors/ParserErrorsTest.kt#L1627-L1639

To be included:
- Operator precedence tests from [SqlParserPrecedenceTest.kt](https://github.com/partiql/partiql-lang-kotlin/blob/34625c68dbcbaf7b8ae60df7a4cf65c60b2a3b79/lang/test/org/partiql/lang/syntax/SqlParserPrecedenceTest.kt)