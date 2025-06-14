/*
 *  Copyright 2024 Maksim Gusev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.lang.{Process=> JProcess}

///
/// Handles the created Process.
///
enum ProcessHandle (JProcess)

mod ProcessHandle {

    ///
    /// Returns the exit value of the process.
    ///
    pub def exitValue(ph: ProcessHandle): Int32 \ {Exec, IO} = match ph {
        case ProcessHandle.ProcessHandle(jProc) => jProc.exitValue()
    }

    ///
    ///  Returns status of the process, if it alive or not.
    ///
    pub def isAlive(ph: ProcessHandle): Bool \ {Exec, IO} = match ph {
        case ProcessHandle.ProcessHandle(jProc) => jProc.isAlive()
    }

    ///
    /// Stops the process.
    ///
    pub def stop(ph: ProcessHandle): Unit \ {Exec, IO} = match ph {
        case ProcessHandle.ProcessHandle(jProc) => jProc.destroy()
    }

}

