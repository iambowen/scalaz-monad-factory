/*
Copyright 2015 ThoughtWorks, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.thoughtworks.scalazMonadFactory

import org.junit.Test
import scalaz._
import scalaz.effect.MonadCatchIO
import scalaz.effect.IO
import org.junit.Assert

class TransformerTest {

  @Test
  def testIoWhile(): Unit = {
    val transformer = Transformer[IO]
    import transformer._

    def s = IO("123")
    var count = 0
    val io = async {
      var i = 0
      while (i < 100) {
        count += s.length
        i += 1
      }
      i
    }

    Assert.assertEquals(100, io.unsafePerformIO())

    Assert.assertEquals(300, count)
  }

  @Test
  def testWhile(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    def s = Some("123")
    var count = 0
    async {
      val i = 100
      while (i != 100) {
        count += s.length
      }
    }

    Assert.assertEquals(0, count)
  }

  @Test
  def testIf(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    val ifOption = async {
      val i = Some(1)
      val j = if (i > 1) 2 else 10
      i + j
    }

    Assert.assertEquals(Some(11), ifOption)
  }

  @Test
  def testReturn(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    def returnExprssions(input: Option[Int]): Option[Int] = async {
      if (input < 0) {
        return Some(-1)
      }
      if (input < 10) {
        return Some(0)
      }
      input
    }

    Assert.assertEquals(Some(-1), returnExprssions(Some(-1234)))
    Assert.assertEquals(Some(0), returnExprssions(Some(5)))
    Assert.assertEquals(Some(13), returnExprssions(Some(13)))
    Assert.assertEquals(None, returnExprssions(None))
  }

  @Test
  def testImport(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    object A {
      def currentImport = "A"
    }

    object B {
      def currentImport = "B"
    }

    object C {
      def currentImport = "C"
    }

    val result = async {
      import A._
      Assert.assertEquals("A", currentImport)

      {
        import B._
        Assert.assertEquals("B", currentImport)

        {
          import C._
          Assert.assertEquals("C", currentImport)
        }
      }

      currentImport
    }

    Assert.assertEquals(Some("A"), result)
  }

  @Test
  def testAssignExpressions(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    val assignExp = async {
      var pi = 3.1415
      pi = 1.0
      pi
    }

    Assert.assertEquals(Some(1.0), assignExp)

  }

  @Test
  def testDefDef(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    val lengthOption = async {
      def s = Some(Nil)
      s.length
    }

    Assert.assertEquals(Monad[Option].map {
      def s = Some(Nil)
      s
    }(_.length), lengthOption)
  }

  @Test
  def testSomeNilLength(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._
    val s = Some(Nil)

    val lengthOption = async {
      s.length
    }

    Assert.assertEquals(Monad[Option].map(s)(_.length), lengthOption)

  }

  @Test
  def testNoneLength(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._
    val s: Option[Seq[_]] = None

    val lengthOption = async {
      s.length
    }

    Assert.assertEquals(Monad[Option].map(s)(_.length), lengthOption)

  }

  @Test
  def testNewByOption(): Unit = {
    import scalaz.std.option._
    val transformer = Transformer[Option]
    import transformer._
    val newS = async {
      new String("a string")
    }

    Assert.assertEquals(Monad[Option].pure(new String("a string")), newS)
    Assert.assertEquals(Some(new String("a string")), newS)
  }

  @Test
  def testNewByList(): Unit = {
    import scalaz.std.list._
    val transformer = Transformer[List]
    import transformer._
    val newS = async {
      new String("a string")
    }

    Assert.assertEquals(Monad[List].pure(new String("a string")), newS)
    Assert.assertEquals(List(new String("a string")), newS)
  }

  @Test
  def testConcatList = {
    import scalaz.std.list._
    val transformer = Transformer[List]
    import transformer._

    val list1 = List("foo", "bar", "baz")
    val list2 = List("Hello", "World!")
    val concatList = async {
      list1.substring(0, 2) + " " + list2.substring(1, 4)
    }

    Assert.assertEquals(List("fo ell", "ba ell", "ba ell", "fo orl", "ba orl", "ba orl"), concatList)
  }

  /* Legacy tests for debugging

  @Test
  def test(): Unit = {

    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._
    val s = Some(Nil)

    async {
      {
        val a = s.length
        val b = a + 1
        a + b + Some(b)
      }
      val b = 2
      1 + b
    }
  }

  def test2(): Unit = {

    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._
    val s = Some(Nil)
    val some2 = async {
      if (s.length != 0) {
        val nothing = s(2)
        val nothing2 = (s(2): Int) + (nothing: Int)
        s.length + s.length + (None: Option[Nil.type]).length
      }
      val nothing = s(2)
    }

  }

  def testCatch3(): Unit = {
    implicit def aaaa: MonadCatchIO[Option] = ???

    val transformer = Transformer[Option]
    val s = Some(Nil)

    Bind[Option].bind(
      Bind[Option].ifM(Bind[Option].map(s)(
        ((parameter$macro$12: scala.collection.immutable.Nil.type) => parameter$macro$12.length.$bang$eq(0))),
        MonadCatchIO.ensuring(
          Bind[Option].map(s)(((parameter$macro$13: scala.collection.immutable.Nil.type) =>
            parameter$macro$13.apply(2))),
          Bind[Option].map(s)(((parameter$macro$14: scala.collection.immutable.Nil.type) =>
            parameter$macro$14.apply(3)))),
        Applicative[Option].point(())))(((parameter$macro$15: Unit) => {
        parameter$macro$15;
        Bind[Option].bind(s)(((parameter$macro$16: scala.collection.immutable.Nil.type) => {
          val nothing: Nothing = parameter$macro$16.apply(2); Applicative[Option].point(())
        }))
      }))
  }

  */

  /*
@Test
def testCatch(): Unit = {
  var count = 0
  val io = MonadCatchIO.ensuring(MonadCatchIO.catchSome[IO, Int, IO[Int]]({
    count += 1
    MonadCatchIO[IO].point(???)
  })({(e: Throwable) => e match {
    case (e@(_: Error)) => Some({
      count += 1
      MonadCatchIO[IO].point(100)
    })
    case _ => None
  }}, identity), MonadCatchIO[IO].point(count += 1))
  Assert.assertEquals(100, io.unsafePerformIO())
  Assert.assertEquals(3, count)
}
*/

  @Test
  def testBlock(): Unit = {
    val transformer = Transformer[IO]
    import transformer._
    var count = 0
    val io = async {
      val _ = await(IO(()))
      count += 1
      count += 1
      count
    }
    Assert.assertEquals(0, count)
    Assert.assertEquals(2, io.unsafePerformIO())
    Assert.assertEquals(2, count)

  }

  @Test
  def testCatch(): Unit = {
    val transformer = Transformer[IO]
    import transformer._
    var count = 0
    val io = async {
      val _ = await(IO(()))
      try {
        count += 1
        (null: Array[Int])(0)
      } catch {
        case e: NullPointerException => {
          count += 1
          100
        }
      } finally {
        count += 1
      }
    }
    Assert.assertEquals(0, count)
    Assert.assertEquals(100, io.unsafePerformIO())
    Assert.assertEquals(3, count)
  }

  @Test
  def testThrowCatch(): Unit = {
    val transformer = Transformer[IO]
    import transformer._
    var count = 0
    val io = async {
      val _ = await(IO(()))
      try {
        count += 1
        throw new Exception
      } catch {
        case e: Exception => {
          count += 1
          100
        }
      } finally {
        count += 1
      }
    }
    Assert.assertEquals(0, count)
    Assert.assertEquals(100, io.unsafePerformIO())
    Assert.assertEquals(3, count)
  }

  @Test
  def testNestedClass(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._
    trait Base {
      def bar: Int
    }
    val nestedClass = async[Base] {
      class Foo() extends Base {
        def bar = 100
      }
      new Foo
    }

    Assert.assertEquals(100, nestedClass.get.bar)
  }

  @Test
  def testVarIf(): Unit = {

    val transformer = Transformer[IO]
    import transformer._
    var count = 0
    def io(initialValue: Int) = async {
      var i = initialValue
      if (i == 0) {
        i = 1
      } else {
        i = 2
      }
      i += 10
      i
    }

    val state = {
      IndexedStateT.stateTMonadState[Int, IO].ifM(
        IndexedStateT.stateTMonadState[Int, IO].get.map(_ == 0),
        IndexedStateT.stateTMonadState[Int, IO].put(1),
        IndexedStateT.stateTMonadState[Int, IO].put(2)
      ).flatMap { _ =>
        IndexedStateT.stateTMonadState[Int, IO].get
      }.flatMap { v =>
        IndexedStateT.stateTMonadState[Int, IO].put(v + 10)
      }.flatMap { _ =>
        IndexedStateT.stateTMonadState[Int, IO].get
      }
    }

    Assert.assertEquals(11, state.eval(0).unsafePerformIO())
    Assert.assertEquals(12, state.eval(-1).unsafePerformIO())

    Assert.assertEquals(state.eval(0).unsafePerformIO(), io(0).unsafePerformIO())
    Assert.assertEquals(state.eval(-1).unsafePerformIO(), io(-1).unsafePerformIO())
  }

  @Test
  def testMatch(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    val optionHead = async {
      await(await(Option(List("foo", "bar", "baz"))) match {
        case head :: tail => {
          Some(head)
        }
        case _ => {
          None
        }
      })
    }

    Assert.assertEquals(Some("foo"), optionHead)
  }

  @Test
  def testIoDoWhile(): Unit = {
    val transformer = Transformer[IO]
    import transformer._

    def s = IO("123")
    var count = 0
    val io = async {
      var i = 0
      do {
        count += s.length
        i += 1
      } while (i < 100)
      i
    }

    Assert.assertEquals(100, io.unsafePerformIO())

    Assert.assertEquals(300, count)
  }


  @Test
  def testDoWhile(): Unit = {
    import scalaz.std.option._

    val transformer = Transformer[Option]
    import transformer._

    def s = Some("123")
    var count = 0
    val option = async {
      var i = 0
      do {
        count += s.length
        i += 1
      } while (i < 0)
      i
    }

    Assert.assertEquals(Some(1), option)

    Assert.assertEquals(3, count)
  }

}

 