package cn.khala.datadisplay.service;

import cn.khala.datadisplay.model.News;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;


public class NewsSimilarity {
    private String[] wordVec1;
    private String[] wordVec2;

    private HashMap<String,Integer> wordMap1;
    private HashMap<String,Integer> wordMap2;

    private HashSet<String> wordSet;

    private Vector<Integer> freqVec1;
    private Vector<Integer> freqVec2;

    private void wordVecToMap(String[] wordVec,HashMap<String,Integer> wordMap){
        for(String word:wordVec){
            if(wordMap.containsKey(word)){
                wordMap.put(word,wordMap.get(word)+1);
            }else{
                wordMap.put(word,1);
            }
        }
    }

    public NewsSimilarity(News news1, News news2) {
        this.wordVec1=news1.getProcessed_content().replace(" | ",",").split(",");
        this.wordVec2=news2.getProcessed_content().replace(" | ",",").split(",");
        wordMap1=new HashMap<>();
        wordMap2=new HashMap<>();
        wordSet=new HashSet<>();
        freqVec1=new Vector<>();
        freqVec2=new Vector<>();

        wordVecToMap(wordVec1,wordMap1);
        wordVecToMap(wordVec2,wordMap2);

        wordSet.addAll(wordMap1.keySet());
        wordSet.addAll(wordMap2.keySet());

        for(String word:wordSet){
            freqVec1.add(wordMap1.get(word)==null?0:wordMap1.get(word));
            freqVec2.add(wordMap2.get(word)==null?0:wordMap2.get(word));
        }
    }

    public double getCosineDistance(){
        double up = 0;
        double down;
        for(int i=0;i<wordSet.size();i++){
            up+=freqVec1.get(i)*freqVec2.get(i);
        }

        double mod1=0.0,mod2=0.0;
        for(int x:freqVec1){
            mod1+=x*x;
        }
        for(int x:freqVec2){
            mod2+=x*x;
        }
        down=Math.sqrt(mod1)*Math.sqrt(mod2);
        return up/down;
    }

    public static void main(String[] args) {
        News news1=new News();
        news1.setProcessed_content("我 | 喜欢 | 看 | 电视 | 不 | 喜欢 | 看 | 电影");
        News news2=new News();
        news2.setProcessed_content("我 | 不 | 喜欢 | 看 | 电视 | 也 | 不 | 喜欢 | 看 | 电影");
        System.out.println(new NewsSimilarity(news1,news2).getCosineDistance());
    }
}
