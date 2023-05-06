package bt.search.analyzer.web;

import bt.search.analyzer.common.pair;
import bt.search.analyzer.pojo.Tag;
import bt.search.analyzer.service.TagService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("tag")
@Slf4j
public class TagController {
    @Autowired
    private TagService tagService;

    @GetMapping("list")
    public List<String> getTagList() {
        LambdaQueryWrapper<Tag> lqw = new LambdaQueryWrapper<>();
        lqw.select(Tag::getTag);
        List<Tag> tags = tagService.list(lqw);

        List<String> tagList = tags.stream().map(item -> item.getTag()).collect(Collectors.toList());
        return tagList;
    }

    @GetMapping
    public pair<List<Long>> getTag(@RequestParam String tag) {
        return tagService.getTagAndHashDataIndex(tag);
    }
}
