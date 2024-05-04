package org.kla;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class LogicalCouplingService {

    @RestClient
    private GitHubService gitHubService;

    Contributor[] contributors;
    int[] listOfId;
    int numberOfContributors;
    String[] sha_of_commits;
    int numberOfCommits;
    String[] files;
    int numberOfFiles;
    int[][] changesInFiles;
    int lengthOfTheTree;

    public String getCommits(String owner, String repo, int page) {
        return gitHubService.getCommits(owner, repo, page);
    }

    public JsonObject getCommit(String owner, String repo, String ref) {
        return gitHubService.getCommit(owner, repo, ref);
    }

    public JsonArray getCommitsMapped(String owner, String repo, int page) {
        return gitHubService.getCommitsMapped(owner, repo, page);
    }

    public String findCoupling(String owner, String repo) {
        //this initializes list of contributors; later change to creating them from id, not name; also check if endpoint returns all of them - more id can be on next pages
        int page = 1;
        List<String> list_of_contributors = new ArrayList<>();
        List<String> list_of_commits = new ArrayList<>();
        Log.info("==== Start ==== time: " + LocalTime.now());
        JsonArray commitsMapped = gitHubService.getCommitsMapped(owner, repo, page);


        while(!commitsMapped.isEmpty() && page<50){
            Log.info("Get commits: " + page + " Time: " + LocalTime.now());
            list_of_contributors.addAll(getListOfContributors(commitsMapped));
            list_of_commits.addAll(getLisOfShaOfCommits(commitsMapped));
            page++;
            commitsMapped = gitHubService.getCommitsMapped(owner, repo, page);
        }
        Log.info("Get commits: Finished, Time: " + LocalTime.now());

//        List<Contributor> contributorList = new ArrayList<>();
//        list_of_contributors.stream().distinct().forEach(c->contributorList.add(new Contributor(c)));

        contributors = list_of_contributors.stream()
                .distinct()
                .map(Contributor::new)
                .toArray(Contributor[] ::new);

        //now save number of contributors
        numberOfContributors = contributors.length;
        if (numberOfContributors < 2) {return "There is only one or none contributors";}

        //get sha from list of all commits and save them to array sha_of_commits; check later if those are really all of them and not only from the first page
        sha_of_commits = list_of_commits.toArray(String[]::new);

        //number of all commits in repo
        numberOfCommits = sha_of_commits.length;

        LocalTime start = LocalTime.now();
        Log.info("=== Get commits detail: Start, Time: " + start);
        List<JsonObject> allCommits = getAllCommits(owner, repo, list_of_commits);
        LocalTime stop = LocalTime.now();
        Duration duration = Duration.between(start, stop);
        long seconds = duration.getSeconds();
        Log.info("=== Get commits detail: Stop, Time: " + stop + " Duration: " + seconds);
        //this initializes files[] - element of this array is a path (now raw_url - is it the right choice?) to the file in repo; all paths are unique

        ArrayList<String> list = new ArrayList<String>();

/*        Log.info("=== Get One commit: Start, Time: " + LocalTime.now());
        for (int i = 0; i < sha_of_commits.length; i++) {
//            this is array of all file url's mentioned in a commit of sha = sha_of_commits[i]:
//            getArrayOfFilesFromOneCommit(getOneShaOfCommit(getCommit(owner, repo, sha_of_commits[i])))
            List<String> filesFromOneCommit = getArrayOfFilesFromOneCommit(allCommits.get(i));
//          merging lists:
            list.addAll(filesFromOneCommit);
            Log.info("Get one commit: " + i + " Time: " + LocalTime.now());
        }*/


        allCommits.forEach(c->list.addAll(getArrayOfFilesFromOneCommit(c)));
        files = list.stream().distinct().toArray(String[]::new);

        //number of files in repo
        numberOfFiles = files.length;

//       Create 2D array: each row (first index) is one contributor, each column is one file (2. Idea: for each contributor create an array to hold number of commited changes to each file)
        changesInFiles = new int[numberOfContributors][numberOfFiles];
        allCommits.forEach(commit-> {
            String name = nameOfAuthorOfCommit(commit);
            int index = getIndexOfContributor(name);
            String[][] filesAndChanges = getArrayOfShaOfFilesAndNumberOfChangesFromOneCommit(commit);
            for (String[] filesAndChange : filesAndChanges) {
                String url = filesAndChange[0];
                int indexOfChangedFile = Arrays.stream(files).toList().indexOf(url);
                int numberOfChangedLines = Integer.valueOf(filesAndChange[1]);
                //index of contributor
                changesInFiles[index][indexOfChangedFile] += numberOfChangedLines;
            }
        });

//            This way, in this nested loop, every change in files was counted in array files
//        changesInFiles consists of: every row represents different Contributor (contributor from row i has a name contributors[i].getUsername)
//        each column represents different file
//        each element of this array contains changes done by specific user in specific file

//        update number of all changed lines for each contributor:

        for (int i = 0; i < numberOfContributors; i++) {
            contributors[i].setSum_of_all_changed_lines(getNumberOfAllChangedLines(i));
        }



//        now the algorithm needs only Contributor[] contributors and int[][] changesInFiles;
//        contributors[i] and changesInFiles[i][] mean the same contributor

//        copyOfContributors = contributors; <- bad idea, do in situ on String array of names (with help of a function that gives object Contributor from name, and another function that gives index from name - now main index doesn't change, only new index in array of String names changes

        listOfId = new int[contributors.length];
        for (int i = 0; i < contributors.length; i++) {
            listOfId[i] = i;
        }

//        now we can swap places from this list of id's and easily get contributors (given the id) (getContributor(listOfId(i)))
        Log.info("=== rec start:"+ LocalTime.now());
        rec(0, numberOfContributors, 0, 0.5);
        Log.info("=== rec stop:"+ LocalTime.now());

        lengthOfTheTree = getMaxLengthOfPositionOnTheTree();



        for (int i = 0; i < contributors.length; i++) {
            int lengthDifference = lengthOfTheTree - contributors[i].getLength_of_position_on_the_tree();
            if(lengthDifference > 0){
                for(int j = 0; j < lengthDifference; j++){
                    contributors[i].update_position_on_the_tree(0);
                }
            }
        }

        Contributor[] sortedContributors = contributors;
        int pair_of_lowest_difference = 0;
//        sorted in descending order according to tree result
        sortedContributors = Arrays.stream(sortedContributors).sorted((a1, a2) -> a2.position_on_the_tree - a1.position_on_the_tree).toArray(Contributor[]::new);
        int lowest_difference = sortedContributors[0].getPosition_on_the_tree() - sortedContributors[1].getPosition_on_the_tree();
        int new_difference;
        for (int i = 1; i < sortedContributors.length - 1; i++) {
            new_difference = sortedContributors[i].getPosition_on_the_tree() - sortedContributors[i + 1].getPosition_on_the_tree();
            if(new_difference < lowest_difference){
                lowest_difference = new_difference;
                pair_of_lowest_difference = i;
            }
        }

        // The wanted pair is sortedContributors[pair_of_lowest_difference] and sortedContributors[pair_of_lowest_difference + 1]

        String first_contributor_in_pair = sortedContributors[pair_of_lowest_difference].getUsername();
        String second_contributor_in_pair = sortedContributors[pair_of_lowest_difference + 1].getUsername();

        return first_contributor_in_pair + " " + second_contributor_in_pair + " number of commits " + numberOfCommits;
    }

    List<JsonObject> getAllCommits1(String owner, String repo, List<String> sha_of_commits) {
        List<JsonObject> allCommits = new ArrayList<>();
//        sha_of_commits.forEach(c->{
//            Log.info("Get commits detail: " + c + "Time: " + LocalTime.now());
//            allCommits.add(gitHubService.getCommit(owner, repo, c));
//        });
//
//        return sha_of_commits.parallelStream().map(c->gitHubService.getCommit(owner, repo, c)).toList();
        sha_of_commits.parallelStream().forEach(c->{
            Log.info("Get commits detail: " + c + "Time: " + LocalTime.now());
            allCommits.add(gitHubService.getCommit(owner, repo, c));
        });
       return allCommits;
    }


    List<JsonObject> getAllCommits(String owner, String repo, List<String> sha_of_commits) {
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            return sha_of_commits.stream()
                    .map(c -> CompletableFuture.supplyAsync(() -> {
//                        Log.info("Get commits detail: " + c + "Time: " + LocalTime.now());
                        return gitHubService.getCommit(owner, repo, c);
                    }, executor))
                    .map(CompletableFuture::join)
                    .toList();
        }
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

    public void rec(int index, int length, int file, double percent){
        if(length > 1){
            int m = 0;
            for (int i = index; i < length - m; i++) {
                if(changesInFiles[listOfId[i]][file] > percent * getContributor(listOfId[i]).sum_of_all_changed_lines){
                    getContributor(listOfId[i]).update_position_on_the_tree(1);
                    getContributor(listOfId[i]).update_length_of_position_on_the_tree();
                    swapId(listOfId, i, length - m - 1);
                    m++;
                    i--;
                }else{
                    getContributor(i).update_position_on_the_tree(0);
                }
            }
            if(m!=length){
                if(file < numberOfFiles - 1){
                    rec(index, length - m, file + 1, percent);
                } else{
                    rec(index, length - m, 0, percent/2);
                }
            }

            if(m != 0){
                rec(length - m, m, file, percent * 1.5);
            }
        }
    }

    //    with duplicates:
    public List<String> getListOfContributors(JsonArray jsonArray){
        JsonObject obj;
        List<String> names = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            obj = jsonArray.getJsonObject(i);
            names.add(
                    obj.get("commit").asJsonObject().get("author").asJsonObject().getJsonString("name").toString()
            );
        }
        return names;
    }

    public List<String> getLisOfShaOfCommits(JsonArray jsonArray){
        JsonObject obj;
        List<String> sha = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            obj = jsonArray.getJsonObject(i);
            sha.add(obj.getString("sha"));
        }
        return sha;
    }

    public List<String> getArrayOfFilesFromOneCommit(JsonObject oneCommit){
        JsonArray jsonArray = oneCommit.getJsonArray("files");
        return jsonArray.stream()
                .map(e->e.asJsonObject().getString("raw_url"))
                .toList();
    }

    public String[][] getArrayOfShaOfFilesAndNumberOfChangesFromOneCommit(JsonObject OneCommit){
        JsonArray jsonArray = OneCommit.getJsonArray("files");

        String[][] filesAndChanges = new String[jsonArray.size()][2];
        for (int i = 0; i < jsonArray.size(); i++) {
            filesAndChanges[i][0] = jsonArray.getJsonObject(i).getString("raw_url");
            Integer tempInt = jsonArray.getJsonObject(i).getInt("changes");
            filesAndChanges[i][1] = Integer.toString(tempInt);
        }
        return filesAndChanges;
    }

    public String nameOfAuthorOfCommit(JsonObject oneCommit){
        return oneCommit.get("commit").asJsonObject().get("author").asJsonObject().getJsonString("name").toString();
    }

    public int getNumberOfAllChangedLines(int idOfContributor){
        int sum = 0;
        for(int i = 0; i < numberOfFiles; i++){
            sum += changesInFiles[idOfContributor][i];
        }
        return sum;
    }

    public Contributor getContributor(int index){
        return contributors[index];
    }

    public void swapId(int[] listOfId, int a, int b){
        int temp = listOfId[a];
        listOfId[a] = listOfId[b];
        listOfId[b] = temp;
    }

    public int getMaxLengthOfPositionOnTheTree(){
        int maxLength = 0;
        for(int i = 0; i < numberOfContributors; i++){
            if(maxLength< contributors[i].getLength_of_position_on_the_tree()){
                maxLength = contributors[i].getLength_of_position_on_the_tree();
            }
        }
        return maxLength;
    }
}
