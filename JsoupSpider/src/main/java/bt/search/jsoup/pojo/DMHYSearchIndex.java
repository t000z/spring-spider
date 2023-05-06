package bt.search.jsoup.pojo;

import lombok.Data;

@Data
public class DMHYSearchIndex {
    private int totalPage;

    private int nextPage;

    public DMHYSearchIndex(int totalPage) {
        this.totalPage = totalPage;
        this.nextPage = 2;
    }

    public synchronized int getNextPage() {
        return nextPage;
    }

    public synchronized void next() {
        nextPage++;
    }

    public synchronized int hash() {
        if (nextPage > totalPage) {
            return 0;
        }
        return nextPage;
    }
}
