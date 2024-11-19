## Type 4 Recovery Steps

In Type 4, all locators are isolated. The recovery steps are similar to Type 3.

1. Fix network issues if any.
2. Check each member to see their CPU usage and available system resources.
3. Stop or kill all members including the locators.
4. Make sure to remove all data stores before restarting the cluster.
5. Restart the cluster in a clean state.
6. Reingest persistent and non-persistent data.
