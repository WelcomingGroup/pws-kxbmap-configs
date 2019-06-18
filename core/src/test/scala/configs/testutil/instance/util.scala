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

package configs.testutil.instance

import java.util.{Locale, UUID}
import scalaprops.Gen
import scalaz.Equal

object util {

  implicit lazy val uuidGen: Gen[UUID] =
    Gen[Array[Byte]].map(UUID.nameUUIDFromBytes)

  implicit lazy val uuidEqual: Equal[UUID] =
    Equal.equalA[UUID]

  implicit lazy val localeGen: Gen[Locale] = {
    val ls = Locale.getAvailableLocales
    Gen.elements(ls.head, ls.tail.toIndexedSeq: _*)
  }

  implicit lazy val localeEqual: Equal[Locale] =
    Equal.equalA[Locale]

}
