package it.vfsfitvnm.reordering

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.TwoWayConverter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AnimatablesPool<T, V : AnimationVector>(
    private val size: Int,
    private val initialValue: T,
    typeConverter: TwoWayConverter<T, V>
) {
    private val values = MutableList(size) {
        Animatable(initialValue = initialValue, typeConverter = typeConverter)
    }

    private val mutex = Mutex()

    init {
        require(size > 0)
    }

    suspend fun acquire(): Animatable<T, V> {
        return mutex.withLock {
            require(values.isNotEmpty())
            values.removeFirst()
        }
    }

    suspend fun release(animatable: Animatable<T, V>) {
        mutex.withLock {
            require(values.size < size)
            animatable.snapTo(initialValue)
            values.add(animatable)
        }
    }
}
