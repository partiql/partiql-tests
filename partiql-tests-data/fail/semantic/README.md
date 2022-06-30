Tests in this directory should have statements that error at some stage of semantic passes or type-checking. That is,
at some stage between parsing and evaluation. It's up to the PartiQL implementation to decide at what stage tests in
this directory will give an error. For some implementations, these tests may fail in the parse stage if they strictly 
implement SQL standard's EBNF, but some implementations may fail at a different stage.