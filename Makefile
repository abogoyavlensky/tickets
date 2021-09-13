# Styling for output
YELLOW := "\e[1;33m"
NC := "\e[0m"
INFO := @sh -c '\
    printf $(YELLOW); \
    echo "=> $$1"; \
    printf $(NC)' VALUE


# Ignore output of make `echo` command
.SILENT:


.PHONY: help  # Show list of targets with descriptions
help:
	@$(INFO) "Commands:"
	@grep '^.PHONY: .* #' Makefile | sed 's/\.PHONY: \(.*\) # \(.*\)/\1 > \2/' | column -tx -s ">"


.PHONY: build  # Build uberjar
build:
	@$(INFO) "Building uberjar..."
	@lein with-profile -dev,+production uberjar


.PHONY: run  # Run uberjar
run:
	@$(INFO) "Running uberjar..."
	@java -jar target/testapp.jar


.PHONY: test  # Run tests
test:
	@$(INFO) "Running tests..."
	@lein eftest-cov
