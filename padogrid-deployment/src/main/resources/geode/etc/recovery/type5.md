## Type 5 Recovery Steps

In Type 5, members are able to communicate with locators but members themselves are not able to communicate with each other.

1. Fix network issues if any.

If there were network issues and they have been resolved:

2.  The cluster should be back to normal. Data loss is not expected.
3.  Nonetheless, check for data loss and inconsistency, and be prepared to reingest data as needed.
4.  Check client applications.

If there were no network issues:

2. Check each member's CPU usage and available system resources.
3. Gracefully stop cache servers not in quorum. Locators need not be restarted.
4. Identify cache servers that ran out of system resources.
5. Increase system resources as needed for those cache servers.
6. Restart the stopped cache servers.
7. Wait for GemFire to auto-recover.
8. Once the restarted cache servers have successfully rejoined the cluster, check for data loss.
9. Data loss is not expected. Nonetheless, check for data loss and inconsistency, and be prepared to reingest data as needed.
10. Check client applications.
