# WSL Guide

This article provides tips for running PadoGrid on Windows Subsystem for Linux (WSL).

## Running Desktop from WSL

To run the desktop app, you need to upgrade your Linux subsystem to include Xlib and install X Server on Windows.

1. On Unbuntu subsystem

Update/Upgrade Ubuntu

```console
sudo apt-get update && sudo apt-get upgrade -y && sudo apt-get upgrade -y && sudo apt-get dist-upgrade -y && sudo apt-get autoremove -y
```

Install X Window System client interface libraries

```console
sudo apt-get install libxrender1
sudo apt-get install libxtst6
sudo apt-get install libxi6
```

Configure bash to use the local X Server

```console
echo "export DISPLAY=localhost:0.0" >> ~/.bashrc
. ~/.bashrc
```

2. Install an X server for Windows

- [VxXsrv](https://sourceforge.net/projects/vcxsrv/)

- [Xming](https://sourceforge.net/projects/xming/files/latest/download)

3. Install Desktop

```console
create_app -app desktop
cd_app desktop; cd bin_sh
./build_app
```

4. Run Desktop

```console
cd_app dekstop
cd hazecast-desktop_<version>/bin_sh
./desktop
```

If the desktop app fails to run, see the log file for missing library packages and install them using the `apt-get install` command as shonw in Step 1.

```console
cat ../log/desktop.log
```

## Running Desktop from Cygwin

If you have Cygwin installed then it is recommended that you run the desktop app from there instead to avoid installing the X Server and get the native Windows GUI look and feel. You only need to install the desktop app.

1. Install Desktop

```console
create_app -app desktop
cd_app desktop; cd bin_sh
./build_app
```

2. Run Desktop

```console
cd_app dekstop
cd hazecast-desktop_<version>/bin_sh
./desktop
```