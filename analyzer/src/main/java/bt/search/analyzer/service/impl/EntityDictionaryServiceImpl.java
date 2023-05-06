package bt.search.analyzer.service.impl;

import bt.search.analyzer.dao.EntityDictionaryDao;
import bt.search.analyzer.pojo.EntityDictionary;
import bt.search.analyzer.service.EntityDictionaryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

/**
 * 将实体词典在服务启动时就加载到内存中
 * 实体词典中只有name字段会经常使用
 */
@Service
@Lazy
public class EntityDictionaryServiceImpl extends ServiceImpl<EntityDictionaryDao, EntityDictionary> implements EntityDictionaryService {
    private Set<String> words;

    public EntityDictionaryServiceImpl() {
        this.words = new HashSet<>();
    }

    @PostConstruct
    public void init() {
        LambdaQueryWrapper<EntityDictionary> lqw = new LambdaQueryWrapper<>();
        lqw.select(EntityDictionary::getWord);
        List<EntityDictionary> entityList = this.list(lqw);

        this.words.addAll(entityList.stream().map(item -> item.getWord()).collect(Collectors.toList()));
    }

    @Override
    public boolean exist(String key) {
        return this.words.contains(key);
    }

    @Override
    public Set<String> getEntityList() {
        return this.words;
    }

    @Override
    public boolean addEntity(String entity, String description) {
        if (!words.contains(entity)) {
            words.add(entity);
            EntityDictionary entityDictionary = new EntityDictionary(entity, description);
            this.save(entityDictionary);
        }

        return true;
    }

    @Override
    public boolean addAllEntity(Set<String> entityList) {
        List<EntityDictionary> saveList = entityList.stream()
                .filter(item -> !words.contains(item))
                .peek(words::add)
                .map(EntityDictionary::new)
                .collect(Collectors.toList());

        this.saveBatch(saveList);

        return true;
    }

    @Override
    public boolean updateExplain(String entity, String description) {
        if (!words.contains(entity)) {
            LambdaUpdateWrapper<EntityDictionary> uqw = new LambdaUpdateWrapper<>();
            uqw.eq(EntityDictionary::getWord, entity).set(EntityDictionary::getDescription, description);
            this.update(uqw);
        }

        return true;
    }

    @Override
    public boolean removeEntity(String entity) {
        if (!words.contains(entity)) {
            words.remove(entity);
            LambdaQueryWrapper<EntityDictionary> lqw = new LambdaQueryWrapper<>();
            lqw.eq(EntityDictionary::getWord, entity);
            this.remove(lqw);
        }

        return true;
    }
}
