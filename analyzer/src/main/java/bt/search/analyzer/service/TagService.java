package bt.search.analyzer.service;

import bt.search.analyzer.common.pair;
import bt.search.analyzer.pojo.HashDataDescriptionMsg;
import bt.search.analyzer.pojo.Tag;
import bt.search.fegin.dto.SpiderDescriptionInto;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface TagService extends IService<Tag> {
    /**
     * 负责更新和插入标签
     */
    void updateTag(String key, HashDataDescriptionMsg one, SpiderDescriptionInto spiderDescriptionInto);

    pair<List<Long>> getTagAndHashDataIndex(String tag);

    /**
     * 为某个标题追加标签
     * @param key
     */
    void insertAddTag(String key);
}
