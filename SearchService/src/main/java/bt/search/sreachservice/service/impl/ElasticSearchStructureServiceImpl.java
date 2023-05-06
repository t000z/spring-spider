package bt.search.sreachservice.service.impl;

import bt.search.sreachservice.dao.ElasticSearchStructureDao;
import bt.search.sreachservice.pojo.ElasticSearchStructure;
import bt.search.sreachservice.service.ElasticSearchStructureService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ElasticSearchStructureServiceImpl extends ServiceImpl<ElasticSearchStructureDao, ElasticSearchStructure> implements ElasticSearchStructureService {
}
