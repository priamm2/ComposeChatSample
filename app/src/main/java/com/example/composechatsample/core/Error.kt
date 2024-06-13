package com.example.composechatsample.core

public sealed class Error {

  public abstract val message: String

  public data class GenericError(override val message: String) : Error()

  public data class ThrowableError(override val message: String, public val cause: Throwable) :
    Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + cause.hashCode()
    }
  }

  public data class NetworkError(
    override val message: String,
    public val serverErrorCode: Int,
    public val statusCode: Int = UNKNOWN_STATUS_CODE,
    public val cause: Throwable? = null
  ) : Error() {

    @StreamHandsOff(
      "Throwable doesn't override the equals method;" +
        " therefore, it needs custom implementation."
    )
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      return (other as? Error)?.let {
        message == it.message && cause.equalCause(it.extractCause())
      } ?: false
    }

    private fun Throwable?.equalCause(other: Throwable?): Boolean {
      if ((this == null && other == null) || this === other) return true
      return this?.message == other?.message && this?.cause.equalCause(other?.cause)
    }

    @StreamHandsOff(
      "Throwable doesn't override the hashCode method;" +
        " therefore, it needs custom implementation."
    )
    override fun hashCode(): Int {
      return 31 * message.hashCode() + (cause?.hashCode() ?: 0)
    }

    public companion object {
      public const val UNKNOWN_STATUS_CODE: Int = -1
    }
  }
}

public fun Error.copyWithMessage(message: String): Error {
  return when (this) {
    is Error.GenericError -> this.copy(message = message)
    is Error.NetworkError -> this.copy(message = message)
    is Error.ThrowableError -> this.copy(message = message)
  }
}

public fun Error.extractCause(): Throwable? {
  return when (this) {
    is Error.GenericError -> null
    is Error.NetworkError -> cause
    is Error.ThrowableError -> cause
  }
}