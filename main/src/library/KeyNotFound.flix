/*
 * Copyright 2024 Jonathan Lindegaard Starup
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

///
/// Indicates that a key was not found.
/// Useful for instances of `Index` or `IndexMut`.
///
eff KeyNotFound {

    /// Stops execution with an error message.
    def keyNotFound(msg: String): Void

}

mod KeyNotFound {

    /// Turns the `KeyNotFound` effect into `Err(msg)`.
    @Experimental
    pub def asRes(f: a -> b \ ef): (a -> Result[String, b] \ ef - KeyNotFound) = {
        x -> run Ok(f(x)) with handler KeyNotFound {
            def keyNotFound(msg, _) = Err(msg)
        }
    }

}
