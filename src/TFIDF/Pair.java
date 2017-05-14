package TFIDF;

public class Pair implements Comparable<Pair> {
    double frequency;
    private String word;
    private String doc;

    Pair(double frequency, String word, String neighbor) {
        this.frequency = frequency;
        this.word = word;
        this.doc = neighbor;
    }
    
    public void setWord(String word){
    	this.word=word;
    }
    
    public void setDoc(String doc){
    	this.doc=doc;
    }
    
    public String getWord(){
    	return this.word;
    }
    
    public String getDoc(){
    	return this.doc;
    }


    @Override
    public int compareTo(Pair pair) {
        if (this.frequency > pair.frequency) {
            return 1;
        } else {
            return -1;
        }
    }
}