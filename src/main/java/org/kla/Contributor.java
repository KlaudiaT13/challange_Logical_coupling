package org.kla;

import java.util.ArrayList;

public class Contributor
{
    public String getUsername() {
        return username;
    }

    public void setSum_of_all_changed_lines(int sum_of_all_changed_lines) {
        this.sum_of_all_changed_lines = sum_of_all_changed_lines;
    }

    //    int id;
    String username;

    public int getLength_of_position_on_the_tree() {
        return length_of_position_on_the_tree;
    }

    int[] contributions;
    int position_on_the_tree = 0;
    int length_of_position_on_the_tree = 0;
    int sum_of_all_changed_lines = 0;
    int treeResult = 0;

    public int getTreeResult() {
        return treeResult;
    }

    public int getPosition_on_the_tree() {
        return position_on_the_tree;
    }

    public Contributor(String username) {
//        this.id = id;
        this.username = username;
    }

    void initialize_contributions(int n){
        contributions = new int[n];
    }

    public void update_contributions(int index, int number_of_lines_changed){
        contributions[index] += number_of_lines_changed;
        sum_of_all_changed_lines += number_of_lines_changed;
    }

    void update_position_on_the_tree(int c){
        position_on_the_tree = (position_on_the_tree + c)*2;
    }

    void update_length_of_position_on_the_tree(){
        length_of_position_on_the_tree++;
    }
//    void update_tree_result(){
//        if(position_on_the_tree.isEmpty()){
//            treeResult = 0;
//        } else{
//            treeResult = Integer.parseInt(this.position_on_the_tree);
//        }
//    }


}
