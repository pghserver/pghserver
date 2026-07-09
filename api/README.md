# Creating a Plugin - Using the PghServerAPI

Creating a plugin for **PghServer** is actually very simple, since the runtime is written in Java!

However, you do need to have good experience with Gradle/Maven and Java! Gradle/Maven helps organize your code and
dependencies, and Java is an object-oriented programming language that runs on its own bytecode language. This
behavior in Java makes plugin development in general so simple, you can include a single dependency and have a plugin
setup ready!

We provide an official API, with an installation guide [here](#installation), which assists in registering routes and
more.

---

## Starting a Plugin

> **Warning!** This guide does not claim to be beginner-friendly, and uses terms and tools some of you probably won't
> know.
> Programming is a more complex topic when you get into the plugin stuff, but it's still surprisingly simple compared to
> other things you could be attempting instead.

Whether you're using Gradle or Maven, the process is very similar. But the [installation](#installation) guide
is **very** different!

Once you follow that guide, you most likely already have a blank project directory setup. We prefer
using [IntelliJ IDEA](https://www.jetbrains.com/idea/)
for creating plugins (and PghServer itself!), but as long as you can write text files it should work fine.

Most importantly, you need a class somewhere implementing `com.pghserver.api.PghPlugin`. For example:

```java
package com.example.fooplugin;

import com.pghserver.api.PghPlugin;
import com.pghserver.api.PghAPI;

public class FooPlugin implements PghPlugin {

    @Override
    void onEnable(PghAPI server) { // Called when plugin is enabled, passes in API instance
    }

    @Override
    void onDisable(PghAPI server) { // Called when plugin is disabled, passes in API instance
    }
}
```

But as you can *probably* tell, that ain't doing anything!

You may *optionally* add some route registrations. This one makes **every** `GET` route send back some big text in HTML
form! Note that route handlers/registrations can be their own classes that implement `com.pghserver.api.RouteHandler`,
but because the handler implementation uses a `void run(Request, Response);` method, it can be inlined very easily:

```java
package com.example.fooplugin;

import com.pghserver.api.PghPlugin;
import com.pghserver.api.PghAPI;

public class FooPlugin implements PghPlugin {

    @Override
    void onEnable(PghAPI server) { // Called when plugin is enabled, passes in API instance
        server.route("/.*", (req, res) -> { // Regex for any route that starts with a forward slash, which is all of 'em
            if (req.method() != RequestMethod.GET) { // We only want GET requests!
                res.status(ResponseStatus.METHOD_NOT_ALLOWED); // PghServerAPI provides a near-complete collection of response statuses in enum form
                res.contentType("text/plain"); // Plain text response
                res.body(res.status().toString()); // Sends back "405 Method Not Allowed"
                return;
            }

            res.contentType("text/html; charset=utf-8"); // UTF-8 HTML response
            res.body("<h1>Hello! " + res.status() + "</h1>", StandardCharsets.UTF_8); // <h1>Hello! 200 OK</h1>
        });

        // NOTE: Any registrations after this one take priority!
        // NOTE: Yes, registrations are order-sensitive for now. Deal with it.
    }

    @Override
    void onDisable(PghAPI server) { // Called when plugin is disabled, passes in API instance
    }
}
```

So, you have a plugin... or do you?

If you think just a *little* harder, you'll notice you never really told the PghServer runtime anything about your
plugin!

PghServer plugins **must** have a manifest inside the root of the JAR named `manifest.pgh`, which can be placed in your
source directory depending on your setup. As Gradle users, we know by heart that you place root JAR files inside
`src/main/resources`! So for Gradle, you'd place your `manifest.pgh` inside `src/main/resources`.

But what does this manifest look like, you may ask?

```properties
name=Foo Plugin
version=1
main-class=com.example.fooplugin.FooPlugin
```

Yeah... dead simple, right? `name` can be any string, `version` can be any string (but will automatically have `v`
prepended), and `main-class` is a string that must resolve to a valid class inside your JAR.

Once you've got that manifest covered, you're ready to make some JARs! Well, just one. But that sounded cooler... did
it?

All jokes aside, yeah make a JAR. You don't need to configure a *regular* manifest or anything, you covered that
already. It just needs your compiled `.class` files, required libraries (except the API, that's provided by the
runtime!!), and the PghServer plugin manifest!

> **Note:** Avoid bundling libraries already provided by PghServer, especially the API, as duplicate classes can cause
> class loading conflicts. Also did you know the PghServerAPI gives you a specific `org.jetbrains:annotations` version
> so everything syncs and works correctly?

But before you test it, why don't you extract your JAR and see if everything's in the
right place?

And yup! If you place it in your server's `plugins` directory, and it successfully loads, you now have a plugin!

If it fails...? Sucks to be you!!

---

## Installation

Before including the dependency, there's an extra step! PghServer's creator maintains a Maven repository for various
artifacts:

**Gradle (Kotlin)**

```kotlin
repositories {
    maven("https://mvn.flappygrant.com")
}
```

**Maven (POM)**

```xml

<repositories>
    <repository>
        <id>boyninja1555mvn</id>
        <name>FloorMannMaven</name>
        <url>https://mvn.flappygrant.com</url>
    </repository>
</repositories>
```

Once you've got the repository included, it's time for the actual library! The API is provided by the PghServer runtime,
so it should only be used during compilation and should not be bundled with your plugin.

**Gradle (Kotlin)**

```kotlin
dependencies {
    compileOnly("com.pghserver:pghserver-api:1") // For v1
}
```

**Maven (POM)**

```xml

<dependencies>
    <dependency>
        <groupId>com.pghserver</groupId>
        <artifactId>pghserver-api</artifactId>
        <version>1</version> <!-- For v1 -->
        <scope>provided</scope>
    </dependency>
</dependencies>
```

You may continue development now!!