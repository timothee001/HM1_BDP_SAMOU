package pagerank;

public class PageAndRank implements Comparable<PageAndRank> {
    double rank;
    private String page;
    

    PageAndRank(double rank, String page) {
        this.rank = rank;
        this.page = page;

    }
    
    public void setPage(String page){
    	this.page=page;
    }
    

    
    public String getPage(){
    	return this.page;
    }


    @Override
    public int compareTo(PageAndRank pair) {
        if (this.rank > pair.rank) {
            return 1;
        } else {
            return -1;
        }
    }
}