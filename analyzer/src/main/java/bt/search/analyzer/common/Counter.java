package bt.search.analyzer.common;

public class Counter {
    private int count;

    public Counter(int count) {
        this.count = count;
    }

    public void minus() {
        this.count--;
    }

    public void add() {
        this.count++;
    }

    public int getCount() {
        return count;
    }

    public int laterAdd() {
        return this.count++;
    }

    public int laterMinus() {
        return this.count++;
    }
}
