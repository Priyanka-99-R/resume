package com.smbc.interview;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SampleTest {

    private final Sample sample = new Sample();

    @Test
    @DisplayName("isReady() returns true")
    void isReadyReturnsTrue() {
        assertThat(sample.isReady()).isTrue();
    }

    @Nested
    @DisplayName("isEven")
    class IsEven {

        @ParameterizedTest
        @ValueSource(ints = {0, 2, -4, 100})
        void treatsTheseAsEven(int n) {
            assertThat(sample.isEven(n)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, -3, 99})
        void treatsTheseAsOdd(int n) {
            assertThat(sample.isEven(n)).isFalse();
        }
    }

    @Nested
    @DisplayName("reverse")
    class Reverse {

        @ParameterizedTest
        @CsvSource({"abc, cba", "a, a", "'', ''"})
        void reversesTheCharacters(String input, String expected) {
            assertThat(sample.reverse(input)).isEqualTo(expected);
        }

        @Test
        void returnsNullForNullRatherThanThrowing() {
            assertThat(sample.reverse(null)).isNull();
        }
    }
}
