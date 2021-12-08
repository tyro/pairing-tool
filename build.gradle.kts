import java.util.Calendar;

fun prop(key: String) = project.findProperty(key).toString()
fun prop(key: String, default: String) = prop(key) ?: default

plugins {
    id("org.jetbrains.intellij") version "1.3.0"
    kotlin("jvm") version "1.6.0"
    id("com.github.hierynomus.license") version "0.16.1"
}

group = "com.tyro.oss.pairing"
version = prop("version", "3.0")

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.apache.kafka:kafka-clients:3.0.0") {
        exclude("org.slf4j:slf4j-api")
    }
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.socket:socket.io-client:2.0.1")
}

intellij {
    version.set("2021.3")
}

tasks {
    patchPluginXml {
        version.set("${project.version}")
        changeNotes.set("""08/12/2021<br>
Bump library versions.<br>
Remove depricated api usage.<br>
<br>
17/08/2021<br>
Tested on 2021.2<br>
<br>
21/07/2021<br>
Add Websocket server support<br>
<br>
02/07/2021<br>
Tested on 2021.1.3<br>
<br>
21/04/2021<br>
Tested on 2021.1.1<br>
<br>
16/02/2021<br>
Tested on 2020.3.2<br>
<br>
04/01/2021<br>
Tested on 2020.3.1<br>
<br>
24/12/2020<br>
Add support for 2020.3<br>
<br>
08/12/2020<br>
Added a custom Icon.<br>
<br>
03/12/2020<br>
This is the first upload of the pairing tool to the jetbrains plugin repo.<br>
<br>
Please report any and all bugs you find to https://github.com/tyro/pairing-tool/issues.""")
    }

    license {
        header = rootProject.file("HEADER.txt")
        strictCheck=true
        include("**/*.kt")
        ext.set("year", Calendar.getInstance().get(Calendar.YEAR))
    }

    publishPlugin {
        token.set(prop("token", "secret"))
        channels.set(listOf(prop("channels", "nightly")))
    }
}
