## Type 3 Recovery Steps

In Type 3, a quorum is established with cache servers but without locators. The recovery steps are similar to Type 4.

1. Fix network issues if any.
2. Check each member to see their CPU usage and available system resources.
3. Stop or kill all cache servers and locators.
4. Make sure to remove all data stores before restarting the cluster.
5. Restart the cluster in a clean state.
6. Reingest persistent and non-persistent data.
