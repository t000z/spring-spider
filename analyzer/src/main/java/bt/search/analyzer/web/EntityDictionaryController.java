package bt.search.analyzer.web;

import bt.search.analyzer.pojo.EntityDictionary;
import bt.search.analyzer.service.EntityDictionaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("entity")
@Slf4j
public class EntityDictionaryController {
    @Autowired
    EntityDictionaryService entityDictionaryService;

    @PutMapping("add")
    public boolean add(EntityDictionary entity) {
        return entityDictionaryService.addEntity(entity.getWord(), entity.getDescription());
    }

    @PutMapping("addAll")
    public boolean addAll(List<EntityDictionary> entityDictionary) {
        entityDictionary.stream()
                .forEach(item -> entityDictionaryService.addEntity(item.getWord(), item.getDescription()));
        return true;
    }
}
