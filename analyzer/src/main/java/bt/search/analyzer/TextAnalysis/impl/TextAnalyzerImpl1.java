package bt.search.analyzer.TextAnalysis.impl;

import bt.search.analyzer.TextAnalysis.TextAnalyzer;
import bt.search.analyzer.common.*;
import bt.search.analyzer.pojo.RegexConfig;
import bt.search.analyzer.pojo.UnknownEntity;
import bt.search.analyzer.service.EntityDictionaryService;
import bt.search.analyzer.service.RegexConfigService;
import bt.search.analyzer.service.UnknownEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.wltea.analyzer.core.IKSegmenter;
import org.wltea.analyzer.core.Lexeme;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class TextAnalyzerImpl1 implements TextAnalyzer {
    @Autowired
    private RegexConfigService regexConfigService;

    private static List<RegexConfig> REGEX_LIST;  // 一类信息有时需要对应一系列正则表达式

    @Autowired
    private EntityDictionaryService entityDictionaryService;

    @Autowired
    private UnknownEntityService unknownEntityService;

    private static ExecutorService EXECUTORSERVICE;

    @PostConstruct
    public void init() {
        REGEX_LIST = regexConfigService.list();
        EXECUTORSERVICE = ServiceThreadPool.createExecutorService();
    }

    @PreDestroy
    public void destroy() {
        ServiceThreadPool.shutdown(EXECUTORSERVICE);
    }

    @Override
    public String matchKeyWordAndRemove(String text, String key) {
        String result = null;

        int indexOf = text.replaceAll("\\s", "")
                .toUpperCase(Locale.ROOT)
                .indexOf(key);
        if (indexOf != -1) {
            StringBuffer buffer = new StringBuffer(text.substring(0, indexOf));
            Counter count = new Counter(key.length());
            // 因为在匹配时去除了空字符影响，因此在截取字符时也要避免空字符影响
            text.chars()
                    .skip(indexOf)
                    .filter(i -> {
                        if (count.getCount() == 0) {
                            return true;
                        }
                        if (i != 32) {  // 不是空字符
                            count.minus();
                        }
                        return false;
                    })
                    .mapToObj(item -> (char) item)
                    .forEach(buffer::append);


            result = buffer.toString();
        }

        return result;
    }

    @Transactional
    @Override
    public List<Tuple<Long, List<String>>> entityMatching(String key, List<Tuple<Long, String>> texts) {
        Set<String> unknownEntity = new HashSet<>();
        Map<Long, List<String>> idToEntity = new HashMap<>();

        List<Tuple<Long, List<String>>> tuples = texts.stream()
                .map(item -> new Tuple<Long, String>(item.getKey(), this.matchKeyWordAndRemove(item.getValue(), key)))  // 去除用户关键字
                .filter(item -> item.getValue() != null)
                .map(item -> {  // 匹配自定义的正则表达式
                    Tuple<List<String>, String> tuple = this.regularMatchAndRemove(item.getValue());
                    return new Tuple<Long, Tuple<List<String>, String>>(item.getKey(),
                            tuple == null ? new Tuple<List<String>, String>(null, item.getValue()) : tuple);
                })
                .peek(item -> {
                    if (item.getValue().getKey() != null) {
                        idToEntity.put(item.getKey(), item.getValue().getKey());
                    }
                })
                .map(item -> new Tuple<Long, String>(item.getKey(), item.getValue().getValue()))
                .map(item -> {
                    try {
                        return new Tuple<Long, List<Tuple<String, String>>>
                                (item.getKey(), this.ikSmart(item.getValue()));  // 分词
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new Tuple<Long, List<Tuple<String, String>>>(item.getKey());
                })
                .filter(item -> item.getValue() != null)
                .map(item -> {  // 保留为实体名词概率比较高的词
                    List<String> collect = item.getValue().stream()
                            .filter(item1 -> {
                                String value = item1.getValue();
                                return value.equals("LETTER") || value.equals("ENGLISH");
                            })
                            .map(item1 -> item1.getKey())
                            .collect(Collectors.toList());
                    return new Tuple<Long, List<String>>(item.getKey(), collect);
                })
                .map(item -> {  // 转小写
                    List<String> collect = item.getValue().stream()
                            .map(item1 -> item1.toLowerCase(Locale.ROOT))
                            .collect(Collectors.toList());
                    return new Tuple<Long, List<String>>(item.getKey(), collect);
                })
                .map(item -> {  // 匹配实体，如果为识别则丢弃并放入未知词表
                    List<String> collect = item.getValue().stream()
                            .map(item1 -> {
                                if (entityDictionaryService.exist(item1)) {
                                    return item1;
                                }
                                unknownEntity.add(item1);
                                return null;
                            })
                            .filter(item1 -> item1 != null)
                            .collect(Collectors.toList());

                    if (idToEntity.containsKey(item.getKey())) {  // 自定义规则匹配出来的正则表达式
                        collect.addAll(idToEntity.get(item.getKey()));
                    }
                    return new Tuple<>(item.getKey(), collect);
                })
                .filter(item -> item.getValue().size() != 0)  // 丢弃没有实体名词的数据
                .collect(Collectors.toList());

        EXECUTORSERVICE.execute(() -> this.savaEntity(idToEntity.values()));  // 增加实体名词
        EXECUTORSERVICE.execute(() -> this.savaUnknownEntity(unknownEntity));
        return tuples;
    }

    @Override
    public Tuple<List<String>, String> regularMatchAndRemove(String text) {
        List<String> entity = new Vector<>();

        String tempText = text;
        for (RegexConfig regexConfig : REGEX_LIST) {  // 将存储的正则规则一一进行匹配
            for (String regex : regexConfig.getRegexList()) {
                Tuple<String, String> tuple = regularMatchAndRemove(tempText, regex);
                if (tuple != null) {  // 匹配到数据则进行下一系列正则匹配
                    // 去除匹配词中的无用字符
                    Tuple<String, String> tuple1 = null;
                    for (String uselessChar : regexConfig.getUselessChar()) {
                        tuple1 = regularMatchAndRemove(tuple.getKey(), uselessChar);
                    }
                    if (tuple1 != null) {  // 说明匹配数据中没有无用字符
                        entity.add(tuple1.getValue());
                    } else {
                        entity.add(tuple.getKey());
                    }
                    tempText = tuple.getValue();
                    break;
                }
            }
        }

        if (entity.size() != 0) {
            return new Tuple<>(entity, tempText);
        }
        return null;
    }

    @Transactional
    void savaUnknownEntity(Set<String> words) {
        if (words.size() != 0) {
            unknownEntityService.checkUnionAndSave(words);
        }
    }

    @Transactional
    void savaEntity(Collection<List<String>> entity) {
        Set<String> entityList = new HashSet<>();
        entity.forEach(entityList::addAll);
        if (entityList.size() != 0) {
            entityDictionaryService.addAllEntity(entityList);
        }
    }

    private List<Tuple<String, String>> ikSmart(String text) throws IOException {
        StringReader sr = new StringReader(text);
        IKSegmenter ikSegmenter = new IKSegmenter(sr, true);
        Lexeme word = null;
        List<Tuple<String, String>> result = new Vector<>();
        while((word = ikSegmenter.next()) != null) {
            result.add(new Tuple<>(word.getLexemeText(), word.getLexemeTypeString()));
        }

        return result.size() != 0 ? result : null;
    }

    private Tuple<String, String> regularMatchAndRemove(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            String group = matcher.group(0);
            return new Tuple<>(group, matcher.replaceAll(""));
        }
        return null;
    }


}
