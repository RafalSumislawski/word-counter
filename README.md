# Word counter

## Building and running

Building and running the application requires Java 8+ and sbt.

Build & run with:

```sh
sbt "run [--timeWindow <duration>] [--httpPort <port>] <executable file path>"
```

`timeWindow` and `httpPort` options are optional. Default `timeWindow` is `15seconds`. Default `httpPort` is `80`.

Example:

```sh
sbt "run --timeWindow 15seconds --httpPort 80 blackbox/blackbox.win.exe"
```

## API

Getting current word counts:

```sh
curl -s http://localhost:<httpPort>/wordCount
```

Example:

```sh
curl -s http://localhost:80/wordCount
{"wordCountByEventType":{"baz":2,"foo":2,"bar":5}}
```

## Assumptions
The implementation relies on the following assumptions:
* Any string of one or more non-whitespace characters is considered a "word"
* Only characters matching the `[ \t\n\x0B\f\r]` regex are considered whitespace characters
* Only words in the `data` field of the JSON are counted.
* The clock of the blackbox is in sync with the clock of the application
* The blackbox executable delivers events in order (that is for any events A and B where B is printed after A, the timestamps of B >= the timestamp of A)
* The blackbox doesn't crash ;)
