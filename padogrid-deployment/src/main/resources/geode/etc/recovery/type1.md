## Type 1 Recovery Steps

In Type 1, cache servers are isolated.

1. Fix network issues if any.
2. Check each isolated member's CPU usage and available system resources.
3. Gracefully stop the members not in quorum.
4. Identify the members that ran out of system resources.
5. Increase system resources as needed for those members.
6. Restart the stopped members.
7. Wait for GemFire to auto-recover the restarted members.
8. Once the restarted members have successfully rejoined the cluster, check for data loss.
9. GemFire is expected to fully recover persistent data.
10. Reingest non-persistent data.

❗ Be aware that auto-restart can take a long time to complete depending on the data volume, disk speeds, and the number of failed members.
