package co.cozygaming.braintest.extensions

interface DataSource
interface Repository

abstract class AbstractFactory<T> {
    abstract fun create(which: Class<*>): T
}

/**
 * Generic type callback that is used for request or response from server or local
 * @param T generic type that will be converted into give data type at runtime
 */
interface Callback<T> {
    fun success(obj: T)
    fun fail(errorCode: Int, errorMsg: String)
}

/**
 * Generic type callback that is used for request or response from server or local
 * @param T generic type that will be converted into give data type at runtime
 */
@FunctionalInterface
interface SimpleCallback<T> {
    fun invoke(obj: T)
}

interface LoadDataCallback<T> {
    fun onDataLoaded(response: T)
    fun onDataNotAvailable(errorCode: Int, reasonMsg: String)
}

/**
 * Callback interface to check if email is updated
 */
interface EmailUpdateCheckCallback {
    fun onSuccess(isEmailUpdated: Boolean)

    fun onFail(message: String?)
}

/**
 * Callback interface for email update
 */
interface EmailUpdateCallback {
    fun onSuccess()

    fun onFail(message: String?)
}
