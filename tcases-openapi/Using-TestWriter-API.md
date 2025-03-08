## Overview ##

When Tcases for OpenAPI generates an executable test for your API, the result is one or more source code files that implement
the test program. You can immediately build this source code and run the test.  To do this, Tcases for OpenAPI has to deal with
all of the issues of constructing an API test program.

* **Which language?** Which programming language are you using to write the test? Java? JavaScript? Some more exotic?

* **Which test framework?** Most test programs are built around a _test framework_ that is used to run test cases and report
  results.  For test developers, such frameworks generally define how to designate individual tests and how to control their
  execution.  Typically, they are general-purpose, equally applicable for all kinds of testing. Depending on the programming
  language, there are usually several test frameworks to choose from.

* **How will tests interact with an API server?** Any API test demands a set of _request execution interfaces_ of some kind.
  These are the interfaces used to construct a request message, deliver it to an API server, and collect the resulting response.
  Some test frameworks come with built-in request execution interfaces. In other cases, many different choices may be available.

Tcases for OpenAPI generates executable tests using the TestWriter API, which brings together the following three elements:

  * A [request test definition](Request-Test-Definition.md) that defines the inputs for request test cases (and that is created
    automatically from an OpenAPI definition via [input resolution](#get-actual-input-values)),
  
  * a [**TestWriter**](http://www.cornutum.org/tcases/docs/api/org/cornutum/tcases/openapi/testwriter/TestWriter.html) that is
    responsible for producing the code required for a specific test framework,

  * and a [**TestCaseWriter**](http://www.cornutum.org/tcases/docs/api/org/cornutum/tcases/openapi/testwriter/TestCaseWriter.html)
    that is responsible for producing the code that uses a specific request execution interface to submit
    API requests and evaluate API responses.

If you want to generate a Java test program, it's likely that Tcases for OpenAPI already has everything you need. Tcases for
OpenAPI has built-in support for the two most common Java test frameworks (JUnit and TestNG) and for a powerful request
execution interface (REST Assured).  But what if you need something different? In that case, you can use the TestWriter API to
add extensions to Tcases for OpenAPI that produce the results you want.

## Get Started ##

#### Java? Nope. I need tests to be written in another language ####

#### Java tests are fine, but I don't use JUnit or TestNG ####

#### I use JUnit (or TestNG), but I want to use a different request execution interface ####

#### Since I'm adding extensions to Tcases, do I have to create my own fork? ####

#### How can I test my own TestWriter implementation? ####

#### Can I get the `tcases-api-test` command to use my own TestWriter? ####

#### Is there a different way to generate test input values? ####


## The TestWriter Lifecycle ##

The work of a TestWriter is carried out via the **TestWriter lifecyle**. This lifecycle consists of a series of steps that
incrementally produce each part of a complete test program.

### Overview ###

The TestWriter lifecycle is an example of the [Template pattern](https://en.wikipedia.org/wiki/Template_method_pattern).  A
TestWriter is implemented by a subclass of the abstract `TestWriter` class, which provides the template. Each step of the
lifecycle is then implemented by a `TestWriter` method.  A lifecycle method may be abstract, in which case every TestWriter
implementation must provide its own implementation. Or, in other cases, a lifecycle method may act as a "hook" that a new
TestWriter subclass can choose to override, either to replace the superclass method or to add new actions before or after
invoking the superclass method.

Here is an overview of the TestWriter lifecycle. Each abstract lifecyle method is indicated by :small_blue_diamond:.

| Lifecycle Method  |     |                   | Purpose |
| ---:              | --- | :---              | --- |
| prepareTestCases  |     |                   | Setup TestWriter for specified test cases |
| writeProlog       | :arrow_heading_down:  |                   | Write parts that precede test cases |
|                   | :small_blue_diamond:  | writeOpening      | Write the opening part of the test program |
|                   | :small_blue_diamond:  | writeDependencies | Write framework dependencies |
|                   | :small_blue_diamond:  | writeDeclarations | Write declarations of framework components |
| writeTestCases    | :arrow_heading_down:  |                   | Write all test cases |
|                   |     | writeTestCase     | Write a single test case |
| writeEpilog       | :arrow_heading_down:  |                   | Write parts that follow test cases |
|                   | :small_blue_diamond:  | writeClosing      | Write the closing part of the test |

### Delegation to TestCaseWriter ###

A TestWriter delegates part of its job to a TestCaseWriter, which is responsible for producing the code that executes a test case.
Consequently, the TestWriter lifecyle also orchestrates the interplay between TestWriter and TestCaseWriter responsibilities.
A TestCaseWriter must be an implementation of the `TestCaseWriter` interface and must provide an implementation for each of its
lifecycle methods.

Here is a overview of how a TestWriter delegates responsibilities to a TestCaseWriter.
Each TestCaseWriter method is indicated by :small_orange_diamond:.

| Lifecycle Method  |     |                   |     |               | Purpose |
| ---:              | --- | :---              | --- | ---           | --- |
| prepareTestCases  | :arrow_heading_down:  |                   |     |               | Setup TestWriter for specified test cases |
|                   | :small_orange_diamond:  | prepareTestCases  |     |               | Setup TestCaseWriter for specified test cases |
| writeProlog       | :arrow_heading_down:  |                   |     |               | Write parts that precede test cases |
|                   | :small_blue_diamond:  | writeOpening      |     |               | Write the opening part of the test program |
|                   | :small_blue_diamond:  | writeDependencies |     |               | Write framework dependencies |
|                   | :small_orange_diamond:  | writeDependencies |     |               | Write request execution dependencies |
|                   | :small_blue_diamond:  | writeDeclarations |     |               | Write declarations of framework components |
|                   | :small_orange_diamond:  | writeDeclarations |     |               | Write declarations of request execution components |
| writeTestCases    | :arrow_heading_down:  |                   |     |               | Write all test cases |
|                   |     | writeTestCase     | :arrow_heading_down:  |               | Write a single test case |
|                   |     |                   | :small_orange_diamond:  | writeTestCase | Write the body of a single test case |
| writeEpilog       | :arrow_heading_down:  |                   |     |               | Write parts that follow test cases |
|                   | :small_orange_diamond:  | writeClosing      |     |               | Write request execution parts that follow test cases |
|                   | :small_blue_diamond:  | writeClosing      |     |               | Write the closing part of the test |
