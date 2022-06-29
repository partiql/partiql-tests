/*
 * Copyright 2022 Amazon.com, Inc. or its affiliates.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at:
 *
 *       http://aws.amazon.com/apache2.0/
 *
 *  or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 *  language governing permissions and limitations under the License.
 */

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
    `java-library`
}

repositories {
    mavenCentral()
}

object Versions {
    const val ionSchema = "1.2.1"
    const val jupiter = "5.8.2"
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use `ion-schema-kotlin` for validation of the test files
    api("com.amazon.ion:ion-schema-kotlin:${Versions.ionSchema}")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.jupiter}")
}

tasks.test {
    useJUnitPlatform()
    environment("PARTIQL_TESTS_DATA", file("../partiql-tests-data/").absolutePath)
}
