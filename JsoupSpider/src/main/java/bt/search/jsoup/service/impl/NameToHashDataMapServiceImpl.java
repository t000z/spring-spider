package bt.search.jsoup.service.impl;

import bt.search.jsoup.dao.NameToHashDataMapDao;
import bt.search.jsoup.pojo.NameToHashData;
import bt.search.jsoup.service.NameToHashDataMapService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NameToHashDataMapServiceImpl extends ServiceImpl<NameToHashDataMapDao, NameToHashData> implements NameToHashDataMapService {
}
