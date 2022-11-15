# Derby App

The `derby` app provides a simple and quick way to manage Derby DB in PadoGrid. You can start, stop, and monitor Derby DB instances which you can use with your applications running in the PadoGrid workspace environment. For example, you can use Derby DB as your DB to test data grid integration.

## 1. Installing Derby DB

Derby DB installation is supported by PadoGrid.

```bash
install_padogrid -product derby
update_products -product derby
```

## 2. Installing Derby App

The `derby` app is part of the padogrid distribution. Run the `create_app` to install it in your workspace.

:pencil2: Note that the `-product` option in `create_app` is not required for the `derby` app since it has no product dependencies.

```bash
# Create Derby app
create_app -app derby

# The Derby DB management scripts are in the app's 'bin_sh' directory.
cd_app derby/bin_sh
```

## 3. Start Derby DB

```bash
# Start Derby DB with the default port `1527` on `localhost`
./start_derby

# Same as above
./start_derby -h localhost -p 1527
```

## 4. Stop Derby DB

```bash
# Stop the Derby DB instance started by this app
./stop_derby

# Stop all Derby DB instances started by PadoGrid
./stop_derby -all
```

## 5. Monitor Derby DB

```bash
# View Derby DB status
./show_derby

# View all Derby DB instances launched by PadoGrid
./show_derby -all

# View log file
./show_derby -log
```
