import sys

from tempp import *

module="io.github.rtmigo:repr"

url="https://github.com/rtmigo/repr_kt"

code="""
    import io.github.rtmigo.repr.*
    fun main() = println(listOf(1, 2, 3).toRepr())
"""

try:
    imp_details = """{ version { branch = "__BRANCH__" } }""".replace("__BRANCH__", sys.argv[1])
except IndexError:
    imp_details = ""

with TempProject(
        files={
            # minimalistic build script to use the library
            "build.gradle.kts": """
                plugins {
                    id("application")
                    kotlin("jvm") version "1.6.10"
                }

                repositories { mavenCentral() }
                application { mainClass.set("MainKt") }

                dependencies {
                    implementation("__MODULE__") __IMP_DETAILS__
                }
            """.replace("__MODULE__", module).replace("__IMP_DETAILS__", imp_details),

            # additional settings, if necessary
            "settings.gradle.kts": """
                sourceControl {
                    gitRepository(java.net.URI("__URL__.git")) {
                        producesModule("__MODULE__")
                    }
                }
            """.replace("__MODULE__", module).replace("__URL__", url),

            # kotlin code that imports and uses the library
            "src/main/kotlin/Main.kt": code}) as app:

    app.print_files()
    result = app.run(["gradle", "run", "-q"])
    assert result.returncode == 0
    assert result.stdout == "listOf(1, 2, 3)\n", result.stdout

print("Everything is OK!")