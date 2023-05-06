package bt.search.analyzer.service;

import bt.search.analyzer.pojo.EntityDictionary;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Set;

public interface EntityDictionaryService extends IService<EntityDictionary> {
    boolean exist(String key);

    Set<String> getEntityList();

    boolean addEntity(String entity, String explain);

    boolean addAllEntity(Set<String> entityList);

    boolean updateExplain(String entity, String description);

    boolean removeEntity(String entity);
}
