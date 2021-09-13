# tickets


## Usage

*Requiremnts: Java11*

You could run the project using pre-built jar-file:

```bash
java -jar dist/testapp.jar
```

## Development

*Requirements: cognitect-dev-tools-0.9.61*

Running inside repl:

```bash
lein repl
(go)
```

```bash
make test  # run tests for api and front
make lint  # lint project's files
make fmt  # format project's files
```

```bash
make build  # build uberjar
make run  # run uberjar
```

## TODO
[] Add pagination.
[] Run tests in CI.
[] Validate form on the client side before sending to server. 
[] Validate length of text fields in form.
