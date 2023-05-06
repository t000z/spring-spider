package bt.search.analyzer.service.impl;

import bt.search.analyzer.dao.RegexConfigDao;
import bt.search.analyzer.pojo.RegexConfig;
import bt.search.analyzer.service.RegexConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RegexConfigServiceImpl extends ServiceImpl<RegexConfigDao, RegexConfig> implements RegexConfigService {
}
