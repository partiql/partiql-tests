## PartiQL Test Data

The `partiql-test-data` folder contains language-neutral test data used to check conformance to the [PartiQL 
specification](https://partiql.org/assets/PartiQL-Specification.pdf).

Consumers of this repository must assume additional nested subfolders may be added, and should therefore recurse down 
from the desired folder, if appropriate.

The test data is partitioned into the following directories:

- `pass` - all tests in this directory should have a valid PartiQL statement that does not result in an error
- `fail` - all tests in this directory should have a PartiQL statement that results in an error when run

Subdirectories:
- `pass/parser` - statements that can parse without error
- `fail/parser` - statements that give an error when parsed
- `pass/eval` - statements that can be evaluated successfully and return the same result as the expected result
- `fail/eval` - statements that throw an error during evaluation

## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

