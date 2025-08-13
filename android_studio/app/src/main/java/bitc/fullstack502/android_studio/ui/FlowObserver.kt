package bitc.fullstack502.android_studio.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FlowObserver(
    private val owner: LifecycleOwner,
    private val block: suspend () -> Unit
) : DefaultLifecycleObserver {
    private var job: Job? = null
    override fun onStart(owner: LifecycleOwner) {
        job = owner.lifecycleScope.launch { block() }
    }
    override fun onStop(owner: LifecycleOwner) {
        job?.cancel()
        job = null
    }
}
