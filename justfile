#!/usr/bin/env just --justfile

# NOTE: The just recipes defined below assume sdkman is installed and used for java and maven selection.
#       Recipes that utilize docker containers assume the existence of the specific docker image existing locally

export JAVA_VER_DISTRO_11 := "11.0.18-zulu"
export JAVA_VER_DISTRO_17 := "17.0.6-zulu"
export JAVA_VER_DISTRO_19 := "19.0.2-zulu"
export DOCKER_CMD := "docker container run --rm -it"
export VOL_NAME := "eclipse-rdf4j"
export M2_REPO := "/root/.m2/repository"
export BLD_DIR := "/usr/src/build"
export IMG := "llsem-ubuntu-maven"

default:
  @echo "Invoke just --list to see a list of possible recipes to run"

clean: clean-11

clean-11:
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_11}
  mvn clean

clean-17:
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_17}
  mvn clean

clean-19:
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_19}
  mvn clean

clean-install: clean-install-11

clean-install-11: clean-11
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_11}
  mvn -Pquick install

clean-install-17: clean-17
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_17}
  mvn -Pquick install

clean-install-19: clean-19
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_19}
  mvn -Pquick install

full-verify: full-verify-11

full-verify-11: clean-install-11
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_11}
  mvn -P-skipSlowTests verify

full-verify-17: clean-install-17
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_17}
  mvn -P-skipSlowTests verify

full-verify-19: clean-install-19
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_19}
  mvn -P-skipSlowTests verify

docker-clean: docker-clean-11

docker-clean-11:
  ${DOCKER_CMD} -v ${VOL_NAME}-11:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:11" mvn clean

docker-clean-17:
  ${DOCKER_CMD} -v ${VOL_NAME}-17:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:17" mvn clean

docker-clean-19:
  ${DOCKER_CMD} -v ${VOL_NAME}-19:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:19" mvn clean

docker-clean-install: docker-clean-install-11

docker-clean-install-11: docker-clean-11
  ${DOCKER_CMD} -v ${VOL_NAME}-11:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:11" mvn -Pquick install

docker-clean-install-17: docker-clean-17
  ${DOCKER_CMD} -v ${VOL_NAME}-17:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:17" mvn -Pquick install

docker-clean-install-19: docker-clean-19
  ${DOCKER_CMD} -v ${VOL_NAME}-19:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:19" mvn -Pquick install

# full-verify does not currently work inside docker (NativeStore tests fail)
#docker-full-verify: docker-full-verify-11
#
#docker-full-verify-11: docker-clean-install-11
#  ${DOCKER_CMD} -v ${VOL_NAME}-11:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:11" mvn -P-skipSlowTests verify
#
#docker-full-verify-17: docker-clean-install-17
#  ${DOCKER_CMD} -v ${VOL_NAME}-17:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:17" mvn -P-skipSlowTests verify
#
#docker-full-verify-19: docker-clean-install-19
#  ${DOCKER_CMD} -v ${VOL_NAME}-19:"${M2_REPO}" -v "$(pwd):${BLD_DIR}" -w ${BLD_DIR} "${IMG}:19" mvn -P-skipSlowTests verify

dependencies:
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_11}
  mvn dependency:tree -Dscope=compile | tee dependencies.txt

updates:
  #!/usr/bin/env bash -l
  sdk use java ${JAVA_VER_DISTRO_11}
  mvn versions:display-dependency-updates | tee updates.txt
