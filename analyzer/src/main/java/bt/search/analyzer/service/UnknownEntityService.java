package bt.search.analyzer.service;

import bt.search.analyzer.pojo.UnknownEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Set;

public interface UnknownEntityService extends IService<UnknownEntity> {
    void checkUnionAndSave(Set<String> words);

    void findEntity();
}
