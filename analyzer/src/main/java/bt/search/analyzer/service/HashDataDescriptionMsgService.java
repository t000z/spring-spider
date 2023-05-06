package bt.search.analyzer.service;

import bt.search.analyzer.pojo.HashDataDescriptionMsg;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HashDataDescriptionMsgService extends IService<HashDataDescriptionMsg> {
    List<String> updateDescriptionAndTag(String key) throws InterruptedException;
}
