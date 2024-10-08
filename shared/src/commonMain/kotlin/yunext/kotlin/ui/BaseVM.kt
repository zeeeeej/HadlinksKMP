package yunext.kotlin.ui

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseVM {
    protected val vmScope =
        CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + CoroutineName("SlaveVMSlaveVM"))

    protected open fun onClear() {
        vmScope.cancel()
    }
}