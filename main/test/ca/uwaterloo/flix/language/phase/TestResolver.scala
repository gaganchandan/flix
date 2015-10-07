package ca.uwaterloo.flix.language.phase

import ca.uwaterloo.flix.language.Compiler

import org.scalatest.FunSuite

class TestResolver extends FunSuite {

  /////////////////////////////////////////////////////////////////////////////
  // Definitions                                                             //
  /////////////////////////////////////////////////////////////////////////////
  test("DuplicateDefinition01") {
    val input =
      s"""namespace A {
         |  val foo: Int = 42;
         |
         |  val foo: Int = 21;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  test("DuplicateDefinition02") {
    val input =
      s"""namespace A {
         |  val foo: Bool = true;
         |
         |  val foo: Int = 42;
         |
         |  val foo: Str = "bar";
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  test("DuplicateDefinition03") {
    val input =
      s"""namespace A {
         |  def foo(x: Int): Int = 42;
         |
         |  def foo(x: Int): Int = 21;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  test("DuplicateDefinition04") {
    val input =
      s"""namespace A {
         |  val foo: Int = 42;
         |
         |  def foo(x: Int): Int = 21;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  test("DuplicateDefinition05") {
    val input =
      s"""namespace A {
         |  val foo: Int = 42;
         |};
         |
         |namespace A {
         |  val foo: Int = 21;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  test("DuplicateDefinition06") {
    val input =
      s"""namespace A::B::C {
         |  val foo: Int = 42;
         |};
         |
         |namespace A {
         |  namespace B {
         |    namespace C {
         |      val foo: Int = 21;
         |    }
         |  };
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.DuplicateDefinition])
  }

  /////////////////////////////////////////////////////////////////////////////
  // Expressions                                                             //
  /////////////////////////////////////////////////////////////////////////////
  test("UnresolvedConstantReference01") {
    val input =
      s"""namespace A {
         |  val x: Int = y;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedConstantReference])
  }

  test("UnresolvedConstantReference02") {
    val input =
      s"""namespace A {
         |  def foo(x: Int, y: Int): Int = x + y + z;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedConstantReference])
  }

  test("UnresolvedEnumReference01") {
    val input =
      s"""namespace A {
         |  val x: Int = Foo.Bar
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedEnumReference])
  }

  test("UnresolvedEnumReference02") {
    val input =
      s"""namespace A {
         |  val x: Int = Foo::Bar.Qux true
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedEnumReference])
  }

  test("UnresolvedTagReference01") {
    val input =
      s"""namespace A {
         |  enum B {
         |    case Foo,
         |    case Bar
         |  }
         |
         |  val b: B = B.Qux;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.isFailure)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedTagReference])
  }

  test("UnresolvedTagReference02") {
    val input =
      s"""namespace A {
         |  enum B {
         |    case Foo,
         |    case Bar
         |  }
         |
         |  val x: B = B.Qux(1 + 2);
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.isFailure)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedTagReference])
  }

  test("UnresolvedTagReference03") {
    val input =
      s"""namespace A {
         |  enum B {
         |    case Foo,
         |    case Bar
         |  }
         |
         |  def foo(b: B): Int = match b with {
         |    case B.Qux => 42;
         |  }
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.isFailure)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedTagReference])
  }

  test("UnresolvedRelationReference01") {
    val input =
      s"""namespace A {
         |  VarPointsTo(1, 2).
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedRelationReference])
  }

  test("UnresolvedRelationReference02") {
    val input =
      s"""namespace A {
         |  VarPointsTo(1, 2).
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedRelationReference])
  }

  test("UnresolvedTypeReference01") {
    val input =
      s"""namespace A {
         |  val x: Foo = 42;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedTypeReference])
  }

  test("UnresolvedTypeReference02") {
    val input =
      s"""namespace A {
         |  def foo(bar: Baz, baz: Baz): Qux = bar;
         |};
       """.stripMargin
    val result = Compiler.compile(input)
    assert(result.hasErrors)
    assert(result.errors.head.isInstanceOf[Resolver.ResolverError.UnresolvedTypeReference])
  }
}
