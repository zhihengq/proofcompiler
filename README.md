# CSE 311 Proof Compiler

CSE 311 Proof Compiler is a compiler for a formal proof DSL for propositional logic.
The compiler validates a formal proof and compiles it into a target code, such
as LaTeX.

- Demo: [Repo](demo.mkv) or [Google Drive](https://drive.google.com/file/d/1DQPxqCrzDWdvgOTwbPtnukkm67Ff8eHP)

## Building the project

This is a Java project using Gradle.
The following command should be enough.

```bash
./gradlew build
```

The packaged application should be in `build/distributions/ProofCompiler.{tar, zip}`.

The project is tested on Java 13.
Support for older versions of Java is unknown.

## The proof language

A proof source code has two parts: declarations and the proof body, separated
by the keyword `proof.` (with the dot).

Comments can be either single line using `//`, `#`, or multi-line using `/* */`.
For example:

```text
/**
 * This is a multi
 * line comment.
 */
// This is a comment
Let p be a proposition  # This is also a comment
```

### Proposition expression

The syntax for propositions in the proof source code is similar to LaTeX.

Atomic propositions are represented by identifiers such as `p`, `_my_id_1`.

Logical connectives are represented by their Unicode characters (`¬`, `∧`,
`∨`, `⊕`, `→`, `↔`) or their LaTeX command (`\neg`, `\land`, `\wedge`,
`\lor`, `\vee`, `\oplus`, `\rightarrow`, `\to`, `\leftrightarrow`).

[Precedences of logical connectives](https://en.wikipedia.org/wiki/Logical_connective#Order_of_precedence)
generally follows the precedences in C-like programming languages.
The precedences, in strictly decreasing order, are ¬, ∧, ⊕, ∨, →, ↔.

### Declaration

All atomic propositions should be declared, using the syntax:

```text
Let <identifier> be a proposition
```

For example:

```text
Let p be a proposition
Let _my_id_1 be a proposition
```

All given conditions should be declared, using the syntax:

```text
Given <expression>
```

For example:

```text
Given \neg p \land (q \to r)
Given ¬p ∧ (q→r)
```

### Proof body

Proof bodies are a list of lines.
Each line contains a line number, a proposition, and the rule used to derive
that proposition, potentially with some line references.
Indentation does not change the semantics of the proof source code, but is a
good style when writing formal proofs.

Example:

```text
1. p [Given]
    2.1. q [Assumption]
    2.2. p \land q [Intro \land: 1, 2.1]
2. q \to (p \land q) [Direct Proof Rule]
```

The entire set of rules follows the
[CSE 311 References](https://courses.cs.washington.edu/courses/cse311/17au/documents/References.pdf).

## The compiler CLI

The application can be launched by its startup script (bundled in the tar or
zip package), or by Gradle.
Each argument specifies a path to a proof source code file.
For each input file `<name>.proof`, the output file `<name>.tex` will be
generated.
If the input file does not have a `.proof` extension, `.tex` will be appended
to the file name.

Example:

```bash
# This will generate output in src/test/resources/valid/17au-3-1.tex
./gradlew run --args "src/test/resources/valid/17au-3-1.proof"
```

## A complete example

The following proof is from CSE 311 17au homework 3.1, written in the proof
language:

```text
// example.proof
let p be a proposition
let q be a proposition
let r be a proposition
let s be a proposition
let t be a proposition

Given (p ∨ r) → (q → s)
Given t
Given (r ∧ t) → ¬s

proof.
        1.1.  r                                     [ Assumption ]
        1.2.  p \lor r                              [ Intro \lor: 1.1 ]
        1.3.  (p \lor r) \to (q \to s)              [ Given ]
        1.4.  (p \lor r) \to (\neg s \to \neg q)    [ Contrapositive: 1.3 ]
        1.5.  \neg s \to \neg q                     [ Modus Ponens: 1.2, 1.4 ]
        1.6.  t                                     [ Given ]
        1.7.  r \land t                             [ Intro \land: 1.6, 1.1 ]
        1.8.  (r \land t) \to \neg s                [ Given ]
        1.9.  \neg s                                [ Modus Ponens: 1.8, 1.7 ]
        1.10. \neg q                                [ Modus Ponens: 1.9, 1.5 ]
    1. r \to \neg q                                 [ Direct Proof Rule ]
```

The proof compiler validates it and compiles it into the following LaTeX
target code:

```latex
% example.tex
\begin{proof} \hfill\par
    \begin{tabular}{rll}
        \multicolumn{3}{l}{\quad \begin{tabular}{rll}
            1.1. & \( r \) & [ Assumption ] \\
            1.2. & \( p \lor r \) & [ Intro \(\lor\): 1.1 ] \\
            1.3. & \( p \lor r \to (q \to s) \) & [ Given ] \\
            1.4. & \( p \lor r \to (\neg s \to \neg q) \) & [ Contrapositive: 1.3 ] \\
            1.5. & \( \neg s \to \neg q \) & [ Modus Ponens: 1.2, 1.4 ] \\
            1.6. & \( t \) & [ Given ] \\
            1.7. & \( r \land t \) & [ Intro \(\land\): 1.1, 1.6 ] \\
            1.8. & \( r \land t \to \neg s \) & [ Given ] \\
            1.9. & \( \neg s \) & [ Modus Ponens: 1.7, 1.8 ] \\
            1.10. & \( \neg q \) & [ Modus Ponens: 1.5, 1.9 ] \\
        \end{tabular} } \\
        1. & \( r \to \neg q \) & [ Direct Proof Rule ] \\
    \end{tabular} \par
\end{proof}
```

The LaTeX code depends on LaTeX packages `amsmath` and `amsthm`.
