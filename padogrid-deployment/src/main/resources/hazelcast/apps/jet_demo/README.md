# Hazelcast `jet_demo` App

The `jet_demo` app provides Jet demo jobs in the form of jar files that can readily be submitted to Jet using the `jet` executable. You must first build the jar files by running `bin_sh/build_app` as described below.

## Building jet_demo

```console
create_app -app jet_demo
cd_app jet_demo
cd bin_sh
./build_app
```

## Running jet_demo

Upon successful build, you can submit any of the jar files in the `lib` directory to Jet using the `jet` executable. You must have a Jet a Jet cluster running before you can submit jobs.

## Jobs

`WordCountJob` is a command-line version of `WordCount` sample code found in the `https://github.com/hazelcast/hazelcast-jet-code-samples.git` repo. It counts and outputs the most frequent words from the specified file(s). The `build_app` copies the downloaded books in the `books` directory. Try submitting the `WordCountJob` with some of the books as arguments.

### Hazelcast 5.x

```console
cd_app jet_demo

# Submit WordCountJob to localhost:5701  (-a to specify different member)
hz-cli submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
```

### Jet 4.1+

```console
cd_app jet_demo

# Submit WordCountJob to localhost:5701  (-a to specify different member)
jet submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
```

### Jet 3.x, 4.0

```console
cd_app jet_demo

# Submit WordCountJob to localhost:5701  (-a to specify different member)
jet.sh submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
```

## Monitoring Jobs

You can use the Jet Management Center to monitor the job. You can also view the Jet log files to monitor the job outputs by running `show_log`.

```console
# View Jet member 1
show_log

# View Jet member 2
show_log -num 2

# View Jet cluster myjet, member 1
show_log -cluster myjet -num 1
```
