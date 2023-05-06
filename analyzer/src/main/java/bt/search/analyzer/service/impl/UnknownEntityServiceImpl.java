package bt.search.analyzer.service.impl;

import bt.search.analyzer.dao.UnknownEntityDao;
import bt.search.analyzer.pojo.UnknownEntity;
import bt.search.analyzer.service.EntityDictionaryService;
import bt.search.analyzer.service.UnknownEntityService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UnknownEntityServiceImpl extends ServiceImpl<UnknownEntityDao, UnknownEntity> implements UnknownEntityService {
    @Autowired
    private EntityDictionaryService entityDictionaryService;

    @Override
    public void checkUnionAndSave(Set<String> words) {
        List<UnknownEntity> list = this.list();
        Set<String> set = list.stream().map(item -> item.getWord()).collect(Collectors.toSet());
        List<UnknownEntity> collect = words.stream()
                .filter(item -> !set.contains(item))
                .map(item -> new UnknownEntity(item))
                .collect(Collectors.toList());
        this.saveBatch(collect);
    }

    @Override
    public void findEntity() {
        List<UnknownEntity> list = this.list();
        LinkedList<String> buffer = new LinkedList<>();
        list.forEach(item -> buffer.add(item.getWord()));
        buffer.addAll(entityDictionaryService.getEntityList());

        /**
         * 链表的首节点做为root，并移除
         * 遍历链表其他节点，判断其是否为root子串
         * 操作结束后，将root加入链表尾部，然后重复上面操作
         * 直至每个节点都遍历过一遍
         */
        for (UnknownEntity entity : list) {
            Set<String> stringSet = this.subStrTree(buffer.removeFirst(), buffer);
            buffer.addLast(entity.getWord());
            if (entity.getRelevancy() == null) {  // 检查之前的子节点，保证每个子节点唯一
                entity.setRelevancy(new ArrayList<>(stringSet));
            } else {
                stringSet.addAll(entity.getRelevancy());
                entity.setRelevancy(stringSet.stream().collect(Collectors.toList()));
            }
        }

        for (UnknownEntity entity : list) {
            this.updateById(entity);
        }
    }

    private Set<String> subStrTree(String root, List<String> words) {
        Set<String> nodes = words.stream()
                .filter(root::contains)
                .collect(Collectors.toSet());
        return nodes;
    }
}
