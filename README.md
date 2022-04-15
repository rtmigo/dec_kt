![Generic badge](https://img.shields.io/badge/maturity-experimental-red.svg)
![Generic badge](https://img.shields.io/badge/JVM-9-blue.svg)
![JaCoCo](https://raw.github.com/rtmigo/dec_kt/dev_updated_by_actions/.github/badges/jacoco.svg)

# io.github.rtmigo : [dec](https://github.com/rtmigo/dec_kt#readme)

`Dec` is a Kotlin wrapper around the Java `BigDecimal`. With the `Dec`, you
can perform calculations while remaining at a
predictable [Decimal64 precision](https://en.wikipedia.org/wiki/Decimal64_floating-point_format)
.

## Dec vs Double

`Double` introduces rounding artifacts where you wouldn't expect them.

```kotlin
0.1 + 0.2  // = 0.30000000000000004
```

Because of this rounding the same trivial operations with the same numbers, but
in a different order, give different results.

```kotlin
(0.1 + 0.2) - (0.1 + 0.2)  // = 0.0
 0.1 + 0.2  -  0.1 - 0.2   // = 2.7755575615628914e-17
```

`Dec` copes with the same task more predictably.

```kotlin
Dec(0.1) + Dec(0.2)  // = 0.3

Dec(0.1) + Dec(0.2) - Dec(0.1) - Dec(0.2)  // = 0.0
```

In fact, `Dec` also has a rounding problem. But this happens when you literally
operate on 16-digit numbers like `2.718281828459045` and some inaccuracy in the
lower digits is expected.

## Dec vs BigDecimal

`BigDecimal` fixes oddities in `Double` rounding.

But the syntax for using BigDecimal in Java is far from intuitive. For example,
here is how we compute `125 / 100`:

```kotlin
BigDecimal("125").divide(BigDecimal("100"), MathContext.DECIMAL64)
```

Kotlin standard library adds arithmetic operators to the `BigDecimal`, but
the result of the calculations can be surprising.

```kotlin
BigDecimal("125") / BigDecimal("100")      // = 1.0
BigDecimal("125.0") / BigDecimal("100.0")  // = 1.2
BigDecimal("125.00") / BigDecimal("100")   // = 1.25
```

Calculations with `Dec` are both intuitive and predictable.

```kotlin
Dec("125") / Dec("100")      // = 1.25
Dec("125.0") / Dec("100.0")  // = 1.25
Dec("125.00") / Dec("100")   // = 1.25 
```

## Implicit Conversions

When the left side of an expression is a `Dec` instance, the right side can have
basic numeric types.

```kotlin
Dec("125") + 8  // = 133.0

BigDecimal("125") + 8  // does not compile in Kotlin 1.6
```

## Speed considerations

### Dec is much slower than Double

`BigDecimal` is much slower, than `Double`.

And `Dec` (with high precision `BigDecimal`) is slower, than low precision `BigDecimal`.


<details>

Don't let
[benchmarks](https://medium.com/@ezioamf/bigdecimal-is-also-faster-than-double-with-the-extra-rounding-834004cc7e2f)
fool you. BigDecimal is only as fast when we compare rounding doubles to rounding BigDecimals,
not keeping precision.

</details>



### Reusing is faster than creation

```kotlin
val CONST_17 = Dec(17)

fun faster(x: Dec) = x * CONST_17

fun slower(x: Dec) = x + Dec(17)
fun also_slower(x: Dec) = x + 17  // this creates Dec(17) anyway
```

### Multiplication is faster than division

```kotlin
Dec(23) / Dec(100)   // slower
Dec(23) * Dec(0.01)  // faster
```

### Dec(string) is faster than Dec(double)

```kotlin
Dec("1.23")  // this is faster
Dec(1.23)  // will create intermediate string "1.23" anyway
```

`Double.toDecBin()` does not use intermediate string conversion, but be prepared
that:

```kotlin
100.0.toDecBin() - 0.1.toDecBin()  // = 99.90000000000001
```
