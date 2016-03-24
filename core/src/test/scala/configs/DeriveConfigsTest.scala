/*
 * Copyright 2013-2016 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configs

import com.typesafe.config.{ConfigFactory, ConfigValue}
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.string._
import configs.testutil.instance.tuple._
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.syntax.equal._
import scalaz.{Apply, Equal, Need}


object DeriveConfigsTest extends Scalaprops {

  case class CC0()

  object CC0 {
    implicit lazy val gen: Gen[CC0] =
      Gen.elements(CC0())

    implicit lazy val equal: Equal[CC0] =
      Equal.equalA[CC0]

    implicit lazy val tc: ToConfig[CC0] =
      _ => ConfigFactory.empty().root()
  }

  val caseClass0 = check[CC0]


  case class CC1(a1: Int)

  object CC1 {
    implicit lazy val gen: Gen[CC1] =
      Gen[Int].map(CC1.apply)

    implicit lazy val equal: Equal[CC1] =
      Equal.equalA[CC1]

    implicit lazy val tc: ToConfig[CC1] =
      ToConfig.by {
        case CC1(a1) => Map("a1" -> a1)
      }
  }

  val caseClass1 = check[CC1]


  case class CC22(
      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
      a21: Int, a22: Int)

  object CC22 {
    implicit lazy val gen: Gen[CC22] =
      Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
        .map((CC22.apply _).tupled)

    implicit lazy val equal: Equal[CC22] =
      Equal.equalA[CC22]

    implicit lazy val tc: ToConfig[CC22] =
      ToConfig.by(
        _.productIterator.zipWithIndex.map {
          case (a, i) => s"a${i + 1}" -> a.asInstanceOf[Int]
        }.toMap)
  }

  val caseClass22 = check[CC22]


  case class CC23(
      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
      a21: Int, a22: Int, a23: Int)

  object CC23 {
    implicit lazy val gen: Gen[CC23] =
      Apply[Gen].apply2(
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)],
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
      )((t1, t2) => CC23(
        t1._1, t1._2, t1._3, t1._4, t1._5, t1._6, t1._7, t1._8, t1._9, t1._10, t1._11, t1._12,
        t2._1, t2._2, t2._3, t2._4, t2._5, t2._6, t2._7, t2._8, t2._9, t2._10, t2._11))

    implicit lazy val equal: Equal[CC23] =
      Equal.equalA[CC23]

    implicit lazy val tc: ToConfig[CC23] =
      ToConfig.by(
        _.productIterator.zipWithIndex.map {
          case (a, i) => s"a${i + 1}" -> a.asInstanceOf[Int]
        }.toMap)
  }

  val caseClass23 = check[CC23]


  case class SubApply(a1: Int, a2: Int)

  object SubApply {

    def apply(s1: String, s2: String): SubApply =
      new SubApply(s1.toInt, s2.toInt)

    implicit lazy val gen: Gen[SubApply] =
      Apply[Gen].apply2(Gen[Int], Gen[Int])(SubApply.apply)

    implicit lazy val equal: Equal[SubApply] =
      Equal.equalA[SubApply]

    implicit lazy val tc: ToConfig[SubApply] =
      ToConfig.by(sa =>
        Map(
          "s1" -> sa.a1.toString,
          "s2" -> sa.a2.toString
        ))
  }

  val subApply = {
    val p1 = check[SubApply]
    val p2 =
      forAll { (a1: Int, a2: Int, s1: Int, s2: Int) =>
        val c = ConfigFactory.parseString(
          s"""a1 = $a1
             |a2 = $a2
             |s1 = $s1
             |s2 = $s2
           """.stripMargin)
        Configs[SubApply].extract(c).exists(_ === SubApply(a1, a2))
      }
    Properties.list(
      p1.toProperties("sub apply"),
      p2.toProperties("synthetic first")
    )
  }


  class PlainClass(val a1: Int, val a2: Int, val a3: Int) {
    def this(a1: Int, a2: Int) =
      this(a1, a2, 42)
  }

  object PlainClass {
    implicit lazy val gen: Gen[PlainClass] =
      Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(new PlainClass(_, _, _))

    implicit lazy val equal: Equal[PlainClass] =
      Equal.equalBy(p => (p.a1, p.a2, p.a3))

    implicit lazy val tc: ToConfig[PlainClass] =
      ToConfig.by(p =>
        Map(
          "a1" -> p.a1,
          "a2" -> p.a2,
          "a3" -> p.a3
        ))
  }

  val plainClass = {
    val p1 = check[PlainClass]
    val p2 =
      forAll { (a1: Int, a2: Int) =>
        val c = ConfigFactory.parseString(
          s"""a1 = $a1
             |a2 = $a2
           """.stripMargin)
        Configs[PlainClass].extract(c).exists(_ === new PlainClass(a1, a2, 42))
      }
    Properties.list(
      p1.toProperties("plain class"),
      p2.toProperties("sub constructor")
    )
  }


  sealed trait Sealed

  case class SealedCase(a1: Int) extends Sealed

  class SealedPlain(val a1: Int) extends Sealed

  case object SealedCaseObject extends Sealed

  object SealedObject extends Sealed

  sealed abstract class SealedSealed extends Sealed

  case class SealedSealedClass(a1: Int) extends SealedSealed

  sealed class SealedConcrete(val a1: Int) extends Sealed

  class NoArgClass extends Sealed

  object Sealed {
    // SI-7046
    implicit lazy val configs: Configs[Sealed] =
      Configs.derive[Sealed]

    implicit lazy val gen: Gen[Sealed] =
      Gen.oneOf(
        Gen[Int].map(SealedCase),
        Gen[Int].map(new SealedPlain(_)),
        Gen[Int].map(SealedSealedClass),
        Gen[Int].map(new SealedConcrete(_)),
        Gen.elements(SealedCaseObject, SealedObject, new NoArgClass())
      )

    implicit lazy val equal: Equal[Sealed] =
      Equal.equalBy {
        case SealedCase(a1) => ("CA", a1)
        case p: SealedPlain => ("PL", p.a1)
        case SealedCaseObject => ("CO", 0)
        case SealedObject => ("SO", 0)
        case SealedSealedClass(a1) => ("SS", a1)
        case s: SealedConcrete => ("SC", s.a1)
        case _: NoArgClass => ("NA", 0)
      }

    implicit lazy val tc: ToConfig[Sealed] =
      ToConfig.by { s =>
        val S = ToConfig[String]
        val N = ToConfig[Int]
        val m = Map("type" -> S.toValue(s.getClass.getSimpleName.stripSuffix("$")))
        s match {
          case SealedCase(a1) => m + ("a1" -> N.toValue(a1))
          case p: SealedPlain => m + ("a1" -> N.toValue(p.a1))
          case SealedCaseObject => m
          case SealedObject => m
          case SealedSealedClass(a1) => m + ("a1" -> N.toValue(a1))
          case s: SealedConcrete => m + ("a1" -> N.toValue(s.a1))
          case _: NoArgClass => m
        }
      }
  }

  val sealedClass = {
    val p1 = check[Sealed]
    val p2 = {
      implicit val moduleAsStringTCV: ToConfig[Sealed] = {
        case SealedCaseObject => ToConfig[String].toValue("SealedCaseObject")
        case SealedObject => ToConfig[String].toValue("SealedObject")
        case s => Sealed.tc.toValue(s)
      }
      check[Sealed]
    }
    Properties.list(
      p1.toProperties("sealed class"),
      p2.toProperties("module as string")
    )
  }


  case class Default1(a1: Int = 1)

  object Default1 {
    implicit lazy val equal: Equal[Default1] =
      Equal.equalA[Default1]
  }

  val default1 = {
    val p1 = {
      implicit val gen: Gen[Default1] =
        Gen[Int].map(Default1.apply)
      implicit val tc: ToConfig[Default1] =
        ToConfig.by {
          case Default1(a1) => Map("a1" -> a1)
        }
      check[Default1]
    }
    val p2 = {
      implicit val gen: Gen[Default1] =
        Gen.elements(Default1())
      implicit val tc: ToConfig[Default1] =
        _ => ConfigFactory.empty().root()
      check[Default1]
    }
    Properties.list(
      p1.toProperties("w/o default"),
      p2.toProperties("w/ default")
    )
  }


  class Default22(
      val a1: Int, val a2: Int, val a3: Int, val a4: Int, val a5: Int, val a6: Int, val a7: Int,
      val a8: Int = 8,
      val a9: Int = 9,
      val a10: Int = 10)(
      val a11: Int,
      val a12: Int = a1 + a8,
      val a13: Int = 13,
      val a14: Int,
      val a15: Int = a10 + 15)(
      val a16: Int = 16,
      val a17: Int,
      val a18: Int = 18 + a2,
      val a19: Int,
      val a20: Int,
      val a21: Int = a3 + a13 + 21,
      val a22: Int = 22 + a9 + a14) {

    override def toString: String = {
      val as1 = (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
      val as2 = (a11, a12, a13, a14, a15)
      val as3 = (a16, a17, a18, a19, a20, a21, a22)
      s"${getClass.getSimpleName}$as1$as2$as3"
    }
  }

  object Default22 {
    implicit lazy val equal: Equal[Default22] =
      Equal.equalBy(d => (
        d.a1, d.a2, d.a3, d.a4, d.a5, d.a6, d.a7, d.a8,
        d.a9, d.a10, d.a11, d.a12, d.a13, d.a14, d.a15, d.a16,
        d.a17, d.a18, d.a19, d.a20, d.a21, d.a22))
  }

  val default22 = {
    val p1 = {
      implicit val gen: Gen[Default22] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) =>
            new Default22(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)(a11, a12, a13, a14, a15)(a16, a17, a18, a19, a20, a21, a22)
          }
      implicit val tc: ToConfig[Default22] =
        ToConfig.by(d => Map(
          "a1" -> d.a1, "a2" -> d.a2, "a3" -> d.a3,
          "a4" -> d.a4, "a5" -> d.a5, "a6" -> d.a6,
          "a7" -> d.a7, "a8" -> d.a8, "a9" -> d.a9,
          "a10" -> d.a10,
          "a11" -> d.a11, "a12" -> d.a12, "a13" -> d.a13,
          "a14" -> d.a14, "a15" -> d.a15,
          "a16" -> d.a16, "a17" -> d.a17, "a18" -> d.a18,
          "a19" -> d.a19, "a20" -> d.a20, "a21" -> d.a21,
          "a22" -> d.a22
        ))
      check[Default22]
    }
    val p2 = {
      implicit val gen: Gen[Default22] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a11, a14, a17, a19, a20) =>
            new Default22(a1, a2, a3, a4, a5, a6, a7)(a11 = a11, a14 = a14)(a17 = a17, a19 = a19, a20 = a20)
        }
      implicit val tc: ToConfig[Default22] =
        ToConfig.by(d => Map(
          "a1" -> d.a1, "a2" -> d.a2, "a3" -> d.a3,
          "a4" -> d.a4, "a5" -> d.a5, "a6" -> d.a6,
          "a7" -> d.a7,
          "a11" -> d.a11, "a14" -> d.a14,
          "a17" -> d.a17, "a19" -> d.a19, "a20" -> d.a20
        ))
      check[Default22]
    }
    Properties.list(
      p1.toProperties("w/o defaults"),
      p2.toProperties("w/ defaults")
    )
  }


  class Default23(
      val a1: Int, val a2: Int, val a3: Int, val a4: Int, val a5: Int, val a6: Int, val a7: Int,
      val a8: Int = 8,
      val a9: Int = 9,
      val a10: Int = 10)(
      val a11: Int,
      val a12: Int = a1 + a8,
      val a13: Int = 13,
      val a14: Int,
      val a15: Int = a10 + 15)(
      val a16: Int = 16,
      val a17: Int,
      val a18: Int = 18 + a2,
      val a19: Int,
      val a20: Int,
      val a21: Int = a3 + a13 + 21,
      val a22: Int = 22 + a9 + a14)(
      val a23: Int = a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10 + a11 + a12 + a13 + a14 + a15 + a16 + a17 + a18 + a19 + a20 + a21 + a22) {

    override def toString: String = {
      val as1 = (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
      val as2 = (a11, a12, a13, a14, a15)
      val as3 = (a16, a17, a18, a19, a20, a21, a22)
      s"${getClass.getSimpleName}$as1$as2$as3($a23)"
    }
  }

  object Default23 {
    implicit lazy val equal: Equal[Default23] =
      Equal.equalBy(d => (
        (d.a1, d.a2, d.a3, d.a4, d.a5, d.a6, d.a7, d.a8, d.a9, d.a10, d.a11, d.a12),
        (d.a13, d.a14, d.a15, d.a16, d.a17, d.a18, d.a19, d.a20, d.a21, d.a22, d.a23)))
  }

  val default23 = {
    val p1 = {
      implicit val gen: Gen[Default23] =
        Apply[Gen].apply2(
          Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)],
          Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
        ) {
          case ((a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12), (a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23)) =>
            new Default23(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)(a11, a12, a13, a14, a15)(a16, a17, a18, a19, a20, a21, a22)(a23)
        }
      implicit val tc: ToConfig[Default23] =
        ToConfig.by(d => Map(
          "a1" -> d.a1, "a2" -> d.a2, "a3" -> d.a3,
          "a4" -> d.a4, "a5" -> d.a5, "a6" -> d.a6,
          "a7" -> d.a7, "a8" -> d.a8, "a9" -> d.a9,
          "a10" -> d.a10,
          "a11" -> d.a11, "a12" -> d.a12, "a13" -> d.a13,
          "a14" -> d.a14, "a15" -> d.a15,
          "a16" -> d.a16, "a17" -> d.a17, "a18" -> d.a18,
          "a19" -> d.a19, "a20" -> d.a20, "a21" -> d.a21,
          "a22" -> d.a22,
          "a23" -> d.a23
        ))
      check[Default23]
    }
    val p2 = {
      implicit val gen: Gen[Default23] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a11, a14, a17, a19, a20) =>
            new Default23(a1, a2, a3, a4, a5, a6, a7)(a11 = a11, a14 = a14)(a17 = a17, a19 = a19, a20 = a20)()
        }
      implicit val tc: ToConfig[Default23] =
        ToConfig.by(d => Map(
          "a1" -> d.a1, "a2" -> d.a2, "a3" -> d.a3,
          "a4" -> d.a4, "a5" -> d.a5, "a6" -> d.a6,
          "a7" -> d.a7,
          "a11" -> d.a11, "a14" -> d.a14,
          "a17" -> d.a17, "a19" -> d.a19, "a20" -> d.a20
        ))
      check[Default23]
    }
    Properties.list(
      p1.toProperties("w/o defaults"),
      p2.toProperties("w/ defaults")
    )
  }


  case class OptionDefault(opt: Option[Int] = Some(42))

  object OptionDefault {
    implicit lazy val equal: Equal[OptionDefault] =
      Equal.equalA[OptionDefault]
  }

  val optionDefault = {
    val p1 = forAll {
      val config = ConfigFactory.empty()
      Configs[OptionDefault].extract(config).exists(_ == OptionDefault(Some(42)))
    }
    val p2 = forAll {
      val config = ConfigFactory.parseString("opt = null")
      Configs[OptionDefault].extract(config).exists(_ == OptionDefault(None))
    }
    Properties.list(
      p1.toProperties("missing"),
      p2.toProperties("null")
    )
  }


  sealed trait Recursive

  case class RCons(head: Int, tail: Recursive) extends Recursive

  case object RNil extends Recursive

  object Recursive {
    implicit lazy val configs: Configs[Recursive] =
      Configs.derive[Recursive]

    implicit lazy val gen: Gen[Recursive] =
      Gen.oneOfLazy(
        Need(Apply[Gen].apply2(Gen[Int], Gen[Recursive])(RCons)),
        Need(Gen.elements(RNil))
      )

    implicit lazy val equal: Equal[Recursive] =
      Equal.equalA[Recursive]

    implicit lazy val tc: ToConfig[Recursive] = {
      case RNil => ToConfig[String].toValue("RNil")
      case RCons(h, t) =>
        ToConfig[Map[String, ConfigValue]].toValue(Map(
          "type" -> ToConfig[String].toValue("RCons"),
          "head" -> ToConfig[Int].toValue(h),
          "tail" -> tc.toValue(t)
        ))
    }
  }

  val recursive = check[Recursive]


  case class RecursiveOpt(value: Option[RecursiveOpt])

  object RecursiveOpt {
    implicit lazy val configs: Configs[RecursiveOpt] =
      Configs.derive[RecursiveOpt]

    implicit lazy val gen: Gen[RecursiveOpt] =
      Gen.oneOfLazy(
        Need(Gen[Option[RecursiveOpt]].map(RecursiveOpt.apply))
      )

    implicit lazy val equal: Equal[RecursiveOpt] =
      Equal.equalA[RecursiveOpt]

    implicit lazy val tc: ToConfig[RecursiveOpt] =
      ToConfig.by {
        case RecursiveOpt(Some(r)) => Map("value" -> tc.toValue(r))
        case RecursiveOpt(None) => Map.empty[String, ConfigValue]
      }
  }

  val recursiveOption = check[RecursiveOpt]


  class ImplicitParam(implicit val a1: Int, val a2: Long = 2L)

  object ImplicitParam {
    implicit lazy val gen: Gen[ImplicitParam] =
      Apply[Gen].apply2(Gen[Int], Gen[Int])(new ImplicitParam()(_, _))

    implicit lazy val equal: Equal[ImplicitParam] =
      Equal.equalBy(i => (i.a1, i.a2))

    implicit lazy val tc: ToConfig[ImplicitParam] =
      ToConfig.by(i => Map(
        "a1" -> ToConfig[Int].toValue(i.a1),
        "a2" -> ToConfig[Long].toValue(i.a2)
      ))
  }

  val implicitParam = {
    val p1 =
      forAll { (a1: Int, a2: Long) =>
        implicit val i1: Int = a1
        implicit val i2: Long = a2
        val c = ConfigFactory.empty()
        Configs[ImplicitParam].extract(c).exists(_ === new ImplicitParam()(a1, a2))
      }
    val p2 =
      forAll { implicit a1: Int =>
        val c = ConfigFactory.empty()
        Configs[ImplicitParam].extract(c).exists(_ === new ImplicitParam()(a1, 2L))
      }
    val p3 = {
      implicit def a1: Int = sys.error("implicit")
      check[ImplicitParam]
    }
    Properties.list(
      p1.toProperties("implicit parameters"),
      p2.toProperties("implicit with default"),
      p3.toProperties("w/o implicits")
    )
  }


  class HyphenSeparated(
      val normalParam: Int,
      val paramWithDefault: Int = 1,
      val duplicatedName: Int,
      val `duplicated-name`: Int)(implicit
      val implicitParam: Int,
      val implicitParamWithDefault: Long = 2)

  object HyphenSeparated {
    implicit lazy val equal: Equal[HyphenSeparated] =
      Equal.equalBy(h =>
        (h.normalParam, h.paramWithDefault, h.implicitParam, h.implicitParamWithDefault))

    implicit lazy val tc: ToConfig[HyphenSeparated] =
      ToConfig.by { h =>
        val N = ToConfig[Int]
        val L = ToConfig[Long]
        Map(
          "normal-param" -> N.toValue(h.normalParam),
          "param-with-default" -> N.toValue(h.paramWithDefault),
          "duplicatedName" -> N.toValue(h.duplicatedName),
          "duplicated-name" -> N.toValue(h.`duplicated-name`),
          "implicit-param" -> N.toValue(h.implicitParam),
          "implicit-param-with-default" -> L.toValue(h.implicitParamWithDefault)
        )
      }
  }

  val hyphenSeparatedPath = {
    implicit val i: Int = 42
    implicit val gen: Gen[HyphenSeparated] =
      Gen[(Int, Int, Int, Int)].map {
        case (a, b, c, d) => new HyphenSeparated(a, b, c, d)
      }
    check[HyphenSeparated]
  }

  val localCaseClass = {
    case class LocalCC(a1: Int)
    implicit val gen: Gen[LocalCC] = Gen[Int].map(LocalCC)
    implicit val equal: Equal[LocalCC] = Equal.equalA[LocalCC]
    implicit val tc: ToConfig[LocalCC] = ToConfig.by(cc => Map("a1" -> cc.a1))
    check[LocalCC]
  }

}
