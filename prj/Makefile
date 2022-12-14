# ----------------------------------------------------------------------------------------------------------------------
# Copyright (c) 2022, Oracle and/or its affiliates.
#
# Licensed under the Universal Permissive License v 1.0 as shown at
# https://oss.oracle.com/licenses/upl.
#
# ----------------------------------------------------------------------------------------------------------------------
# This is the Makefile to build the Coherence.
# Although Coherence is built using Maven, there are sone nuances to the commands to properly do the required
# two stage build. This Makefile makes remember the commands a little simpler.
#
# ----------------------------------------------------------------------------------------------------------------------

ARGS            =
NO_TDE          = -Dtde.compile.not.required=true
CURRENT_VERSION = $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout -nsu)

# ======================================================================================================================
# Makefile targets start here
# ======================================================================================================================

# ----------------------------------------------------------------------------------------------------------------------
# Display the Makefile help - this is a list of the targets with a description.
# This target MUST be the first target in the Makefile so that it is run when running make with no arguments
# ----------------------------------------------------------------------------------------------------------------------
help: ## Display this help.
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_0-9-]+:.*?##/ { printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)


# ======================================================================================================================
# Build targets
# ======================================================================================================================
##@ Build

.PHONY: all
all: coherence modules-no-tde ## A full build of Coherence with all modules

.PHONY: all-no-tde
all-no-tde: coherence-no-tde modules-no-tde ## A full build of Coherence with all modules (without compiling TDE)

.PHONY: coherence
coherence: ## Build coherence.jar
	$(call mvn_build_coherence)

.PHONY: coherence-no-tde
coherence-no-tde: ## Build coherence.jar (without compiling TDE)
	$(call mvn_build_coherence,$(NO_TDE))

.PHONY: modules
modules: ## Build Coherence modules
	$(call mvn_build_modules)

.PHONY: modules-no-tde
modules-no-tde: ## Build Coherence modules (without compiling TDE)
	$(call mvn_build_modules,$(NO_TDE))

.PHONY: docker
docker: ## Build the Coherence image (does not rebuild Coherence jars)
	mvn clean install -Pmodules,-coherence,docker -pl coherence-docker/ -nsu

.PHONY: docker-test
docker-test: ## Build the Coherence image (does not rebuild Coherence image)
	mvn verify -Pmodules,-coherence,docker-test -pl coherence-docker/ -nsu

define mvn_build_coherence
	mvn clean install -T 1.5C -U -DskipTests $(1) $(ARGS)
endef

define mvn_build_modules
	mvn clean install -T 1.5C -nsu -Pmodules,-coherence -DskipTests $(1) $(ARGS)
endef

# ======================================================================================================================
# Run targets
# ======================================================================================================================
##@ Run

storage: # Run a storage enabled Coherence member
	java -cp coherence/target/coherence-$(CURRENT_VERSION).jar:coherence-json/target/coherence-json-$(CURRENT_VERSION).jar \
		-Dcoherence.ttl=0 -Djava.net.preferIPv4Stack=true -Dcoherence.localhost=127.0.0.1 -Dcoherence.wka=127.0.0.1 \
        -Dtangosol.coherence.management=dynamic -Dcoherence.management.http=inherit -Dcoherence.metrics.http.enabled=true \
		-Dcoherence.member=Storage -Dcoherence.log.level=9 \
		$(ARGS) com.tangosol.net.Coherence

