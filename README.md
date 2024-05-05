# Logical coupling analysis in Git repositories

There are two ways to run this algorithm:
- using endpoint on real repo
- running test

Both of those methods log in console pairs of contributors with number of files, to which both of them contributed (sorted from high to low, max 100 of them).

## Endpoint on real repo
> **_NOTE:_** It is important for testing bigger repositories to set env variable GIT_TOKEN to personal git token. Otherwise there is low limit to access GitHub api.

Call endpoint: http://localhost:8080/hello/find_coupling/{owner}/{repo}

Example: http://localhost:8080/hello/find_coupling/quarkusio/quarkus
I set a limit to loading pages of commits to 50. Each page contains 30 commits.
It takes a while to call endpoint for specifications of each of 1500 commits.

## Running test
If you don't want to wait, in commitsDetails3.txt & commitsList3.txt are contained all data to run test for quarkus repo, for first 1500 commits.
