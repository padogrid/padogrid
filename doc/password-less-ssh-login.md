# Configuring Password-less SSH Login

1. Create a publish ssh key

```
ssh-keygen -t rsa 
```

2. Make sure your `.ssh` directory is 700

```
chmod 700 ~/.ssh
```

3. Append the public key to the remote host. For example,

```
cat ~/.ssh/id_rsa.pub | ssh remoteuser@emoteserver.com 'cat >> ~/.ssh/authorized_keys'
```

4. Check to make sure you can login without the password

```
ssh remoteuser@remoteserver.com
```