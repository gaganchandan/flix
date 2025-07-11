/*
 * Copyright 2021 Magnus Madsen
 * Copyright 2021 Benjamin Dahse
 * Copyright 2025 Casper Dalgaard Nielsen
 *                Adam Yasser Tallouzi
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
mod Fixpoint3.Ast.Shared {

    use Fixpoint3.Boxable
    use Fixpoint3.Boxed


    /////////////////////////////////////////////////////////////////////////////
    // Denotation                                                              //
    /////////////////////////////////////////////////////////////////////////////

    ///
    /// Represents the denotation of a predicate symbol.
    ///
    /// A predicate symbol either has relational or lattice semantics.
    ///
    /// If it has lattice semantics it is equipped with:
    ///
    /// - A bottom element.
    /// - A partial order (a leq function).
    /// - a least upper bound (a lub function).
    /// - a greatest lower bound (a glb function).
    ///
    @Internal
    pub enum Denotation[v] {
        case Relational
        case Latticenal(v, v -> v -> Bool, v -> v -> v, v -> v -> v)
    }

    @Internal
    pub type alias BoxedDenotation = Denotation[Boxed]

    ///
    /// Returns `true` if the given denotation is relational.
    ///
    @Internal
    pub def isRelational(den: Denotation[v]): Bool = match den {
        case Denotation.Relational => true
        case _                     => false
    }

    ///
    /// Returns a latticenal denotation associated with the type `t`.
    ///
    @Internal
    pub def lattice(): Denotation[v] with LowerBound[v], JoinLattice[v], MeetLattice[v] =
        Denotation.Latticenal(
            LowerBound.minValue(),
            (x, y) -> PartialOrder.lessEqual(x, y),
            (x, y) -> JoinLattice.leastUpperBound(x, y),
            (x, y) -> MeetLattice.greatestLowerBound(x, y)
        )

    ///
    /// Boxes the lattice components inside the given denotation `d`.
    ///
    @Internal
    pub def box(d: Denotation[t]): Denotation[Boxed] with Order[t] = match d {
        case Denotation.Relational => Denotation.Relational
        case Denotation.Latticenal(bot, leq, lub, glb) =>
            Denotation.Latticenal(
                Boxable.box(bot),
                Boxable.lift2b(leq),
                Boxable.lift2(lub),
                Boxable.lift2(glb)
            )
    }

    /////////////////////////////////////////////////////////////////////////////
    // PredSym                                                                 //
    /////////////////////////////////////////////////////////////////////////////

    @Internal
    pub enum PredSym with Eq, Order {
        case PredSym(String, Int64)
    }

    instance ToString[PredSym] {
        pub def toString(predSym: PredSym): String = match predSym {
            case PredSym.PredSym(name, id) => "${name}%${id}"
        }
    }

}
