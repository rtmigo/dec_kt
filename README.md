![Generic badge](https://img.shields.io/badge/maturity-experimental-red.svg)
![Generic badge](https://img.shields.io/badge/JVM-11-blue.svg)
![JaCoCo](https://raw.github.com/rtmigo/dec_kt/dev_updated_by_actions/.github/badges/jacoco.svg)

# io.github.rtmigo : [dec](https://github.com/rtmigo/dec_kt#readme)

`Dec` is a Kotlin wrapper around the Java `BigDecimal`. With the `Dec`, you
can perform calculations while remaining at a
predictable [Decimal64](https://en.wikipedia.org/wiki/Decimal64_floating-point_format) precision.


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

`Dec` behaves more predictably.

```kotlin
Dec(0.1) + Dec(0.2)  // = 0.3

Dec(0.1) + Dec(0.2) - Dec(0.1) - Dec(0.2)  // = 0.0
```

In fact, `Dec` also has a rounding problem. But this happens when you literally
operate on 16-digit numbers like `2.718281828459045` and some inaccuracy in the
lower digits is expected.

<details>
<summary>More horrors of double arithmetic</summary>


Let's just sum `0.1` multiple times and compare to the ideal result.

```kotlin
fun compute(summands: Int): Double {
    val ideal = summands * 0.1
    val error = (1..summands).sumOf { 0.1 } - ideal
    return error
}
```

Summands | Error (plain) | Error (scientific)
----------|------------------|---------------
10 | -0.000000000000000111 | -1.1102230246251565E-16
100 | -0.00000000000001954 | -1.9539925233402755E-14
1,000 | -0.000000000001406875 | -1.4068746168049984E-12
10,000 | 0.000000000158820512 | 1.588205122970976E-10
100,000 | 0.0000000188483682 | 1.8848368199542165E-8
1,000,000 | 0.000001332882675342 | 1.3328826753422618E-6
10,000,000 | -0.00016102462541312 | -1.610246254131198E-4
100,000,000 | -0.018870549276471138 | -0.018870549276471138
1,000,000,000 | -1.2545821815729141 | -1.2545821815729141
</details>

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
BigDecimal("125") / BigDecimal("100")      // = 1
BigDecimal("125.0") / BigDecimal("100.0")  // = 1.2
BigDecimal("125.00") / BigDecimal("100")   // = 1.25
```

Calculations with `Dec` are both intuitive and predictable.

```kotlin
Dec("125") / Dec("100")      // = 1.25
Dec("125.0") / Dec("100.0")  // = 1.25
Dec("125.00") / Dec("100")   // = 1.25 
```

## Implicit conversions

When the left side of an expression is a `Dec` instance, the right side can have
basic numeric types.

```kotlin
Dec("125") + 8  // = 133.0

BigDecimal("125") + 8  // does not compile in Kotlin 
```

## Speed considerations

### Dec is faster than rounding Double

`Double` with its rounding artifacts is much faster than `Dec`. It's tempting 
to use `Double` and round it up after every calculation. However, properly done 
rounding can be much slower than just using `Dec`.



```kotlin
import org.apache.commons.math3.util.Precision
import io.github.rtmigo.dec.*

fun slowest(n: Int): Double {
    var x: Double = 0.0
    for (i in 1..n) {
        x += 0.01
        x = Precision.round(x, 2)  // slow!
    }
    return x
}

fun average(n: Int): Double {
    var x = Dec("0.0")
    for (i in 1..n) {
        x += Dec("0.01")  // faster than Precision.round 
    }
    return x.toDouble()
}

fun fastest(n: Int): Double {
    var x: Double = 0.0
    for (i in 1..n) {
        x += 0.01
        // we can reuse x if we are satisfied with  
        // inaccurate decimal value on each step 
    }
    // we also must be sure that the accumulated error 
    // is less than the rounding
    return Precision.round(x, 2)
}

fun hyperspeed(n: Int) = Precision.round(n*0.01, 2)  // ;)
```

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

# Install

#### settings.gradle.kts

```kotlin
sourceControl {
    gitRepository(java.net.URI("https://github.com/rtmigo/dec_kt.git")) {
        producesModule("io.github.rtmigo:dec")
    }
}
```

#### build.gradle.kts

```kotlin
dependencies {
    implementation("io.github.rtmigo:dec")
}
```