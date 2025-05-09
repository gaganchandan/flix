/*
 * Copyright 2025 Jakob Schneider Villumsen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.uwaterloo.flix.language.ast

import ca.uwaterloo.flix.language.ast.shared.SymUse.{CaseSymUse, EffectSymUse, OpSymUse}
import ca.uwaterloo.flix.language.ast.shared.*
import ca.uwaterloo.flix.util.collection.Nel

object OccurrenceAst {

  case class Root(defs: Map[Symbol.DefnSym, Def],
                  enums: Map[Symbol.EnumSym, MonoAst.Enum],
                  structs: Map[Symbol.StructSym, MonoAst.Struct],
                  effects: Map[Symbol.EffectSym, MonoAst.Effect],
                  mainEntryPoint: Option[Symbol.DefnSym],
                  entryPoints: Set[Symbol.DefnSym],
                  sources: Map[Source, SourceLocation])

  case class Def(sym: Symbol.DefnSym, fparams: List[FormalParam], spec: MonoAst.Spec, exp: Expr, context: DefContext, loc: SourceLocation)

  sealed trait Expr {
    def tpe: Type

    def eff: Type

    def loc: SourceLocation
  }

  object Expr {

    case class Cst(cst: Constant, tpe: Type, loc: SourceLocation) extends Expr {
      def eff: Type = Type.Pure
    }

    case class Var(sym: Symbol.VarSym, tpe: Type, loc: SourceLocation) extends Expr {
      def eff: Type = Type.Pure
    }

    case class Lambda(fparam: FormalParam, exp: Expr, tpe: Type, loc: SourceLocation) extends Expr {
      def eff: Type = Type.Pure
    }

    case class ApplyAtomic(op: AtomicOp, exps: List[Expr], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class ApplyClo(exp1: Expr, exp2: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class ApplyDef(sym: Symbol.DefnSym, exps: List[Expr], itpe: Type, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class ApplyLocalDef(sym: Symbol.VarSym, exps: List[Expr], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class Let(sym: Symbol.VarSym, exp1: Expr, exp2: Expr, tpe: Type, eff: Type, occur: Occur, loc: SourceLocation) extends Expr

    case class LocalDef(sym: Symbol.VarSym, fparams: List[FormalParam], exp1: Expr, exp2: Expr, tpe: Type, eff: Type, occur: Occur, loc: SourceLocation) extends Expr

    case class Scope(sym: Symbol.VarSym, regSym: Symbol.RegionSym, exp: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class IfThenElse(exp1: Expr, exp2: Expr, exp3: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class Stm(exp1: Expr, exp2: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class Discard(exp: Expr, eff: Type, loc: SourceLocation) extends Expr {
      def tpe: Type = Type.mkUnit(loc)
    }

    case class Match(exp: Expr, rules: List[MatchRule], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class VectorLit(exps: List[Expr], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class VectorLoad(exp1: Expr, exp2: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class VectorLength(exp: Expr, loc: SourceLocation) extends Expr {
      def eff: Type = exp.eff

      def tpe: Type = Type.Int32
    }

    case class Ascribe(exp: Expr, tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class Cast(exp: Expr, declaredType: Option[Type], declaredEff: Option[Type], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class TryCatch(exp: Expr, rules: List[CatchRule], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class RunWith(exp: Expr, effUse: EffectSymUse, rules: List[HandlerRule], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class Do(op: OpSymUse, exps: List[Expr], tpe: Type, eff: Type, loc: SourceLocation) extends Expr

    case class NewObject(name: String, clazz: java.lang.Class[?], tpe: Type, eff: Type, methods: List[JvmMethod], loc: SourceLocation) extends Expr

  }

  sealed trait Pattern {
    def tpe: Type

    def loc: SourceLocation
  }

  object Pattern {

    case class Wild(tpe: Type, loc: SourceLocation) extends Pattern

    case class Var(sym: Symbol.VarSym, tpe: Type, occur: Occur, loc: SourceLocation) extends Pattern

    case class Cst(cst: Constant, tpe: Type, loc: SourceLocation) extends Pattern

    case class Tag(sym: CaseSymUse, pats: List[Pattern], tpe: Type, loc: SourceLocation) extends Pattern

    case class Tuple(pats: Nel[Pattern], tpe: Type, loc: SourceLocation) extends Pattern

    case class Record(pats: List[Pattern.Record.RecordLabelPattern], pat: Pattern, tpe: Type, loc: SourceLocation) extends Pattern

    object Record {
      case class RecordLabelPattern(label: Name.Label, pat: Pattern, tpe: Type, loc: SourceLocation)
    }
  }

  case class FormalParam(sym: Symbol.VarSym, mod: Modifiers, tpe: Type, src: TypeSource, occur: Occur, loc: SourceLocation)

  case class JvmMethod(ident: Name.Ident, fparams: List[FormalParam], exp: Expr, retTpe: Type, eff: Type, loc: SourceLocation)

  case class CatchRule(sym: Symbol.VarSym, clazz: java.lang.Class[?], exp: Expr)

  case class HandlerRule(op: OpSymUse, fparams: List[FormalParam], exp: Expr)

  case class MatchRule(pat: Pattern, guard: Option[Expr], exp: Expr)

  /**
    * Represents occurrence information of binders, i.e., how a binder occurs in the program.
    * A binder may be a variable, function, or effect handler.
    */
  sealed trait Occur

  object Occur {

    /**
      * Represents a binder that is not used in an expression.
      *
      * If the let-binding is pure, then it is safe to remove it, otherwise it can be rewritten to a statement.
      * If the binder is a function, it is safe to remove it. However, [[ca.uwaterloo.flix.language.phase.TreeShaker2]] handles that.
      *
      * Removing the binder results in smaller code size and does not increase work.
      */
    case object Dead extends Occur

    /**
      * Represents a binder that occurs exactly once in an expression.
      *
      * If the let-binding is pure, then it is safe to move its definition to the occurrence.
      *
      * Doing so strictly decreases code size and does not increase work.
      */
    case object Once extends Occur

    /**
      * Represents a binder that occurs exactly once and that occurrence is in the body of a lambda abstraction.
      *
      * If the let-binding is pure, then it is safe to move its definition to the occurrence.
      *
      * Doing so strictly decreases code size but may increase work.
      */
    case object OnceInLambda extends Occur

    /**
      * Represents a binder that occurs exactly once and that occurrence is in the body of a local def.
      *
      * If the let-binding is pure, then it is safe to move its definition to the occurrence.
      *
      * Doing so strictly decreases code size but may increase work.
      *
      * However, it may be beneficial to move the definition of the binder to simplify the expression further.
      * Local defs are often called multiple times so if work duplication is bounded, it may result in
      * smaller code size and less memory usage.
      */
    case object OnceInLocalDef extends Occur

    /**
      * Represents a binder that occurs at most once in distinct branches, but never inside a lambda abstraction or local def.
      *
      * If the let-binding is pure, then it is safe to move its definition to an occurrence.
      *
      * Moving the definition to any or all occurrences increases code size but does not increase work
      * since branches are exclusive.
      */
    case object ManyBranch extends Occur

    /**
      * Represents a binder that occurs more than once (including lambdas, local defs, branches).
      *
      * If the let-binding is pure, then it is safe to move its definition to an occurrence.
      *
      * Doing so may increase both code size and work.
      */
    case object Many extends Occur

  }

  /**
    * A [[DefContext]] contains various pieces of information on a function that are relevant for making an inlining decision.
    *
    * @param localDefs       the number of local defs defined in a function.
    * @param isDirectCall    true if the outermost expression of the body is a function call.
    * @param isSelfRecursive true if the function symbol occurs in the body of the function being defined.
    */
  case class DefContext(localDefs: Int, isDirectCall: Boolean, isSelfRecursive: Boolean)

}
