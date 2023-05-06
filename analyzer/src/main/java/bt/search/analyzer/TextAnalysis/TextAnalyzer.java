package bt.search.analyzer.TextAnalysis;

import bt.search.analyzer.common.Tuple;

import java.util.List;

public interface TextAnalyzer {
    String matchKeyWordAndRemove(String text, String key);

    Tuple<List<String>, String> regularMatchAndRemove(String text);

    List<Tuple<Long, List<String>>> entityMatching(String key, List<Tuple<Long, String>> texts);
}
