package com.yunext.kotlin.kmp.common.domain

sealed interface Effect<out I, out O> {
    data object Idle : Effect<Nothing, Nothing>
    data class Progress<I, Progress : Any>(val input: I, val progress: Progress) :
        Effect<I, Nothing>

    data class Success<I, O>(val input: I, val output: O) : Effect<I, O>
    data class Fail<I>(val input: I, val output: kotlin.Throwable) : Effect<I, Nothing>
    data object Completed : Effect<Nothing, Nothing>
}

val Effect<*, *>.doing: Boolean
    get() = this is Effect.Progress<*, *>


fun <Progress> effectProgress(progress: Progress) = Effect.Success<Unit, Progress>(Unit, progress)
fun <I, Progress : Any> effectProgress(input: I, progress: Progress) =
    Effect.Progress<I, Progress>(input = input, progress)

fun <I> effectProgress(input: I, progress: Int) =
    effectProgress<I, Int>(input = input, progress = progress)

fun <I> effectProgressUnit(input: I) = effectProgress<I, Unit>(input = input, progress = Unit)
fun effectProgress(progress: Int) = Effect.Progress<Unit, Int>(Unit, progress)
fun effectProgress() = Effect.Progress<Unit, Unit>(Unit, Unit)
fun <I, O> effectSuccess(input: I, output: O) = Effect.Success<I, O>(input = input, output)
fun <O> effectSuccess(output: O) = Effect.Success<Unit, O>(Unit, output)
fun <I> effectFail(input: I, output: Throwable) = Effect.Fail<I>(input = input, output)
fun effectFail(output: Throwable) = Effect.Fail<Unit>(Unit, output)
fun effectSuccess() = Effect.Success<Unit, Unit>(Unit, Unit)
fun effectIdle() = Effect.Idle
fun effectCompleted() = Effect.Completed

class ConsumeEffectBuilder<I, O>() {

    private var _onIdle: (() -> Unit)? = null
    private var _onProgress: ((I, Any) -> Unit)? = null
    private var _onSuccess: ((I, O) -> Unit)? = null
    private var _onFail: ((I, Throwable) -> Unit)? = null
    private var _onCompleted: (() -> Unit)? = null


    fun onIdle(block: () -> Unit) {
        this._onIdle = block
    }

    fun onFail(block: (I, Throwable) -> Unit) {
        _onFail = block
    }

    fun onSuccess(block: (I, O) -> Unit) {
        this._onSuccess = block
    }

    fun <Progress : Any> onProgress(block: (I, Progress) -> Unit) {
        _onProgress = block as (I, Any) -> Unit
    }

    fun onCompleted(block: () -> Unit) {
        this._onCompleted = block
    }

    fun handle(effect: Effect<I, O>) {
        when (effect) {
            Effect.Completed -> _onCompleted?.invoke()
            is Effect.Fail -> _onFail?.invoke(effect.input, effect.output)
            Effect.Idle -> _onIdle?.invoke()
            is Effect.Progress<*, *> -> _onProgress?.invoke(effect.input as I, effect.progress)
            is Effect.Success -> _onSuccess?.invoke(effect.input, effect.output)
        }
    }
}

fun <I, O> Effect<I, O>.consumeEffect(action: ConsumeEffectBuilder<I, O>.() -> Unit) {
//    val builder = ConsumeEffectBuilder<I, O>()
//    builder.action()
//    builder.handle(this)
    ConsumeEffectBuilder<I, O>().also(action).handle(this)
}
