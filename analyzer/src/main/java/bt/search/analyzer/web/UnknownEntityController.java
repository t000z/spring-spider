package bt.search.analyzer.web;

import bt.search.analyzer.service.UnknownEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("findEntity")
@Slf4j
public class UnknownEntityController {
    @Autowired
    private UnknownEntityService unknownEntityService;

    @GetMapping
    public boolean subTree() {
        unknownEntityService.findEntity();
        return true;
    }
}
