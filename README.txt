
# ORIOL Compiler: A New Programming Language for Kids

## Overview

The **ORIOL** (ORIENTED RANCH, INNOVATIVE AND ORIGINAL LANGUAGE) compiler/language project is a software tool designed to compile programs written in a custom, child-friendly programming language. The language is designed to introduce programming concepts in an approachable and engaging way, using terminology and concepts inspired by life on a farm. It supports basic arithmetic, logical, relational operations, and control flow structures like loops and conditionals.

The project involves the full creation of the compiler from scratch, including lexical analysis, syntax analysis, semantic analysis, intermediate code generation (Three Address Code), and machine code generation targeting the **MIPS architecture**. The file extension for code written in ORIOL is `.farm`.

The development of the project was carried out by the following contributors:

### Contributors:
- **Oriol González** - oriol.gg
- **Alèxia Cabrera** - alexia.cabrera
- **Àlex Ferré** - alex.fl
- **Aaron Fort** - aaron.fort
- **Gemma Yebra** - gemma.yebra

The project was built using **JDK 19.0+** and Maven for dependency management.

## Getting Started

To get started with the ORIOL compiler, you will need to clone this repository and set up the development environment.

### Prerequisites

- **Java Development Kit (JDK) 19.0+**
- **Maven (for managing dependencies)**
- **IntelliJ IDE** (Recommended for ease of use)

### Installation

1. Clone the repository:

```bash
git clone https://github.com/alexferrelopez/ProgrammingLanguages-ORIOL-language-compiler.git
```

2. Install dependencies using Maven:

```bash
mvn clean install
```

3. Once the build is successful, you can compile ORIOL programs (with the `.farm` extension).

### Writing Programs in ORIOL

Here's a simple example of an ORIOL program:

```farm
miau: ranch() {
    oink a is 3.23;
    oink b is 4.5;
    oink c is 0.2;
    oink total is sumRange(a, b, c);
    poop 0;
}
```

This code declares three variables (`a`, `b`, `c`), performs some arithmetic operations, and calls a function `sumRange`. It also demonstrates the use of the `poop` command to return a value.

### Supported Features

- **Variable Declarations**: You can declare variables using a simplified syntax such as `miau a is 3;` for integer types or `oink b is 2.5;` for floating-point values.
- **Conditional Statements**: Use `check` and `else` for conditionals.
- **Loops**: Use `feed` for `for` loops and `breed` for `while` loops.
- **Functions**: Define and call functions with the `miau` keyword.

### Sample Code

```farm
Farmer: "Example 1: Sum of two numbers and return the result"
miau: ranch() {
    miau num1, num2, sum;
    num1 = 5;
    num2 = 10;
    sum = num1 sum num2;
    poop sum;
}

Farmer: "Example 2: Check if a number is even or odd"
miau: ranch() {
    num1 = 5;
    check (num1 mod 2 equals 0) {
        poop 0;
    }
    else {
        poop 1;
    }
}
```

## How It Works

The ORIOL compiler follows several stages to process and compile code:

1. **Lexical Analysis**: The `scanner` processes the source code and divides it into tokens (e.g., keywords, operators, variables).
2. **Syntactic Analysis**: The `parser` checks the structure of the code and builds a syntax tree to ensure the program adheres to grammatical rules.
3. **Semantic Analysis**: The `semantic analyzer` verifies that the program follows correct logical rules (e.g., type correctness, variable scope).
4. **Intermediate Code Generation**: The `TACGenerator` converts the syntax tree into an intermediate representation (Three Address Code).
5. **Machine Code Generation**: The `TACToMIPSConverter` generates MIPS machine code from the intermediate representation, which can be executed on a MIPS architecture.
