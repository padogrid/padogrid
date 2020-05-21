# Hazelcast `jet_demo` App

The `jet_demo` app provides Jet demo jobs in the form of jar files that can readily be submitted to Jet using the `jet.sh` executable. You must first build the jar files by running `bin_sh/build_app` as described below.

## Building jet_demo

```console
create_app -app jet_demo
cd_app jet_demo
cd bin_sh
./build_app
```

## Running jet_demo

Upon successful build, you can submit any of the jar files in the `lib` directory to Jet using the `jet.sh` executable. You must have a Jet cluster running before you can run `jet.sh`. For `padogrid`, you can simply create a Jet workspace and start a cluster from there as described in the [Jet Workspace](padogrid#jet-workspace) section.

```console
cd_app jet_demo

# Submit WordCountJob to localhost:5701
jet.sh submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt

# Submit WordCountJob to localhost:6701
jet.sh -a localhost:6701 submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
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

## Jobs

### WordCountJob.jar

`WordCountJob` is a command-line version of `WordCount` sample code found in the `https://github.com/hazelcast/hazelcast-jet-code-samples.git` repo. It counts and outputs the most frequent words from the specified file(s). The `build_app` copies the downloaded books in the `books` directory. Try submitting the `WordCountJob` with some of the books as arguments.

```console
# Set the system property outputWords to output words being filtered
# by each member into thier log files
export JAVA_OPTS=-DoutputWords=true

# Submit books for counting most frequent words on localhost:5701
jet.sh -v submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt

# Submit books for counting most frequent words on localhost:6701
jet.sh -v -a localhost:6701 submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
```
