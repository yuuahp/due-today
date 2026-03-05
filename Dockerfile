FROM azul/zulu-openjdk-alpine:25-latest AS builder

WORKDIR /build

COPY gradle gradle

COPY gradlew build.gradle.kts gradle.properties settings.gradle.kts ./

RUN chmod a+x gradlew

RUN ./gradlew build || return 0
RUN rm -rf ./build/libs/*.jar

COPY src src

RUN ./gradlew build

FROM azul/zulu-openjdk-alpine:25-latest AS runner

WORKDIR /app

COPY --from=builder /build/build/libs/due-today-*-all.jar /app

RUN FILE_NAME=$(find . -name "due-today-*-all.jar" -print -quit) && \
    mv ${FILE_NAME} due-today.jar

ENTRYPOINT ["java", "-jar", "due-today.jar"]