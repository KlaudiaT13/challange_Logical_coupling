package org.kla;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.kla.dto.CommitData;
import org.kla.dto.CommitDetails;
import org.kla.dto.CommonFiles;
import org.kla.dto.Contributor;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

@ApplicationScoped
public class LogicalCouplingService {

    private final GitHubService gitHubService;

    Contributor[] contributors;
    int numberOfContributors;
    int numberOfCommits;
    String[] files;
    int numberOfFiles;
    boolean[][] changesInFiles;
    int[][] resultOfNonRecFunction;

    public LogicalCouplingService(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    public String findCoupling(String owner, String repo) {
        Log.info("==== Start ==== time: " + LocalTime.now());

        List<CommitData> commitsList = gitHubService.getCommitsList(owner, repo);
        List<String> contributorList = gitHubService.getContributorsList(commitsList);
        List<String> commitList = gitHubService.getShaOfCommitsList(commitsList);
        Log.info("Get commits: Finished, Time: " + LocalTime.now());

        contributors = contributorList.stream()
                .distinct()
                .map(Contributor::new)
                .toArray(Contributor[] ::new);

        //now save number of contributors
        numberOfContributors = contributors.length;
        if (numberOfContributors < 2) {return "There is only one or none contributors";}

        //number of all commits in repo
        numberOfCommits = commitList.size();

        LocalTime start = LocalTime.now();
        Log.info("=== Get commits detail: Start, Time: " + start);
        List<CommitDetails> allCommitsList = gitHubService.getAllCommitsList(owner, repo, commitList);

        LocalTime stop = LocalTime.now();
        Duration duration = Duration.between(start, stop);
        long seconds = duration.getSeconds();
        Log.info("=== Get commits detail: Stop, Time: " + stop + " Duration: " + seconds);

        files = gitHubService.getFilenameFromCommits(allCommitsList);

        //number of files in repo
        numberOfFiles = files.length;

        changesInFiles = new boolean[numberOfContributors][numberOfFiles];

        allCommitsList.forEach(commit-> {
            int index = getIndexOfContributor(commit.getCommit().getAuthor().getName());
            String[][] filesAndChanges = gitHubService.getArrayOfShaOfFilesAndNumberOfChangesFromCommit(commit);
            for (String[] filesAndChange : filesAndChanges) {
                String url = filesAndChange[0];
                int indexOfChangedFile = Arrays.stream(files).toList().indexOf(url);
                int numberOfChangedLines = Integer.valueOf(filesAndChange[1]);
                try {
                    changesInFiles[index][indexOfChangedFile] = changesInFiles[index][indexOfChangedFile] || (numberOfChangedLines>0);
                } catch (Exception e) {
                    Log.error("Error changesInFiles table. index: " + index + " indexOfChangedFile: " + indexOfChangedFile);
                    throw new RuntimeException(e);
                }
            }
        });

        setResultOfNonRecFunction();

        Log.info("=== rec start:"+ LocalTime.now());
        String result = non_rec();

        Log.info("=== rec stop:"+ LocalTime.now());
        sortResult();

        return result;
    }

    int getIndexOfContributor(String name) {
        int index = -1;
        for(int k = 0; k < contributors.length; k++) {
            if(Objects.equals(contributors[k].getUsername(), name)){
                index = k;
            }
        }
        return index;
    }

    public String non_rec(){
        int max1 = 0;
        int max2 = 0;
        for(int k = 0; k < numberOfContributors; k++) {
            for (int j = 0; j < numberOfContributors; j++) {
                if(resultOfNonRecFunction[k][j] > resultOfNonRecFunction[max1][max2]){
                    max1 = k;
                    max2 = j;
                }
            }
        }
        return getContributor(max1).getUsername() + " " + getContributor(max2).getUsername();
    }

    public void sortResult(){
        PriorityQueue<CommonFiles> queue = new PriorityQueue<>();
        for(int i = 0; i < numberOfContributors; i++){
            for(int j = 0; j < i; j++){
                queue.add(new CommonFiles(resultOfNonRecFunction[i][j], contributors[i].getUsername(), contributors[j].getUsername()));
            }
        }
        for(int i = 0; i < 100 && i < numberOfContributors; i++){
            Log.info(queue.poll().toString());
        }
    }

    public void setResultOfNonRecFunction() {
        resultOfNonRecFunction = new int[numberOfContributors][numberOfContributors];
        for(int k = 0; k < numberOfContributors; k++) {
            for (int j = 0; j < numberOfContributors; j++) {
                resultOfNonRecFunction[k][j] = 0;
                if (k != j) {
                    for (int l = 0; l < numberOfFiles; l++) {
                        resultOfNonRecFunction[k][j] += ((changesInFiles[k][l] && changesInFiles[j][l])?1:0);
                    }
                }
            }
        }
    }

    public Contributor getContributor(int index){
        return contributors[index];
    }
}
