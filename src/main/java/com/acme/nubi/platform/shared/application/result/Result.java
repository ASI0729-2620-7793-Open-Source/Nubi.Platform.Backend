package com.acme.nubi.platform.shared.application.result;

import java.util.Optional;
import java.util.function.Function;

public sealed interface Result<T, E> permits Result.Success, Result.Failure {

    static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    static <T, E> Result<T, E> failure(E error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success<?, ?>;
    }

    default boolean isFailure() {
        return this instanceof Failure<?, ?>;
    }

    default Optional<T> success() {
        if (this instanceof Success<T, E> success) {
            return Optional.of(success.value());
        }
        return Optional.empty();
    }

    default Optional<E> failure() {
        if (this instanceof Failure<T, E> failure) {
            return Optional.of(failure.error());
        }
        return Optional.empty();
    }

    default <R> R fold(Function<? super T, ? extends R> onSuccess,
                       Function<? super E, ? extends R> onFailure) {
        if (this instanceof Success<T, E> success) {
            return onSuccess.apply(success.value());
        } else if (this instanceof Failure<T, E> failure) {
            return onFailure.apply(failure.error());
        }
        throw new IllegalStateException("Unexpected Result type");
    }

    record Success<T, E>(T value) implements Result<T, E> {
    }

    record Failure<T, E>(E error) implements Result<T, E> {
    }
}
