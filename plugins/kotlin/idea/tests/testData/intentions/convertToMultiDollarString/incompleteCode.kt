// IS_APPLICABLE: true
// COMPILER_ARGUMENTS: -Xmulti-dollar-interpolation
// K2_ERROR: Syntax error: Incorrect template argument.
// K2_AFTER_ERROR: Syntax error: Incorrect template argument.

fun test() {
    "foo${}<caret>"
}