package com.wineevaluator.common.error

sealed class DomainException(
    message: String,
    cause: Throwable? = null,
):   RuntimeException(message, cause)


class ValidationException(message: String): DomainException(message)
class NotFoundException(message: String): DomainException(message)
class ProcessingException(message: String): DomainException(message)
