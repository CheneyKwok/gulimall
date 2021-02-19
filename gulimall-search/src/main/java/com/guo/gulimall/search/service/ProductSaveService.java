package com.guo.gulimall.search.service;

import com.guo.common.to.es.SKuEsModule;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
     boolean productStatusUp(List<SKuEsModule> sKuEsModules) throws IOException;

}
