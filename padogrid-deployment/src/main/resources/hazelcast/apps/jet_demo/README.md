# Hazelcast `jet_demo` App

The `jet_demo` app provides Jet demo jobs in the form of jar files that can readily be submitted to Jet using the `hz-cli` executable. You must first build the jar files by running `bin_sh/build_app` as described below.

## Building jet_demo

```console
create_app -app jet_demo
cd_app jet_demo/bin_sh
./build_app
```

The `build_app` compiles the provided source code and downloads books in text files in the `books` directory. 

## Running jet_demo

Upon successful build, you can submit the generated jar file in the `lib` directory to Jet using the `hz-cli` executable. You must have a Hazelast 5.x cluster running before you can submit jobs.

The source code for the jar file is provided in the `src` directory.

## Jobs

`WordCountJob` is a command-line version of `WordCount` sample code found in the <https://github.com/hazelcast/hazelcast-jet/tree/master/examples> repo. It counts and outputs the most frequent words from the specified file(s). Try submitting the `WordCountJob` with some of the downloaded books as arguments.

```console
cd_app jet_demo

# Submit WordCountJob to localhost:5701  (-t to specify a member and/or cluster)
hz-cli submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
hz-cli -t dev@localhost:5701 submit lib/WordCountJob.jar books/a-tale-of-two-cities.txt books/shakespeare-complete-works.txt
```

## Monitoring Jobs

You can use the Hazelcast Management Center (HMC) to monitor the job. You can also view the Hazelcast log files to monitor the job outputs by running `show_log`.

```console
# View Jet member 1
show_log

# View Jet member 2
show_log -num 2

# View Hazelcast cluster myhz, member 1
show_log -cluster myhz -num 1
```

### PHD (PadoGrid Hazelcast Dashboards)

You can also use PHD to monitor Jet jobs. PHD includes comprehensive sets of Grafana dashboards that complement HMC for monitoring multiple Hazelcast clusters. You can install it as follows.

```bash
create_app -product hazelcast -app grafana
```

After creating the app, see the `README.md` file for instructions.

```bash
cd_app grafana
less README.md
```

You can also view the same instructions from the following link.

<https://github.com/padogrid/padogrid/wiki/Hazelcast-Grafana-App>
