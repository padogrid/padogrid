# Configuring Password-less SSH Login

Each VM configured to run PadoGrid must have password-less `ssh` enabled. 

## Generate `ssh` Key Pair

First, generate `ssh` key from each VM as follows.

1. Create an `ssh` key

```
ssh-keygen -t rsa 
```

2. Make sure permissions to your `.ssh` directory is set to 700

```
chmod 700 ~/.ssh
```

The generated key must be placed in each VM's `~/.ssh/authorized_keys` file. This can be done one of the following ways.

### `ssh-copy-id`

OpenSSH includes the `ssh-copy-id` command which adds your public key to the `~/.ssh/authorized_keys` file of the specified remote host. You can also download `ssh-copy-id` from macOS using Homebrew. If your VM has this command then execute it as follows (See [1] for `ssh-copy-id` details.)

```
ssh-copy-id -i ~/.ssh/id_rsa.pub user@vm_host
```

### Append key to `~/.ssh/authorized_keys`

If the `ssh-copy-id` command is not available, then you can manually append the public key to the remote host.

```
cat ~/.ssh/id_rsa.pub | ssh user@vm_host 'cat >> ~/.ssh/authorized_keys'
```

## `ssh-agent` Single Sign-On

If your key requires a paraphrase then you can add the identity (public) key to `ssh-agent` so that you won't have to reenter your paraphrase you use `ssh`. This requires you to start `ssh-agent` in the VM as follows.

```
eval `ssh-agent`
```

## Check `ssh` login

Check to make sure you can login without the password.

```
ssh user@vm_host
```

## References

1. `ssh-copy-id`, [https://www.ssh.com/ssh/copy-id](ssh-copy-id).
2. `ssh-agent`, [https://www.ssh.com/ssh/agent](https://www.ssh.com/ssh/agent).
